package com.anytypeio.anytype.feature_properties.edit

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesItem
import com.anytypeio.anytype.presentation.objects.ObjectIcon

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
            val limitObjectTypes: List<UiPropertyLimitTypeItem> = emptyList(),
            val isPossibleToUnlinkFromType: Boolean
        ) : Visible()

        data class New(
            val name: String,
            val formatName: String,
            val formatIcon: Int?,
            val format: RelationFormat,
            val limitObjectTypes: List<UiPropertyLimitTypeItem> = emptyList()
        ) : Visible()

        data class View(
            val id: Id,
            val key: Key,
            val name: String,
            val formatName: String,
            val formatIcon: Int?,
            val format: RelationFormat,
            val limitObjectTypes: List<UiPropertyLimitTypeItem> = emptyList(),
            val isPossibleToUnlinkFromType: Boolean
        ) : Visible()
    }
}

data class UiPropertyLimitTypeItem(
    val id: Id, val key: Key, val title: String, val icon: ObjectIcon
)

sealed class UiPropertyFormatsListState {
    data class Visible(
        val items: List<UiEditTypePropertiesItem.Format>
    ) : UiPropertyFormatsListState()

    data object Hidden : UiPropertyFormatsListState()
}