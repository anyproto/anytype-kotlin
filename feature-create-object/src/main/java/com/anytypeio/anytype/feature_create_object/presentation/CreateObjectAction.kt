package com.anytypeio.anytype.feature_create_object.presentation

import com.anytypeio.anytype.core_models.Key

/**
 * Actions that can be performed from the create object screen.
 * These actions are triggered by user interactions and handled by the parent component.
 */
sealed interface CreateObjectAction {

    // Media actions
    data object SelectPhotos : CreateObjectAction
    data object TakePhoto : CreateObjectAction
    data object SelectFiles : CreateObjectAction

    // Object actions
    data object AttachExistingObject : CreateObjectAction
    data class CreateObjectOfType(val typeKey: Key, val typeName: String) : CreateObjectAction

    // Search
    data class UpdateSearch(val query: String) : CreateObjectAction

    // Error handling
    data object Retry : CreateObjectAction
}
