package com.anytypeio.anytype.presentation.mapper

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterOperator
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.domain.config.DebugSettings
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.mention.createMentionMarkup
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock
import com.anytypeio.anytype.presentation.objects.ObjectLayoutView
import com.anytypeio.anytype.presentation.objects.ObjectTypeView
import com.anytypeio.anytype.presentation.sets.buildGridRow
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.settings.EditorSettings
import timber.log.Timber

fun Block.Content.File.toPictureView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode,
    isSelected: Boolean = false,
    background: ThemeColor,
    isPreviousBlockMedia: Boolean,
    decorations: List<BlockView.Decoration>
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.MediaPlaceholder.Picture(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected,
        background = background,
        isPreviousBlockMedia = isPreviousBlockMedia,
        decorations = decorations
    )
    Block.Content.File.State.UPLOADING -> BlockView.Upload.Picture(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected,
        background = background,
        decorations = decorations
    )
    Block.Content.File.State.DONE -> BlockView.Media.Picture(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.image(hash),
        indent = indent,
        mode = mode,
        isSelected = isSelected,
        background = background,
        decorations = decorations
    )
    Block.Content.File.State.ERROR -> BlockView.Error.Picture(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected,
        background = background,
        decorations = decorations
    )
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Content.File.toVideoView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode,
    isSelected: Boolean = false,
    background: ThemeColor,
    isPrevBlockMedia: Boolean,
    decorations: List<BlockView.Decoration>
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.MediaPlaceholder.Video(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected,
        background = background,
        isPreviousBlockMedia = isPrevBlockMedia,
        decorations = decorations
    )
    Block.Content.File.State.UPLOADING -> BlockView.Upload.Video(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected,
        background = background,
        decorations = decorations
    )
    Block.Content.File.State.DONE -> BlockView.Media.Video(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.video(hash),
        indent = indent,
        mode = mode,
        isSelected = isSelected,
        background = background,
        decorations = decorations
    )
    Block.Content.File.State.ERROR -> BlockView.Error.Video(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected,
        background = background,
        decorations
    )
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Content.File.toFileView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode,
    isSelected: Boolean = false,
    background: ThemeColor,
    isPrevBlockMedia: Boolean,
    decorations: List<BlockView.Decoration>
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.MediaPlaceholder.File(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected,
        background = background,
        isPreviousBlockMedia = isPrevBlockMedia,
        decorations = decorations
    )
    Block.Content.File.State.UPLOADING -> BlockView.Upload.File(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected,
        background = background,
        decorations = decorations
    )
    Block.Content.File.State.DONE -> BlockView.Media.File(
        id = id,
        size = size,
        name = name,
        mime = mime,
        hash = hash,
        url = urlBuilder.video(hash),
        indent = indent,
        mode = mode,
        isSelected = isSelected,
        background = background,
        decorations = decorations
    )
    Block.Content.File.State.ERROR -> BlockView.Error.File(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected,
        background = background,
        decorations = decorations
    )
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Align.toView(): Alignment = when (this) {
    Block.Align.AlignCenter -> Alignment.CENTER
    Block.Align.AlignLeft -> Alignment.START
    Block.Align.AlignRight -> Alignment.END
    else -> TODO()
}

