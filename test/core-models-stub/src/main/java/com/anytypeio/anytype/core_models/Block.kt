package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.test_utils.MockDataFactory

fun StubHeader(
    id: Id = MockDataFactory.randomUuid(),
    children: List<Id> = emptyList()
): Block = Block(
    id = id,
    content = Block.Content.Layout(
        type = Block.Content.Layout.Type.HEADER
    ),
    fields = Block.Fields.empty(),
    children = children
)

fun StubTitle(
    id: Id = MockDataFactory.randomUuid(),
    text: String = MockDataFactory.randomString()
): Block = Block(
    id = id,
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
    marks: List<Block.Content.Text.Mark> = emptyList(),
    backgroundColor: String? = null
): Block = Block(
    id = id,
    content = StubTextContent(
        text = text,
        style = Block.Content.Text.Style.P,
        marks = marks
    ),
    children = children,
    fields = Block.Fields.empty(),
    backgroundColor = backgroundColor
)

fun StubFile(
    id: Id = MockDataFactory.randomUuid(),
    children: List<Id> = emptyList(),
    backgroundColor: String? = null,
    hash: String = MockDataFactory.randomString(),
    name: String = MockDataFactory.randomString(),
    size: Long = MockDataFactory.randomLong(),
    type: Block.Content.File.Type? = null,
    state: Block.Content.File.State? = null
): Block = Block(
    id = id,
    children = children,
    fields = Block.Fields.empty(),
    backgroundColor = backgroundColor,
    content = Block.Content.File(
        size = size,
        name = name,
        hash = hash,
        type = type,
        state = state
    )
)

fun StubBulleted(
    id: Id = MockDataFactory.randomUuid(),
    text: String = MockDataFactory.randomString(),
    children: List<Id> = emptyList(),
    marks: List<Block.Content.Text.Mark> = emptyList(),
    isChecked: Boolean = MockDataFactory.randomBoolean()
): Block = Block(
    id = id,
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
    id: Id = MockDataFactory.randomUuid(),
    text: String = MockDataFactory.randomString(),
    children: List<Id> = emptyList(),
    marks: List<Block.Content.Text.Mark> = emptyList()
): Block = Block(
    id = id,
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
    marks: List<Block.Content.Text.Mark> = emptyList(),
    backgroundColor: String? = null,
    iconEmoji: String? = null,
    iconImage: String? = null,
): Block = Block(
    id = MockDataFactory.randomUuid(),
    content = StubTextContent(
        text = text,
        style = Block.Content.Text.Style.CALLOUT,
        marks = marks,
        iconEmoji = iconEmoji,
        iconImage = iconImage,
    ),
    children = children,
    fields = Block.Fields.empty(),
    backgroundColor = backgroundColor
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

fun StubBookmark(
    id: Id = MockDataFactory.randomString(),
    url: Url? = MockDataFactory.randomString(),
    description: String? = MockDataFactory.randomString(),
    image: Hash? = null,
    favicon: Hash? = null,
    title: String? = MockDataFactory.randomString(),
    fields: Block.Fields = Block.Fields.empty(),
    backgroundColor: String? = null
): Block = Block(
    id = id,
    content = Block.Content.Bookmark(
        title = title,
        url = url,
        description = description,
        image = image,
        favicon = favicon
    ),
    children = emptyList(),
    fields = fields,
    backgroundColor = backgroundColor
)

fun StubSmartBlock(
    id: Id = MockDataFactory.randomString(),
    children: List<Id> = emptyList()
): Block = Block(
    id = id,
    children = children,
    fields = Block.Fields.empty(),
    content = Block.Content.Smart()
)

fun StubTable(
    id: Id = MockDataFactory.randomUuid(),
    children: List<Id> = emptyList(),
    background: String? = null
): Block = Block(
    id = id,
    content = Block.Content.Table,
    children = children,
    fields = Block.Fields.empty(),
    backgroundColor = background
)

fun StubLayoutRows(
    id: Id = MockDataFactory.randomUuid(),
    children: List<Id> = emptyList(),
): Block = Block(
    id = id,
    content = Block.Content.Layout(type = Block.Content.Layout.Type.TABLE_ROW),
    children = children,
    fields = Block.Fields.empty(),
)

fun StubLayoutColumns(
    id: Id = MockDataFactory.randomUuid(),
    children: List<Id> = emptyList(),
): Block = Block(
    id = id,
    content = Block.Content.Layout(type = Block.Content.Layout.Type.TABLE_COLUMN),
    children = children,
    fields = Block.Fields.empty(),
)

fun StubTableRow(
    id: Id = MockDataFactory.randomUuid(),
    children: List<Id> = emptyList(),
): Block = Block(
    id = id,
    content = Block.Content.TableRow(false),
    children = children,
    fields = Block.Fields.empty(),
)

fun StubTableColumn(
    id: Id = MockDataFactory.randomUuid(),
    children: List<Id> = emptyList(),
    background: String? = null
): Block = Block(
    id = id,
    content = Block.Content.TableColumn,
    children = children,
    fields = Block.Fields.empty(),
    backgroundColor = background
)

fun StubTableColumns(size: Int): List<Block> = (0 until size).map { StubTableColumn() }
fun StubTableRows(size: Int): List<Block> = (0 until size).map { StubTableRow() }
fun StubTableCells(columns: List<Block>, rows: List<Block>): List<Block> {
    val cells = mutableListOf<Block>()
    for (i in rows.indices) {
        for (j in columns.indices) {
            cells.add(StubParagraph(id = "${rows[i].id}-${columns[j].id}"))
        }
    }
    return cells.toList()
}