package com.anytypeio.anytype.feature_create_object.presentation

import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ui.ObjectIcon

/**
 * UI state for the create object screen.
 * Manages the list of object types, search query, and visibility of sections.
 */
data class CreateObjectState(
    val objectTypes: List<ObjectTypeItem> = emptyList(),
    val filteredObjectTypes: List<ObjectTypeItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showMediaSection: Boolean = true,
    val showAttachExisting: Boolean = true
)

/**
 * Represents an object type item in the create object list.
 *
 * @param typeKey The unique key identifying the object type (e.g., "ot-page", "ot-note")
 * @param name The display name of the object type
 * @param icon The visual icon representation for the object type
 */
data class ObjectTypeItem(
    val typeKey: Key,
    val name: String,
    val icon: ObjectIcon
)
