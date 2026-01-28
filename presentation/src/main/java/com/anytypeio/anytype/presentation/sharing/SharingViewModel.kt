package com.anytypeio.anytype.presentation.sharing

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
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.core_models.ui.spaceIcon
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.multiplayer.Permissions
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.objects.CreateObjectFromUrl
import com.anytypeio.anytype.domain.objects.CreatePrefilledNote
import com.anytypeio.anytype.domain.page.AddBackLinkToObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.buildDeletedFilter
import com.anytypeio.anytype.presentation.search.buildLayoutFilter
import com.anytypeio.anytype.presentation.search.buildTemplateFilter
import javax.inject.Inject
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
    private val fieldParser: FieldParser,
    private val addBackLinkToObject: AddBackLinkToObject,
    private val addObjectToCollection: AddObjectToCollection
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
    private var cachedTypesMap: Map<Id, ObjectWrapper.Type> = emptyMap()

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
            is SharingScreenState.ObjectSelection -> {
                _screenState.value = state.copy(commentText = limitedText)
            }
            else -> { /* ignore */ }
        }
    }

    /**
     * Called when retry button is clicked on error screen.
     * Attempts to reload spaces and retry the last operation.
     */
    fun onRetryClicked() {
        val content = sharedContent
        if (content != null) {
            // Reset to loading state and reload spaces
            _screenState.value = SharingScreenState.Loading
            loadSpaces()
        }
    }

    /**
     * Called when an object is selected in the destination list.
     * Single selection mode - selecting a new item deselects the previous one.
     */
    fun onObjectSelected(obj: SelectableObjectView) {
        val currentState = _screenState.value
        if (currentState !is SharingScreenState.ObjectSelection) return

        val wasSelected = obj.id in selectedDestinationObjectIds

        // Clear all selections first (single selection mode)
        selectedDestinationObjectIds.clear()

        // If it wasn't selected before, select it now
        if (!wasSelected) {
            selectedDestinationObjectIds.add(obj.id)
        }
        // If it was selected, clicking again deselects (nothing to add)

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
     * Object destinations get the shared content linked to the destination object.
     */
    private suspend fun sendToMultipleDestinations(state: SharingScreenState.ObjectSelection) {
        val content = sharedContent ?: return
        val spaceId = SpaceId(state.space.targetSpaceId)

        // Partition selected items into chats, collections, and regular objects
        val selectedChats = state.chatObjects.filter { it.id in state.selectedObjectIds }
        val selectedObjects = state.objects.filter { it.id in state.selectedObjectIds }
        val selectedCollections = selectedObjects.filter { it.isCollection }
        val selectedRegularObjects = selectedObjects.filter { !it.isCollection }

        _screenState.value = SharingScreenState.Sending(progress = 0f, message = "Sending...")

        try {
            // Send to chats (with comment)
            selectedChats.forEach { chat ->
                sendContentToChatObject(chat.id, state.space, content)
            }

            // Handle objects and collections - create new objects first
            if (selectedObjects.isNotEmpty()) {
                val newObjectIds = createObjectFromContent(content, state.space.targetSpaceId)

                // Add to collections
                selectedCollections.forEach { collection ->
                    newObjectIds.forEach { newObjectId ->
                        addToCollection(
                            collectionId = collection.id,
                            objectId = newObjectId
                        )
                    }
                }

                // Link to regular objects
                selectedRegularObjects.forEach { destObject ->
                    newObjectIds.forEach { newObjectId ->
                        linkObjectToDestination(
                            objectToLink = newObjectId,
                            destinationObjectId = destObject.id,
                            spaceId = spaceId
                        )
                    }
                }
            }

            _screenState.value = SharingScreenState.Success(
                createdObjectId = null,
                spaceName = state.space.name,
                canOpenObject = false
            )

            // Show Snackbar with option to open the destination
            val firstSelectedChat = selectedChats.firstOrNull()
            val firstSelectedCollection = selectedCollections.firstOrNull()
            val firstSelectedRegularObject = selectedRegularObjects.firstOrNull()

            when {
                // Chat selected - show Snackbar to open chat
                firstSelectedChat != null -> {
                    _commands.emit(
                        SharingCommand.ShowSnackbarWithOpenAction(
                            contentType = content,
                            destinationName = firstSelectedChat.name,
                            spaceName = null,
                            objectId = firstSelectedChat.id,
                            spaceId = state.space.targetSpaceId,
                            isChat = true
                        )
                    )
                }
                // Collection selected - show Snackbar with "added to" format
                firstSelectedCollection != null -> {
                    _commands.emit(
                        SharingCommand.ShowSnackbarWithOpenAction(
                            contentType = content,
                            destinationName = firstSelectedCollection.name,
                            spaceName = null,  // null triggers "added to" format
                            objectId = firstSelectedCollection.id,
                            spaceId = state.space.targetSpaceId,
                            isChat = false,
                            isCollection = true
                        )
                    )
                }
                // Regular object selected - show Snackbar with "linked to" format
                firstSelectedRegularObject != null -> {
                    _commands.emit(
                        SharingCommand.ShowSnackbarWithOpenAction(
                            contentType = content,
                            destinationName = firstSelectedRegularObject.name,
                            spaceName = state.space.name,
                            objectId = firstSelectedRegularObject.id,
                            spaceId = state.space.targetSpaceId,
                            isChat = false
                        )
                    )
                }
            }
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
     * Creates new objects from shared content.
     * Returns the list of created object IDs.
     * For media content, uploads files directly (files are first-class objects in Anytype).
     */
    private suspend fun createObjectFromContent(content: SharedContent, targetSpaceId: Id): List<Id> {
        return when (content) {
            is SharedContent.Text -> {
                listOfNotNull(createNoteAndGetId(content.text, targetSpaceId))
            }
            is SharedContent.Url -> {
                listOfNotNull(createBookmarkAndGetId(content.url, targetSpaceId))
            }
            is SharedContent.SingleMedia -> {
                // Upload file directly - files are objects in Anytype
                listOfNotNull(uploadMediaAndGetId(content.uri, content.type, targetSpaceId))
            }
            is SharedContent.MultipleMedia -> {
                // Upload ALL files
                content.uris.mapNotNull { uri ->
                    uploadMediaAndGetId(uri, content.type, targetSpaceId)
                }
            }
        }
    }

    /**
     * Creates a note and returns its ID, or null on failure.
     * Falls back to PAGE type if NOTE type is deleted or archived.
     */
    private suspend fun createNoteAndGetId(text: String, targetSpaceId: Id): Id? {
        // Check if NOTE type is available (not deleted/archived)
        val usePageFallback = !isNoteTypeAvailable(SpaceId(targetSpaceId))

        val customType = if (usePageFallback) TypeKey(ObjectTypeUniqueKeys.PAGE) else null
        val analyticsType =
            if (usePageFallback) MarketplaceObjectTypeIds.PAGE else MarketplaceObjectTypeIds.NOTE

        var result: Id? = null
        createPrefilledNote.async(
            CreatePrefilledNote.Params(
                text = text,
                space = targetSpaceId,
                customType = customType,
                details = mapOf(
                    Relations.ORIGIN to ObjectOrigin.SHARING_EXTENSION.code.toDouble()
                )
            )
        ).fold(
            onSuccess = { objectId ->
                viewModelScope.sendAnalyticsObjectCreateEvent(
                    analytics = analytics,
                    objType = analyticsType,
                    route = EventsDictionary.Routes.sharingExtension,
                    startTime = System.currentTimeMillis(),
                    spaceParams = provideParams(targetSpaceId)
                )
                result = objectId
            },
            onFailure = { e ->
                Timber.e(e, "Error creating note")
            }
        )
        return result
    }

    /**
     * Creates a bookmark and returns its ID, or null on failure.
     */
    private suspend fun createBookmarkAndGetId(url: String, targetSpaceId: Id): Id? {
        var result: Id? = null
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
                    spaceParams = provideParams(targetSpaceId)
                )
                result = objectId
            },
            failure = { e ->
                Timber.e(e, "Error creating bookmark")
            }
        )
        return result
    }

    /**
     * Uploads a media file and returns its ID, or null on failure.
     * Files in Anytype are first-class objects with their own IDs.
     */
    private suspend fun uploadMediaAndGetId(
        uri: String,
        type: SharedContent.MediaType,
        targetSpaceId: Id
    ): Id? {
        var result: Id? = null
        uploadMediaFile(
            uri = uri,
            type = type,
            spaceId = SpaceId(targetSpaceId)
        ) { fileId ->
            result = fileId
        }
        return result
    }

    /**
     * Links a newly created object to a destination object.
     * Opens the destination, adds a link block to the new object, then closes.
     */
    private suspend fun linkObjectToDestination(
        objectToLink: Id,
        destinationObjectId: Id,
        spaceId: SpaceId
    ) {
        try {
            addBackLinkToObject.async(
                AddBackLinkToObject.Params(
                    objectToLink = objectToLink,
                    objectToPlaceLink = destinationObjectId,
                    saveAsLastOpened = false,
                    spaceId = spaceId
                )
            )
            Timber.d("Successfully linked object $objectToLink to destination $destinationObjectId")
        } catch (e: Exception) {
            Timber.e(e, "Error linking object to destination")
            // Don't throw - the object was created successfully, link is optional
        }
    }

    /**
     * Adds an object to a collection.
     */
    private suspend fun addToCollection(collectionId: Id, objectId: Id) {
        try {
            addObjectToCollection.async(
                AddObjectToCollection.Params(
                    ctx = collectionId,
                    targets = listOf(objectId)
                )
            ).fold(
                onSuccess = {
                    Timber.d("Successfully added object $objectId to collection $collectionId")
                },
                onFailure = { e ->
                    Timber.e(e, "Error adding object to collection")
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error adding object to collection")
            // Don't throw - the object was created successfully, adding to collection is optional
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
            is SharingScreenState.ObjectSelection -> {
                // Go back to space selection
                selectedDataSpace = null
                selectedDestinationObjectIds.clear()
                objectSearchQuery = ""
                commentText = ""
                cachedTypesMap = emptyMap()
                updateSpaceSelectionState()
                true
            }
            else -> false
        }
    }

    // region Private Methods

    private fun updateSpaceSelectionState() {
        val content = sharedContent ?: return

        // Don't transition from Loading until spaces are loaded
        if (allSpaces.isEmpty() && _screenState.value is SharingScreenState.Loading) {
            return
        }

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
            spaces = spacesWithSelection,
            searchQuery = spaceSearchQuery,
            sharedContent = content,
            commentText = commentText
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

        // Fetch types once when entering the space, then search
        viewModelScope.launch {
            val spaceId = SpaceId(space.targetSpaceId)
            cachedTypesMap = fetchObjectTypesForSpace(spaceId)
            searchObjectsAndChatsInSpace(space)
        }
    }

    /**
     * Searches for both regular objects and chat objects in the given space.
     * Uses cached types map (fetched once in navigateToObjectSelection).
     * Preserves selection state across search queries.
     */
    private fun searchObjectsAndChatsInSpace(space: SelectableSpaceView) {
        viewModelScope.launch {
            val spaceId = SpaceId(space.targetSpaceId)

            // Search regular objects and chats using cached types map
            val objects = searchRegularObjects(spaceId, cachedTypesMap)
            val chats = searchChatObjects(spaceId, cachedTypesMap)

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
     * Fetches all object types from the given space.
     * Returns a map of type ID -> ObjectWrapper.Type for quick lookup.
     */
    private suspend fun fetchObjectTypesForSpace(spaceId: SpaceId): Map<Id, ObjectWrapper.Type> {
        val filters = buildList {
            addAll(buildDeletedFilter())
            add(buildTemplateFilter())
            add(
                DVFilter(
                    relation = Relations.LAYOUT,
                    condition = DVFilterCondition.EQUAL,
                    value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
                )
            )
            add(
                DVFilter(
                    relation = Relations.UNIQUE_KEY,
                    condition = DVFilterCondition.NOT_EMPTY
                )
            )
        }

        val keys = ObjectSearchConstants.defaultKeysObjectType

        val params = SearchObjects.Params(
            space = spaceId,
            filters = filters,
            sorts = emptyList(),
            keys = keys,
            limit = 0
        )

        return try {
            val results = searchObjects(params).getOrNull() ?: emptyList()
            results.mapNotNull { obj ->
                obj.map.mapToObjectWrapperType()?.let { type ->
                    type.id to type
                }
            }.toMap()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching object types for space")
            emptyMap()
        }
    }

    /**
     * Checks if the Note object type exists and is valid (not deleted/archived) in the given space.
     * Returns true if NOTE type is available, false if it should fall back to PAGE.
     */
    private suspend fun isNoteTypeAvailable(spaceId: SpaceId): Boolean {
        val filters = buildList {
            add(
                DVFilter(
                    relation = Relations.UNIQUE_KEY,
                    condition = DVFilterCondition.EQUAL,
                    value = ObjectTypeUniqueKeys.NOTE
                )
            )
            add(
                DVFilter(
                    relation = Relations.LAYOUT,
                    condition = DVFilterCondition.EQUAL,
                    value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
                )
            )
        }

        val params = SearchObjects.Params(
            space = spaceId,
            filters = filters,
            sorts = emptyList(),
            keys = listOf(Relations.ID, Relations.UNIQUE_KEY, Relations.IS_DELETED, Relations.IS_ARCHIVED),
            limit = 1
        )

        return try {
            val results = searchObjects(params).getOrNull() ?: emptyList()
            val noteObj = results.firstOrNull()
            noteObj != null && noteObj.isArchived != true && noteObj.isDeleted != true
        } catch (e: Exception) {
            Timber.e(e, "Error checking NOTE type availability")
            false // Fallback to PAGE on error
        }
    }

    /**
     * Searches for regular objects (non-chat) in the space.
     */
    private suspend fun searchRegularObjects(
        spaceId: SpaceId,
        typesMap: Map<Id, ObjectWrapper.Type>
    ): List<SelectableObjectView> {
        val filters = buildObjectSearchFilters()
        val sorts = listOf(
            DVSort(
                relationKey = Relations.LAST_OPENED_DATE,
                type = DVSortType.DESC,
                relationFormat = RelationFormat.DATE,
                includeTime = true
            )
        )

        val params = SearchObjects.Params(
            space = spaceId,
            filters = filters,
            sorts = sorts,
            fulltext = objectSearchQuery,
            keys = ObjectSearchConstants.defaultKeys,
            limit = 200
        )

        return try {
            val objects = searchObjects(params).getOrNull() ?: emptyList()
            objects.map { obj ->
                // Get type from the map using object's type relation
                val typeId = obj.type.firstOrNull()
                val objType = typeId?.let { typesMap[it] }
                SelectableObjectView(
                    id = obj.id,
                    name = fieldParser.getObjectPluralName(obj),
                    icon = obj.objectIcon(
                        builder = urlBuilder,
                        objType = objType
                    ),
                    typeName = objType?.name.orEmpty(),
                    isSelected = obj.id in selectedDestinationObjectIds,
                    isCollection = obj.layout == ObjectType.Layout.COLLECTION
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching objects in space")
            emptyList()
        }
    }

    /**
     * Finds the chat object ID in a space by searching for CHAT_DERIVED layout objects.
     * Used as fallback when SpaceView.chatId is not available (e.g., for 1-1 spaces).
     *
     * @param spaceId The space to search in
     * @return The chat ID if found, null otherwise
     */
    private suspend fun findChatIdInSpace(spaceId: SpaceId): Id? {
        val filters = buildList {
            add(
                DVFilter(
                    relation = Relations.LAYOUT,
                    condition = DVFilterCondition.EQUAL,
                    value = ObjectType.Layout.CHAT_DERIVED.code.toDouble()
                )
            )
            addAll(buildDeletedFilter())
            add(buildTemplateFilter())
        }

        val params = SearchObjects.Params(
            space = spaceId,
            filters = filters,
            sorts = emptyList(),
            keys = listOf(Relations.ID),
            limit = 1
        )

        return try {
            searchObjects(params).getOrNull()?.firstOrNull()?.id
        } catch (e: Exception) {
            Timber.e(e, "Error searching for chat in space")
            null
        }
    }

    /**
     * Searches for chat objects (CHAT_DERIVED layout) in the space.
     */
    private suspend fun searchChatObjects(
        spaceId: SpaceId,
        typesMap: Map<Id, ObjectWrapper.Type>
    ): List<SelectableObjectView> {
        val filters = buildList {
            addAll(buildDeletedFilter())
            add(buildTemplateFilter())
            add(
                DVFilter(
                    relation = Relations.LAYOUT,
                    condition = DVFilterCondition.EQUAL,
                    value = ObjectType.Layout.CHAT_DERIVED.code.toDouble()
                )
            )
        }

        val sorts = listOf(
            DVSort(
                relationKey = Relations.LAST_MODIFIED_DATE,
                type = DVSortType.DESC,
                relationFormat = RelationFormat.DATE,
                includeTime = true
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
                Relations.TYPE,
                Relations.ICON_EMOJI,
                Relations.ICON_IMAGE,
                Relations.ICON_OPTION
            )
        )

        return try {
            val objects = searchObjects(params).getOrNull() ?: emptyList()
            objects.map { obj ->
                val typeId = obj.type.firstOrNull()
                val objType = typeId?.let { typesMap[it] }
                SelectableObjectView(
                    id = obj.id,
                    name = fieldParser.getObjectPluralName(obj),
                    icon = obj.objectIcon(
                        builder = urlBuilder,
                        objType = objType
                    ),
                    typeName = objType?.name ?: "Chat",
                    isSelected = obj.id in selectedDestinationObjectIds,
                    isChatOption = true
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching chat objects in space")
            emptyList()
        }
    }

    private fun buildObjectSearchFilters(): List<DVFilter> = buildList {
        addAll(buildDeletedFilter())
        add(buildTemplateFilter())
        add(
            buildLayoutFilter(
                layouts = listOf(
                    ObjectType.Layout.BASIC,
                    ObjectType.Layout.NOTE,
                    ObjectType.Layout.PROFILE,
                    ObjectType.Layout.TODO,
                    ObjectType.Layout.COLLECTION
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
     * Falls back to searching for chat objects if chatId is not directly available.
     */
    private suspend fun sendToChat(space: SelectableSpaceView) {
        val content = sharedContent ?: return
        val spaceId = SpaceId(space.targetSpaceId)

        // Try to get chatId from SpaceView, fallback to search for 1-1 spaces
        val chatId = space.chatId ?: findChatIdInSpace(spaceId)
        if (chatId == null) {
            Timber.e("No chat found in space ${space.name}")
            _screenState.value = SharingScreenState.Error(
                message = "No chat found in this space",
                canRetry = false
            )
            return
        }

        _screenState.value = SharingScreenState.Sending(progress = 0f, message = "Sending...")

        try {
            sendContentToChat(chatId, spaceId, content)

            _screenState.value = SharingScreenState.Success(
                createdObjectId = null,
                spaceName = space.name,
                canOpenObject = false
            )

            _commands.emit(
                SharingCommand.Dismiss
            )

        } catch (e: Exception) {
            Timber.e(e, "Error sending to chat")
            _screenState.value = SharingScreenState.Error(
                message = e.msg(),
                canRetry = true
            )
        }
    }

    @Throws
    private suspend fun sendContentToChat(chatId: Id, spaceId: SpaceId, content: SharedContent) {
        when (content) {
            is SharedContent.Text -> {
                // Send comment FIRST as separate message if provided
                if (commentText.isNotBlank()) {
                    sendChatMessage(chatId, commentText, emptyList())
                }

                // Then send the shared text (truncated if > 2000 chars)
                val truncatedText = content.text.take(SharedContent.MAX_CHAT_MESSAGE_LENGTH)
                sendChatMessage(chatId, truncatedText, emptyList())
            }

            is SharedContent.Url -> {
                // Create bookmark object and send as attachment
                createBookmarkForChat(url = content.url, spaceId = spaceId, chatId = chatId)
            }

            is SharedContent.SingleMedia -> {
                // Upload and send with caption
                uploadMediaFile(
                    uri = content.uri,
                    type = content.type,
                    spaceId = spaceId
                ) { fileId ->
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
        }
    }

    @Throws
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
                // Send comment FIRST as separate message if provided (per spec)
                if (commentText.isNotBlank()) {
                    sendChatMessage(chatId, commentText, emptyList())
                }

                // Then send the bookmark as attachment
                val bookmarkId = obj.id
                val attachment = Chat.Message.Attachment(
                    target = bookmarkId,
                    type = Chat.Message.Attachment.Type.Link
                )
                sendChatMessage(chatId, "", listOf(attachment))
            },
            onFailure = { e ->
                Timber.e(e, "Error creating bookmark from URL")
            }
        )
    }

    private suspend fun uploadMediaFile(
        uri: String,
        type: SharedContent.MediaType,
        spaceId: SpaceId,
        onSuccess: suspend (Id) -> Unit
    ) {
        val path = try {
            fileSharer.getPath(uri)
        } catch (e: Exception) {
            Timber.e(e, "Error getting path for URI: $uri")
            return
        }

        if (path == null) {
            Timber.e("Path is null for URI: $uri")
            return
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
                val objectId = uploadMediaAndGetId(content.uri, content.type, targetSpaceId)
                if (objectId != null) {
                    handleObjectCreationSuccess(objectId, space, targetSpaceId)
                } else {
                    _screenState.value = SharingScreenState.Error(
                        message = "Failed to upload file",
                        canRetry = true
                    )
                }
            }
            is SharedContent.MultipleMedia -> {
                val uploadedIds = content.uris.mapNotNull { uri ->
                    uploadMediaAndGetId(uri, content.type, targetSpaceId)
                }
                handleUploadResults(uploadedIds, content.uris.size, space, targetSpaceId)
            }
        }
    }

    private suspend fun proceedWithNoteCreation(
        text: String,
        targetSpaceId: Id,
        onSuccess: suspend (Id) -> Unit
    ) {
        val objectId = createNoteAndGetId(text, targetSpaceId)
        if (objectId != null) {
            onSuccess(objectId)
        } else {
            _screenState.value = SharingScreenState.Error(
                message = "Failed to create object",
                canRetry = true
            )
        }
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

    private suspend fun handleUploadResults(
        uploadedIds: List<Id>,
        total: Int,
        space: SelectableSpaceView,
        targetSpaceId: Id
    ) {
        val successCount = uploadedIds.size
        val failCount = total - successCount

        when {
            successCount == total -> {
                handleObjectCreationSuccess(uploadedIds.first(), space, targetSpaceId)
            }
            successCount > 0 -> {
                _commands.emit(
                    SharingCommand.ShowToast("$failCount of $total files failed to upload")
                )
                handleObjectCreationSuccess(uploadedIds.first(), space, targetSpaceId)
            }
            else -> {
                _screenState.value = SharingScreenState.Error(
                    message = "Failed to upload all $total files",
                    canRetry = true
                )
            }
        }
    }

    private suspend fun handleObjectCreationSuccess(
        objectId: Id,
        space: SelectableSpaceView,
        targetSpaceId: Id
    ) {
        val content = sharedContent ?: return
        val currentSpaceId = spaceManager.get()

        _screenState.value = SharingScreenState.Success(
            createdObjectId = objectId,
            spaceName = space.name,
            canOpenObject = targetSpaceId == currentSpaceId
        )

        // Show Snackbar with "Open" action to navigate to the created object
        _commands.emit(
            SharingCommand.ShowSnackbarWithOpenAction(
                contentType = content,
                destinationName = space.name,
                spaceName = null,  // No extra space context needed for "added to space"
                objectId = objectId,
                spaceId = targetSpaceId,
                isChat = false
            )
        )
        _commands.emit(SharingCommand.Dismiss)
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
        private val fieldParser: FieldParser,
        private val addBackLinkToObject: AddBackLinkToObject,
        private val addObjectToCollection: AddObjectToCollection
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
                fieldParser = fieldParser,
                addBackLinkToObject = addBackLinkToObject,
                addObjectToCollection = addObjectToCollection
            ) as T
        }
    }

    // endregion
}