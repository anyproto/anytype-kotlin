package com.anytypeio.anytype.core_ui.lists.objects.stubs

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem

val StubVerticalItems = listOf(
    UiObjectsListItem.Item(
        id = "1",
        obj = ObjectWrapper.Basic(
            mapOf(
                "id" to "1",
                "name" to "Task Object",
                Relations.SPACE_ID to "space1",
                Relations.LAYOUT to ObjectType.Layout.TODO.code
            )
        ),
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
        obj = ObjectWrapper.Basic(
            mapOf(
                "id" to "2",
                "name" to "Page Object",
                Relations.SPACE_ID to "space2",
                Relations.LAYOUT to ObjectType.Layout.BASIC.code
            )
        ),
        name = "Page Object",
        space = SpaceId("space2"),
        type = "type2",
        typeName = "Page",
        createdBy = "by Mike Long",
        layout = ObjectType.Layout.BASIC,
        icon = ObjectIcon.TypeIcon.Default.DEFAULT
    ),
    UiObjectsListItem.Item(
        id = "3",
        obj = ObjectWrapper.Basic(
            mapOf(
                "id" to "3",
                "name" to "File Object",
                Relations.SPACE_ID to "space3",
                Relations.LAYOUT to ObjectType.Layout.FILE.code
            )
        ),
        name = "File Object",
        space = SpaceId("space3"),
        type = "type3",
        typeName = "File",
        createdBy = "by John Doe",
        layout = ObjectType.Layout.FILE,
        icon = ObjectIcon.File(
            mime = "image/png"
        )
    )
)