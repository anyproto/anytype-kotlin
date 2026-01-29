package com.anytypeio.anytype.feature_properties.edit

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesItem
import com.anytypeio.anytype.core_models.ui.ObjectIcon

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
            val isPossibleToUnlinkFromType: Boolean,
            val showLimitTypes: Boolean
        ) : Visible()

        data class New(
            val name: String,
            val formatName: String,
            val formatIcon: Int?,
            val format: RelationFormat,
            val limitObjectTypes: List<UiPropertyLimitTypeItem> = emptyList(),
            val selectedLimitTypeIds: List<Id> = emptyList(),
            val showLimitTypes: Boolean
        ) : Visible()

        data class View(
            val id: Id,
            val key: Key,
            val name: String,
            val formatName: String,
            val formatIcon: Int?,
            val format: RelationFormat,
            val limitObjectTypes: List<UiPropertyLimitTypeItem> = emptyList(),
            val isPossibleToUnlinkFromType: Boolean,
            val showLimitTypes: Boolean
        ) : Visible()
    }
}

sealed class UiPropertyLimitTypesScreen {
    data class Visible(
        val items: List<UiPropertyLimitTypeItem>
    ) : UiPropertyLimitTypesScreen()

    data object Hidden : UiPropertyLimitTypesScreen()
}

data class UiPropertyLimitTypeItem(
    val id: Id,
    val name: String,
    val icon: ObjectIcon? = null,
    val uniqueKey: Key? = null,
    val number: Int? = null
)

sealed class UiPropertyFormatsListState {
    data class Visible(
        val items: List<UiEditTypePropertiesItem.Format>
    ) : UiPropertyFormatsListState()

    data object Hidden : UiPropertyFormatsListState()
}