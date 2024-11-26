package com.anytypeio.anytype.feature_date.ui

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.feature_date.models.UiHorizontalListItem
import com.anytypeio.anytype.feature_date.models.UiVerticalListItem
import com.anytypeio.anytype.presentation.objects.ObjectIcon

val StubVerticalItems = listOf(
    UiVerticalListItem.Item(
        id = "1",
        name = "Task Object",
        space = SpaceId("space1"),
        type = "type1",
        typeName = "Task",
        createdBy = "by Joseph Wolf",
        layout = ObjectType.Layout.TODO,
        icon = ObjectIcon.Task(isChecked = true)
    ),
    UiVerticalListItem.Item(
        id = "2",
        name = "Page Object",
        space = SpaceId("space2"),
        type = "type2",
        typeName = "Page",
        createdBy = "by Mike Long",
        layout = ObjectType.Layout.BASIC,
        icon = ObjectIcon.Empty.Page
    ),
    UiVerticalListItem.Item(
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

val StubHorizontalItems = listOf(
    UiHorizontalListItem.Settings(),
    UiHorizontalListItem.Item.Mention(
        id = "Item 54",
        title = "Mentionssssss",
        key = RelationKey(key = Relations.MENTIONS),
        relationFormat = RelationFormat.DATE
    ),
    UiHorizontalListItem.Item.Default(
        "Item 1",
        title = "Title1",
        key = RelationKey("key1"),
        relationFormat = RelationFormat.DATE
    ),
    UiHorizontalListItem.Item.Default(
        "Item 2",
        title = "Title2",
        key = RelationKey("key2"),
        relationFormat = RelationFormat.DATE
    ),
    UiHorizontalListItem.Item.Default(
        "Item 3",
        title = "Title3",
        key = RelationKey("key3"),
        relationFormat = RelationFormat.DATE
    ),
    UiHorizontalListItem.Item.Default(
        "Item 4",
        title = "Title4",
        key = RelationKey("key4"),
        relationFormat = RelationFormat.DATE
    ),
    UiHorizontalListItem.Item.Default(
        "Item 5",
        title = "Title5",
        key = RelationKey("key5"),
        relationFormat = RelationFormat.DATE
    ),
)