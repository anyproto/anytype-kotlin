package com.anytypeio.anytype.feature_object_type.ui.fields

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat

sealed class FieldEvent {
    data class OnSaveButtonClicked(
        val name: String,
        val format: RelationFormat,
        val limitObjectTypes: List<Id>
    ) : FieldEvent()
    data object OnChangeTypeClick : FieldEvent()
    data object OnLimitTypesClick : FieldEvent()
}
