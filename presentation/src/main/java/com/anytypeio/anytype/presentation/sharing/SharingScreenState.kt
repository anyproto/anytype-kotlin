package com.anytypeio.anytype.presentation.sharing

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.sharing.SharingScreenState.ObjectSelection.Companion.MAX_SELECTION_COUNT

/**
 * Represents the different screen states in the sharing extension flow.
 * This sealed class implements a state machine pattern for navigating between:
 * - Space selection (initial screen)
 * - Object selection (for data spaces - Flow 2 & 3)
 * - Progress and result states
 */
sealed class SharingScreenState {

    /**
     * Initial loading state while spaces are being fetched.
     */
    data object Loading : SharingScreenState()

    /**
     * Empty state when no spaces are available.
     */
    data object NoSpaces : SharingScreenState()

    /**
     * Initial screen showing the grid of available spaces.
     * For chat/one-to-one spaces, shows comment input inline when selected.
     *
     * @property spaces List of spaces available for selection
     * @property searchQuery Current search filter text
     * @property sharedContent The content being shared
     * @property commentText Comment text for chat spaces (shown when chat space is selected)
     */
    data class SpaceSelection(
        val spaces: List<SelectableSpaceView>,
        val searchQuery: String = "",
        val sharedContent: SharedContent,
        val commentText: String = ""
    ) : SharingScreenState()

    /**
     * Object selection screen shown after selecting a data space.
     * Displays both regular objects and chat objects (discovered dynamically).
     * Supports multi-selection of up to [MAX_SELECTION_COUNT] destinations.
     *
     * @property space The selected data space
     * @property objects List of regular objects in the space for selection
     * @property chatObjects List of chat objects (CHAT_DERIVED layout) in the space
     * @property searchQuery Current search filter text
     * @property selectedObjectIds Set of selected destination object IDs (empty = create as new)
     * @property commentText Comment text for chat destinations
     * @property sharedContent The content being shared
     */
    data class ObjectSelection(
        val space: SelectableSpaceView,
        val objects: List<SelectableObjectView>,
        val chatObjects: List<SelectableObjectView> = emptyList(),
        val searchQuery: String = "",
        val selectedObjectIds: Set<Id> = emptySet(),
        val commentText: String = "",
        val sharedContent: SharedContent
    ) : SharingScreenState() {

        /**
         * Returns true if any selected item is a chat.
         * Used to determine whether to show the comment input field.
         */
        val hasAnyChatSelected: Boolean
            get() = chatObjects.any { it.id in selectedObjectIds }

        companion object {
            const val MAX_SELECTION_COUNT = 5
        }
    }

    /**
     * Progress state while content is being uploaded/sent.
     *
     * @property progress Upload progress from 0.0 to 1.0
     * @property message Current status message to display
     */
    data class Sending(
        val progress: Float = 0f,
        val message: String = ""
    ) : SharingScreenState()

    /**
     * Success state after content has been successfully shared.
     *
     * @property createdObjectId ID of the created object (null for chat messages)
     * @property spaceName Name of the space where content was shared
     * @property canOpenObject Whether the created object can be opened
     */
    data class Success(
        val createdObjectId: Id? = null,
        val spaceName: String,
        val canOpenObject: Boolean = false
    ) : SharingScreenState()

    /**
     * Error state when sharing fails.
     *
     * @property message Error message to display
     * @property canRetry Whether the operation can be retried
     */
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : SharingScreenState()
}

/**
 * Commands emitted by the ViewModel for UI-level actions.
 */
sealed class SharingCommand {
    /**
     * Dismiss the sharing bottom sheet.
     */
    data object Dismiss : SharingCommand()

    /**
     * Show a toast message.
     */
    data class ShowToast(val message: String) : SharingCommand()

    /**
     * Show Snackbar with message and "Open" action.
     * Used after successfully adding content to a chat, space, or linking to an object.
     *
     * @property contentType The type of content that was shared (for message formatting)
     * @property destinationName The name of the destination (chat/object/space name)
     * @property spaceName Optional space name for context (used when linking to an object)
     * @property objectId The ID of the object/chat to navigate to when "Open" is clicked
     * @property spaceId The space ID containing the target
     * @property isChat Whether the target is a chat (determines navigation destination)
     * @property isCollection Whether the target is a collection (uses "added to" message format)
     */
    data class ShowSnackbarWithOpenAction(
        val contentType: SharedContent,
        val destinationName: String,
        val spaceName: String? = null,
        val objectId: Id,
        val spaceId: Id,
        val isChat: Boolean,
        val isCollection: Boolean = false
    ) : SharingCommand()
}
