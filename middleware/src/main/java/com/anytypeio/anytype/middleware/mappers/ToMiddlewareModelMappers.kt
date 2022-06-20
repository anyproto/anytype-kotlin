package com.anytypeio.anytype.middleware.mappers

import anytype.model.Range
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.BlockSplitMode
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relation


// ---------------------- BLOCKS ------------------------
fun Block.toMiddlewareModel(): MBlock = when (val content = content) {
    is Block.Content.Text -> {
        MBlock(
            id = id,
            text = content.toMiddlewareModel(),
            backgroundColor = backgroundColor.orEmpty(),
            align = content.align.toMiddlewareModel(),
            childrenIds = children,
            fields = fields.toMiddlewareModel(),
        )
    }
    is Block.Content.Bookmark -> {
        MBlock(
            id = id,
            bookmark = content.toMiddlewareModel(),
            backgroundColor = backgroundColor.orEmpty(),
            fields = fields.toMiddlewareModel()
        )
    }
    is Block.Content.File -> {
        MBlock(
            id = id,
            file_ = content.toMiddlewareModel(),
            backgroundColor = backgroundColor.orEmpty(),
            fields = fields.toMiddlewareModel()
        )
    }
    is Block.Content.Link -> {
        MBlock(
            id = id,
            link = content.toMiddlewareModel(),
            backgroundColor = backgroundColor.orEmpty(),
            fields = fields.toMiddlewareModel()
        )
    }
    is Block.Content.Layout -> {
        MBlock(
            id = id,
            layout = content.toMiddlewareModel(),
            fields = fields.toMiddlewareModel()
        )
    }
    is Block.Content.Divider -> {
        MBlock(
            id = id,
            div = content.toMiddlewareModel(),
            backgroundColor = backgroundColor.orEmpty(),
            fields = fields.toMiddlewareModel()
        )
    }
    else -> MBlock()
}

fun Block.Fields.toMiddlewareModel(): Map<String, *> = this.map

fun Block.Content.Divider.toMiddlewareModel(): MBDiv = when (style) {
    Block.Content.Divider.Style.LINE -> MBDiv(style = MBDivStyle.Line)
    Block.Content.Divider.Style.DOTS -> MBDiv(style = MBDivStyle.Dots)
}

fun Block.Content.Divider.Style.toMiddlewareModel(): MBDivStyle = when (this) {
    Block.Content.Divider.Style.LINE -> MBDivStyle.Line
    Block.Content.Divider.Style.DOTS -> MBDivStyle.Dots
}

fun Block.Content.Layout.toMiddlewareModel(): MBLayout = when (type) {
    Block.Content.Layout.Type.ROW -> MBLayout(style = MBLayoutStyle.Row)
    Block.Content.Layout.Type.COLUMN -> MBLayout(style = MBLayoutStyle.Column)
    Block.Content.Layout.Type.DIV -> MBLayout(style = MBLayoutStyle.Div)
    Block.Content.Layout.Type.HEADER -> MBLayout(style = MBLayoutStyle.Header)
}

fun Block.Content.Bookmark.toMiddlewareModel(): MBBookmark = MBBookmark(
    description = description.orEmpty(),
    faviconHash = favicon.orEmpty(),
    title = title.orEmpty(),
    url = url.orEmpty(),
    imageHash = image.orEmpty()
)

fun Block.Content.File.toMiddlewareModel(): MBFile = MBFile(
    hash = hash.orEmpty(),
    name = name.orEmpty(),
    mime = mime.orEmpty(),
    size = size ?: 0,
    state = state.toMiddlewareModel(),
    type = type.toMiddlewareModel()
)

internal fun Block.Content.Link.toMiddlewareModel(): MBLink {
    return MBLink(
        targetBlockId = target,
        style = type.toMiddlewareModel(),
        iconSize = iconSize.toMiddlewareModel(),
        cardStyle = cardStyle.toMiddlewareModel(),
        description = description.toMiddlewareModel(),
        relations = relations.map { it.toMiddlewareModel() }
    )
}

