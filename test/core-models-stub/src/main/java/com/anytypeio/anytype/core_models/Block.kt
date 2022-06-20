package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.test_utils.MockDataFactory

fun StubHeader(
    children: List<Id> = emptyList()
): Block = Block(
    id = MockDataFactory.randomUuid(),
    content = Block.Content.Layout(
        type = Block.Content.Layout.Type.HEADER
    ),
    fields = Block.Fields.empty(),
    children = children
)

fun StubTitle(
    text: String = MockDataFactory.randomString()
): Block = Block(
    id = MockDataFactory.randomUuid(),
    content = StubTextContent(
        text = text,
        style = Block.Content.Text.Style.TITLE
    ),
    children = emptyList(),
    fields = Block.Fields.empty()
)

fun StubFeatured(
    children: List<Id> = emptyList()
): Block = Block(
    id = MockDataFactory.randomUuid(),
    content = Block.Content.FeaturedRelations,
    children = children,
    fields = Block.Fields.empty()
)

fun StubDescription(
    id: Id = MockDataFactory.randomUuid(),
    text: String = MockDataFactory.randomString()
): Block = Block(
    id = id,
    content = StubTextContent(
        text = text,
        style = Block.Content.Text.Style.DESCRIPTION
    ),
    children = emptyList(),
    fields = Block.Fields.empty()
)

fun StubCheckbox(
    text: String = MockDataFactory.randomString(),
    children: List<Id> = emptyList(),
    marks: List<Block.Content.Text.Mark> = emptyList(),
    isChecked: Boolean = MockDataFactory.randomBoolean()
): Block = Block(
    id = MockDataFactory.randomUuid(),
    content = StubTextContent(
        text = text,
        style = Block.Content.Text.Style.CHECKBOX,
        marks = marks,
        isChecked = isChecked
    ),
    children = children,
    fields = Block.Fields.empty()
)

fun StubParagraph(
    id: Id = MockDataFactory.randomUuid(),
    text: String = MockDataFactory.randomString(),
    children: List<Id> = emptyList(),
    marks: List<Block.Content.Text.Mark> = emptyList()
): Block = Block(
    id = id,
    content = StubTextContent(
        text = text,
        style = Block.Content.Text.Style.P,
        marks = marks
    ),
    children = children,
    fields = Block.Fields.empty()
)

fun StubBulleted(
    text: String = MockDataFactory.randomString(),
    children: List<Id> = emptyList(),
    marks: List<Block.Content.Text.Mark> = emptyList(),
    isChecked: Boolean = MockDataFactory.randomBoolean()
): Block = Block(
    id = MockDataFactory.randomUuid(),
    content = StubTextContent(
        text = text,
        style = Block.Content.Text.Style.BULLET,
        marks = marks,
        isChecked = isChecked
    ),
    children = children,
    fields = Block.Fields.empty()
)

fun StubToggle(
    text: String = MockDataFactory.randomString(),
    children: List<Id> = emptyList(),
    marks: List<Block.Content.Text.Mark> = emptyList()
): Block = Block(
    id = MockDataFactory.randomUuid(),
    content = StubTextContent(
        text = text,
        style = Block.Content.Text.Style.TOGGLE,
        marks = marks
    ),
    children = children,
    fields = Block.Fields.empty()
)

fun StubNumbered(
    text: String = MockDataFactory.randomString(),
    children: List<Id> = emptyList(),
    marks: List<Block.Content.Text.Mark> = emptyList()
): Block = Block(
    id = MockDataFactory.randomUuid(),
    content = StubTextContent(
        text = text,
        style = Block.Content.Text.Style.NUMBERED,
        marks = marks
    ),
    children = children,
    fields = Block.Fields.empty()
)

fun StubQuote(
    text: String = MockDataFactory.randomString(),
    children: List<Id> = emptyList(),
    marks: List<Block.Content.Text.Mark> = emptyList()
): Block = Block(
    id = MockDataFactory.randomUuid(),
    content = StubTextContent(
        text = text,
        style = Block.Content.Text.Style.QUOTE,
        marks = marks
    ),
    children = children,
    fields = Block.Fields.empty()
)

fun StubCallout(
    text: String = MockDataFactory.randomString(),
    children: List<Id> = emptyList(),
    marks: List<Block.Content.Text.Mark> = emptyList()
): Block = Block(
    id = MockDataFactory.randomUuid(),
    content = StubTextContent(
        text = text,
        style = Block.Content.Text.Style.CALLOUT,
        marks = marks
    ),
    children = children,
    fields = Block.Fields.empty()
)

fun StubRelation(
    relationKey: String = MockDataFactory.randomString(),
    format: RelationFormat
): Relation = Relation(
    key = relationKey,
    name = MockDataFactory.randomString(),
    format = format,
    source = Relation.Source.values().random()
)