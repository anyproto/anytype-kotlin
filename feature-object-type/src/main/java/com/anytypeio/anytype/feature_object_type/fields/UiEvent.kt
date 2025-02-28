package com.anytypeio.anytype.feature_object_type.fields

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat

sealed class FieldEvent {

    data object OnFieldEditScreenDismiss : FieldEvent()
    data object OnAddFieldScreenDismiss : FieldEvent()

    data class OnFieldItemClick(val item: UiFieldsListItem) : FieldEvent()

    data class OnAddToHeaderFieldClick(
        val item: UiAddFieldItem
    ) : FieldEvent()

    data class OnAddToSidebarFieldClick(
        val item: UiAddFieldItem
    ) : FieldEvent()

    data class OnSaveButtonClicked(
        val name: String,
        val format: RelationFormat,
        val limitObjectTypes: List<Id>
    ) : FieldEvent()

    data object OnChangeTypeClick : FieldEvent()
    data object OnLimitTypesClick : FieldEvent()

    sealed class FieldItemMenu : FieldEvent() {
        data class OnDeleteFromTypeClick(val item: UiFieldsListItem) : FieldItemMenu()
        data class OnRemoveLocalClick(val item: UiFieldsListItem) : FieldItemMenu()
        data class OnAddLocalToTypeClick(val item: UiFieldsListItem) : FieldItemMenu()
    }

    sealed class FieldLocalInfo : FieldEvent() {
        data object OnDismiss : FieldLocalInfo()
    }

    sealed class Section : FieldEvent() {
        data object OnAddToHeaderIconClick : Section()
        data object OnAddToSidebarIconClick : Section()
        data object OnLocalInfoClick : Section()
    }

    sealed class DragEvent : FieldEvent() {
        data class OnMove(val fromKey: String, val toKey: String) : DragEvent()
        data object OnDragEnd : DragEvent()
    }

    data class OnAddFieldSearchQueryChanged(val query: String) : FieldEvent()
}
