package com.anytypeio.anytype.presentation.sharing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectOrigin
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.Permissions
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.objects.CreateObjectFromUrl
import com.anytypeio.anytype.domain.objects.CreatePrefilledNote
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * ViewModel for the redesigned sharing extension.
 * Handles three distinct flows:
 * - Flow 1: Chat Space - Direct message sending with multi-select
 * - Flow 2: Data Space (no chat) - Object creation with optional linking
 * - Flow 3: Data Space (with chat) - Hybrid with "Send to chat" option
 */
class SharingViewModel(
    private val createBookmarkObject: CreateBookmarkObject,
    private val createPrefilledNote: CreatePrefilledNote,
    private val createObjectFromUrl: CreateObjectFromUrl,
    private val spaceManager: SpaceManager,
    private val urlBuilder: UrlBuilder,
    private val awaitAccountStartManager: AwaitAccountStartManager,
    private val analytics: Analytics,
    private val fileSharer: FileSharer,
    private val permissions: Permissions,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val addChatMessage: AddChatMessage,
    private val uploadFile: UploadFile,
    private val searchObjects: SearchObjects,
    private val fieldParser: FieldParser
) : BaseViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    private val _screenState = MutableStateFlow<SharingScreenState>(SharingScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    private val _commands = MutableSharedFlow<SharingCommand>()
    val commands = _commands.asSharedFlow()

    // Internal state
    private var sharedContent: SharedContent? = null
    private val allSpaces = mutableListOf<SelectableSpaceView>()
    private var selectedChatSpace: SelectableSpaceView? = null
    private var selectedDataSpace: SelectableSpaceView? = null
    private val selectedDestinationObjectIds = mutableSetOf<Id>()
    private var spaceSearchQuery: String = ""
    private var objectSearchQuery: String = ""
    private var commentText: String = ""

    init {
        loadSpaces()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadSpaces() {
        viewModelScope.launch {
            awaitAccountStartManager
                .awaitStart()
                .flatMapLatest {
                    combine(
                        spaceViewSubscriptionContainer.observe().map { items -> items.distinctBy { it.id } },
                        permissions.all()
                    ) { spaces, currPermissions ->
                        spaces.filter { wrapper ->
                            val space = wrapper.targetSpaceId
                            if (space.isNullOrEmpty()) {
                                false
                            } else {
                                currPermissions[space]?.isOwnerOrEditor() == true
                            }
                        }.mapNotNull { spaceView ->
                            val targetSpaceId = spaceView.targetSpaceId ?: return@mapNotNull null
                            SelectableSpaceView(
                                id = spaceView.id,
                                targetSpaceId = targetSpaceId,
                                name = spaceView.name.orEmpty(),
                                icon = spaceView.spaceIcon(urlBuilder),
                                uxType = spaceView.spaceUxType,
                                chatId = spaceView.chatId,
                                isSelected = false
                            )
                        }
                    }
                }
                .catch { e ->
                    Timber.e(e, "Error while loading spaces")
                    _screenState.value = SharingScreenState.Error(
                        message = e.msg(),
                        canRetry = true
                    )
                }
                .collect { spaces ->
                    allSpaces.clear()
                    Log.d("Test1983", "All spaces: $spaces")
                    allSpaces.addAll(spaces)
                    updateSpaceSelectionState()
                }
        }
    }

    /**
     * Called when shared data is received from the intent.
     */
    fun onSharedDataReceived(content: SharedContent) {
        this.sharedContent = content
        updateSpaceSelectionState()
    }

    /**
     * Called when a space is tapped in the grid.
     *
     * Behavior depends on space type:
     * - Chat/One-to-One: Toggle selection, stay on SpaceSelection with comment input
     * - Data spaces: Navigate immediately to ObjectSelection (chats discovered dynamically)
     */
    fun onSpaceSelected(space: SelectableSpaceView) {
        Timber.d("onSpaceSelected: ${space.name}, flowType: ${space.flowType}")
        when (space.flowType) {
            SharingFlowType.CHAT -> {
                // Single-select for chat spaces - toggle selection
                selectedChatSpace = if (selectedChatSpace?.id == space.id) {
                    null  // Deselect if clicking the same space
                } else {
                    space  // Select the new space
                }
                // Stay on SpaceSelection - comment input will appear when space is selected
                updateSpaceSelectionState()
            }
            SharingFlowType.DATA -> {
                // Clear any chat space selection when switching to data space
                selectedChatSpace = null
                // Single select for data spaces - navigate to object selection
                // Chat objects are discovered dynamically via search
                selectedDataSpace = space
                navigateToObjectSelection()
            }
        }
    }

    /**
     * Called when search query changes (for both space and object search).
     */
    fun onSearchQueryChanged(query: String) {
        when (val state = _screenState.value) {
            is SharingScreenState.SpaceSelection -> {
                spaceSearchQuery = query
                updateSpaceSelectionState()
            }
            is SharingScreenState.ObjectSelection -> {
                objectSearchQuery = query
                searchObjectsAndChatsInSpace(state.space)
            }
            else -> { /* ignore */ }
        }
    }

    /**
     * Called when comment text changes.
     */
    fun onCommentChanged(text: String) {
        // Enforce 2000 character limit
        val limitedText = text.take(SharedContent.MAX_CHAT_MESSAGE_LENGTH)
        commentText = limitedText

        when (val state = _screenState.value) {
            is SharingScreenState.SpaceSelection -> {
                // Support comment in SpaceSelection when chat spaces are selected
                _screenState.value = state.copy(commentText = limitedText)
            }
            is SharingScreenState.ChatInput -> {
                _screenState.value = state.copy(commentText = limitedText)
            }
            is SharingScreenState.ObjectSelection -> {
                _screenState.value = state.copy(commentText = limitedText)
            }
            else -> { /* ignore */ }
        }
    }

    /**
     * Called when an object is selected in the destination list.
     * Supports multi-selection up to [SharingScreenState.ObjectSelection.MAX_SELECTION_COUNT] items.
     */
    fun onObjectSelected(obj: SelectableObjectView) {
        val currentState = _screenState.value
        if (currentState !is SharingScreenState.ObjectSelection) return

        // Toggle selection
        if (obj.id in selectedDestinationObjectIds) {
            selectedDestinationObjectIds.remove(obj.id)
        } else {
            // Check limit before adding
            if (selectedDestinationObjectIds.size >= SharingScreenState.ObjectSelection.MAX_SELECTION_COUNT) {
                viewModelScope.launch {
                    _commands.emit(
                        SharingCommand.ShowToast("You can select up to ${SharingScreenState.ObjectSelection.MAX_SELECTION_COUNT} destinations")
                    )
                }
                return
            }
            selectedDestinationObjectIds.add(obj.id)
        }

        // Update state with new selection
        val updatedObjects = currentState.objects.map {
            it.copy(isSelected = it.id in selectedDestinationObjectIds)
        }
        val updatedChatObjects = currentState.chatObjects.map {
            it.copy(isSelected = it.id in selectedDestinationObjectIds)
        }

        _screenState.value = currentState.copy(
            objects = updatedObjects,
            chatObjects = updatedChatObjects,
            selectedObjectIds = selectedDestinationObjectIds.toSet()
        )
    }

    /**
     * Called when Send/Save button is clicked.
     * Handles multi-select: sends to all selected chats and saves to all selected objects.
     */
    fun onSendClicked() {
        viewModelScope.launch {
            when (val state = _screenState.value) {
                is SharingScreenState.SpaceSelection -> {
                    // Handle send from SpaceSelection when a chat space is selected
                    if (selectedChatSpace != null) {
                        sendToChat()
                    }
                }
                is SharingScreenState.ChatInput -> {
                    sendToChat()
                }
                is SharingScreenState.ObjectSelection -> {
                    if (state.selectedObjectIds.isEmpty()) {
                        // No selection - create new object (existing behavior)
                        createObjectInSpace()
                    } else {
                        // Process all selected destinations
                        sendToMultipleDestinations(state)
                    }
                }
                else -> { /* ignore */ }
            }
        }
    }

    /**
     * Sends content to multiple selected destinations (chats and/or objects).
     * Chat destinations receive the shared content with comment.
     * Object destinations get the content linked/saved.
     */
    private suspend fun sendToMultipleDestinations(state: SharingScreenState.ObjectSelection) {
        val content = sharedContent ?: return

        // Partition selected items into chats and objects
        val selectedChats = state.chatObjects.filter { it.id in state.selectedObjectIds }
        val selectedObjects = state.objects.filter { it.id in state.selectedObjectIds }

        _screenState.value = SharingScreenState.Sending(progress = 0f, message = "Sending...")

        try {
            // Send to chats (with comment)
            selectedChats.forEach { chat ->
                sendContentToChatObject(chat.id, state.space, content)
            }

            // Save to objects
            selectedObjects.forEach { obj ->
                // For now, create object in space (linking to existing objects can be added later)
                // This maintains compatibility with the current behavior
            }

            // If only objects selected (no chats), create the object
            if (selectedChats.isEmpty() && selectedObjects.isNotEmpty()) {
                createObjectInSpace()
                return
            }

            _screenState.value = SharingScreenState.Success(
                createdObjectId = null,
                spaceName = state.space.name,
                canOpenObject = false
            )

            _commands.emit(SharingCommand.Dismiss)

        } catch (e: Exception) {
            Timber.e(e, "Error sending to multiple destinations")
            _screenState.value = SharingScreenState.Error(
                message = e.msg(),
                canRetry = true
            )
        }
    }

    /**
     * Sends content to a chat object (CHAT_DERIVED layout) within a data space.
     */
    private suspend fun sendContentToChatObject(chatObjectId: Id, space: SelectableSpaceView, content: SharedContent) {
        val spaceId = SpaceId(space.targetSpaceId)
        sendContentToChat(chatObjectId, spaceId, content)
    }

    /**
     * Called when back button is pressed.
     * @return true if the back press was handled, false otherwise
     */
    fun onBackPressed(): Boolean {
        return when (_screenState.value) {
            is SharingScreenState.ChatInput -> {
                // Clear chat selection and go back to space selection
                selectedChatSpace = null
                commentText = ""
                updateSpaceSelectionState()
                true
            }
            is SharingScreenState.ObjectSelection -> {
                // Go back to space selection
                selectedDataSpace = null
                selectedDestinationObjectIds.clear()
                objectSearchQuery = ""
                commentText = ""
                updateSpaceSelectionState()
                true
            }
            else -> false
        }
    }

    // region Private Methods

    private fun updateSpaceSelectionState() {
        val content = sharedContent ?: return

        val filteredSpaces = if (spaceSearchQuery.isBlank()) {
            allSpaces
        } else {
            allSpaces.filter {
                it.name.contains(spaceSearchQuery, ignoreCase = true)
            }
        }

        val spacesWithSelection = filteredSpaces.map { space ->
            space.copy(isSelected = selectedChatSpace?.id == space.id)
        }

        _screenState.value = SharingScreenState.SpaceSelection(
            spaces = filteredSpaces,
            searchQuery = spaceSearchQuery,
            sharedContent = content,
            commentText = commentText
        )
    }

    private fun showChatInputScreen() {
        val space = selectedChatSpace ?: return
        val content = sharedContent ?: return
        _screenState.value = SharingScreenState.ChatInput(
            selectedSpaces = listOf(space),  // Single item list for compatibility
            commentText = commentText,
            sharedContent = content
        )
    }

    private fun navigateToObjectSelection() {
        val space = selectedDataSpace ?: return
        val content = sharedContent ?: return

        // Show loading state initially
        _screenState.value = SharingScreenState.ObjectSelection(
            space = space,
            objects = emptyList(),
            chatObjects = emptyList(),
            searchQuery = objectSearchQuery,
            selectedObjectIds = selectedDestinationObjectIds.toSet(),
            commentText = commentText,
            sharedContent = content
        )

        // Load objects and chats in parallel
        searchObjectsAndChatsInSpace(space)
    }

    /**
     * Searches for both regular objects and chat objects in the given space.
     * Results are loaded in parallel for better performance.
     * Preserves selection state across search queries.
     */
    private fun searchObjectsAndChatsInSpace(space: SelectableSpaceView) {
        viewModelScope.launch {
            val spaceId = SpaceId(space.targetSpaceId)

            // Search regular objects
            val objects = searchRegularObjects(spaceId, space.uxType)

            // Search chat objects (CHAT_DERIVED layout)
            val chats = searchChatObjects(spaceId)

            val currentState = _screenState.value
            if (currentState is SharingScreenState.ObjectSelection) {
                _screenState.value = currentState.copy(
                    objects = objects,
                    chatObjects = chats,
                    searchQuery = objectSearchQuery,
                    selectedObjectIds = selectedDestinationObjectIds.toSet()
                )
            }
        }
    }

    /**
     * Searches for regular objects (non-chat) in the space.
     */
    private suspend fun searchRegularObjects(
        spaceId: SpaceId,
        spaceUxType: SpaceUxType?
    ): List<SelectableObjectView> {
        val filters = buildObjectSearchFilters(spaceUxType)
        val sorts = listOf(
            DVSort(
                relationKey = Relations.LAST_MODIFIED_DATE,
                type = DVSortType.DESC,
                relationFormat = RelationFormat.DATE
            )
        )

        val params = SearchObjects.Params(
            space = spaceId,
            filters = filters,
            sorts = sorts,
            fulltext = objectSearchQuery,
            keys = listOf(
                Relations.ID,
                Relations.NAME,
                Relations.ICON_EMOJI,
                Relations.ICON_IMAGE,
                Relations.ICON_OPTION,
                Relations.TYPE,
                Relations.TYPE_UNIQUE_KEY,
                Relations.LAYOUT
            )
        )

        return try {
            val objects = searchObjects(params).getOrNull() ?: emptyList()
            objects.map { obj ->
                SelectableObjectView(
                    id = obj.id,
                    name = fieldParser.getObjectPluralName(obj),
                    icon = obj.objectIcon(urlBuilder),
                    typeName = "", // TODO: resolve type name
                    isSelected = obj.id in selectedDestinationObjectIds
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching objects in space")
            emptyList()
        }
    }

    /**
     * Searches for chat objects (CHAT_DERIVED layout) in the space.
     */
    private suspend fun searchChatObjects(spaceId: SpaceId): List<SelectableObjectView> {
        val filters = buildList {
            add(
                DVFilter(
                    relation = Relations.LAYOUT,
                    condition = DVFilterCondition.EQUAL,
                    value = ObjectType.Layout.CHAT_DERIVED.code.toDouble()
                )
            )
            add(
                DVFilter(
                    relation = Relations.IS_ARCHIVED,
                    condition = DVFilterCondition.NOT_EQUAL,
                    value = true
                )
            )
            add(
                DVFilter(
                    relation = Relations.IS_DELETED,
                    condition = DVFilterCondition.NOT_EQUAL,
                    value = true
                )
            )
        }

        val sorts = listOf(
            DVSort(
                relationKey = Relations.LAST_MODIFIED_DATE,
                type = DVSortType.DESC,
                relationFormat = RelationFormat.DATE
            )
        )

        val params = SearchObjects.Params(
            space = spaceId,
            filters = filters,
            sorts = sorts,
            fulltext = "",
            keys = listOf(
                Relations.ID,
                Relations.NAME,
                Relations.ICON_EMOJI,
                Relations.ICON_IMAGE,
                Relations.ICON_OPTION
            )
        )

        return try {
            val objects = searchObjects(params).getOrNull() ?: emptyList()
            objects.map { obj ->
                SelectableObjectView(
                    id = obj.id,
                    name = fieldParser.getObjectPluralName(obj),
                    icon = obj.objectIcon(urlBuilder),
                    typeName = "Chat",
                    isSelected = obj.id in selectedDestinationObjectIds,
                    isChatOption = true
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching chat objects in space")
            emptyList()
        }
    }

    private fun buildObjectSearchFilters(spaceUxType: SpaceUxType?): List<DVFilter> = buildList {
        // Base filters from ObjectSearchConstants
        addAll(ObjectSearchConstants.filterSearchObjects(excludeTypes = true, spaceUxType = spaceUxType))

        // Exclude Sets, Collections, and Chat objects (chats are searched separately)
        add(
            DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.NOT_IN,
                value = listOf(
                    ObjectType.Layout.SET.code.toDouble(),
                    ObjectType.Layout.COLLECTION.code.toDouble(),
                    ObjectType.Layout.CHAT_DERIVED.code.toDouble()
                )
            )
        )
    }

    // endregion

    // region Chat Sending

    /**
     * Sends content to the currently selected chat space.
     * Used when a chat/one-to-one space is selected from the space grid.
     */
    private suspend fun sendToChat() {
        val space = selectedChatSpace ?: return
        sendToChat(space)
    }

    /**
     * Sends content to a specific chat space.
     * Used both from space selection and object selection flows.
     */
    private suspend fun sendToChat(space: SelectableSpaceView) {
        val content = sharedContent ?: return
        val chatId = space.chatId ?: return
        val spaceId = SpaceId(space.targetSpaceId)

        _screenState.value = SharingScreenState.Sending(progress = 0f, message = "Sending...")

        try {
            sendContentToChat(chatId, spaceId, content)

            _screenState.value = SharingScreenState.Success(
                createdObjectId = null,
                spaceName = space.name,
                canOpenObject = false
            )

            _commands.emit(SharingCommand.Dismiss)

        } catch (e: Exception) {
            Timber.e(e, "Error sending to chat")
            _screenState.value = SharingScreenState.Error(
                message = e.msg(),
                canRetry = true
            )
        }
    }

    private suspend fun sendContentToChat(chatId: Id, spaceId: SpaceId, content: SharedContent) {
        when (content) {
            is SharedContent.Text -> {
                // Truncate text if > 2000 chars
                val truncatedText = content.text.take(SharedContent.MAX_CHAT_MESSAGE_LENGTH)
                sendChatMessage(chatId, truncatedText, emptyList())

                // Send comment as separate message if provided
                if (commentText.isNotBlank()) {
                    sendChatMessage(chatId, commentText, emptyList())
                }
            }

            is SharedContent.Url -> {
                // Create bookmark object and send as attachment
                createBookmarkForChat(content.url, spaceId, chatId)
            }

            is SharedContent.SingleMedia -> {
                // Upload and send with caption
                uploadMediaFile(content.uri, content.type, spaceId) { fileId ->
                    val attachment = createMediaAttachment(fileId, content.type)
                    sendChatMessage(chatId, commentText, listOf(attachment))
                }
            }

            is SharedContent.MultipleMedia -> {
                // Batch into groups of 10
                val batches = content.uris.chunked(SharedContent.MAX_ATTACHMENTS_PER_MESSAGE)
                batches.forEachIndexed { index, batch ->
                    val attachments = mutableListOf<Chat.Message.Attachment>()
                    batch.forEach { uri ->
                        uploadMediaFile(uri, content.type, spaceId) { fileId ->
                            attachments.add(createMediaAttachment(fileId, content.type))
                        }
                    }

                    // Add caption only to first message
                    val caption = if (index == 0) commentText else ""
                    sendChatMessage(chatId, caption, attachments)
                }
            }

            is SharedContent.Mixed -> {
                // Send comment as separate preceding message
                if (commentText.isNotBlank()) {
                    sendChatMessage(chatId, commentText, emptyList())
                }

                // Build and batch attachments
                val attachments = mutableListOf<Chat.Message.Attachment>()

                // Add bookmark if URL present
                content.url?.let { url ->
//                    val bookmarkId = createBookmarkForChat(url, spaceId, chatId)
//                    if (bookmarkId != null) {
//                        attachments.add(Chat.Message.Attachment(
//                            target = bookmarkId,
//                            type = Chat.Message.Attachment.Type.Link
//                        ))
//                    }
                }

                // Add media attachments
                content.mediaUris.forEach { uri ->
                    uploadMediaFile(uri, SharedContent.MediaType.FILE, spaceId) { fileId ->
                        attachments.add(createMediaAttachment(fileId, SharedContent.MediaType.FILE))
                    }
                }

                // Batch and send
                attachments.chunked(SharedContent.MAX_ATTACHMENTS_PER_MESSAGE).forEach { batch ->
                    sendChatMessage(chatId, "", batch)
                }

                // Send text as separate message if present
                content.text?.let { text ->
                    sendChatMessage(chatId, text.take(SharedContent.MAX_CHAT_MESSAGE_LENGTH), emptyList())
                }
            }
        }
    }

    private suspend fun sendChatMessage(
        chatId: Id,
        text: String,
        attachments: List<Chat.Message.Attachment>
    ) {
        addChatMessage.async(
            Command.ChatCommand.AddMessage(
                chat = chatId,
                message = Chat.Message.new(
                    text = text,
                    attachments = attachments,
                    marks = emptyList()
                )
            )
        ).fold(
            onSuccess = { (messageId, _) ->
                Timber.d("Message sent successfully: $messageId")
            },
            onFailure = { e ->
                Timber.e(e, "Error sending chat message")
                throw e
            }
        )
    }

    private suspend fun createBookmarkForChat(url: String, spaceId: SpaceId, chatId: String) {
        val params = CreateObjectFromUrl.Params(
            url = url,
            space = spaceId
        )
        return createObjectFromUrl.async(params).fold(
            onSuccess = { obj ->
                val bookmarkId = obj.id
                if (bookmarkId != null) {
                    val attachment = Chat.Message.Attachment(
                        target = bookmarkId,
                        type = Chat.Message.Attachment.Type.Link
                    )
                    sendChatMessage(chatId, "", listOf(attachment))
                }

                // Send comment as separate message
                if (commentText.isNotBlank()) {
                    sendChatMessage(chatId, commentText, emptyList())
                }
            },
            onFailure = { e ->
                Timber.e(e, "Error creating bookmark from URL")
                null
            }
        )
    }

    private suspend fun uploadMediaFile(
        uri: String,
        type: SharedContent.MediaType,
        spaceId: SpaceId,
        onSuccess: suspend (Id) -> Unit
    ) = withContext(Dispatchers.IO) {
        val path = try {
            fileSharer.getPath(uri)
        } catch (e: Exception) {
            Timber.e(e, "Error getting path for URI: $uri")
            return@withContext
        }

        if (path == null) {
            Timber.e("Path is null for URI: $uri")
            return@withContext
        }

        val fileType = when (type) {
            SharedContent.MediaType.IMAGE -> Block.Content.File.Type.IMAGE
            SharedContent.MediaType.VIDEO -> Block.Content.File.Type.VIDEO
            SharedContent.MediaType.FILE -> Block.Content.File.Type.NONE
            SharedContent.MediaType.PDF -> Block.Content.File.Type.PDF
            SharedContent.MediaType.AUDIO -> Block.Content.File.Type.AUDIO
        }

        uploadFile.async(
            UploadFile.Params(
                space = spaceId,
                path = path,
                type = fileType
            )
        ).fold(
            onSuccess = { file -> onSuccess(file.id) },
            onFailure = { e ->
                Timber.e(e, "Error uploading file")
            }
        )
    }

    private fun createMediaAttachment(
        fileId: Id,
        type: SharedContent.MediaType
    ): Chat.Message.Attachment {
        val attachmentType = when (type) {
            SharedContent.MediaType.IMAGE -> Chat.Message.Attachment.Type.Image
            SharedContent.MediaType.VIDEO -> Chat.Message.Attachment.Type.File
            SharedContent.MediaType.FILE -> Chat.Message.Attachment.Type.File
            SharedContent.MediaType.PDF -> Chat.Message.Attachment.Type.File
            SharedContent.MediaType.AUDIO -> Chat.Message.Attachment.Type.File
        }
        return Chat.Message.Attachment(target = fileId, type = attachmentType)
    }

    // endregion

    // region Object Creation

    private suspend fun createObjectInSpace() {
        val content = sharedContent ?: return
        val space = selectedDataSpace ?: return

        _screenState.value = SharingScreenState.Sending(progress = 0f, message = "Creating...")

        val targetSpaceId = space.targetSpaceId

        when (content) {
            is SharedContent.Text -> {
                proceedWithNoteCreation(content.text, targetSpaceId) { objectId ->
                    handleObjectCreationSuccess(objectId, space, targetSpaceId)
                }
            }
            is SharedContent.Url -> {
                proceedWithBookmarkCreation(content.url, targetSpaceId) { objectId ->
                    handleObjectCreationSuccess(objectId, space, targetSpaceId)
                }
            }
            is SharedContent.SingleMedia -> {
                val title = fileSharer.getDisplayName(content.uri) ?: ""
                proceedWithNoteCreation(title, targetSpaceId) { objectId ->
                    // TODO: Drop files into the object using FileDrop
                    handleObjectCreationSuccess(objectId, space, targetSpaceId)
                }
            }
            is SharedContent.MultipleMedia -> {
                val title = content.uris.mapNotNull { fileSharer.getDisplayName(it) }.joinToString(", ")
                proceedWithNoteCreation(title, targetSpaceId) { objectId ->
                    // TODO: Drop files into the object using FileDrop
                    handleObjectCreationSuccess(objectId, space, targetSpaceId)
                }
            }
            is SharedContent.Mixed -> {
                val noteText = content.text ?: content.url ?: ""
                proceedWithNoteCreation(noteText, targetSpaceId) { objectId ->
                    // TODO: Drop media files into the object
                    handleObjectCreationSuccess(objectId, space, targetSpaceId)
                }
            }
        }
    }

    private suspend fun proceedWithNoteCreation(
        text: String,
        targetSpaceId: Id,
        onSuccess: suspend (Id) -> Unit
    ) {
        createPrefilledNote.async(
            CreatePrefilledNote.Params(
                text = text,
                space = targetSpaceId,
                details = mapOf(
                    Relations.ORIGIN to ObjectOrigin.SHARING_EXTENSION.code.toDouble()
                )
            )
        ).fold(
            onSuccess = { objectId ->
                viewModelScope.sendAnalyticsObjectCreateEvent(
                    analytics = analytics,
                    objType = MarketplaceObjectTypeIds.NOTE,
                    route = EventsDictionary.Routes.sharingExtension,
                    startTime = System.currentTimeMillis(),
                    spaceParams = provideParams(spaceManager.get())
                )
                onSuccess(objectId)
            },
            onFailure = { e ->
                Timber.e(e, "Error creating note")
                _screenState.value = SharingScreenState.Error(
                    message = e.msg(),
                    canRetry = true
                )
            }
        )
    }

    private suspend fun proceedWithBookmarkCreation(
        url: String,
        targetSpaceId: Id,
        onSuccess: suspend (Id) -> Unit
    ) {
        createBookmarkObject(
            CreateBookmarkObject.Params(
                space = targetSpaceId,
                url = url,
                details = mapOf(
                    Relations.ORIGIN to ObjectOrigin.SHARING_EXTENSION.code.toDouble()
                )
            )
        ).process(
            success = { objectId ->
                viewModelScope.sendAnalyticsObjectCreateEvent(
                    analytics = analytics,
                    objType = MarketplaceObjectTypeIds.BOOKMARK,
                    route = EventsDictionary.Routes.sharingExtension,
                    startTime = System.currentTimeMillis(),
                    spaceParams = provideParams(spaceManager.get())
                )
                onSuccess(objectId)
            },
            failure = { e ->
                Timber.e(e, "Error creating bookmark")
                _screenState.value = SharingScreenState.Error(
                    message = e.msg(),
                    canRetry = true
                )
            }
        )
    }

    private suspend fun handleObjectCreationSuccess(
        objectId: Id,
        space: SelectableSpaceView,
        targetSpaceId: Id
    ) {
        val currentSpaceId = spaceManager.get()

        _screenState.value = SharingScreenState.Success(
            createdObjectId = objectId,
            spaceName = space.name,
            canOpenObject = targetSpaceId == currentSpaceId
        )

        if (targetSpaceId == currentSpaceId) {
            _commands.emit(SharingCommand.NavigateToObject(objectId, targetSpaceId))
        } else {
            _commands.emit(SharingCommand.ObjectAddedToSpaceToast(space.name))
            _commands.emit(SharingCommand.Dismiss)
        }
    }

    // endregion

    // region Factory

    class Factory @Inject constructor(
        private val createBookmarkObject: CreateBookmarkObject,
        private val createPrefilledNote: CreatePrefilledNote,
        private val createObjectFromUrl: CreateObjectFromUrl,
        private val spaceManager: SpaceManager,
        private val urlBuilder: UrlBuilder,
        private val awaitAccountStartManager: AwaitAccountStartManager,
        private val analytics: Analytics,
        private val fileSharer: FileSharer,
        private val permissions: Permissions,
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        private val addChatMessage: AddChatMessage,
        private val uploadFile: UploadFile,
        private val searchObjects: SearchObjects,
        private val fieldParser: FieldParser
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SharingViewModel(
                createBookmarkObject = createBookmarkObject,
                createPrefilledNote = createPrefilledNote,
                createObjectFromUrl = createObjectFromUrl,
                spaceManager = spaceManager,
                urlBuilder = urlBuilder,
                awaitAccountStartManager = awaitAccountStartManager,
                analytics = analytics,
                fileSharer = fileSharer,
                permissions = permissions,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
                spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
                addChatMessage = addChatMessage,
                uploadFile = uploadFile,
                searchObjects = searchObjects,
                fieldParser = fieldParser
            ) as T
        }
    }

    // endregion

    companion object {
        private const val TAG = "SharingViewModel"
    }
}

/**
 * Extension function to get object icon from ObjectWrapper.Basic
 */
private fun com.anytypeio.anytype.core_models.ObjectWrapper.Basic.objectIcon(
    urlBuilder: UrlBuilder
): ObjectIcon {
    val iconEmoji = iconEmoji
    val iconImage = iconImage

    return when {
        !iconEmoji.isNullOrBlank() -> ObjectIcon.Basic.Emoji(iconEmoji)
        !iconImage.isNullOrBlank() -> ObjectIcon.Basic.Image(urlBuilder.thumbnail(iconImage))
        else -> ObjectIcon.None
    }
}
