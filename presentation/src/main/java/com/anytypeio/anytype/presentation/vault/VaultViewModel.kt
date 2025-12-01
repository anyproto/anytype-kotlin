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
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.const.MimeTypes
import com.anytypeio.anytype.core_utils.tools.AppInfo
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
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
import com.anytypeio.anytype.domain.vault.SetCreateSpaceBadgeSeen
import com.anytypeio.anytype.domain.vault.SetSpaceOrder
import com.anytypeio.anytype.domain.vault.ShouldShowCreateSpaceBadge
import com.anytypeio.anytype.domain.vault.UnpinSpace
import com.anytypeio.anytype.domain.wallpaper.GetSpaceWallpapers
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.extension.resolveParticipantName
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManagerImpl
import com.anytypeio.anytype.presentation.notifications.NotificationStateCalculator.calculateChatNotificationState
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon.FileDefault
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.profile.profileIcon
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import com.anytypeio.anytype.presentation.vault.VaultNavigation.OpenChat
import com.anytypeio.anytype.presentation.vault.VaultNavigation.OpenDateObject
import com.anytypeio.anytype.presentation.vault.VaultNavigation.OpenObject
import com.anytypeio.anytype.presentation.vault.VaultNavigation.OpenParticipant
import com.anytypeio.anytype.presentation.vault.VaultNavigation.OpenSet
import com.anytypeio.anytype.presentation.vault.VaultNavigation.OpenType
import com.anytypeio.anytype.presentation.wallpaper.computeWallpaperResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Data class to hold aggregated unread counts for all chats in a space
 */
private data class UnreadCounts(
    val unreadMessages: Int,
    val unreadMentions: Int
)

