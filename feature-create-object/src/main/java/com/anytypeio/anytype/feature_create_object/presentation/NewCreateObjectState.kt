package com.anytypeio.anytype.feature_create_object.presentation

import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ui.ObjectIcon

/**
 * UI state for the create object screen.
 * Manages the list of object types, search query, and visibility of sections.
 *
 * @param objectTypes Full list of available object types
 * @param filteredObjectTypes List of object types filtered by search query
 * @param searchQuery Current search query entered by user
 * @param isLoading Whether data is currently being loaded
 * @param error Error message if loading failed, null otherwise
 * @param showMediaSection Whether to display the media attachment options
 * @param showAttachExisting Whether to display the attach existing object option
 */
data class NewCreateObjectState(
    val objectTypes: List<ObjectTypeItem> = emptyList(),
    val filteredObjectTypes: List<ObjectTypeItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
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
