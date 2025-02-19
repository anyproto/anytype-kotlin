package com.anytypeio.anytype.feature_date.ui.models

import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.feature_date.viewmodel.UiFieldsItem

val StubHorizontalItems = listOf(
    UiFieldsItem.Settings(),
    UiFieldsItem.Item.Mention(
        id = "Item 54",
        title = "Mentionssssss",
        key = RelationKey(key = Relations.MENTIONS),
        relationFormat = RelationFormat.DATE
    ),
    UiFieldsItem.Item.Default(
        "Item 1",
        title = "Title1",
        key = RelationKey("key1"),
        relationFormat = RelationFormat.DATE
    ),
    UiFieldsItem.Item.Default(
        "Item 2",
        title = "Title2",
        key = RelationKey("key2"),
        relationFormat = RelationFormat.DATE
    ),
    UiFieldsItem.Item.Default(
        "Item 3",
        title = "Title3",
        key = RelationKey("key3"),
        relationFormat = RelationFormat.DATE
    ),
    UiFieldsItem.Item.Default(
        "Item 4",
        title = "Title4",
        key = RelationKey("key4"),
        relationFormat = RelationFormat.DATE
    ),
    UiFieldsItem.Item.Default(
        "Item 5",
        title = "Title5",
        key = RelationKey("key5"),
        relationFormat = RelationFormat.DATE
    ),
)