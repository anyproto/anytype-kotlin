package com.anytypeio.anytype.core_ui.lists.objects.stubs

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem

val StubVerticalItems = listOf(
    UiObjectsListItem.Item(
        id = "1",
        name = "Task Object",
        space = SpaceId("space1"),
        type = "type1",
        typeName = "Task",
        createdBy = "by Joseph Wolf",
        layout = ObjectType.Layout.TODO,
        icon = ObjectIcon.Task(isChecked = true)
    ),
    UiObjectsListItem.Item(
        id = "2",
        name = "Page Object",
        space = SpaceId("space2"),
        type = "type2",
        typeName = "Page",
        createdBy = "by Mike Long",
        layout = ObjectType.Layout.BASIC,
        icon = ObjectIcon.Empty.Page
    ),
    UiObjectsListItem.Item(
        id = "3",
        name = "File Object",
        space = SpaceId("space3"),
        type = "type3",
        typeName = "File",
        createdBy = "by John Doe",
        layout = ObjectType.Layout.FILE,
        icon = ObjectIcon.File(
            mime = "image/png",
            fileName = "test_image.png"
        )
    )
)