fun Block.Content.Text.marks(
    urlBuilder: UrlBuilder,
    details: Block.Details
): List<Markup.Mark> = marks
    .filterByRange(text.length)
    .mapNotNull { mark ->
        when (mark.type) {
            Block.Content.Text.Mark.Type.ITALIC -> {
                Markup.Mark.Italic(
                    from = mark.range.first,
                    to = mark.range.last
                )
            }
            Block.Content.Text.Mark.Type.BOLD -> {
                Markup.Mark.Bold(
                    from = mark.range.first,
                    to = mark.range.last
                )
            }
            Block.Content.Text.Mark.Type.STRIKETHROUGH -> {
                Markup.Mark.Strikethrough(
                    from = mark.range.first,
                    to = mark.range.last
                )
            }
            Block.Content.Text.Mark.Type.UNDERLINE -> {
                Markup.Mark.Underline(
                    from = mark.range.first,
                    to = mark.range.last
                )
            }
            Block.Content.Text.Mark.Type.TEXT_COLOR -> {
                val color = mark.param
                if (color.isNullOrBlank()) null
                else Markup.Mark.TextColor(
                    from = mark.range.first,
                    to = mark.range.last,
                    color = color
                )
            }
            Block.Content.Text.Mark.Type.LINK -> {
                val param = mark.param
                if (param.isNullOrBlank()) null
                else Markup.Mark.Link(
                    from = mark.range.first,
                    to = mark.range.last,
                    param = param
                )
            }
            Block.Content.Text.Mark.Type.BACKGROUND_COLOR -> {
                val background = mark.param
                if (background.isNullOrBlank()) null
                else Markup.Mark.BackgroundColor(
                    from = mark.range.first,
                    to = mark.range.last,
                    background = background
                )
            }
            Block.Content.Text.Mark.Type.KEYBOARD -> {
                Markup.Mark.Keyboard(
                    from = mark.range.first,
                    to = mark.range.last
                )
            }
            Block.Content.Text.Mark.Type.MENTION -> {

                val wrapper = if (!details.details.containsKey(mark.param)) {
                    null
                } else {
                    ObjectWrapper.Basic(map = details.details[mark.param]?.map ?: emptyMap())
                }

                mark.createMentionMarkup(
                    obj = wrapper,
                    urlBuilder = urlBuilder
                )
            }
            Block.Content.Text.Mark.Type.OBJECT -> {
                val wrapper = if (!details.details.containsKey(mark.param)) {
                    null
                } else {
                    ObjectWrapper.Basic(map = details.details[mark.param]?.map ?: emptyMap())
                }
                val param = mark.param
                if (param.isNullOrBlank()) null
                else Markup.Mark.Object(
                    from = mark.range.first,
                    to = mark.range.last,
                    param = param,
                    isArchived = wrapper?.isArchived == true
                )
            }
            else -> null
        }
    }

fun List<Block.Content.Text.Mark>.filterByRange(textLength: Int): List<Block.Content.Text.Mark> {
    return this.mapNotNull { mark ->
        when {
            mark.range.first >= textLength -> null
            mark.range.first == mark.range.last -> null
            mark.range.last < 0 -> null
            else -> {
                var result = mark
                if (result.range.first < 0) {
                    result = result.copy(range = 0..result.range.last)
                }
                if (mark.range.last > textLength) {
                    result = result.copy(range = result.range.first..textLength)
                }
                if (result.range.first > mark.range.last) {
                    val from = result.range.first
                    val to = mark.range.last
                    result = result.copy(range = to..from)
                }
                return@mapNotNull result
            }
        }
    }
}

fun UiBlock.style(): Block.Content.Text.Style = when (this) {
    UiBlock.TEXT -> Block.Content.Text.Style.P
    UiBlock.HEADER_ONE -> Block.Content.Text.Style.H1
    UiBlock.HEADER_TWO -> Block.Content.Text.Style.H2
    UiBlock.HEADER_THREE -> Block.Content.Text.Style.H3
    UiBlock.HIGHLIGHTED -> Block.Content.Text.Style.QUOTE
    UiBlock.CHECKBOX -> Block.Content.Text.Style.CHECKBOX
    UiBlock.BULLETED -> Block.Content.Text.Style.BULLET
    UiBlock.NUMBERED -> Block.Content.Text.Style.NUMBERED
    UiBlock.TOGGLE -> Block.Content.Text.Style.TOGGLE
    UiBlock.CODE -> Block.Content.Text.Style.CODE_SNIPPET
    UiBlock.CALLOUT -> Block.Content.Text.Style.CALLOUT
    UiBlock.PAGE -> throw IllegalStateException("Could not extract style from block: $this")
    UiBlock.LINK_TO_OBJECT -> throw IllegalStateException("Could not extract style from block: $this")
    UiBlock.FILE -> throw IllegalStateException("Could not extract style from block: $this")
    UiBlock.IMAGE -> throw IllegalStateException("Could not extract style from block: $this")
    UiBlock.VIDEO -> throw IllegalStateException("Could not extract style from block: $this")
    UiBlock.BOOKMARK -> throw IllegalStateException("Could not extract style from block: $this")
    UiBlock.LINE_DIVIDER -> throw IllegalStateException("Could not extract style from block: $this")
    UiBlock.THREE_DOTS -> throw IllegalStateException("Could not extract style from block: $this")
    UiBlock.RELATION -> throw IllegalStateException("Could not extract style from block: $this")
}

