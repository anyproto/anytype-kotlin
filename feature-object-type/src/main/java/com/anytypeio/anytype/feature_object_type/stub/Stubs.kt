package com.anytypeio.anytype.feature_object_type.stub

import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.feature_object_type.fields.UiFieldObjectItem
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem
import com.anytypeio.anytype.presentation.objects.ObjectIcon

fun createDummyFieldItem(): UiFieldsListItem.FieldItem {
    return UiFieldsListItem.FieldItem(
        id = "dummyId",
        fieldKey = "dummyKey",
        fieldTitle = "Field Title",
        format = RelationFormat.OBJECT,
        limitObjectTypes = listOf(
            UiFieldObjectItem(
                id = "dummyObjectId1",
                key = "dummyKey1",
                title = "Dummy Object Type 1",
                icon = ObjectIcon.Empty.ObjectType,

                ),
            UiFieldObjectItem(
                id = "dummyObjectId1",
                key = "dummyKey1",
                title = "Dummy Object Type 1",
                icon = ObjectIcon.Empty.ObjectType,

                ),
            UiFieldObjectItem(
                id = "dummyObjectId1",
                key = "dummyKey1",
                title = "Dummy Object Type 1",
                icon = ObjectIcon.Empty.ObjectType,

                ),
        ),
        canDrag = true,
        canDelete = true
    )
}