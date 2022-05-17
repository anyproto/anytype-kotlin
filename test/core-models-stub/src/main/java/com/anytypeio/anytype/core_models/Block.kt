package com.anytypeio.anytype.core_models

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
    content = Block.Content.Text(
        text = text,
        style = Block.Content.Text.Style.TITLE,
        marks = emptyList()
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
    content = Block.Content.Text(
        text = text,
        style = Block.Content.Text.Style.CHECKBOX,
        marks = marks,
        isChecked = isChecked
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
    content = Block.Content.Text(
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
    content = Block.Content.Text(
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
    content = Block.Content.Text(
        text = text,
        style = Block.Content.Text.Style.NUMBERED,
        marks = marks
    ),
    children = children,
    fields = Block.Fields.empty()
)