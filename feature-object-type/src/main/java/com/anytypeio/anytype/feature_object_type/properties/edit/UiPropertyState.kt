package com.anytypeio.anytype.feature_object_type.properties.edit

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.feature_object_type.fields.UiFieldObjectItem
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem

sealed class UiEditPropertyState {
    data object Hidden : UiEditPropertyState()
    sealed class Visible : UiEditPropertyState() {

        data class Edit(
            val id: Id,
            val key: Key,
            val name: String,
            val formatName: String,
            val formatIcon: Int?,
            val format: RelationFormat,
            val limitObjectTypes: List<UiFieldObjectItem> = emptyList()
        ) : Visible()

        data class New(
            val name: String,
            val formatName: String,
            val formatIcon: Int?,
            val format: RelationFormat,
            val limitObjectTypes: List<UiFieldObjectItem> = emptyList()
        ) : Visible()

        data class View(
            val id: Id,
            val key: Key,
            val name: String,
            val formatName: String,
            val formatIcon: Int?,
            val format: RelationFormat,
            val limitObjectTypes: List<UiFieldObjectItem> = emptyList()
        ) : Visible()
    }
}

sealed class UiPropertyLimitObjectTypesState {
    data class Edit(
        val limitObjectTypes: List<UiObjectsListItem>
    ) : UiPropertyLimitObjectTypesState()

    data class View(
        val limitObjectTypes: List<UiObjectsListItem>
    ) : UiPropertyLimitObjectTypesState()
}