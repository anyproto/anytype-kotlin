package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.Indent
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.test_utils.MockDataFactory

fun StubParagraphView(
    id: Id = MockDataFactory.randomString(),
    text: String = MockDataFactory.randomString(),
    marks: List<Markup.Mark> = emptyList(),
    isFocused: Boolean = MockDataFactory.randomBoolean(),
    isSelected: Boolean = MockDataFactory.randomBoolean(),
    color: String? = null,
    indent: Indent = 0,
    searchFields: List<BlockView.Searchable.Field> = emptyList(),
    backgroundColor: String? = null,
    mode: BlockView.Mode = BlockView.Mode.EDIT,
    decorations: List<BlockView.Decoration> = emptyList(),
    ghostSelection: IntRange? = null,
    cursor: Int? = null,
    alignment: Alignment? = null
): BlockView.Text.Paragraph = BlockView.Text.Paragraph(
    id = id,
    text = text,
    marks = marks,
    isFocused = isFocused,
    isSelected = isSelected,
    color = color,
    indent = indent,
    searchFields = searchFields,
    backgroundColor = backgroundColor,
    mode = mode,
    decorations = decorations,
    ghostEditorSelection = ghostSelection,
    cursor = cursor,
    alignment = alignment
)

fun StubNumberedView(
    id: Id = MockDataFactory.randomString(),
    text: String = MockDataFactory.randomString(),
    marks: List<Markup.Mark> = emptyList(),
    isFocused: Boolean = MockDataFactory.randomBoolean(),
    isSelected: Boolean = MockDataFactory.randomBoolean(),
    color: String? = null,
    indent: Indent = 0,
    searchFields: List<BlockView.Searchable.Field> = emptyList(),
    backgroundColor: String? = null,
    mode: BlockView.Mode = BlockView.Mode.EDIT,
    decorations: List<BlockView.Decoration> = emptyList(),
    ghostSelection: IntRange? = null,
    cursor: Int? = null,
    alignment: Alignment? = null,
    number: Int = 1
): BlockView.Text.Numbered = BlockView.Text.Numbered(
    id = id,
    text = text,
    marks = marks,
    isFocused = isFocused,
    isSelected = isSelected,
    color = color,
    indent = indent,
    searchFields = searchFields,
    backgroundColor = backgroundColor,
    mode = mode,
    decorations = decorations,
    ghostEditorSelection = ghostSelection,
    cursor = cursor,
    alignment = alignment,
    number = number
)

fun StubBulletedView(
    id: Id = MockDataFactory.randomString(),
    text: String = MockDataFactory.randomString(),
    marks: List<Markup.Mark> = emptyList(),
    isFocused: Boolean = MockDataFactory.randomBoolean(),
    isSelected: Boolean = MockDataFactory.randomBoolean(),
    color: String? = null,
    indent: Indent = 0,
    searchFields: List<BlockView.Searchable.Field> = emptyList(),
    backgroundColor: String? = null,
    mode: BlockView.Mode = BlockView.Mode.EDIT,
    decorations: List<BlockView.Decoration> = emptyList(),
    ghostSelection: IntRange? = null,
    cursor: Int? = null,
    alignment: Alignment? = null,
): BlockView.Text.Bulleted = BlockView.Text.Bulleted(
    id = id,
    text = text,
    marks = marks,
    isFocused = isFocused,
    isSelected = isSelected,
    color = color,
    indent = indent,
    searchFields = searchFields,
    backgroundColor = backgroundColor,
    mode = mode,
    decorations = decorations,
    ghostEditorSelection = ghostSelection,
    cursor = cursor,
    alignment = alignment
)

fun StubCheckboxView(
    id: Id = MockDataFactory.randomString(),
    text: String = MockDataFactory.randomString(),
    marks: List<Markup.Mark> = emptyList(),
    isFocused: Boolean = MockDataFactory.randomBoolean(),
    isSelected: Boolean = MockDataFactory.randomBoolean(),
    color: String? = null,
    indent: Indent = 0,
    searchFields: List<BlockView.Searchable.Field> = emptyList(),
    backgroundColor: String? = null,
    mode: BlockView.Mode = BlockView.Mode.EDIT,
    decorations: List<BlockView.Decoration> = emptyList(),
    ghostSelection: IntRange? = null,
    cursor: Int? = null,
    alignment: Alignment? = null,
    isChecked: Boolean = false
): BlockView.Text.Checkbox = BlockView.Text.Checkbox(
    id = id,
    text = text,
    marks = marks,
    isFocused = isFocused,
    isSelected = isSelected,
    color = color,
    indent = indent,
    searchFields = searchFields,
    backgroundColor = backgroundColor,
    mode = mode,
    decorations = decorations,
    ghostEditorSelection = ghostSelection,
    cursor = cursor,
    alignment = alignment,
    isChecked = isChecked
)

fun StubToggleView(
    id: Id = MockDataFactory.randomString(),
    text: String = MockDataFactory.randomString(),
    marks: List<Markup.Mark> = emptyList(),
    isFocused: Boolean = MockDataFactory.randomBoolean(),
    isSelected: Boolean = MockDataFactory.randomBoolean(),
    color: String? = null,
    indent: Indent = 0,
    searchFields: List<BlockView.Searchable.Field> = emptyList(),
    backgroundColor: String? = null,
    mode: BlockView.Mode = BlockView.Mode.EDIT,
    decorations: List<BlockView.Decoration> = emptyList(),
    ghostSelection: IntRange? = null,
    cursor: Int? = null,
    alignment: Alignment? = null,
    isEmpty: Boolean = false,
    toggled: Boolean = false
): BlockView.Text.Toggle = BlockView.Text.Toggle(
    id = id,
    text = text,
    marks = marks,
    isFocused = isFocused,
    isSelected = isSelected,
    color = color,
    indent = indent,
    searchFields = searchFields,
    backgroundColor = backgroundColor,
    mode = mode,
    decorations = decorations,
    ghostEditorSelection = ghostSelection,
    cursor = cursor,
    alignment = alignment,
    isEmpty = isEmpty,
    toggled = toggled
)

fun StubCalloutView(
    id: Id = MockDataFactory.randomString(),
    text: String = MockDataFactory.randomString(),
    marks: List<Markup.Mark> = emptyList(),
    isFocused: Boolean = MockDataFactory.randomBoolean(),
    isSelected: Boolean = MockDataFactory.randomBoolean(),
    color: String? = null,
    indent: Indent = 0,
    searchFields: List<BlockView.Searchable.Field> = emptyList(),
    backgroundColor: String? = null,
    mode: BlockView.Mode = BlockView.Mode.EDIT,
    decorations: List<BlockView.Decoration> = emptyList(),
    ghostSelection: IntRange? = null,
    cursor: Int? = null,
    icon: ObjectIcon = ObjectIcon.None
): BlockView.Text.Callout = BlockView.Text.Callout(
    id = id,
    text = text,
    marks = marks,
    isFocused = isFocused,
    isSelected = isSelected,
    color = color,
    indent = indent,
    searchFields = searchFields,
    backgroundColor = backgroundColor,
    mode = mode,
    decorations = decorations,
    ghostEditorSelection = ghostSelection,
    cursor = cursor,
    icon = icon
)