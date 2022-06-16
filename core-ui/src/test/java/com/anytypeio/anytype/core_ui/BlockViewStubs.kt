package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.Indent
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
) : BlockView.Text.Paragraph = BlockView.Text.Paragraph(
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