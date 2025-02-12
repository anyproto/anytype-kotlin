package com.anytypeio.anytype.feature_object_type.fields

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat

sealed class FieldEvent {

    data object OnFieldEditScreenDismiss : FieldEvent()

    data class OnFieldItemClick(val item: UiFieldsListItem) : FieldEvent()

    data class OnSaveButtonClicked(
        val name: String,
        val format: RelationFormat,
        val limitObjectTypes: List<Id>
    ) : FieldEvent()

    data object OnChangeTypeClick : FieldEvent()
    data object OnLimitTypesClick : FieldEvent()

    data class FieldOrderChanged(val items: List<UiFieldsListItem>) : FieldEvent()

    sealed class FieldItemMenu : FieldEvent() {
        data class OnDeleteFromTypeClick(val item: UiFieldsListItem) : FieldItemMenu()
        data class OnRemoveLocalClick(val item: UiFieldsListItem) : FieldItemMenu()
        data class OnAddLocalToTypeClick(val item: UiFieldsListItem) : FieldItemMenu()
    }

    sealed class FieldLocalInfo : FieldEvent() {
        data object OnDismiss : FieldLocalInfo()
    }

    sealed class Section : FieldEvent() {
        data object OnAddIconClick : Section()
        data object OnLocalInfoClick : Section()
    }
}