fun DebugSettings.toView(): EditorSettings =
    EditorSettings(customContextMenu = this.isAnytypeContextMenuEnabled)

fun Block.Fields.getName(): String =
    this.name.let { name ->
        if (name.isNullOrBlank()) Relations.RELATION_NAME_EMPTY else name
    }

fun Markup.Mark.mark(): Block.Content.Text.Mark = when (this) {
    is Markup.Mark.Bold -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.BOLD
    )
    is Markup.Mark.Italic -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.ITALIC
    )
    is Markup.Mark.Underline -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.UNDERLINE
    )
    is Markup.Mark.Strikethrough -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
    )
    is Markup.Mark.TextColor -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.TEXT_COLOR,
        param = color
    )
    is Markup.Mark.BackgroundColor -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.BACKGROUND_COLOR,
        param = background
    )
    is Markup.Mark.Link -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.LINK,
        param = param
    )
    is Markup.Mark.Keyboard -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.KEYBOARD
    )
    is Markup.Mark.Mention -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.MENTION,
        param = param
    )
    is Markup.Mark.Object -> Block.Content.Text.Mark(
        range = from..to,
        type = Block.Content.Text.Mark.Type.OBJECT,
        param = param
    )
}

fun Block.Content.DataView.Sort.Type.toView(): Viewer.SortType = when (this) {
    Block.Content.DataView.Sort.Type.ASC -> Viewer.SortType.ASC
    Block.Content.DataView.Sort.Type.DESC -> Viewer.SortType.DESC
    Block.Content.DataView.Sort.Type.CUSTOM -> Viewer.SortType.CUSTOM
}

fun DVFilterOperator.toView(): Viewer.FilterOperator = when (this) {
    Block.Content.DataView.Filter.Operator.AND -> Viewer.FilterOperator.And
    Block.Content.DataView.Filter.Operator.OR -> Viewer.FilterOperator.Or
}

fun DVFilterCondition.toTextView(): Viewer.Filter.Condition.Text = when (this) {
    DVFilterCondition.EQUAL -> Viewer.Filter.Condition.Text.Equal()
    DVFilterCondition.NOT_EQUAL -> Viewer.Filter.Condition.Text.NotEqual()
    DVFilterCondition.LIKE -> Viewer.Filter.Condition.Text.Like()
    DVFilterCondition.NOT_LIKE -> Viewer.Filter.Condition.Text.NotLike()
    DVFilterCondition.EMPTY -> Viewer.Filter.Condition.Text.Empty()
    DVFilterCondition.NOT_EMPTY -> Viewer.Filter.Condition.Text.NotEmpty()
    DVFilterCondition.NONE -> Viewer.Filter.Condition.Text.None()
    else -> throw IllegalStateException("Unexpected filter condition $this for Text relations")
}


fun DVFilterCondition.toDateView(): Viewer.Filter.Condition.Date = when (this) {
    DVFilterCondition.EQUAL -> Viewer.Filter.Condition.Date.Equal()
    DVFilterCondition.GREATER -> Viewer.Filter.Condition.Date.Greater()
    DVFilterCondition.LESS -> Viewer.Filter.Condition.Date.Less()
    DVFilterCondition.GREATER_OR_EQUAL -> Viewer.Filter.Condition.Date.GreaterOrEqual()
    DVFilterCondition.LESS_OR_EQUAL -> Viewer.Filter.Condition.Date.LessOrEqual()
    DVFilterCondition.IN -> Viewer.Filter.Condition.Date.In()
    DVFilterCondition.EMPTY -> Viewer.Filter.Condition.Date.Empty()
    DVFilterCondition.NOT_EMPTY -> Viewer.Filter.Condition.Date.NotEmpty()
    DVFilterCondition.NONE -> Viewer.Filter.Condition.Date.None()
    else -> throw IllegalStateException("Unexpected filter condition $this for Number or Date relations")
}

