package com.anytypeio.anytype.feature_object_type.properties

import com.anytypeio.anytype.feature_object_type.fields.UiPropertyItemState
import com.anytypeio.anytype.core_ui.R

object PropertyStub {

    fun getPropertyItem(): UiPropertyItemState {
        return UiPropertyItemState.Text(
            id = "dummyId1",
            key = "dummyKey",
            name = "Property Text",
            formatName = "Text",
            formatIcon = R.drawable.ic_relation_format_date_small
        )
    }

    fun getListOfPropertyItems(): List<UiPropertyItemState> {
        return listOf(
            UiPropertyItemState.Text(
                id = "dummyId1",
                key = "dummyKey1",
                name = "Property Text",
                formatName = "Text",
                formatIcon = R.drawable.ic_relation_format_date_small
            ),
            UiPropertyItemState.Number(
                id = "dummyId2",
                key = "dummyKey2",
                name = "Property Number",
                formatName = "Number",
                formatIcon = R.drawable.ic_relation_format_number_small
            ),
            UiPropertyItemState.Date(
                id = "dummyId3",
                key = "dummyKey3",
                name = "Property Date",
                formatName = "Date",
                formatIcon = R.drawable.ic_relation_format_date_small
            ),
            UiPropertyItemState.Object(
                id = "dummyId4",
                key = "dummyKey4",
                name = "Property Object",
                formatName = "Object",
                formatIcon = R.drawable.ic_relation_format_object_small,
                limitObjectTypesCount = 3
            ),
        )
    }
}