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
}
