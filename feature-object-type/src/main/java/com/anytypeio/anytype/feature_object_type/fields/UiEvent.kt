package com.anytypeio.anytype.feature_object_type.fields

import com.anytypeio.anytype.core_models.Id

sealed class FieldEvent {

    data object OnDismissScreen : FieldEvent()

    data object OnEditPropertyScreenDismiss : FieldEvent()

    data object OnBackClick : FieldEvent()

    data class OnFieldItemClick(val item: UiFieldsListItem) : FieldEvent()

    sealed class FieldItemMenu : FieldEvent() {
        data class OnRemoveFromTypeClick(val id: Id) : FieldItemMenu()
        data class OnMoveToBinClick(val id: Id) : FieldItemMenu()
        data class OnAddLocalToTypeClick(val item: UiFieldsListItem) : FieldItemMenu()
    }

    sealed class FieldLocalInfo : FieldEvent() {
        data object OnDismiss : FieldLocalInfo()
    }

    sealed class Section : FieldEvent() {
        data object OnAddToSidebarIconClick : Section()
        data object OnLocalInfoClick : Section()
    }

    sealed class DragEvent : FieldEvent() {
        data class OnMove(val fromKey: String, val toKey: String) : DragEvent()
        data object OnDragEnd : DragEvent()
    }

    sealed class EditProperty : FieldEvent() {
        data class OnPropertyNameUpdate(val name: String) : EditProperty()
        data object OnSaveButtonClicked : EditProperty()
        data object OnLimitTypesClick : EditProperty()
        data object OnLimitTypesDismiss : EditProperty()
    }
}