internal fun Block.Content.Link.Relation.toMiddlewareModel(): String {
    return when (this) {
        Block.Content.Link.Relation.COVER -> "cover"
        Block.Content.Link.Relation.NAME -> "name"
        Block.Content.Link.Relation.TYPE -> "type"
        is Block.Content.Link.Relation.UNKNOWN -> this.value
    }
}

internal fun Block.Content.Link.Description.toMiddlewareModel(): MBLinkDescription {
    return when (this) {
        Block.Content.Link.Description.NONE -> MBLinkDescription.None
        Block.Content.Link.Description.ADDED -> MBLinkDescription.Added
        Block.Content.Link.Description.CONTENT -> MBLinkDescription.Content
    }
}

internal fun Block.Content.Link.CardStyle.toMiddlewareModel(): MBLinkCardStyle {
    return when (this) {
        Block.Content.Link.CardStyle.TEXT -> MBLinkCardStyle.Text
        Block.Content.Link.CardStyle.CARD -> MBLinkCardStyle.Card
        Block.Content.Link.CardStyle.INLINE -> MBLinkCardStyle.Inline
    }
}

internal fun Block.Content.Link.IconSize.toMiddlewareModel(): MBLinkIconSize {
    return when (this) {
        Block.Content.Link.IconSize.NONE -> MBLinkIconSize.SizeNone
        Block.Content.Link.IconSize.SMALL -> MBLinkIconSize.SizeSmall
        Block.Content.Link.IconSize.MEDIUM -> MBLinkIconSize.SizeMedium
    }
}

fun Block.Content.Link.Type.toMiddlewareModel(): MBLinkStyle = when (this) {
    Block.Content.Link.Type.PAGE -> MBLinkStyle.Page
    Block.Content.Link.Type.DATA_VIEW -> MBLinkStyle.Dataview
    Block.Content.Link.Type.DASHBOARD -> MBLinkStyle.Dashboard
    Block.Content.Link.Type.ARCHIVE -> MBLinkStyle.Archive
}

fun Block.Content.Text.toMiddlewareModel(): MBText =
    MBText(
        text = text,
        marks = toMiddlewareModelMarks(),
        style = style.toMiddlewareModel(),
        color = color.orEmpty(),
        checked = isChecked ?: false,
        iconEmoji = iconEmoji.orEmpty(),
        iconImage = iconImage.orEmpty()
    )

fun Block.Content.Text.toMiddlewareModelMarks(): MBMarks =
    MBMarks(marks = marks.map { it.toMiddlewareModel() })

fun Block.Content.Text.Mark.toMiddlewareModel(): MBMark = when (type) {
    Block.Content.Text.Mark.Type.STRIKETHROUGH -> {
        MBMark(
            range = range.range(),
            type = MBMarkType.Strikethrough
        )
    }
    Block.Content.Text.Mark.Type.KEYBOARD -> {
        MBMark(
            range = range.range(),
            type = MBMarkType.Keyboard
        )
    }
    Block.Content.Text.Mark.Type.ITALIC -> {
        MBMark(
            range = range.range(),
            type = MBMarkType.Italic
        )
    }
    Block.Content.Text.Mark.Type.BOLD -> {
        MBMark(
            range = range.range(),
            type = MBMarkType.Bold
        )
    }
    Block.Content.Text.Mark.Type.LINK -> {
        MBMark(
            range = range.range(),
            type = MBMarkType.Link,
            param_ = param.orEmpty()
        )
    }
    Block.Content.Text.Mark.Type.TEXT_COLOR -> {
        MBMark(
            range = range.range(),
            type = MBMarkType.TextColor,
            param_ = param.orEmpty()
        )
    }
    Block.Content.Text.Mark.Type.BACKGROUND_COLOR -> {
        MBMark(
            range = range.range(),
            type = MBMarkType.BackgroundColor,
            param_ = param.orEmpty()
        )
    }
    Block.Content.Text.Mark.Type.MENTION -> {
        MBMark(
            range = range.range(),
            type = MBMarkType.Mention,
            param_ = param.orEmpty()
        )
    }
    Block.Content.Text.Mark.Type.OBJECT -> {
        MBMark(
            range = range.range(),
            type = MBMarkType.Object,
            param_ = param.orEmpty()
        )
    }
    else -> throw IllegalStateException("Unsupported mark type: ${type.name}")
}

