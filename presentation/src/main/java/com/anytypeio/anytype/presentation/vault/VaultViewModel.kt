package com.anytypeio.anytype.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.const.MimeTypes
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.chats.VaultChatPreviewContainer
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.notifications.SetSpaceNotificationMode
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.spaces.DeleteSpace
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.vault.SetSpaceOrder
import com.anytypeio.anytype.domain.vault.UnpinSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.notifications.NotificationStateCalculator
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon.FileDefault
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.profile.profileIcon
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import com.anytypeio.anytype.presentation.vault.VaultNavigation.OpenChat
import com.anytypeio.anytype.presentation.vault.VaultNavigation.OpenDateObject
import com.anytypeio.anytype.presentation.vault.VaultNavigation.OpenObject
import com.anytypeio.anytype.presentation.vault.VaultNavigation.OpenParticipant
import com.anytypeio.anytype.presentation.vault.VaultNavigation.OpenSet
import com.anytypeio.anytype.presentation.vault.VaultNavigation.OpenType
import com.anytypeio.anytype.presentation.vault.VaultSectionView.Companion.MAX_PINNED_SPACES
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class VaultViewModel(
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val spaceManager: SpaceManager,
    private val saveCurrentSpace: SaveCurrentSpace,
    private val analytics: Analytics,
    private val deepLinkToObjectDelegate: DeepLinkToObjectDelegate,
    private val appActionManager: AppActionManager,
    private val spaceInviteResolver: SpaceInviteResolver,
    private val profileContainer: ProfileSubscriptionManager,
    private val chatPreviewContainer: VaultChatPreviewContainer,
    private val pendingIntentStore: PendingIntentStore,
    private val stringResourceProvider: StringResourceProvider,
    private val dateProvider: DateProvider,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val setSpaceNotificationMode: SetSpaceNotificationMode,
    private val deleteSpace: DeleteSpace,
    private val userPermissionProvider: UserPermissionProvider,
    private val notificationPermissionManager: NotificationPermissionManager,
    private val unpinSpace: UnpinSpace,
    private val setSpaceOrder: SetSpaceOrder
) : ViewModel(),
    DeepLinkToObjectDelegate by deepLinkToObjectDelegate {

    val sections = MutableStateFlow<VaultSectionView>(VaultSectionView())
    val loadingState = MutableStateFlow(false)
    private var isReturningFromSpace = false
    private var hasInitialLoadCompleted = false
    val commands = MutableSharedFlow<VaultCommand>(replay = 0)
    val navigations = MutableSharedFlow<VaultNavigation>(replay = 0)
    val showChooseSpaceType = MutableStateFlow(false)
    val notificationError = MutableStateFlow<String?>(null)
    val vaultErrors = MutableStateFlow<VaultErrors>(VaultErrors.Hidden)
    
    // Track notification permission status for profile icon badge
    val isNotificationDisabled = MutableStateFlow(false)


    val profileView = profileContainer.observe().map { obj ->
        AccountProfile.Data(
            name = obj.name.orEmpty(),
            icon = obj.profileIcon(urlBuilder)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(1000L),
        AccountProfile.Idle
    )

    init {
        Timber.i("VaultViewModel - init started")
        var isFirstEmission = true
        Timber.d("VaultViewModel - Starting combine flow")
        viewModelScope.launch {
            combine(
                spaceViewSubscriptionContainer
                    .observe()
                    .take(1)
                    .onCompletion {
                        emitAll(
                            spaceViewSubscriptionContainer.observe()
                        )
                    },
                chatPreviewContainer.observePreviewsWithAttachments(),
                userPermissionProvider.all(),
                notificationPermissionManager.permissionState()
            ) { spacesFromFlow, chatPreviews, permissions, _ ->
                transformToVaultSpaceViews(spacesFromFlow, chatPreviews, permissions)
            }.collect { resultingSections ->
                val ids = resultingSections.mainSpaces.map { it.space.id }
                Timber.d("Vault sections main spaces IDs: $ids")
                Timber.d("VaultViewModel - isFirstEmission: $isFirstEmission, isReturningFromSpace: $isReturningFromSpace")
                Timber.d("VaultViewModel - sections before: ${sections.value.mainSpaces.size} main, ${sections.value.pinnedSpaces.size} pinned")
                Timber.d("VaultViewModel - sections after: ${resultingSections.mainSpaces.size} main, ${resultingSections.pinnedSpaces.size} pinned")

                if (isFirstEmission) {
                    Timber.d("VaultViewModel - Showing loading for first emission")
                    // Show loading briefly on first emission
                    loadingState.value = true
                    delay(200)
                    isFirstEmission = false
                    hasInitialLoadCompleted = true
                    loadingState.value = false
                    Timber.d("VaultViewModel - Loading hidden after first emission")
                } else if (isReturningFromSpace && hasInitialLoadCompleted) {
                    Timber.d("VaultViewModel - Handling return from space emission")
                    // Loading may already be showing from onStart(), just ensure it stays for enough time
                    if (!loadingState.value) {
                        loadingState.value = true
                    }
                    delay(200)
                    loadingState.value = false
                    isReturningFromSpace = false
                    Timber.d("VaultViewModel - Loading hidden after return from space")
                } else {
                    Timber.d("VaultViewModel - No loading needed - isFirstEmission: $isFirstEmission, isReturningFromSpace: $isReturningFromSpace, hasInitialLoadCompleted: $hasInitialLoadCompleted")
                }
                
                sections.value = resultingSections
                Timber.d("VaultViewModel - Sections updated")
            }
        }
        
        // Track notification permission status for profile icon badge
        viewModelScope.launch {
            try {
                // Check notification permission status on app launch
                updateNotificationBadgeState()
                
                // Observe permission state changes
                notificationPermissionManager.permissionState().collect { permissionState ->
                    try {
                        updateNotificationBadgeState()
                    } catch (e: Exception) {
                        Timber.e(e, "Error updating notification badge state")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error initializing notification permission monitoring")
            }
        }
    }
    
    private fun updateNotificationBadgeState() {
        try {
            val isDisabled = !notificationPermissionManager.areNotificationsEnabled()
            isNotificationDisabled.value = isDisabled
            Timber.d("Notification badge state updated: isDisabled = $isDisabled")
        } catch (e: Exception) {
            Timber.e(e, "Error checking notification permission state")
            // Set a safe default state if we can't determine the actual state
            isNotificationDisabled.value = true
        }
    }

    private suspend fun transformToVaultSpaceViews(
        spacesFromFlow: List<ObjectWrapper.SpaceView>,
        chatPreviews: List<Chat.Preview>,
        permissions: Map<Id, SpaceMemberPermissions>
    ): VaultSectionView {
        // Index chatPreviews by space.id for O(1) lookup
        val chatPreviewMap = chatPreviews.associateBy { it.space.id }
        // Map all active spaces to VaultSpaceView objects
        val allSpacesRaw = spacesFromFlow
            .filter { space -> (space.isActive || space.isLoading) }

        // Compute pinned count first
        val pinnedIds = allSpacesRaw.filter { !it.spaceOrder.isNullOrEmpty() }.map { it.id }.toSet()
        val pinnedCount = pinnedIds.size

        val allSpaces = allSpacesRaw.map { space ->
            val isPinned = !space.spaceOrder.isNullOrEmpty()
            val chatPreview = space.targetSpaceId?.let { spaceId ->
                chatPreviewMap[spaceId]
            }?.takeIf { preview ->
                // Only use chat preview if it matches the main space chat ID
                // This filters out previews from other chats in multi-chat spaces
                space.chatId?.let { spaceChatId ->
                    preview.chat == spaceChatId
                } == true // If no chatId is set, don't show preview
            }
            mapToVaultSpaceViewItemWithCanPin(space, chatPreview, permissions, isPinned, pinnedCount)
        }

        val loadingSpaceIndex = allSpaces.indexOfFirst { space -> space.space.isLoading == true }
        if (loadingSpaceIndex != -1) {
            loadingState.value = true
            Timber.d("Found loading space ID: ${allSpaces[loadingSpaceIndex].space.id}, space name: ${allSpaces[loadingSpaceIndex].space.name}")
        } else {
            loadingState.value = false
            Timber.d("No loading space found")
        }

        // Separate pinned and unpinned spaces based on spaceOrder
        val (pinnedSpaces, unpinnedSpaces) = allSpaces.partition { space ->
            !space.space.spaceOrder.isNullOrEmpty()
        }

        // Sort pinned spaces by spaceOrder (ascending)
        val sortedPinnedSpaces = pinnedSpaces.sortedWith(
            compareBy(nullsLast()) { it.space.spaceOrder }
        )

        // Sort unpinned spaces by message date (descending), then by creation date (descending)
        val sortedUnpinnedSpaces = unpinnedSpaces.sortedWith(
            compareByDescending<VaultSpaceView> { it.lastMessageDate ?: 0L }
                .thenByDescending { it.space.getSingleValue<Double>(Relations.CREATED_DATE) ?: 0.0 }
        )

        return VaultSectionView(
            pinnedSpaces = sortedPinnedSpaces,
            mainSpaces = sortedUnpinnedSpaces
        )
    }

    private suspend fun mapToVaultSpaceViewItemWithCanPin(
        space: ObjectWrapper.SpaceView,
        chatPreview: Chat.Preview?,
        permissions: Map<Id, SpaceMemberPermissions>,
        isPinned: Boolean,
        pinnedCount: Int
    ): VaultSpaceView {
        val showPinButton = isPinned || pinnedCount < VaultSectionView.MAX_PINNED_SPACES
        return when {
            chatPreview != null -> {
                createChatView(space, chatPreview, permissions, showPinButton)
            }
            else -> {
                createStandardSpaceView(space, permissions, showPinButton)
            }
        }
    }

    private suspend fun mapToAttachmentPreview(
        attachment: Chat.Message.Attachment,
        dependency: ObjectWrapper.Basic
    ): VaultSpaceView.AttachmentPreview? {
        // 1️⃣ Determine if we have a valid object to render a "real" icon
        val isValid = dependency.isValid == true

        // 2️⃣ Helper to pick the preview‐type enum
        val previewType = when (attachment.type) {
            Chat.Message.Attachment.Type.Image -> VaultSpaceView.AttachmentType.IMAGE
            Chat.Message.Attachment.Type.File -> VaultSpaceView.AttachmentType.FILE
            Chat.Message.Attachment.Type.Link -> VaultSpaceView.AttachmentType.LINK
        }

        // 3️⃣ Helper to produce the "default" fallback icon when dependency is missing or invalid
        fun defaultIconFor(type: Chat.Message.Attachment.Type): ObjectIcon = when (type) {
            Chat.Message.Attachment.Type.Image ->
                FileDefault(mime = MimeTypes.Category.IMAGE)

            Chat.Message.Attachment.Type.File ->
                FileDefault(mime = MimeTypes.Category.OTHER)

            Chat.Message.Attachment.Type.Link ->
                ObjectIcon.TypeIcon.Default.DEFAULT
        }

        // 4️⃣ Helper to produce the "real" icon when we have a valid object
        suspend fun realIconFor(type: Chat.Message.Attachment.Type): ObjectIcon = when (type) {
            Chat.Message.Attachment.Type.Image,
            Chat.Message.Attachment.Type.Link ->
                dependency.objectIcon(
                    builder = urlBuilder,
                    objType = storeOfObjectTypes.getTypeOfObject(dependency)
                )

            Chat.Message.Attachment.Type.File -> {
                val mime = dependency.getSingleValue<String>(Relations.FILE_MIME_TYPE)
                val ext = dependency.getSingleValue<String>(Relations.FILE_EXT)
                ObjectIcon.File(mime = mime, extensions = ext)
            }
        }

        // 5️⃣ Build the preview, choosing between default vs. real icon
        val icon = if (isValid) {
            realIconFor(type = attachment.type)
        } else {
            Timber.w("Object for attachment ${attachment.target} not valid")
            defaultIconFor(type = attachment.type)
        }

        // 6️⃣ Only link‐types get a title
        val title = if (isValid && attachment.type == Chat.Message.Attachment.Type.Link) {
            fieldParser.getObjectName(objectWrapper = dependency)
        } else null

        return VaultSpaceView.AttachmentPreview(
            type = previewType,
            objectIcon = icon,
            title = title
        )
    }



    private suspend fun createChatView(
        space: ObjectWrapper.SpaceView,
        chatPreview: Chat.Preview,
        permissions: Map<Id, SpaceMemberPermissions>,
        showPinButton: Boolean
    ): VaultSpaceView.Chat {
        val creator = chatPreview.message?.creator ?: ""
        val messageText = chatPreview.message?.content?.text

        val creatorName = if (creator.isNotEmpty()) {
            val creatorObj = chatPreview.dependencies.find {
                it.getSingleValue<String>(
                    Relations.IDENTITY
                ) == creator
            }
            creatorObj?.name ?: "Unknown"
        } else {
            null
        }

        val previewText = if (creatorName != null && messageText != null) {
            "$creatorName: $messageText"
        } else {
            messageText
        }

        val messageTime = chatPreview.message?.createdAt?.let { timeInSeconds ->
            if (timeInSeconds > 0) {
                dateProvider.getChatPreviewDate(timeInSeconds = timeInSeconds)
            } else null
        }

        // Build attachment previews with proper URLs
        val attachmentPreviews = chatPreview.message?.attachments?.mapNotNull { attachment ->
            val dependency = chatPreview.dependencies.find { it.id == attachment.target }
            if (dependency != null) {
                mapToAttachmentPreview(
                    attachment = attachment,
                    dependency = dependency
                )
            } else {
                null
            }

        } ?: emptyList()

        val perms =
            space.targetSpaceId?.let { permissions[it] } ?: SpaceMemberPermissions.NO_PERMISSIONS
        val isOwner = perms.isOwner()
        val isMuted = NotificationStateCalculator.calculateMutedState(space, notificationPermissionManager)

        return VaultSpaceView.Chat(
            space = space,
            icon = space.spaceIcon(
                builder = urlBuilder,
                spaceGradientProvider = SpaceGradientProvider.Default
            ),
            chatPreview = chatPreview,
            previewText = previewText,
            creatorName = creatorName,
            messageText = messageText,
            messageTime = messageTime,
            unreadMessageCount = chatPreview.state?.unreadMessages?.counter ?: 0,
            unreadMentionCount = chatPreview.state?.unreadMentions?.counter ?: 0,
            attachmentPreviews = attachmentPreviews,
            isOwner = isOwner,
            isMuted = isMuted,
            showPinButton = showPinButton
        )
    }

    private fun createStandardSpaceView(
        space: ObjectWrapper.SpaceView,
        permissions: Map<Id, SpaceMemberPermissions>,
        showPinButton: Boolean
    ): VaultSpaceView.Space {
        val perms =
            space.targetSpaceId?.let { permissions[it] } ?: SpaceMemberPermissions.NO_PERMISSIONS
        val isOwner = perms.isOwner()
        val isMuted = if (space.chatId == null) {
            null
        } else {
            NotificationStateCalculator.calculateMutedState(space, notificationPermissionManager)
        }

        val icon = space.spaceIcon(
            builder = urlBuilder,
            spaceGradientProvider = SpaceGradientProvider.Default
        )

        val accessType = stringResourceProvider.getSpaceAccessTypeName(accessType = space.spaceAccessType)
        
        return VaultSpaceView.Space(
            space = space,
            icon = icon,
            accessType = accessType,
            isOwner = isOwner,
            isMuted = isMuted,
            showPinButton = showPinButton
        )
    }

    fun onSpaceClicked(view: VaultSpaceView) {
        Timber.i("onSpaceClicked")
        viewModelScope.launch {
            handleSpaceSelection(view, emitSettings = false)
        }
    }

    fun onSpaceSettingsClicked(spaceId: Id) {
        viewModelScope.launch {
            val spaceView = sections.value.pinnedSpaces.find { it.space.id == spaceId }
                ?: sections.value.mainSpaces.find { it.space.id == spaceId }
            if (spaceView != null) {
                handleSpaceSelection(spaceView, emitSettings = true)
            } else {
                Timber.e("SpaceView not found for id: $spaceId")
            }
        }
    }

    private suspend fun handleSpaceSelection(view: VaultSpaceView, emitSettings: Boolean) {
        val targetSpace = view.space.targetSpaceId
        if (targetSpace != null) {
            analytics.sendEvent(eventName = EventsDictionary.switchSpace)
            spaceManager.set(targetSpace).fold(
                onFailure = {
                    Timber.e(it, "Could not select space")
                },
                onSuccess = {
                    proceedWithSavingCurrentSpace(
                        targetSpace = targetSpace,
                        chat = view.space.chatId?.ifEmpty { null },
                        spaceUxType = view.space.spaceUxType,
                        emitSettings = emitSettings
                    )
                }
            )
        } else {
            Timber.e("Missing target space")
        }
    }

    fun onSettingsClicked() {
        viewModelScope.launch {
            commands.emit(VaultCommand.OpenProfileSettings)
        }
    }

    fun onChooseSpaceTypeClicked() {
        viewModelScope.launch {
            showChooseSpaceType.value = true
        }
    }

    fun onCreateSpaceClicked() {
        viewModelScope.launch {
            showChooseSpaceType.value = false
            commands.emit(VaultCommand.CreateNewSpace)
        }
    }

    fun onCreateChatClicked() {
        viewModelScope.launch {
            showChooseSpaceType.value = false
            commands.emit(VaultCommand.CreateChat)
        }
    }

    fun onChooseSpaceTypeDismissed() {
        viewModelScope.launch {
            showChooseSpaceType.value = false
        }
    }

    fun onResume(deeplink: DeepLinkResolver.Action? = null) {
        Timber.d("onResume, deep link: $deeplink")
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenVault,
                props = Props(
                    map = mapOf(
                        EventsPropertiesKey.type to EventsDictionary.Type.general
                    )
                )
            )
        }
        
        // Refresh notification permission state to ensure UI reflects latest system settings
        notificationPermissionManager.refreshPermissionState()
        
        viewModelScope.launch {
            when (deeplink) {
                is DeepLinkResolver.Action.Import.Experience -> {
                    commands.emit(
                        VaultCommand.Deeplink.GalleryInstallation(
                            deepLinkType = deeplink.type,
                            deepLinkSource = deeplink.source
                        )
                    )
                }

                is DeepLinkResolver.Action.Invite -> {
                    delay(1000)
                    commands.emit(VaultCommand.Deeplink.Invite(deeplink.link))
                }

                is DeepLinkResolver.Action.Unknown -> {
                    if (BuildConfig.DEBUG) {
                        //sendToast("Could not resolve deeplink")
                    }
                }

                is DeepLinkResolver.Action.DeepLinkToObject -> {
                    onDeepLinkToObjectAwait(
                        obj = deeplink.obj,
                        space = deeplink.space,
                        switchSpaceIfObjectFound = true
                    ).collect { result ->
                        when (result) {
                            is DeepLinkToObjectDelegate.Result.Error -> {
                                val link = deeplink.invite
                                if (link != null) {
                                    commands.emit(
                                        VaultCommand.Deeplink.Invite(
                                            link = spaceInviteResolver.createInviteLink(
                                                contentId = link.cid,
                                                encryptionKey = link.key
                                            )
                                        )
                                    )
                                } else {
                                    commands.emit(VaultCommand.Deeplink.DeepLinkToObjectNotWorking)
                                }
                            }

                            is DeepLinkToObjectDelegate.Result.Success -> {
                                proceedWithNavigation(result.obj.navigation())
                            }
                        }
                    }
                }

                is DeepLinkResolver.Action.DeepLinkToMembership -> {
                    commands.emit(
                        VaultCommand.Deeplink.MembershipScreen(
                            tierId = deeplink.tierId
                        )
                    )
                }

                else -> {
                    Timber.d("No deep link")
                }
            }
        }
        viewModelScope.launch {
            appActionManager.setup(AppActionManager.Action.ClearAll)
        }
    }

    fun onStart() {
        Timber.d("VaultViewModel - onStart called")
        // Only set isReturningFromSpace if initial load is complete
        if (hasInitialLoadCompleted) {
            isReturningFromSpace = true
            // Show loading immediately to prevent seeing old data
            loadingState.value = true
            Timber.d("VaultViewModel - isReturningFromSpace set to true, loading started immediately")
        } else {
            Timber.d("VaultViewModel - Initial load not complete, not setting isReturningFromSpace")
        }
        chatPreviewContainer.start()
        Timber.d("VaultViewModel - chatPreviewContainer started")
    }

    fun onStop() {
        Timber.d("onStop")
        chatPreviewContainer.stop()
    }

    fun processPendingDeeplink() {
        viewModelScope.launch {
            delay(1000) // Simulate some delay
            pendingIntentStore.getDeepLinkInvite()?.let { deeplink ->
                Timber.d("Processing pending deeplink: $deeplink")
                commands.emit(VaultCommand.Deeplink.Invite(deeplink))
                pendingIntentStore.clearDeepLinkInvite()
            }
        }
    }

    private suspend fun proceedWithSavingCurrentSpace(
        targetSpace: String,
        chat: Id?,
        spaceUxType: SpaceUxType?,
        emitSettings: Boolean = false
    ) {
        saveCurrentSpace.async(
            SaveCurrentSpace.Params(SpaceId(targetSpace))
        ).fold(
            onFailure = {
                Timber.e(it, "Error while saving current space on vault screen")
            },
            onSuccess = {
                Timber.d("Successfully saved current space: $targetSpace, Space UX Type: $spaceUxType, Chat ID: $chat")
                if (emitSettings) {
                    commands.emit(VaultCommand.OpenSpaceSettings(SpaceId(targetSpace)))
                } else if (spaceUxType == SpaceUxType.CHAT && chat != null) {
                    commands.emit(
                        VaultCommand.EnterSpaceLevelChat(
                            space = Space(targetSpace),
                            chat = chat
                        )
                    )
                } else {
                    commands.emit(
                        VaultCommand.EnterSpaceHomeScreen(
                            space = Space(targetSpace)
                        )
                    )
                }
            }
        )
    }

    private fun proceedWithNavigation(navigation: OpenObjectNavigation) {
        val nav = when (navigation) {
            is OpenObjectNavigation.OpenDataView -> {
                OpenSet(
                    ctx = navigation.target,
                    space = navigation.space,
                    view = null
                )
            }

            is OpenObjectNavigation.OpenEditor -> {

                OpenObject(
                    ctx = navigation.target,
                    space = navigation.space
                )

            }

            is OpenObjectNavigation.OpenChat -> {
                OpenChat(
                    ctx = navigation.target,
                    space = navigation.space
                )

            }

            is OpenObjectNavigation.UnexpectedLayoutError -> {
                VaultNavigation.ShowError("Unexpected layout: ${navigation.layout}")
            }

            OpenObjectNavigation.NonValidObject -> {
                VaultNavigation.ShowError("Object id is missing")
            }

            is OpenObjectNavigation.OpenDateObject -> {
                OpenDateObject(
                    ctx = navigation.target,
                    space = navigation.space
                )

            }

            is OpenObjectNavigation.OpenParticipant -> {
                OpenParticipant(
                    ctx = navigation.target,
                    space = navigation.space
                )

            }

            is OpenObjectNavigation.OpenType -> {
                OpenType(
                    target = navigation.target,
                    space = navigation.space
                )
            }

            is OpenObjectNavigation.OpenBookmarkUrl -> {
                VaultNavigation.OpenUrl(url = navigation.url)
            }
        }
        viewModelScope.launch {
            Timber.d("Proceeding with navigation: $nav")
            navigations.emit(nav)
        }
    }

    fun setSpaceNotificationState(spaceTargetId: Id, newState: NotificationState) {
        Timber.d("Setting notification state for spaceTargetId: $spaceTargetId to $newState")
        viewModelScope.launch {
            setSpaceNotificationMode.async(
                SetSpaceNotificationMode.Params(spaceViewId = spaceTargetId, mode = newState)
            ).fold(
                onSuccess = {
                    Timber.d("Successfully set notification state to: $newState for space: $spaceTargetId")
                },
                onFailure = { error ->
                    Timber.e("Failed to set notification state: $error")
                    notificationError.value = error.message ?: "Unknown error"
                }
            )
        }
    }

    fun clearNotificationError() {
        notificationError.value = null
    }

    fun clearVaultError() {
        vaultErrors.value = VaultErrors.Hidden
    }

    fun onDeleteSpaceMenuClicked(spaceId: Id?) {
        if (spaceId == null) {
            Timber.e("Space ID is null, cannot proceed with deletion")
            return
        }
        viewModelScope.launch {
            commands.emit(VaultCommand.ShowDeleteSpaceWarning(spaceId))
        }
    }

    fun onLeaveSpaceMenuClicked(spaceId: Id) {
        viewModelScope.launch {
            commands.emit(VaultCommand.ShowLeaveSpaceWarning(spaceId))
        }
    }

    fun onPinSpaceClicked(spaceId: Id) {
        viewModelScope.launch {
            val currentSections = sections.value
            val pinnedSpaces = currentSections.pinnedSpaces
            
            if (!canPinSpace()) {
                Timber.w("Max pinned spaces limit reached: ${MAX_PINNED_SPACES}")
                // Show limit reached error
                vaultErrors.value = VaultErrors.MaxPinnedSpacesReached
                return@launch
            }
            
            // Filter out the space being pinned if it's already in the list
            val newOrder = pinnedSpaces.filter { it.space.id != spaceId }.map { it.space.id }.toMutableList()
            // Insert the space at the beginning (position 0)
            newOrder.add(0, spaceId)
            
            setSpaceOrder.async(
                SetSpaceOrder.Params(
                    spaceViewId = spaceId,
                    spaceViewOrder = newOrder
                )
            ).fold(
                onFailure = { error ->
                    Timber.e(error, "Failed to pin space: $spaceId")
                    notificationError.value = error.message ?: "Failed to pin space"
                },
                onSuccess = { finalOrder ->
                    Timber.d("Successfully pinned space: $spaceId with final order: $finalOrder")
                    // The finalOrder contains the actual order from middleware with lexids
                    // Backend has confirmed the order, so we can trust the current state
                    // The space subscription will automatically update with the new order
                }
            )
        }
    }

    fun onUnpinSpaceClicked(spaceId: Id) {
        viewModelScope.launch {
            unpinSpace.async(
                UnpinSpace.Params(spaceId = spaceId)
            ).fold(
                onFailure = { error ->
                    Timber.e(error, "Failed to unpin space: $spaceId")
                    notificationError.value = error.message ?: "Failed to unpin space"
                },
                onSuccess = {
                    Timber.d("Successfully unpinned space: $spaceId")
                }
            )
        }
    }

    fun onDeleteSpaceWarningCancelled() {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.clickDeleteSpaceWarning,
                props = Props(mapOf(EventsPropertiesKey.type to "Cancel"))
            )
        }
    }

    fun onLeaveSpaceWarningCancelled() {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.clickDeleteSpaceWarning,
                props = Props(mapOf(EventsPropertiesKey.type to "Cancel"))
            )
        }
    }

    fun onDeleteSpaceAcceptedClicked(spaceId: Id?) {
        if (spaceId == null) {
            Timber.e("Space ID is null, cannot proceed with delete space")
            return
        }
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.clickDeleteSpaceWarning,
                props = Props(mapOf(EventsPropertiesKey.type to "Delete"))
            )
        }
        proceedWithSpaceDeletion(spaceId)
    }

    fun onLeaveSpaceAcceptedClicked(spaceId: Id?) {
        if (spaceId == null) {
            Timber.e("Space ID is null, cannot proceed with leaving space")
            return
        }
        viewModelScope.launch {
            analytics.sendEvent(eventName = EventsDictionary.leaveSpace)
        }
        proceedWithSpaceDeletion(spaceId)
    }

    private fun proceedWithSpaceDeletion(spaceId: Id) {
        viewModelScope.launch {
            deleteSpace
                .async(SpaceId(spaceId))
                .fold(
                    onSuccess = {
                        analytics.sendEvent(
                            eventName = EventsDictionary.deleteSpace,
                            props = Props(mapOf(EventsPropertiesKey.type to "Private"))
                        )
                    },
                    onFailure = { error ->
                        Timber.e(error, "Error while deleting or leaving space")
                        notificationError.value = error.message ?: "Unknown error"
                    }
                )
        }
    }

    fun getPermissionsForSpace(spaceId: Id): SpaceMemberPermissions {
        return userPermissionProvider.get(SpaceId(spaceId)) ?: SpaceMemberPermissions.NO_PERMISSIONS
    }

    /**
     * Returns true if the user can pin a space (i.e., the max pinned spaces limit is not reached).
     */
    fun canPinSpace(): Boolean {
        return sections.value.pinnedSpaces.size < MAX_PINNED_SPACES
    }

    // Local state for tracking order changes during drag operations
    private var pendingPinnedSpacesOrder: List<Id>? = null
    private var lastMovedSpaceId: Id? = null

    //region Drag and Drop
    fun onOrderChanged(fromSpaceId: String, toSpaceId: String) {
        Timber.d("onOrderChanged: from=$fromSpaceId, to=$toSpaceId")

        // Get current settings to work with the existing order
        val currentSections = sections.value
        val currentPinnedSpaces = currentSections.pinnedSpaces

        // Find indices in the current pinned spaces list
        val fromIndex = currentPinnedSpaces.indexOfFirst { it.space.id == fromSpaceId }
        val toIndex = currentPinnedSpaces.indexOfFirst { it.space.id == toSpaceId }

        if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
            // Create new ordered list by moving the item (only update local state)
            val newPinnedSpacesList = currentPinnedSpaces.toMutableList()
            val movedItem = newPinnedSpacesList.removeAt(fromIndex)
            newPinnedSpacesList.add(toIndex, movedItem)

            // Store the new order for later persistence in onDragEnd
            val newPinnedOrder = newPinnedSpacesList.map { it.space.id }

            // Store pending order to be saved in onDragEnd (only pinned spaces)
            pendingPinnedSpacesOrder = newPinnedOrder
            lastMovedSpaceId = fromSpaceId

            // Update local sections state immediately for UI responsiveness
            val updatedSections = currentSections.copy(pinnedSpaces = newPinnedSpacesList)
            sections.value = updatedSections
        }
    }

    fun onDragEnd() {
        Timber.d("onDragEnd called")
        // Persist the order changes made during the drag operation
        pendingPinnedSpacesOrder?.let { newOrder ->
            viewModelScope.launch {
                analytics.sendEvent(eventName = EventsDictionary.reorderSpace)
                
                // Get the current pinned spaces to determine which space was moved
                val currentPinnedSpaces = sections.value.pinnedSpaces
                
                if (currentPinnedSpaces.isNotEmpty()) {
                    // Use the tracked moved space ID or fall back to the first space
                    val movedSpaceId = lastMovedSpaceId ?: newOrder.firstOrNull() ?: return@launch
                    
                    setSpaceOrder.async(
                        SetSpaceOrder.Params(
                            spaceViewId = movedSpaceId,
                            spaceViewOrder = newOrder
                        )
                    ).fold(
                        onFailure = { error ->
                            Timber.e(error, "Failed to reorder pinned spaces: $newOrder")
                            notificationError.value = error.message ?: "Failed to reorder spaces"
                            // Reset pending state on failure
                            pendingPinnedSpacesOrder = null
                            lastMovedSpaceId = null
                        },
                        onSuccess = { finalOrder ->
                            Timber.d("Successfully reordered pinned spaces with final order: $finalOrder")
                            // The finalOrder contains the actual order from middleware with lexids
                            // Verify if the backend order matches our expected order
                            if (finalOrder != newOrder) {
                                Timber.w("Backend order differs from expected order. Expected: $newOrder, Actual: $finalOrder")
                                // The subscription will automatically update with the correct order
                            }
                            // Clear pending state as the order has been persisted
                            pendingPinnedSpacesOrder = null
                            lastMovedSpaceId = null
                        }
                    )
                }
            }
        }
    }

    //endregion


    companion object {
        const val SPACE_VAULT_DEBOUNCE_DURATION = 300L
    }
}