fun DVFilterCondition.toNumberView(): Viewer.Filter.Condition.Number = when (this) {
    DVFilterCondition.EQUAL -> Viewer.Filter.Condition.Number.Equal()
    DVFilterCondition.NOT_EQUAL -> Viewer.Filter.Condition.Number.NotEqual()
    DVFilterCondition.GREATER -> Viewer.Filter.Condition.Number.Greater()
    DVFilterCondition.LESS -> Viewer.Filter.Condition.Number.Less()
    DVFilterCondition.GREATER_OR_EQUAL -> Viewer.Filter.Condition.Number.GreaterOrEqual()
    DVFilterCondition.LESS_OR_EQUAL -> Viewer.Filter.Condition.Number.LessOrEqual()
    DVFilterCondition.EMPTY -> Viewer.Filter.Condition.Number.Empty()
    DVFilterCondition.NOT_EMPTY -> Viewer.Filter.Condition.Number.NotEmpty()
    DVFilterCondition.NONE -> Viewer.Filter.Condition.Number.None()
    else -> throw IllegalStateException("Unexpected filter condition $this for Number or Date relations")
}

fun DVFilterCondition.toSelectedView(): Viewer.Filter.Condition.Selected = when (this) {
    DVFilterCondition.IN -> Viewer.Filter.Condition.Selected.In()
    DVFilterCondition.ALL_IN -> Viewer.Filter.Condition.Selected.AllIn()
    DVFilterCondition.EQUAL -> Viewer.Filter.Condition.Selected.Equal()
    DVFilterCondition.NOT_IN -> Viewer.Filter.Condition.Selected.NotIn()
    DVFilterCondition.EMPTY -> Viewer.Filter.Condition.Selected.Empty()
    DVFilterCondition.NOT_EMPTY -> Viewer.Filter.Condition.Selected.NotEmpty()
    DVFilterCondition.NONE -> Viewer.Filter.Condition.Selected.None()
    else -> throw IllegalStateException("Unexpected filter condition $this for Selected relations")
}

fun DVFilterCondition.toCheckboxView(): Viewer.Filter.Condition.Checkbox = when (this) {
    DVFilterCondition.EQUAL -> Viewer.Filter.Condition.Checkbox.Equal()
    DVFilterCondition.NOT_EQUAL -> Viewer.Filter.Condition.Checkbox.NotEqual()
    DVFilterCondition.NONE -> Viewer.Filter.Condition.Checkbox.None()
    else -> throw IllegalStateException("Unexpected filter condition $this for Checkbox relations")
}

fun Viewer.FilterOperator.toDomain(): DVFilterOperator = when (this) {
    Viewer.FilterOperator.And -> DVFilterOperator.AND
    Viewer.FilterOperator.Or -> DVFilterOperator.OR
}

fun Viewer.Filter.Condition.toDomain(): DVFilterCondition = when (this) {
    is Viewer.Filter.Condition.Checkbox.Equal -> DVFilterCondition.EQUAL
    is Viewer.Filter.Condition.Checkbox.NotEqual -> DVFilterCondition.NOT_EQUAL
    is Viewer.Filter.Condition.Date.Equal -> DVFilterCondition.EQUAL
    is Viewer.Filter.Condition.Date.Greater -> DVFilterCondition.GREATER
    is Viewer.Filter.Condition.Date.GreaterOrEqual -> DVFilterCondition.GREATER_OR_EQUAL
    is Viewer.Filter.Condition.Date.Less -> DVFilterCondition.LESS
    is Viewer.Filter.Condition.Date.LessOrEqual -> DVFilterCondition.LESS_OR_EQUAL
    is Viewer.Filter.Condition.Date.In -> DVFilterCondition.IN
    is Viewer.Filter.Condition.Date.Empty -> DVFilterCondition.EMPTY
    is Viewer.Filter.Condition.Date.NotEmpty -> DVFilterCondition.NOT_EMPTY
    is Viewer.Filter.Condition.Number.Equal -> DVFilterCondition.EQUAL
    is Viewer.Filter.Condition.Number.Greater -> DVFilterCondition.GREATER
    is Viewer.Filter.Condition.Number.GreaterOrEqual -> DVFilterCondition.GREATER_OR_EQUAL
    is Viewer.Filter.Condition.Number.Less -> DVFilterCondition.LESS
    is Viewer.Filter.Condition.Number.LessOrEqual -> DVFilterCondition.LESS_OR_EQUAL
    is Viewer.Filter.Condition.Number.NotEqual -> DVFilterCondition.NOT_EQUAL
    is Viewer.Filter.Condition.Number.Empty -> DVFilterCondition.EMPTY
    is Viewer.Filter.Condition.Number.NotEmpty -> DVFilterCondition.NOT_EMPTY
    is Viewer.Filter.Condition.Selected.AllIn -> DVFilterCondition.ALL_IN
    is Viewer.Filter.Condition.Selected.Empty -> DVFilterCondition.EMPTY
    is Viewer.Filter.Condition.Selected.Equal -> DVFilterCondition.EQUAL
    is Viewer.Filter.Condition.Selected.In -> DVFilterCondition.IN
    is Viewer.Filter.Condition.Selected.NotEmpty -> DVFilterCondition.NOT_EMPTY
    is Viewer.Filter.Condition.Selected.NotIn -> DVFilterCondition.NOT_IN
    is Viewer.Filter.Condition.Text.Empty -> DVFilterCondition.EMPTY
    is Viewer.Filter.Condition.Text.Equal -> DVFilterCondition.EQUAL
    is Viewer.Filter.Condition.Text.Like -> DVFilterCondition.LIKE
    is Viewer.Filter.Condition.Text.NotEmpty -> DVFilterCondition.NOT_EMPTY
    is Viewer.Filter.Condition.Text.NotEqual -> DVFilterCondition.NOT_EQUAL
    is Viewer.Filter.Condition.Text.NotLike -> DVFilterCondition.NOT_LIKE
    is Viewer.Filter.Condition.Checkbox.None -> DVFilterCondition.NONE
    is Viewer.Filter.Condition.Date.None -> DVFilterCondition.NONE
    is Viewer.Filter.Condition.Number.None -> DVFilterCondition.NONE
    is Viewer.Filter.Condition.Selected.None -> DVFilterCondition.NONE
    is Viewer.Filter.Condition.Text.None -> DVFilterCondition.NONE
}

