package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.test_utils.MockDataFactory

fun StubObjectType(
    url: Url = MockDataFactory.randomString(),
    name: String = MockDataFactory.randomString(),
    relations: List<Relation> = emptyList(),
    layout: ObjectType.Layout = ObjectType.Layout.BASIC
): ObjectType = ObjectType(
    url = url,
    name = name,
    relations = relations,
    emoji = MockDataFactory.randomString(),
    layout = layout,
    description = "",
    isHidden = false,
    smartBlockTypes = listOf(),
    isArchived = false,
    isReadOnly = false
)

fun StubObject(
    id: String = MockDataFactory.randomUuid(),
    name: String = MockDataFactory.randomString(),
    objectType: String = MockDataFactory.randomString(),
    layout: Double = ObjectType.Layout.BASIC.code.toDouble()
): ObjectWrapper.Basic = ObjectWrapper.Basic(
    map = mapOf(
        Relations.ID to id,
        Relations.NAME to name,
        Relations.TYPE to objectType,
        Relations.LAYOUT to layout
    )
)