package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.Indent
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.test_utils.MockDataFactory
import java.util.regex.Pattern

fun StubParagraphView(
    id: Id = MockDataFactory.randomString(),
    text: String = MockDataFactory.randomString(),
    marks: List<Markup.Mark> = emptyList(),
    isFocused: Boolean = MockDataFactory.randomBoolean(),
    isSelected: Boolean = MockDataFactory.randomBoolean(),
    color: ThemeColor = ThemeColor.DEFAULT,
    indent: Indent = 0,
    searchFields: List<BlockView.Searchable.Field> = emptyList(),
    backgroundColor: ThemeColor = ThemeColor.DEFAULT,
    mode: BlockView.Mode = BlockView.Mode.EDIT,
    decorations: List<BlockView.Decoration> = if (BuildConfig.NESTED_DECORATION_ENABLED)
        listOf(
            BlockView.Decoration(
                background = backgroundColor
            )
        ) else
        emptyList(),
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
    background = backgroundColor,
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
    color: ThemeColor = ThemeColor.DEFAULT,
    indent: Indent = 0,
    searchFields: List<BlockView.Searchable.Field> = emptyList(),
    backgroundColor: ThemeColor = ThemeColor.DEFAULT,
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
    background = backgroundColor,
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
    color: ThemeColor = ThemeColor.DEFAULT,
    indent: Indent = 0,
    searchFields: List<BlockView.Searchable.Field> = emptyList(),
    backgroundColor: ThemeColor = ThemeColor.DEFAULT,
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
    background = backgroundColor,
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
    color: ThemeColor = ThemeColor.DEFAULT,
    indent: Indent = 0,
    searchFields: List<BlockView.Searchable.Field> = emptyList(),
    backgroundColor: ThemeColor = ThemeColor.DEFAULT,
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
    background = backgroundColor,
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
    color: ThemeColor = ThemeColor.DEFAULT,
    indent: Indent = 0,
    searchFields: List<BlockView.Searchable.Field> = emptyList(),
    backgroundColor: ThemeColor = ThemeColor.DEFAULT,
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
    background = backgroundColor,
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
    color: ThemeColor = ThemeColor.DEFAULT,
    indent: Indent = 0,
    searchFields: List<BlockView.Searchable.Field> = emptyList(),
    backgroundColor: ThemeColor = ThemeColor.DEFAULT,
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
    background = backgroundColor,
    mode = mode,
    decorations = decorations,
    ghostEditorSelection = ghostSelection,
    cursor = cursor,
    icon = icon
)

fun StubBookmarkPlaceholderView(
    id: Id = MockDataFactory.randomUuid(),
    indent: Indent = MockDataFactory.randomInt(),
    isPreviousBlockMedia: Boolean = false,
    background: ThemeColor = ThemeColor.DEFAULT,
    decorations: List<BlockView.Decoration> = if (BuildConfig.NESTED_DECORATION_ENABLED)
        listOf(
            BlockView.Decoration(
                style = BlockView.Decoration.Style.Card,
                background = background
            )
        )
    else
        emptyList()
): BlockView.MediaPlaceholder.Bookmark = BlockView.MediaPlaceholder.Bookmark(
    id = id,
    indent = indent,
    isPreviousBlockMedia = isPreviousBlockMedia,
    background = background,
    decorations = decorations
)

fun StubPicturePlaceholderView(
    id: Id = MockDataFactory.randomUuid(),
    indent: Indent = MockDataFactory.randomInt(),
    isPreviousBlockMedia: Boolean = false,
    background: ThemeColor = ThemeColor.DEFAULT,
    decorations: List<BlockView.Decoration> = if (BuildConfig.NESTED_DECORATION_ENABLED)
        listOf(
            BlockView.Decoration(
                style = BlockView.Decoration.Style.Card,
                background = background
            )
        )
    else
        emptyList()
): BlockView.MediaPlaceholder.Picture = BlockView.MediaPlaceholder.Picture(
    id = id,
    indent = indent,
    isPreviousBlockMedia = isPreviousBlockMedia,
    background = background,
    decorations = decorations
)

fun StubVideoPlaceholderView(
    id: Id = MockDataFactory.randomUuid(),
    indent: Indent = MockDataFactory.randomInt(),
    isPreviousBlockMedia: Boolean = false,
    background: ThemeColor = ThemeColor.DEFAULT,
    decorations: List<BlockView.Decoration> = if (BuildConfig.NESTED_DECORATION_ENABLED)
        listOf(
            BlockView.Decoration(
                style = BlockView.Decoration.Style.Card,
                background = background
            )
        )
    else
        emptyList()
): BlockView.MediaPlaceholder.Video = BlockView.MediaPlaceholder.Video(
    id = id,
    indent = indent,
    isPreviousBlockMedia = isPreviousBlockMedia,
    background = background,
    decorations = decorations
)

