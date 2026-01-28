package com.anytypeio.anytype.feature_chats.presentation

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.clickShareSpaceShareLink
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenQr
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.LinkPreview
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.misc.navigation
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.core_models.ui.SpaceMemberIconView
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.core_models.ui.spaceIcon
import com.anytypeio.anytype.core_ui.text.splitByMarks
import com.anytypeio.anytype.core_utils.common.DefaultFileInfo
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.notifications.NotificationPermissionManager
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.chats.DeleteChatMessage
import com.anytypeio.anytype.domain.chats.EditChatMessage
import com.anytypeio.anytype.domain.chats.ToggleChatMessageReaction
import com.anytypeio.anytype.domain.invite.GetCurrentInviteAccessLevel
import com.anytypeio.anytype.domain.invite.SpaceInviteLinkStore
import com.anytypeio.anytype.domain.media.DiscardPreloadedFile
import com.anytypeio.anytype.domain.media.PreloadFile
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.misc.GetLinkPreview
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer.Store
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.notifications.NotificationBuilder
import com.anytypeio.anytype.domain.notifications.NotificationStateCalculator
import com.anytypeio.anytype.domain.notifications.SetChatNotificationMode
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.CreateObjectFromUrl
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.feature_chats.BuildConfig
import com.anytypeio.anytype.feature_chats.tools.ClearChatsTempFolder
import com.anytypeio.anytype.feature_chats.tools.LinkDetector
import com.anytypeio.anytype.feature_chats.tools.syncStatus
import com.anytypeio.anytype.feature_chats.tools.toNotificationSetting
import com.anytypeio.anytype.feature_chats.tools.toNotificationState
import com.anytypeio.anytype.feature_chats.ui.NotificationSetting
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.confgs.ChatConfig
import com.anytypeio.anytype.presentation.extension.sendAnalyticsChangeMessageNotificationState
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView
import com.anytypeio.anytype.presentation.spaces.UiSpaceQrCodeState
import com.anytypeio.anytype.presentation.spaces.UiSpaceQrCodeState.SpaceInvite
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.vault.ExitToVaultDelegate
import com.anytypeio.anytype.presentation.widgets.PinObjectAsWidgetDelegate
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ChatViewModel @Inject constructor(
    private val vmParams: Params.Default,
    private val chatContainer: ChatContainer,
    private val addChatMessage: AddChatMessage,
    private val editChatMessage: EditChatMessage,
    private val deleteChatMessage: DeleteChatMessage,
    private val toggleChatMessageReaction: ToggleChatMessageReaction,
    private val members: ActiveSpaceMemberSubscriptionContainer,
    private val getAccount: GetAccount,
    private val urlBuilder: UrlBuilder,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val dispatchers: AppCoroutineDispatchers,
    private val uploadFile: UploadFile,
    private val preloadFile: PreloadFile,
    private val discardPreloadedFile: DiscardPreloadedFile,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val copyFileToCacheDirectory: CopyFileToCacheDirectory,
    private val exitToVaultDelegate: ExitToVaultDelegate,
    private val getLinkPreview: GetLinkPreview,
    private val createObjectFromUrl: CreateObjectFromUrl,
    private val notificationPermissionManager: NotificationPermissionManager,
    private val spacePermissionProvider: UserPermissionProvider,
    private val notificationBuilder: NotificationBuilder,
    private val clearChatsTempFolder: ClearChatsTempFolder,
    private val createObject: CreateObject,
    private val getObject: GetObject,
    private val analytics: Analytics,
    private val spaceInviteLinkStore: SpaceInviteLinkStore,
    private val getCurrentInviteAccessLevel: GetCurrentInviteAccessLevel,
    private val pinObjectAsWidgetDelegate: PinObjectAsWidgetDelegate,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val setObjectDetails: SetObjectDetails,
    private val setSpaceDetails: SetSpaceDetails,
    private val setChatNotificationMode: SetChatNotificationMode,
    private val fieldParser: FieldParser
) : BaseViewModel(),
    ExitToVaultDelegate by exitToVaultDelegate,
    PinObjectAsWidgetDelegate by pinObjectAsWidgetDelegate {
    private val preloadingJobs = mutableListOf<Job>()

    private val visibleRangeUpdates = MutableSharedFlow<Pair<Id, Id>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val header = MutableStateFlow<HeaderView>(HeaderView.Init)
    val uiState = MutableStateFlow(ChatViewState(isLoading = true))
    val chatBoxAttachments = MutableStateFlow<List<ChatView.Message.ChatBoxAttachment>>(emptyList())
    val commands = MutableSharedFlow<ViewModelCommand>()
    val uXCommands = MutableSharedFlow<UXCommand>()
    val navigation = MutableSharedFlow<OpenObjectNavigation>()
    val chatBoxMode = MutableStateFlow<ChatBoxMode>(ChatBoxMode.Default())
    val mentionPanelState = MutableStateFlow<MentionPanelState>(MentionPanelState.Hidden)
    val showNotificationPermissionDialog = MutableStateFlow(false)
    val showMoveToBinDialog = MutableStateFlow(false)

    val canCreateInviteLink = MutableStateFlow(false)
    private val spaceAccessType = MutableStateFlow<SpaceAccessType?>(null)
    val errorState = MutableStateFlow<UiErrorState>(UiErrorState.Hidden)
    val uiQrCodeState = MutableStateFlow<UiSpaceQrCodeState>(UiSpaceQrCodeState.Hidden)
    val inviteLinkAccessLevel =
        MutableStateFlow<SpaceInviteLinkAccessLevel>(SpaceInviteLinkAccessLevel.LinkDisabled())

    private val syncStatus = MutableStateFlow<SyncStatus?>(null)
    private val currentPermission = MutableStateFlow<SpaceMemberPermissions?>(null)
    private val _currentSpaceUxType = MutableStateFlow<SpaceUxType?>(null)
    private val _chatObjectWrapper = MutableStateFlow<ObjectWrapper.Basic?>(null)
    val currentSpaceUxType = _currentSpaceUxType
    private val dateFormatter = SimpleDateFormat("d MMMM YYYY")
    private val messageRateLimiter = MessageRateLimiter()

    val isSyncing = syncStatus.map { status ->
        (status == SyncStatus.Syncing)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    private var account: Id = ""

    init {
        Timber.d("DROID-2966 init")

        viewModelScope.launch {
            spacePermissionProvider
                .observe(vmParams.space)
                .collect { permission ->
                    currentPermission.value = permission
                    if (permission?.isOwnerOrEditor() == true) {
                        if (chatBoxMode.value is ChatBoxMode.ReadOnly) {
                            chatBoxMode.value = ChatBoxMode.Default()
                        }
                    } else if (permission == SpaceMemberPermissions.READER || permission == SpaceMemberPermissions.NO_PERMISSIONS) {
                        chatBoxMode.value = ChatBoxMode.ReadOnly
                    }
                    canCreateInviteLink.value = permission?.isOwner() == true
                    // Update header with new permission if already set
                    val currentHeader = header.value
                    when (currentHeader) {
                        is HeaderView.Default -> {
                            header.value = currentHeader.copy(
                                canEdit = permission?.isOwnerOrEditor() == true
                            )
                        }
                        is HeaderView.ChatObject -> {
                            header.value = currentHeader.copy(
                                canEdit = permission?.isOwnerOrEditor() == true
                            )
                        }
                        else -> {}
                    }
                }
        }

        viewModelScope.launch {
            combine(
                spaceViews.observe(vmParams.space).distinctUntilChanged(),
                _chatObjectWrapper
            ) { view, wrapper ->
                Pair(view, wrapper)
            }.collect { (spaceView, chatObject) ->
                Timber.d("Space view updated: $spaceView or chatObject: $chatObject")
                _currentSpaceUxType.value = spaceView.spaceUxType
                val notificationSetting = NotificationStateCalculator
                    .calculateChatNotificationState(chatSpace = spaceView, chatId = vmParams.ctx)
                    .toNotificationSetting()
                val isMuted = notificationSetting == NotificationSetting.MUTE
                    || notificationSetting == NotificationSetting.MENTIONS
                val canEdit = currentPermission.value?.isOwnerOrEditor() == true

                // Chat space
                if (spaceView.spaceUxType == SpaceUxType.CHAT || spaceView.spaceUxType == SpaceUxType.ONE_TO_ONE) {
                    header.value = HeaderView.Default(
                        title = spaceView.name.orEmpty(),
                        icon = spaceView.spaceIcon(builder = urlBuilder),
                        isMuted = isMuted,
                        notificationSetting = notificationSetting,
                        canEdit = canEdit,
                        showAddMembers = spaceView.spaceUxType != SpaceUxType.ONE_TO_ONE
                    )
                } else if (chatObject != null) {
                    // Chat object - use wrapper from ObjectWatcher
                    val type = storeOfObjectTypes.getTypeOfObject(chatObject)
                    // Check if chat is pinned
                    val isPinned = isChatPinned(vmParams.space, vmParams.ctx)
                    header.value = HeaderView.ChatObject(
                        title = chatObject.name.orEmpty(),
                        icon = chatObject.objectIcon(
                            builder = urlBuilder,
                            objType = type
                        ),
                        isMuted = isMuted,
                        notificationSetting = notificationSetting,
                        isPinned = isPinned,
                        canEdit = canEdit
                    )
                }
                // Note: if wrapper is null for chat object, header remains Init until wrapper is available
            }
        }

        viewModelScope.launch {
            visibleRangeUpdates
                .distinctUntilChanged()
                .collect { (from, to) ->
                    chatContainer.onVisibleRangeChanged(from, to)
                }
        }

        viewModelScope.launch {
            getAccount
                .async(Unit)
                .onSuccess { acc ->
                    account = acc.id
                }
                .onFailure {
                    Timber.e(it, "Failed to find account for space-level chat")
                }

            proceedWithObservingChatMessages(
                account = account,
                chat = vmParams.ctx,
                fieldParser = fieldParser
            )
        }

        viewModelScope.launch {
            proceedWithObservingSyncStatus()
        }

        proceedWithSpaceSubscription()

        viewModelScope.launch {
            val route = if (vmParams.triggeredByPush)
                EventsDictionary.ChatRoute.PUSH.value
            else
                EventsDictionary.ChatRoute.NAVIGATION.value
            analytics.sendEvent(
                eventName = EventsDictionary.chatScreenChat,
                props = Props(mapOf(
                    EventsPropertiesKey.route to route,
                    EventsPropertiesKey.chatId to vmParams.ctx
                ))
            )
        }
        subscribeToInviteLinkState()
    }

    private fun subscribeToInviteLinkState() {
        viewModelScope.launch {
            spaceInviteLinkStore
                .observe(vmParams.space)
                .onStart {
                    val params = GetCurrentInviteAccessLevel.Params(space = vmParams.space)
                    getCurrentInviteAccessLevel.async(params).getOrNull()
                }
                .catch {
                    Timber.e(it, "Error observing invite link access level")
                    // Emit default value on error
                    inviteLinkAccessLevel.value = SpaceInviteLinkAccessLevel.LinkDisabled()
                }
                .collect { accessLevel ->
                    Timber.d("Invite link access level updated: $accessLevel")
                    inviteLinkAccessLevel.value = accessLevel
                }
        }
    }

    fun onResume() {
        notificationBuilder.clearNotificationChannel(
            spaceId = vmParams.space.id,
            chatId = vmParams.ctx
        )
    }

    private suspend fun proceedWithObservingChatMessages(
        account: Id,
        chat: Id,
        fieldParser: FieldParser
    ) {
        combine(
            chatContainer.watchWhileTrackingAttachments(chat = chat).distinctUntilChanged(),
            chatContainer.subscribeToAttachments(vmParams.ctx, vmParams.space)
                .distinctUntilChanged(),
            chatContainer.fetchReplies(chat = chat).distinctUntilChanged()
        ) { result, dependencies, replies ->
            Timber.d("DROID-2966 Chat counter state from container: ${result.state}, unread section: ${result.initialUnreadSectionMessageId}")
            Timber.d("DROID-2966 Intent from container: ${result.intent}")
            Timber.d("DROID-2966 Message results size from container: ${result.messages.size}")
            var previousDate: ChatView.DateSection? = null
            val messageViews = buildList {

                var prevCreator: String? = null
                var prevDateInterval: Long = 0

                result.messages.forEach { msg ->

                    val formattedMsgDate = dateFormatter.format(msg.createdAt * 1000)

                    val isPrevTimeIntervalBig = if (prevDateInterval > 0) {
                        (msg.createdAt - prevDateInterval) > ChatConfig.GROUPING_DATE_INTERVAL_IN_SECONDS
                    } else {
                        false
                    }

                    val shouldHideUsername = prevCreator == msg.creator && !isPrevTimeIntervalBig

                    val allMembers = members.get()
                    val member = allMembers.let { type ->
                        when (type) {
                            is Store.Data -> type.members.find { member ->
                                member.identity == msg.creator
                            }

                            is Store.Empty -> null
                        }
                    }

                    val content = msg.content

                    val replyToId = msg.replyToMessageId

                    val reply = if (replyToId.isNullOrEmpty()) {
                        null
                    } else {
                        val replyMessage = replies[replyToId]
                        if (replyMessage != null) {
                            ChatView.Message.Reply(
                                msg = replyMessage.id,
                                text = replyMessage.content?.text.orEmpty().ifEmpty {
                                    // Fallback to attachment name if empty
                                    if (replyMessage.attachments.isNotEmpty()) {
                                        val attachment = replyMessage.attachments.last()
                                        val dependency = dependencies[attachment.target]
                                        val name = dependency?.name.orEmpty()
                                        val ext = dependency?.fileExt
                                        if (!ext.isNullOrEmpty()) {
                                            "$name.$ext"
                                        } else {
                                            name
                                        }
                                    } else {
                                        ""
                                    }
                                },
                                author = allMembers.let { type ->
                                    when (type) {
                                        is Store.Data -> type.members.find { member ->
                                            member.identity == replyMessage.creator
                                        }?.name.orEmpty()

                                        is Store.Empty -> ""
                                    }
                                }
                            )
                        } else {
                            null
                        }
                    }

                    val view = ChatView.Message(
                        id = msg.id,
                        order = msg.order,
                        timestamp = msg.createdAt * 1000,
                        content = ChatView.Message.Content(
                            msg = content?.text.orEmpty(),
                            parts = content?.text
                                .orEmpty()
                                .let { text ->
                                    // Add detected links (URLs, emails, phones) to existing marks
                                    val enhancedMarks = LinkDetector.addLinkMarksToText(
                                        text = text,
                                        existingMarks = content?.marks.orEmpty()
                                    )
                                    text.splitByMarks(marks = enhancedMarks)
                                }
                                .map { (part, styles) ->
                                    ChatView.Message.Content.Part(
                                        part = part,
                                        styles = styles
                                    )
                                }
                        ),
                        reply = reply,
                        author = member?.name.orEmpty(),
                        creator = member?.id,
                        isUserAuthor = msg.creator == account,
                        shouldHideUsername = shouldHideUsername,
                        isEdited = msg.modifiedAt > msg.createdAt,
                        isSynced = msg.synced,
                        reactions = msg.reactions
                            .toList()
                            .sortedByDescending { (emoji, ids) -> ids.size }
                            .map { (emoji, ids) ->
                                ChatView.Message.Reaction(
                                    emoji = emoji,
                                    count = ids.size,
                                    isSelected = ids.contains(account)
                                )
                            }
                            .take(ChatConfig.MAX_REACTION_COUNT),
                        attachments = msg.attachments.mapNotNull { attachment ->

                            val wrapper = dependencies[attachment.target]
                            if (wrapper == null || !wrapper.isValid) return@mapNotNull null

                            when (attachment.type) {
                                Chat.Message.Attachment.Type.Image -> {
                                    ChatView.Message.Attachment.Image(
                                        obj = attachment.target,
                                        url = urlBuilder.large(path = attachment.target),
                                        name = wrapper.name.orEmpty(),
                                        ext = wrapper.fileExt.orEmpty(),
                                        status = wrapper.syncStatus()
                                    )
                                }

                                else -> {
                                    when (wrapper.layout) {
                                        ObjectType.Layout.IMAGE -> {
                                            ChatView.Message.Attachment.Image(
                                                obj = attachment.target,
                                                url = urlBuilder.large(path = attachment.target),
                                                name = wrapper.name.orEmpty(),
                                                ext = wrapper.fileExt.orEmpty(),
                                                status = wrapper.syncStatus()
                                            )
                                        }

                                        ObjectType.Layout.VIDEO -> {
                                            ChatView.Message.Attachment.Video(
                                                obj = attachment.target,
                                                url = urlBuilder.large(path = attachment.target),
                                                name = wrapper.name.orEmpty(),
                                                ext = wrapper.fileExt.orEmpty(),
                                                status = wrapper.syncStatus()
                                            )
                                        }

                                        ObjectType.Layout.BOOKMARK -> {
                                            ChatView.Message.Attachment.Bookmark(
                                                id = wrapper.id,
                                                url = wrapper.getSingleValue<String>(Relations.SOURCE)
                                                    .orEmpty(),
                                                title = wrapper.name.orEmpty(),
                                                description = wrapper.description.orEmpty(),
                                                imageUrl = wrapper.getSingleValue<String?>(Relations.PICTURE)
                                                    .let { hash ->
                                                        if (!hash.isNullOrEmpty())
                                                            urlBuilder.large(hash)
                                                        else
                                                            null
                                                    }
                                            )
                                        }

                                        else -> {
                                            val (_, typeName) = fieldParser.getObjectTypeIdAndName(
                                                wrapper,
                                                storeOfObjectTypes.getAll()
                                            )
                                            ChatView.Message.Attachment.Link(
                                                obj = attachment.target,
                                                wrapper = wrapper,
                                                icon = wrapper.objectIcon(
                                                    builder = urlBuilder,
                                                    objType = storeOfObjectTypes.getTypeOfObject(
                                                        wrapper
                                                    )
                                                ),
                                                typeName = typeName,
                                                isDeleted = wrapper.isDeleted == true
                                            )
                                        }
                                    }
                                }
                            }
                        }.let { results ->
                            if (results.size >= 2) {
                                val images =
                                    results.filterIsInstance<ChatView.Message.Attachment.Image>()
                                if (images.size == results.size) {
                                    listOf(
                                        ChatView.Message.Attachment.Gallery(
                                            images = images
                                        )
                                    )
                                } else {
                                    results
                                }
                            } else {
                                results
                            }
                        },
                        avatar = if (member != null && !member.iconImage.isNullOrEmpty()) {
                            ChatView.Message.Avatar.Image(
                                urlBuilder.thumbnail(member.iconImage!!)
                            )
                        } else {
                            ChatView.Message.Avatar.Initials(member?.name.orEmpty())
                        },
                        startOfUnreadMessageSection = result.initialUnreadSectionMessageId == msg.id,
                        formattedDate = formattedMsgDate
                    )
                    val currDate = ChatView.DateSection(
                        formattedDate = formattedMsgDate,
                        timeInMillis = msg.createdAt * 1000L
                    )
                    if (currDate.formattedDate != previousDate?.formattedDate) {
                        add(currDate)
                        previousDate = currDate
                    }
                    add(view)

                    prevCreator = msg.creator
                    prevDateInterval = msg.createdAt
                }
            }.reversed()
            ChatViewState(
                messages = messageViews,
                intent = result.intent,
                counter = ChatViewState.Counter(
                    messages = result.state.unreadMessages?.counter ?: 0,
                    mentions = result.state.unreadMentions?.counter ?: 0
                ),
                isLoading = false
            )
        }.flowOn(dispatchers.io)
            .distinctUntilChanged()
            .collect {
                uiState.value = it
            }
    }

    private suspend fun proceedWithObservingSyncStatus() {
        chatContainer.subscribeToChatObject(
            chat = vmParams.ctx,
            space = vmParams.space
        ).map { wrapper ->
            // Emit wrapper for header use
            _chatObjectWrapper.value = wrapper
            // Parse syncStatus from wrapper
            val syncStatusCode = wrapper?.getSingleValue<Double>(Relations.SYNC_STATUS)
            val status = syncStatusCode?.toInt()?.let(SyncStatus::fromCode)
            Triple(
                status,
                wrapper?.isDeleted,
                wrapper?.isArchived
            )
        }
            .distinctUntilChanged()
            .onEach { (status, isDeleted, isArchived) ->
                Timber.d("DROID-4200 Object state - sync: $status, deleted: $isDeleted, archived: $isArchived")
            }
            .catch { e ->
                Timber.w(e, "DROID-4200 Error observing object state for: ${vmParams.ctx}")
            }
            .flowOn(dispatchers.io)
            .collect { (status, isDeleted, isArchived) ->
                syncStatus.value = status

                // Handle chat deletion or archival
                when {
                    isDeleted == true -> {
                        Timber.w("DROID-4200 Chat was deleted, exiting to vault")
                        handleChatUnavailable()
                    }
                    isArchived == true -> {
                        Timber.w("DROID-4200 Chat was archived, exiting to vault")
                        handleChatUnavailable()
                    }
                }
            }
    }

    private suspend fun handleChatUnavailable() {
        // Clean up chat container (also unsubscribes chat object subscription)
        withContext(dispatchers.io) {
            chatContainer.stop(chat = vmParams.ctx)
        }
        // Show toast and exit
        sendToast("Chat is no longer available")
        commands.emit(ViewModelCommand.Exit)
    }

    fun onChatBoxInputChanged(
        selection: IntRange,
        text: String
    ) {
        Timber.d("DROID-2966 onChatBoxInputChanged")
        
        // Disable mention functionality for ONE_TO_ONE spaces
        if (_currentSpaceUxType.value == SpaceUxType.ONE_TO_ONE) {
            mentionPanelState.value = MentionPanelState.Hidden
            return
        }
        
        val query = resolveMentionQuery(
            text = text,
            selectionStart = selection.start
        )
        if (isMentionTriggered(text, selection.start)) {
            val results = getMentionedMembers(query)
            if (query != null && results.isNotEmpty()) {
                mentionPanelState.value = MentionPanelState.Visible(
                    results = results,
                    query = query
                )
            } else {
                Timber.w("DROID-2966 Query is empty or results are empty when mention is triggered")
            }
        } else if (shouldHideMention(text, selection.start)) {
            mentionPanelState.value = MentionPanelState.Hidden
        } else {
            val results = getMentionedMembers(query)
            if (results.isNotEmpty() && query != null) {
                mentionPanelState.value = MentionPanelState.Visible(
                    results = results,
                    query = query
                )
            } else {
                mentionPanelState.value = MentionPanelState.Hidden
            }
        }
    }

    private fun getMentionedMembers(query: MentionPanelState.Query?): List<MentionPanelState.Member> {
        Timber.d("getMentionedMembers, query: $query")
        val results = members.get().let { store ->
            when (store) {
                is Store.Data -> {
                    store.members
                        .filter { member -> member.permissions?.isAtLeastReader() == true }
                        .map { member ->
                            MentionPanelState.Member(
                                id = member.id,
                                name = member.name.orEmpty(),
                                icon = SpaceMemberIconView.icon(
                                    obj = member,
                                    urlBuilder = urlBuilder
                                ),
                                isUser = member.identity == account
                            )
                        }.filter { m ->
                            if (query != null) {
                                m.name.contains(query.query, true)
                            } else {
                                true
                            }
                        }
                }

                Store.Empty -> {
                    emptyList()
                }
            }
        }
        return results
    }

    fun onMessageSent(msg: String, markup: List<Block.Content.Text.Mark>) {
        if (BuildConfig.DEBUG) {
            Timber.d("DROID-2635 OnMessageSent, markup: $markup}")
        }

        viewModelScope.launch {

            // Cancelling preloading jobs if there are any:

            preloadingJobs.cancel()

            // Discard mention panel state on send action
            mentionPanelState.value = MentionPanelState.Hidden

            // Use LinkDetector to find all types of links (URLs, emails, phones)
            val detectedLinkMarks = LinkDetector.addLinkMarksToText(
                text = msg,
                existingMarks = markup
            )

            // The LinkDetector already handles deduplication, so we can use its result directly
            val normalizedMarkup = detectedLinkMarks.sortedBy { it.range.first }

            var shouldClearChatTempFolder = false

            chatBoxMode.value = chatBoxMode.value.updateIsSendingBlocked(isBlocked = true)
            val attachments = buildList {
                val currAttachments = chatBoxAttachments.value
                currAttachments.forEachIndexed { idx, attachment ->
                    when (attachment) {
                        is ChatView.Message.ChatBoxAttachment.Link -> {
                            add(
                                Chat.Message.Attachment(
                                    target = attachment.target,
                                    type = Chat.Message.Attachment.Type.Link
                                )
                            )
                        }

                        is ChatView.Message.ChatBoxAttachment.Existing.Link -> {
                            add(
                                Chat.Message.Attachment(
                                    target = attachment.target,
                                    type = Chat.Message.Attachment.Type.Link
                                )
                            )
                        }

                        is ChatView.Message.ChatBoxAttachment.Existing.Image -> {
                            add(
                                Chat.Message.Attachment(
                                    target = attachment.target,
                                    type = Chat.Message.Attachment.Type.Image
                                )
                            )
                        }

                        is ChatView.Message.ChatBoxAttachment.Existing.Video -> {
                            add(
                                Chat.Message.Attachment(
                                    target = attachment.target,
                                    type = Chat.Message.Attachment.Type.File
                                )
                            )
                        }

                        is ChatView.Message.ChatBoxAttachment.Media -> {
                            chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                set(
                                    index = idx,
                                    element = attachment.copy(
                                        state = ChatView.Message.ChatBoxAttachment.State.Uploading
                                    )
                                )
                            }
                            val state = attachment.state
                            var preloadedFileId: Id? = null
                            var path: String
                            var wasCopiedToCache = false

                            if (state is ChatView.Message.ChatBoxAttachment.State.Preloaded) {
                                preloadedFileId = state.preloadedFileId
                                path = state.path
                                wasCopiedToCache = true
                            } else {
                                path = if (attachment.capturedByCamera) {
                                    shouldClearChatTempFolder = true
                                    wasCopiedToCache = true
                                    try {
                                        withContext(dispatchers.io) {
                                            copyFileToCacheDirectory.copy(attachment.uri)
                                        }
                                    } catch (e: Exception) {
                                        Timber.e(e, "Failed to copy media file to cache: ${attachment.uri}")
                                        chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                            set(idx, attachment.copy(state = ChatView.Message.ChatBoxAttachment.State.Failed))
                                        }
                                        return@forEachIndexed
                                    }
                                } else {
                                    attachment.uri
                                }
                            }

                            uploadFile.async(
                                UploadFile.Params(
                                    space = vmParams.space,
                                    path = path,
                                    type = if (attachment.isVideo)
                                        Block.Content.File.Type.VIDEO
                                    else
                                        Block.Content.File.Type.IMAGE,
                                    preloadFileId = preloadedFileId
                                )
                            ).onSuccess { file ->
                                if (wasCopiedToCache) {
                                    withContext(dispatchers.io) {
                                        val isDeleted = copyFileToCacheDirectory.delete(path)
                                        if (isDeleted) {
                                            Timber.d("DROID-2966 Successfully deleted temp file: ${attachment.uri}")
                                        } else {
                                            Timber.w("DROID-2966 Error while deleting temp file: ${attachment.uri}")
                                        }
                                    }
                                }
                                add(
                                    Chat.Message.Attachment(
                                        target = file.id,
                                        type = if (attachment.isVideo)
                                            Chat.Message.Attachment.Type.File
                                        else
                                            Chat.Message.Attachment.Type.Image
                                    )
                                )
                                chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                    set(
                                        index = idx,
                                        element = attachment.copy(
                                            state = ChatView.Message.ChatBoxAttachment.State.Uploaded
                                        )
                                    )
                                }
                            }.onFailure {
                                Timber.e(it, "DROID-2966 Error while uploading file as attachment")
                                chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                    set(
                                        index = idx,
                                        element = attachment.copy(
                                            state = ChatView.Message.ChatBoxAttachment.State.Failed
                                        )
                                    )
                                }
                            }
                        }

                        is ChatView.Message.ChatBoxAttachment.Bookmark -> {
                            chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                set(
                                    index = idx,
                                    element = attachment.copy(
                                        isUploading = true
                                    )
                                )
                            }
                            createObjectFromUrl.async(
                                params = CreateObjectFromUrl.Params(
                                    url = attachment.preview.url,
                                    space = vmParams.space
                                )
                            ).onSuccess { obj ->
                                if (obj.isValid) {
                                    add(
                                        Chat.Message.Attachment(
                                            target = obj.id,
                                            type = Chat.Message.Attachment.Type.Link
                                        )
                                    )
                                } else {
                                    Timber.w("DROID-2966 Created object from URL is not valid")
                                }
                            }.onFailure {
                                Timber.e(it, "DROID-2966 Error while creating object from url")
                            }
                        }

                        is ChatView.Message.ChatBoxAttachment.File -> {
                            var preloadedFileId: Id? = null
                            var path: String? = null
                            val state = attachment.state
                            if (state is ChatView.Message.ChatBoxAttachment.State.Preloaded) {
                                preloadedFileId = state.preloadedFileId
                                path = state.path
                            } else {
                                path = try {
                                    withContext(dispatchers.io) {
                                        copyFileToCacheDirectory.copy(attachment.uri)
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to copy file to cache: ${attachment.uri}")
                                    chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                        set(idx, attachment.copy(state = ChatView.Message.ChatBoxAttachment.State.Failed))
                                    }
                                    return@forEachIndexed
                                }
                            }
                            chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                set(
                                    index = idx,
                                    element = attachment.copy(
                                        state = ChatView.Message.ChatBoxAttachment.State.Uploading
                                    )
                                )
                            }
                            uploadFile.async(
                                UploadFile.Params(
                                    space = vmParams.space,
                                    path = path,
                                    type = Block.Content.File.Type.NONE,
                                    preloadFileId = preloadedFileId
                                )
                            ).onSuccess { file ->
                                copyFileToCacheDirectory.delete(path)
                                add(
                                    Chat.Message.Attachment(
                                        target = file.id,
                                        type = Chat.Message.Attachment.Type.File
                                    )
                                )
                                chatBoxAttachments.value =
                                    currAttachments.toMutableList().apply {
                                        set(
                                            index = idx,
                                            element = attachment.copy(
                                                state = ChatView.Message.ChatBoxAttachment.State.Uploaded
                                            )
                                        )
                                    }
                            }.onFailure {
                                Timber.e(
                                    it,
                                    "DROID-2966 Error while uploading file as attachment"
                                )
                                chatBoxAttachments.value =
                                    currAttachments.toMutableList().apply {
                                        set(
                                            index = idx,
                                            element = attachment.copy(
                                                state = ChatView.Message.ChatBoxAttachment.State.Failed
                                            )
                                        )
                                    }
                            }
                        }
                    }
                }
            }
            when (val mode = chatBoxMode.value) {
                is ChatBoxMode.Default -> {
                    // TODO consider moving this use-case inside chat container
                    if (messageRateLimiter.shouldShowRateLimitWarning()) {
                        uXCommands.emit(UXCommand.ShowRateLimitWarning)
                    }

                    addChatMessage.async(
                        params = Command.ChatCommand.AddMessage(
                            chat = vmParams.ctx,
                            message = Chat.Message.new(
                                text = msg.trim(),
                                attachments = attachments,
                                marks = normalizedMarkup
                            )
                        )
                    ).onSuccess { (id, payload) ->
                        chatContainer.onPayload(payload)
                        delay(JUMP_TO_BOTTOM_DELAY)
                        uXCommands.emit(UXCommand.JumpToBottom)
                    }.onFailure {
                        Timber.e(it, "Error while adding message")
                    }
                    chatBoxAttachments.value = emptyList()
                    chatBoxMode.value = ChatBoxMode.Default()
                }

                is ChatBoxMode.EditMessage -> {
                    editChatMessage.async(
                        params = Command.ChatCommand.EditMessage(
                            chat = vmParams.ctx,
                            message = Chat.Message.updated(
                                id = mode.msg,
                                text = msg.trim(),
                                attachments = attachments,
                                marks = normalizedMarkup,
                                synced = false
                            )
                        )
                    ).onSuccess {
                        delay(JUMP_TO_BOTTOM_DELAY)
                        uXCommands.emit(UXCommand.JumpToBottom)
                    }.onFailure {
                        Timber.e(it, "Error while editing message")
                    }.onSuccess {
                        Timber.d("Message edited with success")
                    }
                    chatBoxAttachments.value = emptyList()
                    chatBoxMode.value = ChatBoxMode.Default()
                }

                is ChatBoxMode.Reply -> {
                    addChatMessage.async(
                        params = Command.ChatCommand.AddMessage(
                            chat = vmParams.ctx,
                            message = Chat.Message.new(
                                text = msg.trim(),
                                replyToMessageId = mode.msg,
                                attachments = attachments,
                                marks = normalizedMarkup
                            )
                        )
                    ).onSuccess { (id, payload) ->
                        chatContainer.onPayload(payload)
                        delay(JUMP_TO_BOTTOM_DELAY)
                        uXCommands.emit(UXCommand.JumpToBottom)
                    }.onFailure {
                        Timber.e(it, "Error while adding message")
                    }
                    chatBoxAttachments.value = emptyList()
                    chatBoxMode.value = ChatBoxMode.Default()
                }

                is ChatBoxMode.ReadOnly -> {
                    // Do nothing.
                }
            }

            if (shouldClearChatTempFolder) {
                withContext(dispatchers.io) {
                    clearChatsTempFolder()
                }
            }
        }
        viewModelScope.launch {
            val hasAttachments = chatBoxAttachments.value.isNotEmpty()
            val hasText = msg.isNotEmpty()
            val type = when {
                hasText && !hasAttachments -> EventsDictionary.ChatSentMessageType.TEXT
                !hasText && hasAttachments -> EventsDictionary.ChatSentMessageType.ATTACHMENT
                hasText && hasAttachments -> EventsDictionary.ChatSentMessageType.MIXED
                else -> null
            }
            analytics.sendEvent(
                eventName = EventsDictionary.chatSentMessage,
                props = Props(
                    map = mapOf(
                        EventsPropertiesKey.type to type,
                        EventsPropertiesKey.chatId to vmParams.ctx
                    )
                )
            )
        }
    }

    fun onRequestEditMessageClicked(msg: ChatView.Message) {
        Timber.d("onRequestEditMessageClicked")
        viewModelScope.launch {
            chatBoxAttachments.value = buildList {
                msg.attachments.forEach { a ->
                    when (a) {
                        is ChatView.Message.Attachment.Image -> {
                            add(
                                ChatView.Message.ChatBoxAttachment.Existing.Image(
                                    target = a.obj,
                                    url = a.url
                                )
                            )
                        }

                        is ChatView.Message.Attachment.Video -> {
                            add(
                                ChatView.Message.ChatBoxAttachment.Existing.Video(
                                    target = a.obj,
                                    url = a.url
                                )
                            )
                        }

                        is ChatView.Message.Attachment.Bookmark -> {
                            add(
                                ChatView.Message.ChatBoxAttachment.Existing.Link(
                                    target = a.id,
                                    name = a.title,
                                    icon = ObjectIcon.None,
                                    typeName = storeOfObjectTypes.get(ObjectTypeUniqueKeys.BOOKMARK)?.name.orEmpty()
                                )
                            )
                        }

                        is ChatView.Message.Attachment.Gallery -> {
                            a.images.forEach { image ->
                                add(
                                    ChatView.Message.ChatBoxAttachment.Existing.Image(
                                        target = image.obj,
                                        url = image.url
                                    )
                                )
                            }
                        }

                        is ChatView.Message.Attachment.Link -> {
                            val wrapper = a.wrapper
                            if (wrapper != null && wrapper.isValid) {
                                val (_, typeName) = fieldParser.getObjectTypeIdAndName(
                                    wrapper,
                                    storeOfObjectTypes.getAll()
                                )
                                val name = if (wrapper.layout == ObjectType.Layout.NOTE) {
                                    wrapper.snippet.orEmpty()
                                } else {
                                    wrapper.name.orEmpty()
                                }
                                add(
                                    ChatView.Message.ChatBoxAttachment.Existing.Link(
                                        target = wrapper.id,
                                        name = name,
                                        icon = wrapper.objectIcon(
                                            builder = urlBuilder,
                                            objType = storeOfObjectTypes.getTypeOfObject(wrapper)
                                        ),
                                        typeName = typeName
                                    )
                                )
                            }
                        }
                    }
                }
            }
            chatBoxMode.value = ChatBoxMode.EditMessage(msg.id)
        }
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickMessageMenuEdit
            )
        }
    }

    fun onAttachObject(obj: GlobalSearchItemView) {
        chatBoxAttachments.value += listOf(
            ChatView.Message.ChatBoxAttachment.Link(
                target = obj.id,
                wrapper = obj
            )
        )
        
        viewModelScope.launch {
            val objectType = obj.type
            
            // Fire ClickScreenChatAttach event
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickScreenChatAttach,
                props = Props(
                    map = buildMap {
                        put(EventsPropertiesKey.type, EventsDictionary.ChatAttachType.OBJECT.value)
                        put(EventsPropertiesKey.chatId, vmParams.ctx)
                    }
                )
            )
            
            // Fire AttachItemChat event
            analytics.sendEvent(
                eventName = EventsDictionary.chatAttachItemChat,
                props = Props(
                    map = buildMap {
                        put(EventsPropertiesKey.type, EventsDictionary.ChatAttachType.OBJECT.value)
                        put(EventsPropertiesKey.chatId, vmParams.ctx)
                    }
                )
            )
        }
    }

    fun onAttachObject(target: Id) {
        Timber.d("DROID-2966 onAttachObject: $target")
        viewModelScope.launch {
            getObject.async(
                GetObject.Params(
                    target = target,
                    space = vmParams.space
                )
            ).onSuccess { view ->
                val wrapper = ObjectWrapper.Basic(view.details[target].orEmpty())
                Timber.e("DROID-2966 Fetched attach-to-chat target: $wrapper")
                if (wrapper.isValid) {
                    val type = storeOfObjectTypes.getTypeOfObject(wrapper)
                    val typeName = type?.name.orEmpty()
                    val icon = wrapper.objectIcon(
                        builder = urlBuilder,
                        objType = type
                    )
                    val title = if (wrapper.layout == ObjectType.Layout.NOTE) {
                        wrapper.snippet.orEmpty()
                    } else {
                        wrapper.name.orEmpty()
                    }
                    chatBoxAttachments.value += listOf(
                        ChatView.Message.ChatBoxAttachment.Link(
                            target = target,
                            wrapper = GlobalSearchItemView(
                                id = target,
                                obj = wrapper,
                                title = title,
                                icon = icon,
                                layout = wrapper.layout ?: ObjectType.Layout.BASIC,
                                space = vmParams.space,
                                type = typeName,
                                meta = GlobalSearchItemView.Meta.None
                            )
                        )
                    )
                    
                    // Fire ClickScreenChatAttach event
                    analytics.sendEvent(
                        eventName = EventsDictionary.chatClickScreenChatAttach,
                        props = Props(
                            map = buildMap {
                                put(EventsPropertiesKey.type, EventsDictionary.ChatAttachType.OBJECT.value)
                                put(EventsPropertiesKey.chatId, vmParams.ctx)
                            }
                        )
                    )
                    
                    // Fire AttachItemChat event
                    analytics.sendEvent(
                        eventName = EventsDictionary.chatAttachItemChat,
                        props = Props(
                            map = buildMap {
                                put(EventsPropertiesKey.type, EventsDictionary.ChatAttachType.OBJECT.value)
                                put(EventsPropertiesKey.count, 1)
                                put(EventsPropertiesKey.chatId, vmParams.ctx)
                            }
                        )
                    )
                }
            }.onFailure {
                Timber.e(it, "DROID-2966 Error while getting attach-to-chat target")
            }
        }
    }

    fun onClearAttachmentClicked(attachment: ChatView.Message.ChatBoxAttachment) {
        chatBoxAttachments.value = chatBoxAttachments.value.filter {
            it != attachment
        }
        viewModelScope.launch {
            var path: String? = null
            val preloaded = when (attachment) {
                is ChatView.Message.ChatBoxAttachment.File -> {
                    val state = attachment.state
                    if (state is ChatView.Message.ChatBoxAttachment.State.Preloaded) {
                        path = state.path
                        state.preloadedFileId
                    } else {
                        null
                    }
                }

                is ChatView.Message.ChatBoxAttachment.Media -> {
                    val state = attachment.state
                    if (state is ChatView.Message.ChatBoxAttachment.State.Preloaded) {
                        path = state.path
                        state.preloadedFileId
                    } else {
                        null
                    }
                }

                else -> null
            }

            if (!preloaded.isNullOrEmpty()) {
                discardPreloadedFile.async(
                    params = preloaded
                ).onSuccess {
                    Timber.d("Successfully Discarded preloaded file: $preloaded")
                }.onFailure {
                    Timber.e("Error while discarding preloaded file: $preloaded")
                }
            }
            if (!path.isNullOrEmpty()) {
                copyFileToCacheDirectory.delete(path)
            }
        }
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatDetachItemChat,
                props = Props(
                    map = mapOf(EventsPropertiesKey.chatId to vmParams.ctx)
                )
            )
        }
    }

    fun onClearReplyClicked() {
        viewModelScope.launch {
            chatBoxMode.value = ChatBoxMode.Default()
        }
    }

    fun onReacted(msg: Id, reaction: String) {
        Timber.d("onReacted")
        viewModelScope.launch {
            val message = uiState.value.messages.find { it is ChatView.Message && it.id == msg }
            if (message != null) {
                toggleChatMessageReaction.async(
                    Command.ChatCommand.ToggleMessageReaction(
                        chat = vmParams.ctx,
                        msg = msg,
                        emoji = reaction
                    )
                ).onFailure {
                    Timber.e(it, "Error while toggling chat message reaction")
                }.onSuccess {
                    Timber.d("Toggled chat reaction")
                }

                // Sending analytics
                if (message is ChatView.Message) {
                    val hasAlreadyUserReaction = message.reactions.any { r ->
                        r.emoji == reaction && r.isSelected
                    }
                    if (hasAlreadyUserReaction) {
                        analytics.sendEvent(
                            eventName = EventsDictionary.chatRemoveReaction,
                            props = Props(
                                map = mapOf(EventsPropertiesKey.chatId to vmParams.ctx)
                            )
                        )
                    } else {
                        analytics.sendEvent(
                            eventName = EventsDictionary.chatAddReaction,
                            props = Props(
                                map = mapOf(EventsPropertiesKey.chatId to vmParams.ctx)
                            )
                        )
                    }
                }
            } else {
                Timber.w("Target message not found for reaction")
            }
        }
    }

    fun onReplyMessage(msg: ChatView.Message) {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickMessageMenuReply
            )
        }
        viewModelScope.launch {
            chatBoxMode.value = ChatBoxMode.Reply(
                msg = msg.id,
                text = msg.content.msg.ifEmpty {
                    // Fallback to attachment name if empty
                    if (msg.attachments.isNotEmpty()) {
                        when (val attachment = msg.attachments.last()) {
                            is ChatView.Message.Attachment.Image -> {
                                if (attachment.ext.isNotEmpty()) {
                                    "${attachment.name}.${attachment.ext}"
                                } else {
                                    attachment.name
                                }
                            }

                            is ChatView.Message.Attachment.Video -> {
                                if (attachment.ext.isNotEmpty()) {
                                    "${attachment.name}.${attachment.ext}"
                                } else {
                                    attachment.name
                                }
                            }

                            is ChatView.Message.Attachment.Gallery -> {
                                val first = attachment.images.firstOrNull()
                                if (first != null) {
                                    if (first.ext.isNotEmpty()) {
                                        "${first.name}.${first.ext}"
                                    } else {
                                        first.name
                                    }
                                } else {
                                    EMPTY_STRING_VALUE
                                }
                            }

                            is ChatView.Message.Attachment.Link -> {
                                attachment.title
                            }

                            is ChatView.Message.Attachment.Bookmark -> {
                                attachment.url
                            }
                        }
                    } else {
                        ""
                    }
                },
                author = msg.author,
                isSendingMessageBlocked = false
            )
        }
    }

    fun onDeleteMessage(msg: ChatView.Message) {
        Timber.d("onDeleteMessageClicked msg: ${msg.id}")
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatDeleteMessage
            )
        }
        viewModelScope.launch {
            deleteChatMessage.async(
                Command.ChatCommand.DeleteMessage(
                    chat = vmParams.ctx,
                    msg = msg.id
                )
            ).onFailure {
                Timber.e(it, "Error while deleting chat message")
            }
        }
    }

    fun onDeleteMessageWarningTriggered() {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickMessageMenuDelete
            )
        }
    }

    fun onCopyMessageTextActionTriggered() {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickMessageMenuCopy
            )
        }
    }

    fun onAttachmentMenuTriggered() {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatScreenChatAttach,
                props = Props(
                    map = mapOf(EventsPropertiesKey.chatId to vmParams.ctx)
                )
            )
        }
    }

    fun onAttachmentClicked(msg: ChatView.Message, attachment: ChatView.Message.Attachment) {
        Timber.d("onAttachmentClicked: m")
        viewModelScope.launch {
            when (attachment) {
                is ChatView.Message.Attachment.Image -> {
                    val images = msg.attachments
                        .flatMap { a ->
                            when (a) {
                                is ChatView.Message.Attachment.Image -> {
                                    listOf(a)
                                }

                                is ChatView.Message.Attachment.Gallery -> {
                                    a.images
                                }

                                else -> {
                                    emptyList()
                                }
                            }
                        }
                    val index = images.indexOfFirst {
                        it.obj == attachment.obj
                    }
                    val objects = images.map { item -> item.obj }
                    uXCommands.emit(
                        UXCommand.OpenFullScreenImage(
                            objects = objects,
                            msg = msg,
                            idx = index
                        )
                    )
                }

                is ChatView.Message.Attachment.Video -> {
                    // TODO
                }

                is ChatView.Message.Attachment.Gallery -> {
                    // Do nothing.
                }

                is ChatView.Message.Attachment.Bookmark -> {
                    commands.emit(ViewModelCommand.Browse(attachment.url))
                }

                is ChatView.Message.Attachment.Link -> {
                    val wrapper = attachment.wrapper
                    if (wrapper != null && !attachment.isDeleted) {
                        if (wrapper.layout == ObjectType.Layout.BOOKMARK) {
                            val bookmark = ObjectWrapper.Bookmark(wrapper.map)
                            val url = bookmark.source
                            if (!url.isNullOrEmpty()) {
                                commands.emit(ViewModelCommand.Browse(url))
                            } else {
                                // If url not found, open bookmark object instead of browsing.
                                navigation.emit(wrapper.navigation())
                            }
                        } else if (wrapper.layout == ObjectType.Layout.AUDIO) {
                            commands.emit(
                                ViewModelCommand.PlayAudio(
                                    obj = wrapper.id,
                                    name = wrapper.name.orEmpty()
                                )
                            )
                        } else {
                            navigation.emit(wrapper.navigation())
                        }
                    } else {
                        Timber.w("Wrapper is not found in attachment")
                    }
                }
            }
        }
    }

    fun onChatBoxMediaPicked(uris: List<ChatBoxMediaUri>) {
        Timber.d("DROID-2966 onChatBoxMediaPicked: $uris")
        val hasCameraCapture = uris.any { it.capturedByCamera }
        val attachType = if (hasCameraCapture) {
            EventsDictionary.ChatAttachType.CAMERA.value
        } else {
            EventsDictionary.ChatAttachType.PHOTO.value
        }
        
        viewModelScope.launch {

            // Fire ClickScreenChatAttach event
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickScreenChatAttach,
                props = Props(
                    map = mapOf(
                        EventsPropertiesKey.type to attachType,
                        EventsPropertiesKey.chatId to vmParams.ctx
                    )
                )
            )
            
            // Fire AttachItemChat event
            analytics.sendEvent(
                eventName = EventsDictionary.chatAttachItemChat,
                props = Props(
                    map = mapOf(
                        EventsPropertiesKey.type to attachType,
                        EventsPropertiesKey.count to uris.size,
                        EventsPropertiesKey.chatId to vmParams.ctx
                    )
                )
            )
        }

        viewModelScope.launch {
            chatBoxAttachments.value += uris.map { uri ->
                ChatView.Message.ChatBoxAttachment.Media(
                    uri = uri.uri,
                    isVideo = uri.isVideo,
                    capturedByCamera = uri.capturedByCamera
                )
            }
        }

        // Starting preloading files

        preloadingJobs += viewModelScope.launch {
            uris.forEach { info ->
                val path = if (info.capturedByCamera) {
                    withContext(dispatchers.io) {
                        copyFileToCacheDirectory.copy(info.uri)
                    }
                } else {
                    info.uri
                }
                if (path != null) {
                    preloadFile.async(
                        params = PreloadFile.Params(
                            space = vmParams.space,
                            path = path,
                            type = Block.Content.File.Type.NONE
                        )
                    ).onSuccess { preloadedFileId: Id ->
                        chatBoxAttachments.value = chatBoxAttachments.value.map { a ->
                            if (a is ChatView.Message.ChatBoxAttachment.Media && a.uri == info.uri) {
                                a.copy(
                                    state = ChatView.Message.ChatBoxAttachment.State.Preloaded(
                                        preloadedFileId = preloadedFileId,
                                        path = path
                                    )
                                )
                            } else {
                                a
                            }
                        }
                    }.onFailure {
                        chatBoxAttachments.value = chatBoxAttachments.value.map { a ->
                            if (a is ChatView.Message.ChatBoxAttachment.Media && a.uri == info.uri) {
                                a.copy(
                                    state = ChatView.Message.ChatBoxAttachment.State.Failed
                                )
                            } else {
                                a
                            }
                        }
                    }
                }
            }
        }
    }

    fun onChatBoxFilePicked(infos: List<DefaultFileInfo>) {
        Timber.d("DROID-2966 onChatBoxFilePicked: $infos")
        chatBoxAttachments.value += infos.map { info ->
            ChatView.Message.ChatBoxAttachment.File(
                uri = info.uri,
                name = info.name,
                size = info.size,
                state = ChatView.Message.ChatBoxAttachment.State.Preloading
            )
        }
        
        viewModelScope.launch {
            // Fire ClickScreenChatAttach event
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickScreenChatAttach,
                props = Props(
                    map = mapOf(
                        EventsPropertiesKey.type to EventsDictionary.ChatAttachType.FILE.value,
                        EventsPropertiesKey.chatId to vmParams.ctx
                    )
                )
            )
            
            // Fire AttachItemChat event
            analytics.sendEvent(
                eventName = EventsDictionary.chatAttachItemChat,
                props = Props(
                    map = mapOf(
                        EventsPropertiesKey.type to EventsDictionary.ChatAttachType.FILE.value,
                        EventsPropertiesKey.count to infos.size,
                        EventsPropertiesKey.chatId to vmParams.ctx
                    )
                )
            )
        }
        // Starting preloading files
        preloadingJobs += viewModelScope.launch {
            infos.forEach { info ->
                val path = withContext(dispatchers.io) {
                    copyFileToCacheDirectory.copy(info.uri)
                }
                if (path != null) {
                    preloadFile.async(
                        params = PreloadFile.Params(
                            space = vmParams.space,
                            path = path,
                            type = Block.Content.File.Type.NONE
                        )
                    ).onSuccess { preloadedFileId: Id ->
                        chatBoxAttachments.value = chatBoxAttachments.value.map { a ->
                            if (a is ChatView.Message.ChatBoxAttachment.File && a.uri == info.uri) {
                                a.copy(
                                    state = ChatView.Message.ChatBoxAttachment.State.Preloaded(
                                        preloadedFileId = preloadedFileId,
                                        path = path
                                    )
                                )
                            } else {
                                a
                            }
                        }
                    }.onFailure {
                        chatBoxAttachments.value = chatBoxAttachments.value.map { a ->
                            if (a is ChatView.Message.ChatBoxAttachment.File && a.uri == info.uri) {
                                a.copy(
                                    state = ChatView.Message.ChatBoxAttachment.State.Failed
                                )
                            } else {
                                a
                            }
                        }
                    }
                }
            }
        }
    }

    fun onExitEditMessageMode() {
        Timber.d("onExitEditMessageMode")
        viewModelScope.launch {
            chatBoxAttachments.value = emptyList()
            chatBoxMode.value = ChatBoxMode.Default()
        }
    }

    fun onBackButtonPressed(isExitingVault: Boolean) {
        Timber.d("onBackButtonPressed")
        viewModelScope.launch {
            // Clean up chat container (also unsubscribes chat object subscription)
            withContext(dispatchers.io) {
                chatContainer.stop(chat = vmParams.ctx)
            }
            if (isExitingVault) {
                proceedWithClearingSpaceBeforeExitingToVault()
            }
            commands.emit(ViewModelCommand.Exit)
        }
    }

    fun onSpaceIconClicked() {
        Timber.d("onSpaceIconClicked")
        viewModelScope.launch {
            commands.emit(ViewModelCommand.OpenWidgets)
        }
    }

    fun onInviteMembersClicked() {
        Timber.d("onInviteMembersClicked")
        viewModelScope.launch {
            commands.emit(ViewModelCommand.OpenSpaceMembers(space = vmParams.space))
        }
    }

    fun onEditInfo() {
        viewModelScope.launch {
            val headerView = header.value
            if (headerView is HeaderView.ChatObject) {
                val name = headerView.title
                // Fire analytics event for opening chat info screen
                analytics.sendEvent(eventName = EventsDictionary.screenChatInfo)
                commands.emit(
                    ViewModelCommand.OpenChatInfo(
                        name = name,
                        icon = headerView.icon
                    )
                )
            }
        }
    }

    fun onPinChatAsWidget() {
        Timber.d("onPinChatAsWidget clicked")
        viewModelScope.launch(dispatchers.io) {
            // Check current pin state
            val isPinned = isChatPinned(vmParams.space, vmParams.ctx)
            
            if (isPinned) {
                // Unpin the chat
                unpinChat(
                    space = vmParams.space,
                    obj = vmParams.ctx
                ).onSuccess {
                    Timber.d("Unpinned chat widget successfully")
                    // Update header state immediately
                    val currentHeader = header.value
                    if (currentHeader is HeaderView.ChatObject) {
                        header.value = currentHeader.copy(isPinned = false)
                    }
                    // Payload dispatched automatically, HomeScreenViewModel will update
                }.onFailure {
                    Timber.e(it, "Error while unpinning chat widget")
                }
            } else {
                // Pin the chat
                pinChat(
                    space = vmParams.space,
                    obj = vmParams.ctx
                ).onSuccess {
                    Timber.d("Pinned chat as widget successfully")
                    // Update header state immediately
                    val currentHeader = header.value
                    if (currentHeader is HeaderView.ChatObject) {
                        header.value = currentHeader.copy(isPinned = true)
                    }
                    commands.emit(ViewModelCommand.Toast.PinnedChatAsWidget)
                    // Payload dispatched automatically, HomeScreenViewModel will update
                }.onFailure {
                    Timber.e(it, "Error while pinning object as widget")
                }
            }
        }
    }

    fun onCopyChatLink() {
        // TODO: Implement copy link functionality
    }

    fun onUpdateChatObjectInfoRequested(
        originalName: String,
        originalIcon: ObjectIcon,
        update: ChatInfoUpdate
    ) {
        viewModelScope.launch {
            // Only update name if it has changed
            val nameChanged = update.name != originalName
            if (nameChanged && update.name.isNotEmpty()) {
                setObjectDetails.async(
                    SetObjectDetails.Params(
                        ctx = vmParams.ctx,
                        details = mapOf(Relations.NAME to update.name)
                    )
                ).onSuccess {
                    val currentHeader = header.value
                    when (currentHeader) {
                        is HeaderView.Default -> {
                            header.value = currentHeader.copy(title = update.name)
                        }
                        is HeaderView.ChatObject -> {
                            header.value = currentHeader.copy(title = update.name)
                        }
                        else -> {}
                    }
                }.onFailure { e ->
                    Timber.e(e, "Failed to update chat name while saving chat info")
                }
            }

            // Only update icon if it has changed
            when (val icon = update.chatIcon) {
                is ChatObjectIcon.Image -> {
                    // Upload and set image icon for chat object
                    uploadFile.async(
                        UploadFile.Params(
                            path = icon.uri,
                            space = vmParams.space,
                            type = Block.Content.File.Type.IMAGE
                        )
                    ).onSuccess { file ->
                        setObjectDetails.async(
                            SetObjectDetails.Params(
                                ctx = vmParams.ctx,
                                details = mapOf(
                                    Relations.ICON_IMAGE to file.id,
                                    Relations.ICON_EMOJI to ""
                                )
                            )
                        ).onSuccess {
                            Timber.d("Successfully updated chat object icon with image")
                            // Update local header state immediately
                            val currentHeader = header.value
                            if (currentHeader is HeaderView.ChatObject) {
                                header.value = currentHeader.copy(
                                    icon = ObjectIcon.Profile.Image(
                                        hash = urlBuilder.thumbnail(file.id),
                                        name = update.name
                                    )
                                )
                            }
                            sendToast("Chat icon updated")
                        }.onFailure { e ->
                            Timber.e(e, "Error while setting uploaded chat icon")
                            sendToast("Failed to update chat icon")
                        }
                    }.onFailure { e ->
                        Timber.e(e, "Error while uploading chat icon from uri")
                        sendToast("Failed to upload image")
                    }
                }
                is ChatObjectIcon.Emoji -> {
                    setObjectDetails.async(
                        SetObjectDetails.Params(
                            ctx = vmParams.ctx,
                            details = mapOf(
                                Relations.ICON_EMOJI to icon.unicode,
                                Relations.ICON_IMAGE to ""
                            )
                        )
                    ).onSuccess {
                        Timber.d("Successfully updated chat object icon with emoji")
                        // Update local header state immediately
                        val currentHeader = header.value
                        if (currentHeader is HeaderView.ChatObject) {
                            header.value = currentHeader.copy(
                                icon = ObjectIcon.Basic.Emoji(unicode = icon.unicode)
                            )
                        }
                        sendToast("Chat icon updated")
                    }.onFailure { e ->
                        Timber.e(e, "Error while setting emoji icon for chat")
                        sendToast("Failed to update chat icon")
                    }
                }
                ChatObjectIcon.Removed -> {
                    // User explicitly removed icon - clear both image and emoji
                    setObjectDetails.async(
                        SetObjectDetails.Params(
                            ctx = vmParams.ctx,
                            details = mapOf(
                                Relations.ICON_IMAGE to "",
                                Relations.ICON_EMOJI to ""
                            )
                        )
                    ).onSuccess {
                        Timber.d("Successfully removed chat object icon")
                        // Update local header state immediately
                        val currentHeader = header.value
                        if (currentHeader is HeaderView.ChatObject) {
                            header.value = currentHeader.copy(
                                icon = ObjectIcon.None
                            )
                        }
                        sendToast("Chat icon removed")
                    }.onFailure { e ->
                        Timber.e(e, "Error while removing chat icon")
                        sendToast("Failed to remove chat icon")
                    }
                }
                ChatObjectIcon.None -> {
                    // No change to icon requested
                }
            }
        }
    }

    fun onMoveToBin() {
        showMoveToBinDialog.value = true
    }

    fun onMoveToBinConfirmed() {
        viewModelScope.launch {
            setObjectListIsArchived.async(
                SetObjectListIsArchived.Params(
                    targets = listOf(vmParams.ctx),
                    isArchived = true
                )
            ).onSuccess {
                Timber.d("Successfully moved chat to bin")
                showMoveToBinDialog.value = false
                onBackButtonPressed(isExitingVault = false)
            }.onFailure { e ->
                Timber.e(e, "Error while moving chat to bin")
                sendToast("Failed to move chat to bin")
                showMoveToBinDialog.value = false
            }
        }
    }

    fun onMoveToBinCancelled() {
        showMoveToBinDialog.value = false
    }

    fun onMediaPreview(objects: List<Id>, index: Int) {
        Timber.d("onMediaPreview, objects: $objects")
        viewModelScope.launch {
            commands.emit(
                ViewModelCommand.MediaPreview(
                    index = index,
                    objects = objects
                )
            )
        }
    }

    fun onSelectChatReaction(msg: Id) {
        Timber.d("onSelectChatReaction, msg: $msg")
        viewModelScope.launch {
            commands.emit(
                ViewModelCommand.SelectChatReaction(
                    msg = msg
                )
            )
        }
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickMessageMenuReaction
            )
        }
    }

    fun onViewChatReaction(
        msg: Id,
        emoji: String
    ) {
        Timber.d("onViewChatReaction, msg: $msg, emoji: $emoji")
        viewModelScope.launch {
            commands.emit(
                ViewModelCommand.ViewChatReaction(
                    msg = msg,
                    emoji = emoji
                )
            )
        }
    }

    fun onMemberIconClicked(member: Id?) {
        Timber.d("onMemberIconClicked: $member")
        viewModelScope.launch {
            if (member != null) {
                commands.emit(
                    ViewModelCommand.ViewMemberCard(
                        member = member,
                        space = vmParams.space
                    )
                )
            } else {
                Timber.e("Space member not found in space-level chat")
            }
        }
    }

    fun onEmptyStateAction() {
        viewModelScope.launch {
            commands.emit(ViewModelCommand.OpenSpaceMembers(space = vmParams.space))
        }
    }

    fun onShowQRCode() {
        when (val inviteLinkState = inviteLinkAccessLevel.value) {
            is SpaceInviteLinkAccessLevel.EditorAccess -> proceedWithShowingQRCode(inviteLinkState.link)
            is SpaceInviteLinkAccessLevel.RequestAccess -> proceedWithShowingQRCode(inviteLinkState.link)
            is SpaceInviteLinkAccessLevel.ViewerAccess -> proceedWithShowingQRCode(inviteLinkState.link)
            is SpaceInviteLinkAccessLevel.LinkDisabled -> {
                Timber.w("Invite link is not ready yet")
                sendToast("Invite link is not available")
                return
            }
        }
    }

    fun onShareInviteLink(link: String) {
        viewModelScope.launch {
            // Analytics Event #6: ClickShareSpaceShareLink
            analytics.sendEvent(eventName = clickShareSpaceShareLink)

            commands.emit(
                ViewModelCommand.ShareInviteLink(link)
            )
        }
    }

    fun onHideQRCodeScreen() {
        uiQrCodeState.value = UiSpaceQrCodeState.Hidden
    }

    fun onNotificationSettingChanged(setting: NotificationSetting) {
        viewModelScope.launch {
            // Convert UI setting to domain model
            val mode = setting.toNotificationState()

            // Update header optimistically for instant UI feedback
            val currentHeader = header.value
            val updatedHeader = when (currentHeader) {
                is HeaderView.Default -> currentHeader.copy(notificationSetting = setting)
                is HeaderView.ChatObject -> currentHeader.copy(notificationSetting = setting)
                else -> currentHeader
            }
            header.value = updatedHeader

            setChatNotificationMode.async(
                params = SetChatNotificationMode.Params(
                    space = vmParams.space,
                    chatIds = listOf(vmParams.ctx),
                    mode = mode
                )
            ).onSuccess { payload ->
                Timber.d("Notification setting changed successfully to: $setting")
                // Fire analytics event for chat-level notification change
                analytics.sendAnalyticsChangeMessageNotificationState(
                    spaceUxType = _currentSpaceUxType.value,
                    notificationState = setting.toNotificationState()
                )
            }.onFailure { e ->
                Timber.e(e, "Failed to change notification setting")
                // Revert header to previous state on error
                header.value = currentHeader
                sendToast("Failed to update notification setting")
            }
        }
    }

    private fun proceedWithShowingQRCode(link: String) {
        viewModelScope.launch {
            val currentHeader = header.value
            if (currentHeader is HeaderView.Default) {
                val name = currentHeader.title
                val icon = currentHeader.icon
                val (spaceName, spaceIcon) = name to icon
                uiQrCodeState.value = SpaceInvite(
                    link = link,
                    spaceName = spaceName,
                    icon = spaceIcon
                )
                analytics.sendEvent(
                    eventName = screenQr,
                    props = Props(
                        mapOf(EventsPropertiesKey.route to EventsDictionary.ScreenQrRoutes.CHAT)
                    )
                )
            } else {
                Timber.e("Sharing QR is not supported for non-chat spaces")
            }
        }
    }

    fun onMentionClicked(member: Id) {
        Timber.d("onMentionClicked: $member")
        viewModelScope.launch {
            commands.emit(
                ViewModelCommand.ViewMemberCard(
                    member = member,
                    space = vmParams.space
                )
            )
        }
    }

    fun checkNotificationPermissionDialogState() {
        val shouldShow = notificationPermissionManager.shouldShowPermissionDialog()
        Timber.d("shouldShowNotificationPermissionDialog: $shouldShow")
        if (shouldShow) {
            showNotificationPermissionDialog.value = true
        }
    }

    fun onNotificationPermissionRequested() {
        notificationPermissionManager.onPermissionRequested()
    }

    fun onNotificationPermissionGranted() {
        showNotificationPermissionDialog.value = false
        notificationPermissionManager.onPermissionGranted()
    }

    fun onNotificationPermissionDenied() {
        showNotificationPermissionDialog.value = false
        notificationPermissionManager.onPermissionDenied()
    }

    fun onNotificationPermissionDismissed() {
        showNotificationPermissionDialog.value = false
        notificationPermissionManager.onPermissionDismissed()
    }

    private fun isMentionTriggered(text: String, selectionStart: Int): Boolean {
        if (selectionStart <= 0 || selectionStart > text.length) return false
        val previousChar = text[selectionStart - 1]
        return previousChar == '@'
                && (selectionStart == 1 || !text[selectionStart - 2].isLetterOrDigit())
    }

    private fun shouldHideMention(text: String, selectionStart: Int): Boolean {
        if (selectionStart > text.length) return false
        // Check if the current character is a space
        val currentChar = if (selectionStart > 0) text[selectionStart - 1] else null
        // Hide mention when a space is typed, or '@' character has been deleted (even if it was the first character)
        val atCharExists = text.lastIndexOf('@', selectionStart - 1) != -1
        return currentChar == ' ' || !atCharExists
    }

    private fun resolveMentionQuery(text: String, selectionStart: Int): MentionPanelState.Query? {
        val atIndex = text.lastIndexOf('@', selectionStart - 1)
        if (atIndex == -1) return null

        val beforeAt = text.getOrNull(atIndex - 1)
        if (beforeAt != null && beforeAt.isLetterOrDigit()) return null

        val endIndex = text.indexOfAny(charArrayOf(' ', '\n'), startIndex = atIndex)
            .takeIf { it != -1 } ?: text.length

        val query = text.substring(atIndex + 1, endIndex)
        return MentionPanelState.Query(query, atIndex until endIndex)
    }

    fun onChatScrolledToTop() {
        Timber.d("DROID-2966 onChatScrolledToTop")
        viewModelScope.launch {
            chatContainer.onLoadPrevious()
        }
    }

    fun onChatScrolledToBottom() {
        Timber.d("DROID-2966 onChatScrolledToBottom")
        viewModelScope.launch {
            chatContainer.onLoadNext()
        }
    }

    fun onGoToMentionClicked() {
        Timber.d("DROID-2966 onGoToMentionClicked")
        viewModelScope.launch {
            chatContainer.onGoToMention()
        }
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickScrollToMention,
                props = Props(
                    map = mapOf(EventsPropertiesKey.chatId to vmParams.ctx)
                )
            )
        }
    }

    fun onChatScrollToReply(replyMessage: Id) {
        Timber.d("DROID-2966 onScrollToReply: $replyMessage")
        viewModelScope.launch {
            chatContainer.onLoadToReply(replyMessage = replyMessage)
        }
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickScrollToReply,
                props = Props(
                    map = mapOf(EventsPropertiesKey.chatId to vmParams.ctx)
                )
            )
        }
    }

    fun onScrollToBottomClicked(lastVisibleMessage: Id?) {
        Timber.d("DROID-2966 onScrollToBottom")
        viewModelScope.launch {
            chatContainer.onLoadChatTail(lastVisibleMessage)
        }
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.chatClickScrollToBottom,
                props = Props(
                    map = mapOf(EventsPropertiesKey.chatId to vmParams.ctx)
                )
            )
        }
    }

    fun onClearChatViewStateIntent() {
        Timber.d("DROID-2966 onClearChatViewStateIntent")
        viewModelScope.launch {
            chatContainer.onClearIntent()
        }
    }

    fun onVisibleRangeChanged(
        from: Id,
        to: Id
    ) {
        Timber.d("DROID-2966 onVisibleRangeChanged, from: $from, to: $to")
        visibleRangeUpdates.tryEmit(from to to)
    }

    fun onUrlPasted(url: Url) {
        viewModelScope.launch {
            val curr = chatBoxAttachments.value
            chatBoxAttachments.value = buildList {
                addAll(curr)
                add(
                    ChatView.Message.ChatBoxAttachment.Bookmark(
                        preview = LinkPreview(
                            url = url
                        ),
                        isLoadingPreview = true
                    )
                )
            }
            getLinkPreview.async(
                params = url
            ).onSuccess { preview ->
                chatBoxAttachments.value = buildList {
                    addAll(curr)
                    add(
                        ChatView.Message.ChatBoxAttachment.Bookmark(
                            preview = preview,
                            isLoadingPreview = false
                        )
                    )
                }
            }.onFailure {
                Timber.e(it, "Failed to get link preview")
            }
        }
    }

    fun onCreateAndAttachObject() {
        Timber.d("DROID-2966 onCreateAndAttachObject")
        viewModelScope.launch {
            createObject.async(
                params = CreateObject.Param(
                    space = vmParams.space
                )
            ).onSuccess { result ->
                navigation.emit(
                    result.obj.navigation(
                        effect = OpenObjectNavigation.SideEffect.AttachToChat(
                            chat = vmParams.ctx,
                            space = vmParams.space.id
                        )
                    )
                )
            }.onFailure {
                Timber.d(it, "DROID-2966 Error while creating attach-to-chat object")
            }
        }
    }

    fun hideError() {
        errorState.value = UiErrorState.Hidden
    }

    fun onCameraPermissionDenied() {
        errorState.value = UiErrorState.CameraPermissionDenied
    }

    private fun proceedWithSpaceSubscription() {
        viewModelScope.launch {
            spaceViews.observe().collect { spaces ->
                val space = spaces.firstOrNull { it.targetSpaceId == vmParams.space.id }
                spaceAccessType.value = space?.spaceAccessType
            }
        }
    }

    data class ChatBoxMediaUri(
        val uri: String,
        val isVideo: Boolean = false,
        val capturedByCamera: Boolean = false
    )

    sealed class ViewModelCommand {
        data object Exit : ViewModelCommand()
        data object OpenWidgets : ViewModelCommand()
        data class OpenSpaceMembers(val space: SpaceId) : ViewModelCommand()
        data class MediaPreview(val index: Int, val objects: List<Id>) : ViewModelCommand()
        data class Browse(val url: String) : ViewModelCommand()
        data class PlayAudio(val obj: Id, val name: String) : ViewModelCommand()
        data class SelectChatReaction(val msg: Id) : ViewModelCommand()
        data class ViewChatReaction(val msg: Id, val emoji: String) : ViewModelCommand()
        data class ViewMemberCard(val member: Id, val space: SpaceId) : ViewModelCommand()
        data class ShareInviteLink(val link: String) : ViewModelCommand()
        data class ShareQrCode(val link: String) : ViewModelCommand()
        data class OpenChatInfo(val name: String, val icon: ObjectIcon) : ViewModelCommand()
        sealed class Toast : ViewModelCommand() {
            data object PinnedChatAsWidget : Toast()
        }
    }

    sealed class UXCommand {
        data object JumpToBottom : UXCommand()
        data class SetChatBoxInput(val input: String) : UXCommand()
        data class OpenFullScreenImage(
            val msg: ChatView.Message,
            val objects: List<Id>,
            val idx: Int = 0
        ) : UXCommand()

        data object ShowRateLimitWarning : UXCommand()
    }

    sealed class ChatBoxMode {

        abstract val isSendingMessageBlocked: Boolean

        data object ReadOnly : ChatBoxMode() {
            override val isSendingMessageBlocked: Boolean = true
        }

        data class Default(
            override val isSendingMessageBlocked: Boolean = false
        ) : ChatBoxMode()

        data class EditMessage(
            val msg: Id,
            override val isSendingMessageBlocked: Boolean = false
        ) : ChatBoxMode()

        data class Reply(
            val msg: Id,
            val text: String,
            val author: String,
            override val isSendingMessageBlocked: Boolean = false
        ) : ChatBoxMode()
    }

    private fun ChatBoxMode.updateIsSendingBlocked(isBlocked: Boolean): ChatBoxMode {
        return when (this) {
            is ChatBoxMode.Default -> copy(isSendingMessageBlocked = isBlocked)
            is ChatBoxMode.EditMessage -> copy(isSendingMessageBlocked = isBlocked)
            is ChatBoxMode.Reply -> copy(isSendingMessageBlocked = isBlocked)
            is ChatBoxMode.ReadOnly -> this
        }
    }

    sealed class MentionPanelState {
        data object Hidden : MentionPanelState()
        data class Visible(
            val results: List<Member>,
            val query: Query
        ) : MentionPanelState()

        data class Member(
            val id: Id,
            val name: String,
            val icon: SpaceMemberIconView,
            val isUser: Boolean = false
        )

        data class Query(
            val query: String,
            val range: IntRange
        )
    }

    sealed class HeaderView {
        data object Init : HeaderView()
        data class Default(
            val icon: SpaceIconView,
            val title: String,
            val isMuted: Boolean = false,
            val showDropDownMenu: Boolean = true,
            val showAddMembers: Boolean = true,
            val notificationSetting: NotificationSetting = NotificationSetting.ALL,
            val canEdit: Boolean = true
        ) : HeaderView()
        data class ChatObject(
            val icon: ObjectIcon,
            val title: String,
            val isMuted: Boolean = false,
            val showDropDownMenu: Boolean = true,
            val showAddMembers: Boolean = false,
            val notificationSetting: NotificationSetting = NotificationSetting.ALL,
            val isPinned: Boolean = false,
            val canEdit: Boolean = true
        ) : HeaderView()
    }

    sealed class UiErrorState {
        data object Hidden : UiErrorState()
        data class Show(val msg: String) : UiErrorState()
        data object CameraPermissionDenied : UiErrorState()
    }

    sealed class Params {
        abstract val space: Space

        data class Default(
            val ctx: Id,
            override val space: Space,
            val triggeredByPush: Boolean = false
        ) : Params()
    }

    companion object {
        /**
         * Delay before jump-to-bottom after adding new message to the chat.
         */
        const val JUMP_TO_BOTTOM_DELAY = 50L
    }
}