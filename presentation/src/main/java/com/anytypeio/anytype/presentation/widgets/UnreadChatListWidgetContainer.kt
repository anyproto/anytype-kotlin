package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.AttachmentPreview
import com.anytypeio.anytype.core_models.ui.AttachmentType
import com.anytypeio.anytype.core_models.ui.MimeCategory
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.notifications.NotificationStateCalculator
import com.anytypeio.anytype.domain.`object`.resolveParticipantName
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

/**
 * Container for the Unread section widget that displays all chats with unread messages.
 * Reuses ChatListWidgetContainer's query logic but filters to only show chats with unread count > 0.
 */
class UnreadChatListWidgetContainer(
    private val space: SpaceId,
    private val widget: Widget.UnreadChatList,
    private val storage: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val isWidgetCollapsed: Flow<Boolean>,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val chatPreviewContainer: ChatPreviewContainer,
    private val dateProvider: DateProvider,
    private val stringResourceProvider: StringResourceProvider,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val participantContainer: ParticipantSubscriptionContainer,
    isSessionActiveFlow: Flow<Boolean>,
    onRequestCache: () -> WidgetView? = { null }
) : WidgetContainer {

    /**
     * Reactive flow that emits widget view states based on session activity and collapsed state.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override val view: Flow<WidgetView> =
        isSessionActiveFlow
            .flatMapLatest { isActive ->
                if (isActive)
                    buildViewFlow().onStart {
                        isWidgetCollapsed
                            .map { isCollapsed ->
                                val cached = onRequestCache()
                                if (cached != null) {
                                    val adjustedCache = when (cached) {
                                        is WidgetView.UnreadChatList -> cached.copy(
                                            isExpanded = !isCollapsed
                                        )
                                        else -> cached
                                    }
                                    emit(adjustedCache)
                                } else {
                                    emit(
                                        createWidgetView(
                                            isCollapsed = isCollapsed
                                        )
                                    )
                                }
                            }
                    }
                else
                    flowOf(createWidgetView(isCollapsed = true))
            }

    /**
     * Helper function that emits an empty or loading WidgetView when collapsed,
     * or subscribes to the actual data flow when expanded.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun dataOrEmptyWhenCollapsed(
        isCollapsedFlow: Flow<Boolean>,
        buildData: () -> Flow<WidgetView>
    ): Flow<WidgetView> =
        isCollapsedFlow.distinctUntilChanged().flatMapLatest { isCollapsed ->
            if (isCollapsed) {
                flowOf(createWidgetView(isCollapsed = true))
            } else {
                buildData()
            }
        }

    /**
     * Builds the main widget view flow.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun buildViewFlow(): Flow<WidgetView> =
        dataOrEmptyWhenCollapsed(isWidgetCollapsed) {
            flow {
                // Create subscription params to get all chat objects in space
                val params = StoreSearchParams(
                    space = space,
                    subscription = "subscription.unread.chats.${widget.id}",
                    keys = buildList {
                        addAll(ObjectSearchConstants.defaultKeys)
                        add(Relations.CHAT_ID)
                        add(Relations.LAST_MESSAGE_DATE)
                    },
                    filters = buildList {
                        add(
                            DVFilter(
                                relation = Relations.LAYOUT,
                                condition = DVFilterCondition.EQUAL,
                                value = ObjectType.Layout.CHAT_DERIVED.code.toDouble()
                            )
                        )
                        addAll(ObjectSearchConstants.defaultDataViewFilters())
                    },
                    limit = 0, // No limit - get all chats
                    source = emptyList(),
                    collection = null,
                    sorts = listOf(
                        DVSort(
                            relationKey = Relations.LAST_MESSAGE_DATE,
                            type = DVSortType.DESC,
                            relationFormat = Relation.Format.DATE,
                            includeTime = true
                        )
                    )
                )

                val chatObjects = storage.subscribe(params)
                    .distinctUntilChanged()

                val previews = chatPreviewContainer
                    .observePreviewsBySpaceId(space)
                    .distinctUntilChanged()

                val spaceViews = spaceViewSubscriptionContainer
                    .observe()
                    .distinctUntilChanged()

                // Observe global participants for creator name resolution
                val participantsFlow = participantContainer
                    .observe()
                    .map { participants ->
                        participants.associateBy { it.identity }
                    }
                    .distinctUntilChanged()

                val unreadChats = combine(
                    chatObjects,
                    previews,
                    spaceViews,
                    participantsFlow
                ) { objects, previewList, spaces, participantsByIdentity ->
                    
                    Timber.d("UnreadChatList: Processing ${objects.size} chat objects, ${previewList.size} previews")
                    
                    // Filter to only chats with unread messages
                    val unreadChatIds = previewList
                        .filter { preview ->
                            val unreadCount = (preview.state?.unreadMessages?.counter ?: 0) +
                                    (preview.state?.unreadMentions?.counter ?: 0)
                            val hasUnread = unreadCount > 0
                            if (hasUnread) {
                                Timber.d("UnreadChatList: Chat ${preview.chat} has $unreadCount unread messages")
                            }
                            hasUnread
                        }
                        .map { it.chat }
                        .toSet()

                    Timber.d("UnreadChatList: Found ${unreadChatIds.size} chats with unread messages")
                    
                    // Filter objects to only those with unread messages and sort by lastMessageDate
                    val unreadObjects = objects
                        .filter { obj -> unreadChatIds.contains(obj.id) }

                    // Map to widget elements with preview data
                    val elements = unreadObjects.map { obj ->
                        val preview = previewList.find { it.chat == obj.id }
                        val state = preview?.state

                        if (preview != null && state != null) {
                            val creatorName = participantsByIdentity.resolveParticipantName(
                                identity = preview.message?.creator,
                                fallback = stringResourceProvider.getUntitledCreatorName()
                            )
                            val messageText = preview.message?.content?.text
                            val messageTime = preview.message?.createdAt?.let { timeInSeconds ->
                                if (timeInSeconds > 0) {
                                    dateProvider.getChatPreviewDate(timeInSeconds = timeInSeconds)
                                } else null
                            }
                            val attachmentPreviews = preview.message?.attachments?.map { attachment ->
                                val dependency = preview.dependencies.find { it.id == attachment.target }
                                mapToAttachmentPreview(attachment, dependency)
                            } ?: emptyList()
                            // Find the space view for this chat's space
                            val chatSpace = spaces.find { it.targetSpaceId == space.id }
                            val chatNotificationState = if (chatSpace != null) {
                                NotificationStateCalculator.calculateChatNotificationState(
                                    chatSpace = chatSpace,
                                    chatId = obj.id
                                )
                            } else {
                                NotificationState.ALL
                            }

                            WidgetView.SetOfObjects.Element.Chat(
                                obj = obj,
                                objectIcon = obj.objectIcon(
                                    builder = urlBuilder,
                                    objType = storeOfObjectTypes.getTypeOfObject(obj)
                                ),
                                name = WidgetView.Name.Default(
                                    prettyPrintName = fieldParser.getObjectPluralName(obj, false)
                                ),
                                cover = null,
                                counter = WidgetView.ChatCounter(
                                    unreadMentionCount = state.unreadMentions?.counter ?: 0,
                                    unreadMessageCount = state.unreadMessages?.counter ?: 0
                                ),
                                creatorName = creatorName,
                                messageText = messageText,
                                messageTime = messageTime,
                                attachmentPreviews = attachmentPreviews,
                                chatNotificationState = chatNotificationState
                            )
                        } else {
                            // Fallback to regular element if no preview
                            WidgetView.SetOfObjects.Element.Regular(
                                obj = obj,
                                objectIcon = obj.objectIcon(
                                    builder = urlBuilder,
                                    objType = storeOfObjectTypes.getTypeOfObject(obj)
                                ),
                                name = WidgetView.Name.Default(
                                    prettyPrintName = fieldParser.getObjectPluralName(obj, false)
                                )
                            )
                        }
                    }

                    Timber.d("UnreadChatList: Creating widget view with ${elements.size} elements")
                    
                    WidgetView.UnreadChatList(
                        id = widget.id,
                        source = widget.source,
                        elements = elements,
                        isExpanded = true,
                        icon = widget.icon,
                        name = WidgetView.Name.Bundled(widget.source),
                        sectionType = widget.sectionType
                    )
                }

                emitAll(unreadChats)
            }
        }.catch { e ->
            Timber.e(e, "Error in unread chat list container flow")
            emit(createWidgetView(isCollapsed = false))
        }

    /**
     * Creates a default/empty widget view.
     */
    private fun createWidgetView(isCollapsed: Boolean): WidgetView.UnreadChatList {
        return WidgetView.UnreadChatList(
            id = widget.id,
            source = widget.source,
            elements = emptyList(),
            isExpanded = !isCollapsed,
            icon = widget.icon,
            name = WidgetView.Name.Bundled(widget.source),
            sectionType = widget.sectionType
        )
    }

    /**
     * Transforms a Chat.Message.Attachment to AttachmentPreview.
     * Handles Image/File/Link types with proper ObjectIcon creation.
     */
    private suspend fun mapToAttachmentPreview(
        attachment: Chat.Message.Attachment,
        dependency: ObjectWrapper.Basic?
    ): AttachmentPreview {
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
        
        // Map to preview type enum
        val previewType = when (effectiveType) {
            Chat.Message.Attachment.Type.Image -> AttachmentType.IMAGE
            Chat.Message.Attachment.Type.File -> AttachmentType.FILE
            Chat.Message.Attachment.Type.Link -> AttachmentType.LINK
        }
        
        // Default fallback icon
        fun defaultIconFor(type: Chat.Message.Attachment.Type): ObjectIcon = when (type) {
            Chat.Message.Attachment.Type.Image -> ObjectIcon.FileDefault(mime = MimeCategory.IMAGE)
            Chat.Message.Attachment.Type.File -> ObjectIcon.FileDefault(mime = MimeCategory.OTHER)
            Chat.Message.Attachment.Type.Link -> ObjectIcon.TypeIcon.Default.DEFAULT
        }
        
        // Build the icon
        val icon = if (hasValidDependency) {
            try {
                when (effectiveType) {
                    Chat.Message.Attachment.Type.Image -> {
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
                    Chat.Message.Attachment.Type.Link -> {
                        dependency.objectIcon(
                            builder = urlBuilder,
                            objType = storeOfObjectTypes.getTypeOfObject(dependency)
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to create icon for attachment ${attachment.target}")
                defaultIconFor(effectiveType)
            }
        } else {
            defaultIconFor(effectiveType)
        }
        
        // Only link types get a title
        val title = if (hasValidDependency && effectiveType == Chat.Message.Attachment.Type.Link) {
            fieldParser.getObjectName(objectWrapper = dependency)
        } else null
        
        return AttachmentPreview(
            type = previewType,
            objectIcon = icon,
            title = title
        )
    }
}