fun StubFilePlaceholderView(
    id: Id = MockDataFactory.randomUuid(),
    indent: Indent = MockDataFactory.randomInt(),
    isPreviousBlockMedia: Boolean = false,
    background: ThemeColor = ThemeColor.DEFAULT,
    decorations: List<BlockView.Decoration> = if (BuildConfig.NESTED_DECORATION_ENABLED)
        listOf(
            BlockView.Decoration(
                style = BlockView.Decoration.Style.Card,
                background = background
            )
        )
    else
        emptyList()
): BlockView.MediaPlaceholder.File = BlockView.MediaPlaceholder.File(
    id = id,
    indent = indent,
    isPreviousBlockMedia = isPreviousBlockMedia,
    background = background,
    decorations = decorations
)

fun StubBlockViewSearchFiled(
    key: Key = BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY,
    highlights: List<IntRange> = emptyList(),
    target: IntRange = IntRange.EMPTY
): BlockView.Searchable.Field =
    BlockView.Searchable.Field(
        key = key,
        highlights = highlights,
        target = target
    )

fun StubTwoRowsThreeColumnsSimpleTable(
    tableId: String = MockDataFactory.randomUuid(),
    rowId1: String = "rowId1",
    rowId2: String = "rowId2",
    columnId1: String = "columnId1",
    columnId2: String = "columnId2",
    columnId3: String = "columnId3",
    textR1C1: String = MockDataFactory.randomString(),
    textR1C2: String = MockDataFactory.randomString(),
    textR1C3: String = MockDataFactory.randomString(),
    textR2C1: String = MockDataFactory.randomString(),
    textR2C2: String = MockDataFactory.randomString(),
    textR2C3: String = MockDataFactory.randomString(),
): BlockView.Table {

    val row1Block1 = StubParagraph(id = "$rowId1-$columnId1", text = textR1C1)
    val row1Block2 = StubParagraph(id = "$rowId1-$columnId2", text = textR1C2)
    val row1Block3 = StubParagraph(id = "$rowId1-$columnId3", text = textR1C3)
    val row2Block1 = StubParagraph(id = "$rowId2-$columnId1", text = textR2C1)
    val row2Block2 = StubParagraph(id = "$rowId2-$columnId2", text = textR2C2)
    val row2Block3 = StubParagraph(id = "$rowId2-$columnId3", text = textR2C3)

    val cells = listOf(
        BlockView.Table.Cell.Text(
            block = BlockView.Text.Paragraph(
                id = row1Block1.id,
                text = row1Block1.content.asText().text
            ),
            rowId = rowId1,
            columnId = columnId1
        ),
        BlockView.Table.Cell.Text(
            block = BlockView.Text.Paragraph(
                id = row1Block2.id,
                text = row1Block2.content.asText().text
            ),
            rowId = rowId1,
            columnId = columnId2
        ),
        BlockView.Table.Cell.Text(
            block = BlockView.Text.Paragraph(
                id = row1Block3.id,
                text = row1Block3.content.asText().text
            ),
            rowId = rowId1,
            columnId = columnId3
        ),
        BlockView.Table.Cell.Text(
            block = BlockView.Text.Paragraph(
                id = row2Block1.id,
                text = row2Block1.content.asText().text
            ),
            rowId = rowId2,
            columnId = columnId1
        ),
        BlockView.Table.Cell.Text(
            block = BlockView.Text.Paragraph(
                id = row2Block2.id,
                text = row2Block2.content.asText().text
            ),
            rowId = rowId2,
            columnId = columnId2
        ),
        BlockView.Table.Cell.Text(
            block = BlockView.Text.Paragraph(
                id = row2Block3.id,
                text = row2Block3.content.asText().text
            ),
            rowId = rowId2,
            columnId = columnId3
        )
    )

    val columns = listOf(
        BlockView.Table.Column(id = columnId1, background = ThemeColor.DEFAULT),
        BlockView.Table.Column(id = columnId2, background = ThemeColor.DEFAULT),
        BlockView.Table.Column(id = columnId3, background = ThemeColor.DEFAULT)
    )

    return BlockView.Table(
        id = tableId,
        cells = cells,
        columns = columns,
        rowCount = 2,
        isSelected = false
    )
}

fun StubPattern(
    query: String
): Pattern {
    val flags = Pattern.MULTILINE or Pattern.CASE_INSENSITIVE
    val escaped = Pattern.quote(query)
    return Pattern.compile(escaped, flags)
}