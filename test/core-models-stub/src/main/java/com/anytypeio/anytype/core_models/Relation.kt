package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.test_utils.MockDataFactory

@Deprecated("To be deleted")
fun StubRelation(
    key: String = MockDataFactory.randomString(),
    name: String = MockDataFactory.randomString(),
    format: Relation.Format = Relation.Format.SHORT_TEXT,
    source: Relation.Source = Relation.Source.LOCAL,
    isHidden: Boolean = false,
    isReadOnly: Boolean = false,
    isMulti: Boolean = false,
    selections: List<Relation.Option> = emptyList(),
    objectTypes: List<String> = emptyList(),
    defaultValue: Any? = null
): Relation = Relation(
    key,
    name,
    format,
    source,
    isHidden,
    isReadOnly,
    isMulti,
    selections,
    objectTypes,
    defaultValue
)

fun StubRelationObject(
    id: String = MockDataFactory.randomString(),
    key: String = MockDataFactory.randomString(),
    name: String = MockDataFactory.randomString(),
    format: Relation.Format = Relation.Format.SHORT_TEXT,
    isHidden: Boolean = false,
    isReadOnly: Boolean = false,
    objectTypes: List<Id> = emptyList(),
    relationOptionsDict: List<Id> = emptyList()
): ObjectWrapper.Relation = ObjectWrapper.Relation(
    map = mapOf(
        Relations.ID to id,
        Relations.RELATION_KEY to key,
        Relations.NAME to name,
        Relations.IS_HIDDEN to isHidden,
        Relations.IS_READ_ONLY to isReadOnly,
        Relations.RELATION_FORMAT_OBJECT_TYPES to objectTypes,
        Relations.RELATION_FORMAT to format.code.toDouble(),
        Relations.RELATION_OPTION_DICT to relationOptionsDict
    )
)

@Deprecated("To be deleted")
fun StubRelationOption(
    id: String = MockDataFactory.randomUuid(),
    text: String = MockDataFactory.randomString(),
    color: String = MockDataFactory.randomString()
): Relation.Option = Relation.Option(
    id = id,
    text = text,
    color = color
)

fun StubRelationOptionObject(
    id: String = MockDataFactory.randomUuid(),
    text: String = MockDataFactory.randomString(),
    color: String = MockDataFactory.randomString()
): ObjectWrapper.Option = ObjectWrapper.Option(
        mapOf(
            Relations.ID to id,
            Relations.RELATION_OPTION_TEXT to text,
            Relations.RELATION_OPTION_COLOR to color,
        )
)