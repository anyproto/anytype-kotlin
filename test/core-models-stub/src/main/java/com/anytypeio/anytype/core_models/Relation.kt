package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.test_utils.MockDataFactory

fun StubRelationObject(
    id: String = MockDataFactory.randomString(),
    uniqueKey: String = MockDataFactory.randomString(),
    key: String = MockDataFactory.randomString(),
    name: String = MockDataFactory.randomString(),
    format: Relation.Format = Relation.Format.SHORT_TEXT,
    isHidden: Boolean = false,
    isReadOnly: Boolean = false,
    isReadOnlyValue: Boolean = false,
    objectTypes: List<Id> = emptyList(),
    relationOptionsDict: List<Id> = emptyList(),
    sourceObject: Id = MockDataFactory.randomUuid(),
    spaceId: Id = MockDataFactory.randomString()
): ObjectWrapper.Relation = ObjectWrapper.Relation(
    map = mapOf(
        Relations.ID to id,
        Relations.UNIQUE_KEY to uniqueKey,
        Relations.RELATION_KEY to key,
        Relations.NAME to name,
        Relations.IS_HIDDEN to isHidden,
        Relations.IS_READ_ONLY to isReadOnly,
        Relations.RELATION_READ_ONLY_VALUE to isReadOnlyValue,
        Relations.RELATION_FORMAT_OBJECT_TYPES to objectTypes,
        Relations.RELATION_FORMAT to format.code.toDouble(),
        Relations.RELATION_OPTION_DICT to relationOptionsDict,
        Relations.SOURCE_OBJECT to sourceObject,
        Relations.SPACE_ID to spaceId
    )
)

fun StubRelationOptionObject(
    id: String = MockDataFactory.randomUuid(),
    space: Id = MockDataFactory.randomUuid(),
    text: String = MockDataFactory.randomString(),
    color: String = MockDataFactory.randomString()
): ObjectWrapper.Option = ObjectWrapper.Option(
        mapOf(
            Relations.ID to id,
            Relations.SPACE_ID to space,
            Relations.NAME to text,
            Relations.RELATION_OPTION_COLOR to color,
        )
)