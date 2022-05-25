package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.test_utils.MockDataFactory

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

fun StubRelationOption(
    id: String = MockDataFactory.randomUuid(),
    text: String = MockDataFactory.randomString(),
    color: String = MockDataFactory.randomString(),
    scope: Relation.OptionScope = Relation.OptionScope.LOCAL
): Relation.Option =
    Relation.Option(
        id, text, color, scope
    )