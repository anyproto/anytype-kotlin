package com.anytypeio.anytype.presentation.sharing

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

/**
 * Represents a space that can be selected in the sharing flow.
 * Supports both single and multi-selection depending on the space type.
 *
 * @property id The unique identifier of the space
 * @property targetSpaceId The target space ID for operations
 * @property name Display name of the space
 * @property icon The space icon to display
 * @property uxType The UX type determining the sharing flow
 * @property chatId The chat ID if this space has chat functionality (null for pure data spaces)
 * @property isSelected Whether this space is currently selected
 */
data class SelectableSpaceView(
    val id: Id,
    val targetSpaceId: Id,
    val name: String,
    val icon: SpaceIconView,
    val uxType: SpaceUxType?,
    val chatId: Id?,
    val isSelected: Boolean = false
) {
    /**
     * Determines which sharing flow should be used for this space.
     */
    val flowType: SharingFlowType
        get() = when (uxType) {
            SpaceUxType.CHAT, SpaceUxType.ONE_TO_ONE -> SharingFlowType.CHAT
            else -> SharingFlowType.DATA  // DATA, STREAM, or null
        }
}

/**
 * Represents an object that can be selected as a destination in the sharing flow.
 * Used in Data Space flow for selecting target objects or chats.
 *
 * @property id The unique identifier of the object
 * @property name Display name of the object
 * @property icon The object icon to display
 * @property typeName Human-readable type name (e.g., "Page", "Note", "Chat")
 * @property isSelected Whether this object is currently selected
 * @property isChatOption True if this represents a chat object (CHAT_DERIVED layout)
 * @property isCollection True if this represents a collection object
 */
data class SelectableObjectView(
    val id: Id,
    val name: String,
    val icon: ObjectIcon = ObjectIcon.None,
    val typeName: String,
    val isSelected: Boolean = false,
    val isChatOption: Boolean = false,
    val isCollection: Boolean = false
)

/**
 * Defines the two sharing flows based on space type.
 */
enum class SharingFlowType {
    /**
     * Flow 1: Pure chat space (SpaceUxType.CHAT or ONE_TO_ONE).
     * - Content is sent directly as chat messages
     * - Multi-select spaces allowed
     * - Comment becomes message or caption
     */
    CHAT,

    /**
     * Flow 2: Data space (SpaceUxType.DATA or STREAM).
     * - Content is created as objects in the space
     * - Single space selection
     * - Dynamically discovers chat objects for "Send to chat" option
     */
    DATA
}