suspend fun List<Id>.toGridRecordRows(
    showIcon: Boolean,
    columns: List<ColumnView>,
    relations: List<ObjectWrapper.Relation>,
    details: Map<Id, Block.Fields>,
    builder: UrlBuilder,
    store: ObjectStore,
): List<Viewer.GridView.Row> {
    val rows = mutableListOf<Viewer.GridView.Row>()
    forEach { id ->
        val record = store.get(id)
        if (record != null) {
            val row = columns.buildGridRow(
                showIcon = showIcon,
                obj = record,
                relations = relations,
                store = store,
                builder = builder,
                details = details
            )
            rows.add(row)
        } else {
            Timber.w("Could not found record with id: $id")
        }
    }
    return rows
}

// TODO maybe rename toViewerHeaders
fun List<Block.Content.DataView.Viewer.ViewerRelation>.toViewerColumns(
    relations: List<ObjectWrapper.Relation>,
    filterBy: List<String>
): List<ColumnView> {
    val columns = mutableListOf<ColumnView>()
    this.filter { it.key !in filterBy }
        .forEach { viewerRelation ->
            relations
                .firstOrNull { it.key == viewerRelation.key }
                ?.let { relation ->
                    columns.add(
                        ColumnView(
                            key = relation.key,
                            text = relation.name.orEmpty(),
                            format = relation.format.toView(),
                            width = viewerRelation.width ?: 0,
                            isVisible = viewerRelation.isVisible,
                            isHidden = relation.isHidden ?: false,
                            isReadOnly = relation.isReadonlyValue,
                            isDateIncludeTime = viewerRelation.isDateIncludeTime,
                            dateFormat = viewerRelation.dateFormat,
                            timeFormat = viewerRelation.timeFormat
                        )
                    )
                }
        }
    return columns
}

fun List<Block.Content.DataView.Viewer.ViewerRelation>.toSimpleRelationView(
    relations: List<ObjectWrapper.Relation>
): ArrayList<SimpleRelationView> {
    val result = arrayListOf<SimpleRelationView>()
    this.forEach { viewerRelation ->
        relations
            .firstOrNull { it.key == viewerRelation.key }
            ?.let { relation ->
                result.add(
                    SimpleRelationView(
                        key = relation.key,
                        title = relation.name.orEmpty(),
                        format = relation.format.toView(),
                        isVisible = viewerRelation.isVisible,
                        isHidden = relation.isHidden ?: false,
                        isReadonly = relation.isReadonlyValue,
                        isDefault = Relations.systemRelationKeys.contains(viewerRelation.key)
                    )
                )
            }
    }
    return result
}

