package com.anytypeio.anytype.feature_object_type.ui

import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.feature_object_type.fields.UiFieldObjectItem
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem
import com.anytypeio.anytype.presentation.objects.ObjectIcon

fun createDummyFieldDraggableItem(isEditableField: Boolean = true): UiFieldsListItem.Item.Draggable {
    return UiFieldsListItem.Item.Draggable(
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
        isEditableField = isEditableField,
        canDelete = true
    )
}