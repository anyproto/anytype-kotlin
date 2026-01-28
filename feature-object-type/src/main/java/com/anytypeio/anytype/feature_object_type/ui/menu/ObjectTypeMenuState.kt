package com.anytypeio.anytype.feature_object_type.ui.menu

import com.anytypeio.anytype.core_models.ui.ObjectIcon

/**
 * UI state for ObjectType menu bottom sheet
 */
data class UiObjectTypeMenuState(
    val isVisible: Boolean = false,
    val isPinned: Boolean = false,
    val canDelete: Boolean = true,
    val icon: ObjectIcon = ObjectIcon.None,
    val isDescriptionFeatured: Boolean = false,
    val canEditDetails: Boolean = true
) {
    companion object {
        val Hidden = UiObjectTypeMenuState(isVisible = false)
    }
}

/**
 * Events triggered from ObjectType menu
 */
sealed class ObjectTypeMenuEvent {
    data object OnDismiss : ObjectTypeMenuEvent()
    data object OnIconClick : ObjectTypeMenuEvent()
    data object OnDescriptionClick : ObjectTypeMenuEvent()
    data object OnToBinClick : ObjectTypeMenuEvent()
    data object OnPinToggleClick : ObjectTypeMenuEvent()
}
