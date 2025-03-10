package com.anytypeio.anytype.feature_object_type.fields

sealed class FieldEvent {

    data object OnEditPropertyScreenDismiss : FieldEvent()

    data class OnFieldItemClick(val item: UiFieldsListItem) : FieldEvent()

    sealed class FieldItemMenu : FieldEvent() {
        data class OnDeleteFromTypeClick(val item: UiFieldsListItem) : FieldItemMenu()
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
}
