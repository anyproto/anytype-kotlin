package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.test_utils.MockDataFactory

fun StubObject(
    id: String = MockDataFactory.randomUuid(),
    name: String = MockDataFactory.randomString(),
    objectType: String = MockDataFactory.randomString(),
    layout: Double = ObjectType.Layout.BASIC.code.toDouble(),
    smartBlockTypes: List<Double> = emptyList(),
    isDeleted: Boolean? = null,
    isArchived: Boolean? = null,
    description: String? = null,
    iconEmoji: String? = null,
    isReadOnly: Boolean? = null,
    isHidden: Boolean? = null
): ObjectWrapper.Basic = ObjectWrapper.Basic(
    map = mapOf(
        Relations.ID to id,
        Relations.NAME to name,
        Relations.TYPE to objectType,
        Relations.LAYOUT to layout,
        Relations.SMARTBLOCKTYPES to smartBlockTypes,
        Relations.IS_ARCHIVED to isArchived,
        Relations.IS_DELETED to isDeleted,
        Relations.DESCRIPTION to description,
        Relations.ICON_EMOJI to iconEmoji,
        Relations.IS_READ_ONLY to isReadOnly,
        Relations.IS_HIDDEN to isHidden
    )
)

fun StubObjectType(
    id: String = MockDataFactory.randomUuid(),
    name: String = MockDataFactory.randomString(),
    objectType: String = MockDataFactory.randomString(),
    layout: Double = ObjectType.Layout.BASIC.code.toDouble(),
    smartBlockTypes: List<Double> = emptyList(),
    isDeleted: Boolean? = null,
    isArchived: Boolean? = null,
    description: String? = null,
    iconEmoji: String? = null,
    isReadOnly: Boolean? = null,
    isHidden: Boolean? = null,
    sourceObject: Id? = null
): ObjectWrapper.Type = ObjectWrapper.Type(
    map = mapOf(
        Relations.ID to id,
        Relations.NAME to name,
        Relations.TYPE to objectType,
        Relations.LAYOUT to layout,
        Relations.SMARTBLOCKTYPES to smartBlockTypes,
        Relations.IS_ARCHIVED to isArchived,
        Relations.IS_DELETED to isDeleted,
        Relations.DESCRIPTION to description,
        Relations.ICON_EMOJI to iconEmoji,
        Relations.IS_READ_ONLY to isReadOnly,
        Relations.IS_HIDDEN to isHidden,
        Relations.SOURCE_OBJECT to sourceObject
    )
)