fun IntRange.range(): Range = Range(from = first, to = last)

fun Block.Content.File.State?.toMiddlewareModel(): MBFileState = when (this) {
    Block.Content.File.State.EMPTY -> MBFileState.Empty
    Block.Content.File.State.UPLOADING -> MBFileState.Uploading
    Block.Content.File.State.DONE -> MBFileState.Done
    Block.Content.File.State.ERROR -> MBFileState.Error
    null -> MBFileState.Empty
}

fun Block.Content.File.Type?.toMiddlewareModel(): MBFileType = when (this) {
    Block.Content.File.Type.NONE -> MBFileType.None
    Block.Content.File.Type.FILE -> MBFileType.File
    Block.Content.File.Type.IMAGE -> MBFileType.Image
    Block.Content.File.Type.VIDEO -> MBFileType.Video
    Block.Content.File.Type.AUDIO -> MBFileType.Audio
    Block.Content.File.Type.PDF -> MBFileType.PDF
    null -> MBFileType.None
}

fun Block.Align?.toMiddlewareModel(): MBAlign = when (this) {
    Block.Align.AlignLeft -> MBAlign.AlignLeft
    Block.Align.AlignCenter -> MBAlign.AlignCenter
    Block.Align.AlignRight -> MBAlign.AlignRight
    else -> MBAlign.AlignLeft
}

fun Block.Content.Text.Style.toMiddlewareModel(): MBTextStyle = when (this) {
    Block.Content.Text.Style.P -> MBTextStyle.Paragraph
    Block.Content.Text.Style.H1 -> MBTextStyle.Header1
    Block.Content.Text.Style.H2 -> MBTextStyle.Header2
    Block.Content.Text.Style.H3 -> MBTextStyle.Header3
    Block.Content.Text.Style.H4 -> MBTextStyle.Header4
    Block.Content.Text.Style.TITLE -> MBTextStyle.Title
    Block.Content.Text.Style.QUOTE -> MBTextStyle.Quote
    Block.Content.Text.Style.CODE_SNIPPET -> MBTextStyle.Code
    Block.Content.Text.Style.BULLET -> MBTextStyle.Marked
    Block.Content.Text.Style.NUMBERED -> MBTextStyle.Numbered
    Block.Content.Text.Style.TOGGLE -> MBTextStyle.Toggle
    Block.Content.Text.Style.CHECKBOX -> MBTextStyle.Checkbox
    Block.Content.Text.Style.DESCRIPTION -> MBTextStyle.Description
    Block.Content.Text.Style.CALLOUT -> MBTextStyle.Callout
}

fun Position.toMiddlewareModel(): MBPosition = when (this) {
    Position.NONE -> MBPosition.None
    Position.TOP -> MBPosition.Top
    Position.BOTTOM -> MBPosition.Bottom
    Position.LEFT -> MBPosition.Left
    Position.RIGHT -> MBPosition.Right
    Position.INNER -> MBPosition.Inner
    Position.REPLACE -> MBPosition.Replace
}

fun BlockSplitMode.toMiddlewareModel() = when (this) {
    BlockSplitMode.BOTTOM -> MBSplitMode.BOTTOM
    BlockSplitMode.TOP -> MBSplitMode.TOP
    BlockSplitMode.INNER -> MBSplitMode.INNER
}


