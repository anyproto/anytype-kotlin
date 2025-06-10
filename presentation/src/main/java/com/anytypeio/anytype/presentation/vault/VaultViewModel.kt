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
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.settings.VaultSettings
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.vault.ObserveVaultSettings
import com.anytypeio.anytype.domain.vault.SetVaultSpaceOrder
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.confgs.ChatConfig
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
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
import com.anytypeio.anytype.presentation.mapper.objectIcon
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
    private val observeVaultSettings: ObserveVaultSettings,
    private val setVaultSpaceOrder: SetVaultSpaceOrder,
    private val analytics: Analytics,
    private val deepLinkToObjectDelegate: DeepLinkToObjectDelegate,
    private val appActionManager: AppActionManager,
    private val spaceInviteResolver: SpaceInviteResolver,
    private val profileContainer: ProfileSubscriptionManager,
    private val chatPreviewContainer: ChatPreviewContainer,
    private val pendingIntentStore: PendingIntentStore,
    private val stringResourceProvider: StringResourceProvider,
    private val dateProvider: DateProvider,
    private val fieldParser: FieldParser
) : ViewModel(),
    DeepLinkToObjectDelegate by deepLinkToObjectDelegate {

    val spaces = MutableStateFlow<List<VaultSpaceView>>(emptyList())
    val commands = MutableSharedFlow<VaultCommand>(replay = 0)
    val navigations = MutableSharedFlow<VaultNavigation>(replay = 0)
    val showChooseSpaceType = MutableStateFlow(false)

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
        Timber.i("VaultViewModel, init")
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
                observeVaultSettings.flow(),
                chatPreviewContainer.observePreviews()
            ) { spacesFromFlow, settings, chatPreviews ->
                transformToVaultSpaceViews(spacesFromFlow, settings, chatPreviews)
            }.collect { resultingSpaceViews ->
                spaces.value = resultingSpaceViews
            }
        }
    }

    private fun transformToVaultSpaceViews(
        spacesFromFlow: List<ObjectWrapper.SpaceView>,
        settings: VaultSettings,
        chatPreviews: List<Chat.Preview>
    ): List<VaultSpaceView> {
        return spacesFromFlow
            .filter { space -> (space.isActive || space.isLoading) }
            .map { space ->
                val chatPreview = space.targetSpaceId?.let { spaceId ->
                    chatPreviews.find { it.space.id == spaceId }
                }
                mapToVaultSpaceViewItem(space, chatPreview)
            }.sortedBy { spaceView ->
                val idx = settings.orderOfSpaces.indexOf(
                    spaceView.space.id
                )
                if (idx == -1) {
                    Int.MIN_VALUE
                } else {
                    idx
                }
            }
    }

    private fun mapToVaultSpaceViewItem(
        space: ObjectWrapper.SpaceView,
        chatPreview: Chat.Preview?
    ): VaultSpaceView {
        // Debug logging to diagnose the issue
        Timber.d("Space ${space.id}: Space name: ${space.name}, isLoading=${space.isLoading}, isActive=${space.isActive}, chatPreview=${chatPreview != null}, spaceLocalStatus=${space.spaceLocalStatus}, spaceAccountStatus=${space.spaceAccountStatus}")
        
        return when {
            space.isLoading -> {
                Timber.d("Creating loading view for space ${space.id}")
                createLoadingView(space)
            }
            chatPreview != null -> {
                Timber.d("Creating chat view for space ${space.id}")
                createChatView(space, chatPreview)
            }
            else -> {
                Timber.d("Creating standard space view for space ${space.id}")
                createStandardSpaceView(space)
            }
        }
    }

    private fun createLoadingView(
        space: ObjectWrapper.SpaceView
    ): VaultSpaceView.Loading {
        Timber.d("Space ${space.id} is loading")
        return VaultSpaceView.Loading(
            space = space,
            icon = space.spaceIcon(
                builder = urlBuilder,
                spaceGradientProvider = SpaceGradientProvider.Default
            )
        )
    }

    private fun createChatView(
        space: ObjectWrapper.SpaceView,
        chatPreview: Chat.Preview
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
        val attachmentPreviews = chatPreview.message?.attachments?.take(3)?.mapNotNull { attachment ->
            val dependency = chatPreview.dependencies.find { it.id == attachment.target }
            when (attachment.type) {
                Chat.Message.Attachment.Type.Image -> {
                    VaultSpaceView.AttachmentPreview(
                        type = VaultSpaceView.AttachmentType.IMAGE,
                        imageUrl = urlBuilder.thumbnail(attachment.target)
                    )
                }
                Chat.Message.Attachment.Type.File -> {
                    val mimeType = dependency?.getSingleValue<String>(Relations.FILE_MIME_TYPE)
                    val fileExt = dependency?.getSingleValue<String>(Relations.FILE_EXT)
                    VaultSpaceView.AttachmentPreview(
                        type = VaultSpaceView.AttachmentType.FILE,
                        mimeType = mimeType,
                        fileExtension = fileExt
                    )
                }
                Chat.Message.Attachment.Type.Link -> {
                    val obj = chatPreview.dependencies.find { it.id == attachment.target }
                    if (obj == null) {
                        Timber.w("Object for link attachment not found: ${attachment.target}")
                        return@mapNotNull null
                    }
                    VaultSpaceView.AttachmentPreview(
                        type = VaultSpaceView.AttachmentType.LINK,
                        objectIcon = obj.objectIcon(
                            builder = urlBuilder,
                            objType = null
                        ),
                        title = fieldParser.getObjectName(objectWrapper = obj)
                    )
                }
            }
        } ?: emptyList()

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
            attachmentPreviews = attachmentPreviews
        )
    }

    private fun createStandardSpaceView(
        space: ObjectWrapper.SpaceView
    ): VaultSpaceView.Space {
        return VaultSpaceView.Space(
            space = space,
            icon = space.spaceIcon(
                builder = urlBuilder,
                spaceGradientProvider = SpaceGradientProvider.Default
            ),
            accessType = stringResourceProvider
                .getSpaceAccessTypeName(accessType = space.spaceAccessType)
        )
    }

    fun onSpaceClicked(view: VaultSpaceView) {
        Timber.i("onSpaceClicked")
        viewModelScope.launch {
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
                            spaceUxType = view.space.spaceUxType
                        )
                    }
                )
            } else {
                Timber.e("Missing target space")
            }
        }
    }

    fun onSettingsClicked() {
        viewModelScope.launch {
            commands.emit(VaultCommand.OpenProfileSettings)
        }
    }

    fun onOrderChanged(order: List<Id>) {
        Timber.d("onOrderChanged")
        viewModelScope.launch { analytics.sendEvent(eventName = EventsDictionary.reorderSpace) }
        viewModelScope.launch { setVaultSpaceOrder.async(params = order) }
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
        spaceUxType: SpaceUxType?
    ) {
        saveCurrentSpace.async(
            SaveCurrentSpace.Params(SpaceId(targetSpace))
        ).fold(
            onFailure = {
                Timber.e(it, "Error while saving current space on vault screen")
            },
            onSuccess = {
                if (spaceUxType == SpaceUxType.CHAT && chat != null && ChatConfig.isChatAllowed(
                        space = targetSpace
                    )
                ) {
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
        }
        viewModelScope.launch {
            Timber.d("Proceeding with navigation: $nav")
            navigations.emit(nav)
        }
    }

    companion object {
        const val SPACE_VAULT_DEBOUNCE_DURATION = 300L
    }
}