@OptIn(ExperimentalCoroutinesApi::class)
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
    private val chatPreviewContainer: ChatPreviewContainer,
    private val chatsDetailsContainer: ChatsDetailsSubscriptionContainer,
    private val participantContainer: ParticipantSubscriptionContainer,
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
    private val setSpaceOrder: SetSpaceOrder,
    private val getSpaceWallpapers: GetSpaceWallpapers,
    private val shouldShowCreateSpaceBadge: ShouldShowCreateSpaceBadge,
    private val setCreateSpaceBadgeSeen: SetCreateSpaceBadgeSeen,
    private val appInfo: AppInfo
) : ViewModel(),
    DeepLinkToObjectDelegate by deepLinkToObjectDelegate {

    val commands = MutableSharedFlow<VaultCommand>(replay = 0)
    val navigations = MutableSharedFlow<VaultNavigation>(replay = 0)
    val showChooseSpaceType = MutableStateFlow(false)
    val notificationError = MutableStateFlow<String?>(null)
    val vaultErrors = MutableStateFlow<VaultErrors>(VaultErrors.Hidden)

    // Track notification permission status for profile icon badge
    val isNotificationDisabled = MutableStateFlow(false)

    // Track whether to show the blue dot badge on "Create a new space" button
    val showCreateSpaceBadge = MutableStateFlow(false)

    private val previewFlow: StateFlow<ChatPreviewContainer.PreviewState> =
        chatPreviewContainer.observePreviewsWithAttachments()
            .filterIsInstance<ChatPreviewContainer.PreviewState.Ready>() // wait until ready
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                ChatPreviewContainer.PreviewState.Loading
            )

    private val spaceFlow: StateFlow<List<ObjectWrapper.SpaceView>> =
        spaceViewSubscriptionContainer.observe()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val permissionsFlow: StateFlow<Map<Id, SpaceMemberPermissions>> =
        userPermissionProvider.all()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val notificationsFlow: StateFlow<NotificationPermissionManagerImpl.PermissionState> =
        notificationPermissionManager.permissionState()
            .stateIn(
                viewModelScope, SharingStarted.Eagerly,
                NotificationPermissionManagerImpl.PermissionState.NotRequested
            )

    private val chatDetailsFlow: StateFlow<List<ObjectWrapper.Basic>> =
        chatsDetailsContainer.observe()
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val participantsFlow: StateFlow<List<ObjectWrapper.SpaceMember>> =
        participantContainer.observe()
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val profileView = profileContainer.observe().map { obj ->
        AccountProfile.Data(
            name = obj.name.orEmpty(),
            icon = obj.profileIcon(urlBuilder)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountProfile.Idle)

    private val _uiState = MutableStateFlow<VaultUiState>(VaultUiState.Loading)
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        Timber.i("VaultViewModel - init started")
        combine(
            combine(
                previewFlow.filterIsInstance<ChatPreviewContainer.PreviewState.Ready>(),
                spaceFlow,
                permissionsFlow
            ) { previews, spaces, perms -> Triple(previews, spaces, perms) },
            combine(
                notificationsFlow,
                chatDetailsFlow,
                participantsFlow
            ) { _, chatDetails, participants -> Pair(chatDetails, participants) }
        ) { (previews, spaces, perms), (chatDetails, participants) ->
            transformToVaultSpaceViews(spaces, previews.items, perms, chatDetails, participants)
        }.onEach { sections ->
            val previousState = _uiState.value

            // Check if we should preserve drag order during backend transactions
            val isDuringBackendTransaction = pendingPinnedSpacesOrder != null
            val isBackendRemovingSpaces = isDuringBackendTransaction && 
                previousState is VaultUiState.Sections && 
                sections.pinnedSpaces.size < previousState.pinnedSpaces.size
            val shouldPreserveDragOrder = isDuringBackendTransaction && isBackendRemovingSpaces
            
            if (shouldPreserveDragOrder) {
                Timber.d("VaultViewModel - ⚡ Preserving drag order during backend UNSET transaction (preventing space removal)")
                // Don't update the UI state - keep the current drag order visible
            } else {
                Timber.d("VaultViewModel - 🎨 Updating UI state with new sections, pinned spaces: ${sections.pinnedSpaces.size}")
                _uiState.value = sections
            }
        }
            .launchIn(viewModelScope)
        
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
                Timber.w(e, "Error initializing notification permission monitoring")
            }
        }

        // Track whether to show blue dot badge on "Create a new space" button
        viewModelScope.launch {
            shouldShowCreateSpaceBadge.async(
                ShouldShowCreateSpaceBadge.Params(
                    currentAppVersion = appInfo.versionName
                )
            ).fold(
                onSuccess = { shouldShow ->
                    showCreateSpaceBadge.value = shouldShow
                    Timber.d("DROID-3864, Create space badge visibility: $shouldShow")
                },
                onFailure = { e ->
                    Timber.w(e, "DROID-3864, Error checking create space badge visibility")
                    showCreateSpaceBadge.value = false
                }
            )
        }
    }

    fun onStart() {
        Timber.i("VaultViewModel - onStart")
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
        permissions: Map<Id, SpaceMemberPermissions>,
        chatDetails: List<ObjectWrapper.Basic>,
        participants: List<ObjectWrapper.SpaceMember>
    ): VaultUiState.Sections {
        // Index participants by identity for O(1) lookup when resolving creator names
        val participantsByIdentity: Map<Id, ObjectWrapper.SpaceMember> = participants.associateBy { it.identity }

        // Fetch all space wallpapers once
        val wallpapers: Map<Id, Wallpaper> = getSpaceWallpapers.async(Unit).getOrNull() ?: run {
            Timber.w("Failed to fetch space wallpapers")
            emptyMap()
        }

        // Index chatPreviews by space.id for O(1) lookup, selecting most recent per space
        val chatPreviewMap = chatPreviews.groupBy { it.space.id }
            .mapValues { (_, previews) ->
                // Select preview with latest timestamp, falling back to first if all timestamps are invalid
                previews.maxByOrNull { it.message?.createdAt ?: 0L } ?: previews.firstOrNull()
            }

        // Calculate total unread counts for all chats per space
        val unreadCountsPerSpace = chatPreviews.groupBy { it.space.id }
            .mapValues { (_, previews) ->
                // Message count: sum all messages and cap at 999 (displayed as number badge in UI)
                val totalUnreadMessages = previews.sumOf { it.state?.unreadMessages?.counter ?: 0 }.coerceAtMost(999)
                // Mention count: 1 if ANY chat has mentions, 0 otherwise (displayed as icon-only indicator in UI)
                val hasMentions = if (previews.any { (it.state?.unreadMentions?.counter ?: 0) > 0 }) 1 else 0
                UnreadCounts(totalUnreadMessages, hasMentions)
            }

        // Index chatDetails by chat ID for O(1) lookup of chat names
        val chatDetailsMap = chatDetails.associateBy { it.id }
        // Map all active spaces to VaultSpaceView objects
        val allSpacesRaw = spacesFromFlow
            .filter { space -> (space.isActive || space.isLoading) }

        val allSpaces = allSpacesRaw.map { space ->
            val chatPreview = space.targetSpaceId?.let { spaceId ->
                chatPreviewMap[spaceId]
            }
            val unreadCounts = space.targetSpaceId?.let { spaceId ->
                unreadCountsPerSpace[spaceId]
            }

            mapToVaultSpaceViewItemWithCanPin(space, chatPreview, unreadCounts, permissions, wallpapers, chatDetailsMap, participantsByIdentity)
        }

        // Loading state is now managed in the main combine flow, not here
        val loadingSpaceIndex = allSpaces.indexOfFirst { space -> space.space.isLoading }
        if (loadingSpaceIndex != -1) {
            Timber.d("Found loading space ID: ${allSpaces[loadingSpaceIndex].space.id}, " +
                    "space name: ${allSpaces[loadingSpaceIndex].space.name}")
        } else {
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

        // Sort unpinned spaces by effective date (descending), then by creation date (descending)
        val sortedUnpinnedSpaces = unpinnedSpaces.sortedWith(
            compareByDescending<VaultSpaceView> { calculateEffectiveDate(it) ?: 0L }
                .thenByDescending { it.space.getSingleValue<Double>(Relations.CREATED_DATE) ?: 0.0 }
        )

        return VaultUiState.Sections(
            pinnedSpaces = sortedPinnedSpaces,
            mainSpaces = sortedUnpinnedSpaces
        )
    }

    /**
     * Calculates the effective date for a space by taking the maximum of lastMessageDate and spaceJoinDate.
     * - If both lastMessageDate and spaceJoinDate are available, return the maximum
     * - If only one is available, return that one
     * - If neither is available, return null (fallback to creation date in comparator)
     */
    private fun calculateEffectiveDate(space: VaultSpaceView): Long? {
        val lastMessageDate = space.lastMessageDate
        val spaceJoinDate = space.space.spaceJoinDate?.toLong()

        return when {
            lastMessageDate != null && spaceJoinDate != null -> maxOf(lastMessageDate, spaceJoinDate)
            lastMessageDate != null -> lastMessageDate
            spaceJoinDate != null -> spaceJoinDate
            else -> null
        }
    }

    private suspend fun mapToVaultSpaceViewItemWithCanPin(
        space: ObjectWrapper.SpaceView,
        chatPreview: Chat.Preview?,
        unreadCounts: UnreadCounts?,
        permissions: Map<Id, SpaceMemberPermissions>,
        wallpapers: Map<Id, Wallpaper>,
        chatDetailsMap: Map<Id, ObjectWrapper.Basic>,
        participantsByIdentity: Map<Id, ObjectWrapper.SpaceMember>
    ): VaultSpaceView {
        return when {
            // Pure CHAT space with chat preview → VaultSpaceView.ChatSpace
            space.spaceUxType == SpaceUxType.CHAT && chatPreview != null -> {
                createChatSpaceView(space, chatPreview, unreadCounts, permissions, wallpapers, chatDetailsMap, participantsByIdentity)
            }
            // any other space with chat preview → VaultSpaceView.DataSpaceWithChat
            chatPreview != null -> {
                createDataSpaceWithChatView(space, chatPreview, unreadCounts, permissions, wallpapers, chatDetailsMap, participantsByIdentity)
            }
            // any other space without chat preview → VaultSpaceView.DataSpace
            else -> {
                createDataSpaceView(space, permissions, wallpapers)
            }
        }
    }

    private suspend fun mapToAttachmentPreview(
        attachment: Chat.Message.Attachment,
        dependency: ObjectWrapper.Basic?
    ): VaultSpaceView.AttachmentPreview {
        Timber.d("mapToAttachmentPreview, attachment: $attachment, dependency: $dependency")
        
        // Check if we have a valid dependency with ID
        val hasValidDependency = dependency != null && dependency.isValid
        
        // Determine the actual type based on MIME type if available
        val effectiveType = if (hasValidDependency && attachment.type == Chat.Message.Attachment.Type.File) {
            val mimeType = dependency.getSingleValue<String>(Relations.FILE_MIME_TYPE)
            when {
                mimeType?.startsWith("image/") == true -> Chat.Message.Attachment.Type.Image
                else -> attachment.type
            }
        } else {
            attachment.type
        }
        
        // Helper to pick the preview‐type enum based on effective type
        val previewType = when (effectiveType) {
            Chat.Message.Attachment.Type.Image -> VaultSpaceView.AttachmentType.IMAGE
            Chat.Message.Attachment.Type.File -> VaultSpaceView.AttachmentType.FILE
            Chat.Message.Attachment.Type.Link -> VaultSpaceView.AttachmentType.LINK
        }

        // Helper to produce the "default" fallback icon when dependency is missing or invalid
        fun defaultIconFor(type: Chat.Message.Attachment.Type): ObjectIcon = when (type) {
            Chat.Message.Attachment.Type.Image ->
                FileDefault(mime = MimeTypes.Category.IMAGE)

            Chat.Message.Attachment.Type.File ->
                FileDefault(mime = MimeTypes.Category.OTHER)

            Chat.Message.Attachment.Type.Link ->
                ObjectIcon.TypeIcon.Default.DEFAULT
        }

        // Build the icon based on whether we have valid dependency data
        val icon = if (hasValidDependency) {
            try {
                when (effectiveType) {
                    Chat.Message.Attachment.Type.Image -> {
                        // For image attachments, create an ObjectIcon.Basic.Image with the actual image URL
                        val imageHash = dependency.id
                        val imageUrl = urlBuilder.thumbnail(imageHash)
                        ObjectIcon.Basic.Image(
                            hash = imageUrl,
                            fallback = ObjectIcon.TypeIcon.Fallback.DEFAULT
                        )
                    }

                    Chat.Message.Attachment.Type.File -> {
                        val mime = dependency.getSingleValue<String>(Relations.FILE_MIME_TYPE)
                        val ext = dependency.getSingleValue<String>(Relations.FILE_EXT)
                        ObjectIcon.File(mime = mime, extensions = ext)
                    }
                    
                    Chat.Message.Attachment.Type.Link ->
                        dependency.objectIcon(
                            builder = urlBuilder,
                            objType = storeOfObjectTypes.getTypeOfObject(dependency)
                        )
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to create icon for attachment ${attachment.target}")
                defaultIconFor(type = effectiveType)
            }
        } else {
            // No dependency yet or invalid - use default icon as placeholder
            Timber.d("Using default icon for attachment ${attachment.target} (dependency: ${dependency?.id})")
            defaultIconFor(type = effectiveType)
        }

        // Only link‐types get a title when we have valid dependency
        val title = if (hasValidDependency && effectiveType == Chat.Message.Attachment.Type.Link) {
            fieldParser.getObjectName(objectWrapper = dependency)
        } else null

        return VaultSpaceView.AttachmentPreview(
            type = previewType,
            objectIcon = icon,
            title = title
        )
    }


    /**
     * Data class holding extracted message preview data.
     * Used to avoid duplicating preview extraction logic.
     */
    private data class MessagePreviewData(
        val creatorName: String?,
        val messageText: String?,
        val messageTime: String?,
        val attachmentPreviews: List<VaultSpaceView.AttachmentPreview>
    )

    /**
     * Extracts message preview data from a chat preview.
     * Returns null values when message is deleted (null).
     */
    private suspend fun extractMessagePreviewData(
        chatPreview: Chat.Preview?,
        participantsByIdentity: Map<Id, ObjectWrapper.SpaceMember>
    ): MessagePreviewData {
        val message = chatPreview?.message

        val creatorName = if (message != null) {
            participantsByIdentity.resolveParticipantName(
                identity = message.creator,
                fallback = stringResourceProvider.getUntitledCreatorName()
            )
        } else null

        val messageText = message?.content?.text

        val messageTime = message?.createdAt?.let { timeInSeconds ->
            if (timeInSeconds > 0) {
                dateProvider.getChatPreviewDate(timeInSeconds = timeInSeconds)
            } else null
        }

        val attachmentPreviews = if (chatPreview != null && message != null) {
            message.attachments?.map { attachment ->
                val dependency = chatPreview.dependencies.find { it.id == attachment.target }
                val preview = mapToAttachmentPreview(attachment, dependency)
                Timber.d("Created attachment preview: $preview for attachment: $attachment")
                preview
            } ?: emptyList()
        } else {
            emptyList()
        }

        return MessagePreviewData(creatorName, messageText, messageTime, attachmentPreviews)
    }

    /**
     * Create a Vault View for a Chat Space with a chat preview
     */
    private suspend fun createChatSpaceView(
        space: ObjectWrapper.SpaceView,
        chatPreview: Chat.Preview?,
        unreadCounts: UnreadCounts?,
        permissions: Map<Id, SpaceMemberPermissions>,
        wallpapers: Map<Id, Wallpaper>,
        chatDetailsMap: Map<Id, ObjectWrapper.Basic>,
        participantsByIdentity: Map<Id, ObjectWrapper.SpaceMember>
    ): VaultSpaceView.ChatSpace {
        val previewData = extractMessagePreviewData(chatPreview, participantsByIdentity)

        val icon = space.spaceIcon(urlBuilder)

        val perms =
            space.targetSpaceId?.let { permissions[it] } ?: SpaceMemberPermissions.NO_PERMISSIONS
        val isOwner = perms.isOwner()

        val wallpaper = space.targetSpaceId.let { wallpapers[it] } ?: Wallpaper.Default
        val wallpaperResult = computeWallpaperResult(
            icon = icon,
            wallpaper = wallpaper
        )

        return VaultSpaceView.ChatSpace(
            space = space,
            icon = icon,
            chatPreview = chatPreview,
            creatorName = previewData.creatorName,
            messageText = previewData.messageText,
            messageTime = previewData.messageTime,
            unreadMessageCount = unreadCounts?.unreadMessages ?: 0,
            unreadMentionCount = unreadCounts?.unreadMentions ?: 0,
            attachmentPreviews = previewData.attachmentPreviews,
            isOwner = isOwner,
            spaceNotificationState = space.spacePushNotificationMode,
            wallpaper = wallpaperResult
        )
    }

    /**
     * Create a Vault View for Data Space with a chat preview
     */
    private suspend fun createDataSpaceWithChatView(
        space: ObjectWrapper.SpaceView,
        chatPreview: Chat.Preview,
        unreadCounts: UnreadCounts?,
        permissions: Map<Id, SpaceMemberPermissions>,
        wallpapers: Map<Id, Wallpaper>,
        chatDetailsMap: Map<Id, ObjectWrapper.Basic>,
        participantsByIdentity: Map<Id, ObjectWrapper.SpaceMember>
    ): VaultSpaceView.DataSpaceWithChat {
        val previewData = extractMessagePreviewData(chatPreview, participantsByIdentity)

        val icon = space.spaceIcon(urlBuilder)

        val perms =
            space.targetSpaceId?.let { permissions[it] } ?: SpaceMemberPermissions.NO_PERMISSIONS
        val isOwner = perms.isOwner()

        val wallpaper = space.targetSpaceId.let { wallpapers[it] } ?: Wallpaper.Default
        val wallpaperResult = computeWallpaperResult(
            icon = icon,
            wallpaper = wallpaper
        )

        // Lookup chat name from chat details subscription
        val chatName = chatDetailsMap[chatPreview.chat]?.name.orEmpty()

        return VaultSpaceView.DataSpaceWithChat(
            space = space,
            icon = icon,
            chatPreview = chatPreview,
            creatorName = previewData.creatorName,
            messageText = previewData.messageText,
            messageTime = previewData.messageTime,
            unreadMessageCount = unreadCounts?.unreadMessages ?: 0,
            unreadMentionCount = unreadCounts?.unreadMentions ?: 0,
            attachmentPreviews = previewData.attachmentPreviews,
            isOwner = isOwner,
            chatNotificationState = calculateChatNotificationState(
                chatSpace = space,
                chatId = chatPreview.chat
            ),
            wallpaper = wallpaperResult,
            spaceNotificationState = space.spacePushNotificationMode,
            chatName = chatName
        )
    }

    /**
     * Create a Vault View for Data Space WITHOUT chat preview
     */
    private fun createDataSpaceView(
        space: ObjectWrapper.SpaceView,
        permissions: Map<Id, SpaceMemberPermissions>,
        wallpapers: Map<Id, Wallpaper>
    ): VaultSpaceView.DataSpace {
        val perms =
            space.targetSpaceId?.let { permissions[it] } ?: SpaceMemberPermissions.NO_PERMISSIONS
        val isOwner = perms.isOwner()

        val icon = space.spaceIcon(urlBuilder)

        val accessType = stringResourceProvider.getSpaceAccessTypeName(accessType = space.spaceAccessType)

        val wallpaper = space.targetSpaceId?.let { wallpapers[it] } ?: Wallpaper.Default
        val wallpaperResult = computeWallpaperResult(
            icon = icon,
            wallpaper = wallpaper
        )

        return VaultSpaceView.DataSpace(
            space = space,
            icon = icon,
            accessType = accessType,
            isOwner = isOwner,
            wallpaper = wallpaperResult
        )
    }

    fun onSpaceClicked(view: VaultSpaceView) {
        Timber.i("onSpaceClicked")
        viewModelScope.launch {
            handleSpaceSelection(view, emitSettings = false)
        }
    }

    fun onSpaceSettingsClicked(spaceId: Id) {
        val state = uiState.value
        if (state !is VaultUiState.Sections) return
        viewModelScope.launch {
            val spaceView = state.pinnedSpaces.find { it.space.id == spaceId }
                ?: state.mainSpaces.find { it.space.id == spaceId }
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
            analytics.sendEvent(
                eventName = EventsDictionary.chatScreenVaultCreateMenu
            )
            showChooseSpaceType.value = true
            if (showCreateSpaceBadge.value) {
                // Mark the badge as seen when user clicks the create space button
                setCreateSpaceBadgeSeen.async(Unit).fold(
                    onSuccess = {
                        Timber.d("Create space badge marked as seen")
                    },
                    onFailure = { e ->
                        Timber.w(e, "Error marking create space badge as seen")
                    }
                )
                showCreateSpaceBadge.value = false
                Timber.d("Create space badge dismissed")
            }
        }
    }

    fun onCreateSpaceClicked() {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickVaultCreateMenuSpace
            )
        }
        viewModelScope.launch {
            showChooseSpaceType.value = false
            commands.emit(
                VaultCommand.CreateNewSpace(
                    spaceUxType = SpaceUxType.DATA // Default to DATA space type
                )
            )
        }
    }

    fun onCreateChatClicked() {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickVaultCreateMenuChat
            )
        }
        viewModelScope.launch {
            showChooseSpaceType.value = false
            commands.emit(
                VaultCommand.CreateNewSpace(
                    spaceUxType = SpaceUxType.CHAT // Default to CHAT space type
                )
            )
        }
    }

    fun onChooseSpaceTypeDismissed() {
        viewModelScope.launch {
            showChooseSpaceType.value = false
        }
    }
    
    fun onJoinViaQrClicked() {
        viewModelScope.launch {
            showChooseSpaceType.value = false
            commands.emit(VaultCommand.ScanQrCode)
        }
    }
    
    fun onQrCodeScanned(qrCode: String) {
        Timber.d("onQrCodeScanned: $qrCode")
        if (qrCode.isBlank()) {
            Timber.e("QR code is empty or blank")
            vaultErrors.value = VaultErrors.QrCodeIsNotValid
            return
        }
        vaultErrors.value = VaultErrors.Hidden
        viewModelScope.launch {
            try {
                val fileKey = spaceInviteResolver.parseFileKey(qrCode)
                val contentId = spaceInviteResolver.parseContentId(qrCode)

                if (fileKey != null && contentId != null) {
                    commands.emit(
                        VaultCommand.NavigateToRequestJoinSpace(link = qrCode)
                    )
                } else {
                    Timber.e("Failed to parse QR code: invalid format")
                    vaultErrors.value = VaultErrors.QrCodeIsNotValid
                }
            } catch (e: Exception) {
                Timber.e(e, "Error processing QR code")
                vaultErrors.value = VaultErrors.QrCodeIsNotValid
            }
        }
    }

    fun onQrScannerError() {
        Timber.e("QR scanner error")
        vaultErrors.value = VaultErrors.QrScannerError
    }
    
    fun onModalTryAgainClicked() {
        vaultErrors.value = VaultErrors.Hidden
        viewModelScope.launch {
            commands.emit(VaultCommand.ScanQrCode)
        }
    }
    
    fun onModalCancelClicked() {
        vaultErrors.value = VaultErrors.Hidden
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
    
    fun onShowCameraPermissionSettingsDialog() {
        vaultErrors.value = VaultErrors.CameraPermissionDenied
    }

    fun onPinSpaceClicked(spaceId: Id) {
        val state = uiState.value
        if (state !is VaultUiState.Sections) return
        viewModelScope.launch {
            val currentSections = state
            val pinnedSpaces = currentSections.pinnedSpaces

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
                    analytics.sendEvent(eventName = EventsDictionary.pinSpace)
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
                    analytics.sendEvent(eventName = EventsDictionary.unpinSpace)
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


    //region Drag and Drop
    // Local state for tracking order changes during drag operations
    private var pendingPinnedSpacesOrder: List<Id>? = null
    private var lastMovedSpaceId: Id? = null


    /**
     * Called when the order of pinned spaces changes during a drag operation.
     *
     * This method updates the UI state immediately to reflect the new order,
     * allowing for instant visual feedback during the drag operation.
     *
     * @param fromSpaceId The ID of the space being dragged from.
     * @param toSpaceId The ID of the space being dragged to.
     */
    fun onOrderChanged(fromSpaceId: String, toSpaceId: String) {
        Timber.d("VaultViewModel - onOrderChanged: from=$fromSpaceId, to=$toSpaceId")
        val previousState = _uiState.value
        
        // Mark that we're starting a drag operation if we haven't already
        if (pendingPinnedSpacesOrder == null) {
            Timber.d("VaultViewModel - Starting drag operation")
        }
        
        _uiState.update { state ->
            if (state !is VaultUiState.Sections) {
                Timber.d("VaultViewModel - onOrderChanged: Not in Sections state, ignoring")
                return      // no-op
            }
            val current = state.pinnedSpaces.toMutableList()
            val from = current.indexOfFirst { it.space.id == fromSpaceId }
            val to   = current.indexOfFirst { it.space.id == toSpaceId }
            if (from == -1 || to == -1 || from == to) {
                Timber.d("VaultViewModel - onOrderChanged: Invalid indices (from=$from, to=$to), ignoring")
                return // no-op
            }

            val movedItem = current.removeAt(from)
            current.add(to, movedItem)

            val newState = state.copy(pinnedSpaces = current)
            Timber.d("VaultViewModel - onOrderChanged: Creating new state with order: ${current.map { "${it.space.name}(${it.space.id.take(8)}...)" }}")
            
            newState      // ← immediate value visible to UI
        }
        
        val newState = _uiState.value
        val stateChanged = previousState != newState
        Timber.d("VaultViewModel - onOrderChanged: State changed: $stateChanged")
        if (stateChanged) {
            Timber.d("VaultViewModel - onOrderChanged: 🎨 Compose WILL redraw (immediate drag feedback)")
        } else {
            Timber.d("VaultViewModel - onOrderChanged: ⚡ Compose will NOT redraw (same state)")
        }
        
        pendingPinnedSpacesOrder = _uiState.value
            .let { (it as? VaultUiState.Sections)?.pinnedSpaces?.map { v -> v.space.id } }
        lastMovedSpaceId = fromSpaceId
    }

    /**
     * Called when the drag operation ends, either successfully or cancelled.
     *
     * This method persists the order changes made during the drag operation
     * and resets the local state tracking the pending order.
     */
    fun onDragEnd() {
        Timber.d("onDragEnd called")
        val state = uiState.value
        if (state !is VaultUiState.Sections) return
        // Persist the order changes made during the drag operation
        pendingPinnedSpacesOrder?.let { newOrder ->
            viewModelScope.launch {
                analytics.sendEvent(eventName = EventsDictionary.reorderSpace)
                
                // Get the current pinned spaces to determine which space was moved
                val currentPinnedSpaces = state.pinnedSpaces
                
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
                            clearDragState()
                        },
                        onSuccess = { finalOrder ->
                            Timber.d("Successfully reordered pinned spaces with final order: $finalOrder")
                            clearDragState()
                        }
                    )
                }
            }
        } ?: run {
            // No pending order changes, just clear drag state
            clearDragState()
        }
    }

    /**
     * Clears the drag-and-drop state by resetting the pending order and last moved space ID.
     *
     * This method is called at the end of a drag-and-drop operation, regardless of whether
     * the operation was successful or failed. It ensures that the local state is reset
     * to avoid inconsistencies in subsequent drag-and-drop actions.
     */
    private fun clearDragState() {
        Timber.d("VaultViewModel - Clearing drag state")
        pendingPinnedSpacesOrder = null
        lastMovedSpaceId = null
    }
    //endregion
}