// ---------------------- DATA VIEW ------------------------
fun Block.Content.DataView.Viewer.toMiddlewareModel(): MDVView =
    MDVView(
        id = id,
        name = name,
        type = type.toMiddlewareModel(),
        sorts = sorts.map { it.toMiddlewareModel() },
        filters = filters.map { it.toMiddlewareModel() },
        relations = viewerRelations.map { it.toMiddlewareModel() },
        coverRelationKey = coverRelationKey.orEmpty(),
        coverFit = coverFit,
        hideIcon = hideIcon,
        cardSize = when (cardSize) {
            Block.Content.DataView.Viewer.Size.SMALL -> MDVViewCardSize.Small
            Block.Content.DataView.Viewer.Size.MEDIUM -> MDVViewCardSize.Medium
            Block.Content.DataView.Viewer.Size.LARGE -> MDVViewCardSize.Large
        }
    )

fun Block.Content.DataView.Viewer.Type.toMiddlewareModel(): MDVViewType = when (this) {
    Block.Content.DataView.Viewer.Type.GRID -> MDVViewType.Table
    Block.Content.DataView.Viewer.Type.LIST -> MDVViewType.List
    Block.Content.DataView.Viewer.Type.GALLERY -> MDVViewType.Gallery
    Block.Content.DataView.Viewer.Type.BOARD -> MDVViewType.Kanban
}

fun Block.Content.DataView.Sort.toMiddlewareModel(): MDVSort =
    MDVSort(
        RelationKey = relationKey,
        type = type.toMiddlewareModel()
    )

fun Block.Content.DataView.Sort.Type.toMiddlewareModel(): MDVSortType = when (this) {
    Block.Content.DataView.Sort.Type.ASC -> MDVSortType.Asc
    Block.Content.DataView.Sort.Type.DESC -> MDVSortType.Desc
}

fun Block.Content.DataView.Filter.toMiddlewareModel(): MDVFilter =
    MDVFilter(
        RelationKey = relationKey,
        operator_ = operator.toMiddlewareModel(),
        condition = condition.toMiddlewareModel(),
        value_ = value
    )

fun Block.Content.DataView.Filter.Operator.toMiddlewareModel(): MDVFilterOperator = when (this) {
    Block.Content.DataView.Filter.Operator.AND -> MDVFilterOperator.And
    Block.Content.DataView.Filter.Operator.OR -> MDVFilterOperator.Or
}

fun Block.Content.DataView.Filter.Condition.toMiddlewareModel(): MDVFilterCondition = when (this) {
    Block.Content.DataView.Filter.Condition.EQUAL -> MDVFilterCondition.Equal
    Block.Content.DataView.Filter.Condition.NOT_EQUAL -> MDVFilterCondition.NotEqual
    Block.Content.DataView.Filter.Condition.GREATER -> MDVFilterCondition.Greater
    Block.Content.DataView.Filter.Condition.LESS -> MDVFilterCondition.Less
    Block.Content.DataView.Filter.Condition.GREATER_OR_EQUAL -> MDVFilterCondition.GreaterOrEqual
    Block.Content.DataView.Filter.Condition.LESS_OR_EQUAL -> MDVFilterCondition.LessOrEqual
    Block.Content.DataView.Filter.Condition.LIKE -> MDVFilterCondition.Like
    Block.Content.DataView.Filter.Condition.NOT_LIKE -> MDVFilterCondition.NotLike
    Block.Content.DataView.Filter.Condition.IN -> MDVFilterCondition.In
    Block.Content.DataView.Filter.Condition.NOT_IN -> MDVFilterCondition.NotIn
    Block.Content.DataView.Filter.Condition.EMPTY -> MDVFilterCondition.Empty
    Block.Content.DataView.Filter.Condition.NOT_EMPTY -> MDVFilterCondition.NotEmpty
    Block.Content.DataView.Filter.Condition.ALL_IN -> MDVFilterCondition.AllIn
    Block.Content.DataView.Filter.Condition.NOT_ALL_IN -> MDVFilterCondition.NotAllIn
    Block.Content.DataView.Filter.Condition.NONE -> MDVFilterCondition.None
}