fun RelationFormat.toView() = when (this) {
    RelationFormat.SHORT_TEXT -> ColumnView.Format.SHORT_TEXT
    RelationFormat.LONG_TEXT -> ColumnView.Format.LONG_TEXT
    RelationFormat.NUMBER -> ColumnView.Format.NUMBER
    RelationFormat.STATUS -> ColumnView.Format.STATUS
    RelationFormat.DATE -> ColumnView.Format.DATE
    RelationFormat.FILE -> ColumnView.Format.FILE
    RelationFormat.CHECKBOX -> ColumnView.Format.CHECKBOX
    RelationFormat.URL -> ColumnView.Format.URL
    RelationFormat.EMAIL -> ColumnView.Format.EMAIL
    RelationFormat.PHONE -> ColumnView.Format.PHONE
    RelationFormat.EMOJI -> ColumnView.Format.EMOJI
    RelationFormat.OBJECT -> ColumnView.Format.OBJECT
    RelationFormat.TAG -> ColumnView.Format.TAG
    RelationFormat.RELATIONS -> ColumnView.Format.RELATIONS
    RelationFormat.UNDEFINED -> ColumnView.Format.UNDEFINED
}

fun ObjectWrapper.Type.toObjectTypeView(selectedSources: List<Id> = emptyList()): ObjectTypeView =
    ObjectTypeView(
        id = id,
        key = uniqueKey,
        name = name.orEmpty(),
        emoji = iconEmoji,
        description = description,
        isSelected = selectedSources.contains(id)
    )

fun List<ObjectType.Layout>.toView(): List<ObjectLayoutView> = mapNotNull { layout ->
    when (layout) {
        ObjectType.Layout.BASIC -> ObjectLayoutView.Basic(id = layout.code, isSelected = false)
        ObjectType.Layout.PROFILE -> ObjectLayoutView.Profile(id = layout.code, isSelected = false)
        ObjectType.Layout.TODO -> ObjectLayoutView.Todo(id = layout.code, isSelected = false)
        ObjectType.Layout.SET -> ObjectLayoutView.Set(id = layout.code, isSelected = false)
        ObjectType.Layout.OBJECT_TYPE -> ObjectLayoutView.ObjectType(
            id = layout.code,
            isSelected = false
        )
        ObjectType.Layout.RELATION -> ObjectLayoutView.Relation(
            id = layout.code,
            isSelected = false
        )
        ObjectType.Layout.FILE -> ObjectLayoutView.File(id = layout.code, isSelected = false)
        ObjectType.Layout.DASHBOARD -> ObjectLayoutView.Dashboard(
            id = layout.code,
            isSelected = false
        )
        ObjectType.Layout.IMAGE -> ObjectLayoutView.Image(id = layout.code, isSelected = false)
        ObjectType.Layout.NOTE -> ObjectLayoutView.Note(id = layout.code, isSelected = false)
        ObjectType.Layout.DATABASE -> ObjectLayoutView.Database(
            id = layout.code,
            isSelected = false
        )
        ObjectType.Layout.SPACE -> ObjectLayoutView.Space(id = layout.code, isSelected = false)
        ObjectType.Layout.BOOKMARK -> ObjectLayoutView.Bookmark(
            id = layout.code,
            isSelected = false
        )
        else -> null
    }
}

fun ObjectLayoutView.toObjectLayout() = when (this) {
    is ObjectLayoutView.Basic -> ObjectType.Layout.BASIC
    is ObjectLayoutView.Dashboard -> ObjectType.Layout.DASHBOARD
    is ObjectLayoutView.Database -> ObjectType.Layout.DATABASE
    is ObjectLayoutView.File -> ObjectType.Layout.FILE
    is ObjectLayoutView.Image -> ObjectType.Layout.IMAGE
    is ObjectLayoutView.Note -> ObjectType.Layout.NOTE
    is ObjectLayoutView.ObjectType -> ObjectType.Layout.OBJECT_TYPE
    is ObjectLayoutView.Profile -> ObjectType.Layout.PROFILE
    is ObjectLayoutView.Relation -> ObjectType.Layout.RELATION
    is ObjectLayoutView.Set -> ObjectType.Layout.SET
    is ObjectLayoutView.Space -> ObjectType.Layout.SPACE
    is ObjectLayoutView.Todo -> ObjectType.Layout.TODO
    is ObjectLayoutView.Bookmark -> ObjectType.Layout.BOOKMARK
}