fun Block.Content.DataView.DateFormat?.toMiddlewareModel(): MDVDateFormat = when (this) {
    Block.Content.DataView.DateFormat.MONTH_ABBR_BEFORE_DAY -> MDVDateFormat.MonthAbbrBeforeDay
    Block.Content.DataView.DateFormat.MONTH_ABBR_AFTER_DAY -> MDVDateFormat.MonthAbbrAfterDay
    Block.Content.DataView.DateFormat.SHORT -> MDVDateFormat.Short
    Block.Content.DataView.DateFormat.SHORTUS -> MDVDateFormat.ShortUS
    Block.Content.DataView.DateFormat.ISO -> MDVDateFormat.ISO
    null -> MDVDateFormat.MonthAbbrAfterDay
}

fun Block.Content.DataView.TimeFormat?.toMiddlewareModel(): MDVTimeFormat = when (this) {
    Block.Content.DataView.TimeFormat.H12 -> MDVTimeFormat.Format12
    Block.Content.DataView.TimeFormat.H24 -> MDVTimeFormat.Format24
    null -> MDVTimeFormat.Format24
}

fun Block.Content.DataView.Viewer.ViewerRelation.toMiddlewareModel(): MDVRelation =
    MDVRelation(
        key = key,
        isVisible = isVisible,
        width = width ?: 0,
        dateFormat = dateFormat.toMiddlewareModel(),
        timeFormat = timeFormat.toMiddlewareModel(),
        dateIncludeTime = isDateIncludeTime ?: false
    )


// ---------------------- OBJECT & RELATIONS ------------------------
fun ObjectType.Layout.toMiddlewareModel(): MOTypeLayout = when (this) {
    ObjectType.Layout.BASIC -> MOTypeLayout.basic
    ObjectType.Layout.PROFILE -> MOTypeLayout.profile
    ObjectType.Layout.TODO -> MOTypeLayout.todo
    ObjectType.Layout.SET -> MOTypeLayout.set_
    ObjectType.Layout.OBJECT_TYPE -> MOTypeLayout.objectType
    ObjectType.Layout.FILE -> MOTypeLayout.file_
    ObjectType.Layout.RELATION -> MOTypeLayout.relation
    ObjectType.Layout.DASHBOARD -> MOTypeLayout.dashboard
    ObjectType.Layout.DATABASE -> MOTypeLayout.database
    ObjectType.Layout.IMAGE -> MOTypeLayout.image
    ObjectType.Layout.NOTE -> MOTypeLayout.note
    ObjectType.Layout.SPACE -> MOTypeLayout.space
}

fun Relation.Format.toMiddlewareModel(): MRelationFormat = when (this) {
    Relation.Format.SHORT_TEXT -> MRelationFormat.shorttext
    Relation.Format.LONG_TEXT -> MRelationFormat.longtext
    Relation.Format.NUMBER -> MRelationFormat.number
    Relation.Format.STATUS -> MRelationFormat.status
    Relation.Format.DATE -> MRelationFormat.date
    Relation.Format.FILE -> MRelationFormat.file_
    Relation.Format.CHECKBOX -> MRelationFormat.checkbox
    Relation.Format.URL -> MRelationFormat.url
    Relation.Format.EMAIL -> MRelationFormat.email
    Relation.Format.PHONE -> MRelationFormat.phone
    Relation.Format.EMOJI -> MRelationFormat.emoji
    Relation.Format.OBJECT -> MRelationFormat.object_
    Relation.Format.TAG -> MRelationFormat.tag
    Relation.Format.RELATIONS -> MRelationFormat.relations
}