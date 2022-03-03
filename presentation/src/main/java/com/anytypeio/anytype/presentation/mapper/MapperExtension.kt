package com.anytypeio.anytype.presentation.mapper

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.config.DebugSettings
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.dashboard.DashboardView
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.mention.createMentionMarkup
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock
import com.anytypeio.anytype.presentation.navigation.ObjectView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectLayoutView
import com.anytypeio.anytype.presentation.objects.ObjectTypeView
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.relations.type
import com.anytypeio.anytype.presentation.sets.buildGridRow
import com.anytypeio.anytype.presentation.sets.model.*
import com.anytypeio.anytype.presentation.settings.EditorSettings

fun Block.Content.File.toPictureView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode,
    isSelected: Boolean = false
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.MediaPlaceholder.Picture(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected
    )
    Block.Content.File.State.UPLOADING -> BlockView.Upload.Picture(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected
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
        isSelected = isSelected
    )
    Block.Content.File.State.ERROR -> BlockView.Error.Picture(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected
    )
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Content.File.toVideoView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode,
    isSelected: Boolean = false
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.MediaPlaceholder.Video(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected
    )
    Block.Content.File.State.UPLOADING -> BlockView.Upload.Video(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected
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
        isSelected = isSelected
    )
    Block.Content.File.State.ERROR -> BlockView.Error.Video(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected
    )
    else -> throw IllegalStateException("Unexpected state: $state")
}

fun Block.Content.File.toFileView(
    id: String,
    urlBuilder: UrlBuilder,
    indent: Int,
    mode: BlockView.Mode,
    isSelected: Boolean = false
): BlockView = when (state) {
    Block.Content.File.State.EMPTY -> BlockView.MediaPlaceholder.File(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected
    )
    Block.Content.File.State.UPLOADING -> BlockView.Upload.File(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected
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
        isSelected = isSelected
    )
    Block.Content.File.State.ERROR -> BlockView.Error.File(
        id = id,
        indent = indent,
        mode = mode,
        isSelected = isSelected
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
                val param = mark.param
                if (param.isNullOrBlank()) null
                else Markup.Mark.Object(
                    from = mark.range.first,
                    to = mark.range.last,
                    param = param
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

fun List<Block>.toDashboardViews(
    details: Block.Details = Block.Details(),
    builder: UrlBuilder,
    objectTypes: List<ObjectType> = emptyList()
): List<DashboardView> = this.mapNotNull { block ->
    when (val content = block.content) {
        is Block.Content.Link -> {
            val targetDetails = details.details[content.target]
            val typeUrl = targetDetails?.map?.type
            val type = objectTypes.find { it.url == typeUrl }
            val layoutCode = targetDetails?.layout?.toInt()
            val layout = layoutCode?.let { code ->
                ObjectType.Layout.values().find { layout ->
                    layout.code == code
                }
            }
            when (layout) {
                ObjectType.Layout.BASIC -> content.toPageView(
                    id = block.id,
                    details = details,
                    builder = builder,
                    type = type?.url,
                    typeName = type?.name,
                    layout = layout
                )
                ObjectType.Layout.SET -> content.toSetView(block.id, details, builder)
                else -> {
                    when (content.type) {
                        Block.Content.Link.Type.PAGE -> content.toPageView(
                            id = block.id,
                            details = details,
                            builder = builder,
                            type = type?.url,
                            typeName = type?.name,
                            layout = layout
                        )
                        Block.Content.Link.Type.DATA_VIEW -> content.toSetView(
                            block.id,
                            details,
                            builder
                        )
                        Block.Content.Link.Type.ARCHIVE -> content.toArchiveView(block.id, details)
                        else -> null
                    }
                }
            }
        }
        else -> null
    }
}

fun Block.Content.Link.toArchiveView(
    id: String,
    details: Block.Details
): DashboardView.Archive {
    return DashboardView.Archive(
        id = id,
        target = target,
        title = details.details[target]?.name.orEmpty()
    )
}

fun Block.Content.Link.toPageView(
    id: String,
    details: Block.Details,
    builder: UrlBuilder,
    layout: ObjectType.Layout?,
    typeName: String?,
    type: String?
): DashboardView.Document {

    val obj = ObjectWrapper.Basic(details.details[target]?.map ?: emptyMap())

    return DashboardView.Document(
        id = id,
        target = target,
        title = obj.getProperName(),
        emoji = details.details[target]?.iconEmoji?.let { unicode ->
            if (unicode.isNotEmpty()) unicode else null
        },
        image = details.details[target]?.iconImage?.let { hash ->
            if (hash.isNotEmpty()) builder.image(hash) else null
        },
        isArchived = details.details[target]?.isArchived ?: false,
        isLoading = !details.details.containsKey(target),
        typeName = typeName,
        type = type,
        layout = layout,
        done = details.details[target]?.done,
        icon = ObjectIcon.from(
            obj = obj,
            layout = layout,
            builder = builder
        )
    )
}

fun Block.Content.Link.toSetView(
    id: String,
    details: Block.Details,
    urlBuilder: UrlBuilder
): DashboardView.ObjectSet {
    val obj = ObjectWrapper.Basic(details.details[target]?.map ?: emptyMap())
    return DashboardView.ObjectSet(
        id = id,
        target = target,
        title = details.details[target]?.name,
        icon = ObjectIcon.from(
            obj = obj,
            layout = obj.layout,
            builder = urlBuilder
        ),
        isArchived = details.details[target]?.isArchived ?: false
    )
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
    else -> throw IllegalStateException("Could not extract style from block: $this")
}

fun DebugSettings.toView(): EditorSettings =
    EditorSettings(customContextMenu = this.isAnytypeContextMenuEnabled)

fun DocumentInfo.toView(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectType>
): ObjectView {
    val typeId = obj.type.firstOrNull()
    val type = objectTypes.find { it.url == typeId }
    return ObjectView(
        id = id,
        title = obj.name.orEmpty(),
        subtitle = type?.name.orEmpty(),
        icon = ObjectIcon.from(
            obj = obj,
            layout = obj.layout,
            builder = urlBuilder
        )
    )
}

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

fun SortingExpression.toDomain(): DVSort = DVSort(
    relationKey = key,
    type = when (type) {
        Viewer.SortType.ASC -> Block.Content.DataView.Sort.Type.ASC
        Viewer.SortType.DESC -> Block.Content.DataView.Sort.Type.DESC
    }
)

fun FilterExpression.toDomain(): DVFilter = DVFilter(
    relationKey = key,
    operator = operator.toDomain(),
    condition = condition.toDomain(),
    value = when (value) {
        is FilterValue.Number -> value.value
        is FilterValue.Status -> value.value
        is FilterValue.Tag -> value.value
        is FilterValue.Text -> value.value
        is FilterValue.Url -> value.value
        is FilterValue.Email -> value.value
        is FilterValue.Phone -> value.value
        is FilterValue.Date -> value.value
        is FilterValue.TextShort -> value.value
        is FilterValue.Check -> value.value
        is FilterValue.Object -> value.value
        null -> null
    }
)

fun Viewer.FilterOperator.toDomain(): DVFilterOperator = when (this) {
    Viewer.FilterOperator.And -> DVFilterOperator.AND
    Viewer.FilterOperator.Or -> DVFilterOperator.OR
}

fun Viewer.Filter.Condition.toDomain(): DVFilterCondition = when (this) {
    is Viewer.Filter.Condition.Checkbox.Equal -> DVFilterCondition.EQUAL
    is Viewer.Filter.Condition.Checkbox.NotEqual -> DVFilterCondition.NOT_EQUAL
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
    is Viewer.Filter.Condition.Number.None -> DVFilterCondition.NONE
    is Viewer.Filter.Condition.Selected.None -> DVFilterCondition.NONE
    is Viewer.Filter.Condition.Text.None -> DVFilterCondition.NONE
}

fun List<Map<String, Any?>>.filterRecordsBy(filterBy: String): List<Map<String, Any?>> =
    filter { it.containsKey(filterBy) }

fun List<Map<String, Any?>>.toGridRecordRows(
    columns: List<ColumnView>,
    relations: List<Relation>,
    types: List<ObjectType>,
    details: Map<Id, Block.Fields>,
    builder: UrlBuilder
): List<Viewer.GridView.Row> {
    val rows = mutableListOf<Viewer.GridView.Row>()
    forEach { record ->
        val row = columns.buildGridRow(
            record = record,
            relations = relations,
            details = details,
            builder = builder,
            objectTypes = types
        )
        rows.add(row)
    }
    return rows
}

// TODO maybe rename toViewerHeaders
fun List<Block.Content.DataView.Viewer.ViewerRelation>.toViewerColumns(
    relations: List<Relation>,
    filterBy: List<String>
): List<ColumnView> {
    val columns = mutableListOf<ColumnView>()
    // Adding virtual name column, whose rows should not have any content.
    columns.add(
        ColumnView(
            Relations.NAME,
            format = ColumnView.Format.SHORT_TEXT,
            isVisible = true,
            isHidden = false,
            isReadOnly = false,
            text = "",
            width = 0
        )
    )
    this.filter { it.key !in filterBy }
        .forEach { viewerRelation ->
            relations
                .firstOrNull { it.key == viewerRelation.key }
                ?.let { relation ->
                    columns.add(
                        ColumnView(
                            key = relation.key,
                            text = relation.name,
                            format = relation.format.toView(),
                            width = viewerRelation.width ?: 0,
                            isVisible = viewerRelation.isVisible,
                            isHidden = relation.isHidden,
                            isReadOnly = relation.isReadOnly,
                            isDateIncludeTime = viewerRelation.isDateIncludeTime,
                            dateFormat = viewerRelation.dateFormat,
                            timeFormat = viewerRelation.timeFormat
                        )
                    )
                }
        }
    return columns
}

fun List<Block.Content.DataView.Viewer.ViewerRelation>.toSimpleRelations(
    relations: List<Relation>
): ArrayList<SimpleRelationView> {
    val result = arrayListOf<SimpleRelationView>()
    this.forEach { viewerRelation ->
        relations
            .firstOrNull { it.key == viewerRelation.key }
            ?.let { relation ->
                result.add(
                    SimpleRelationView(
                        key = relation.key,
                        title = relation.name,
                        format = relation.format.toView(),
                        isVisible = viewerRelation.isVisible,
                        isHidden = relation.isHidden,
                        isReadonly = relation.isReadOnly,
                        isDefault = Relations.defaultRelations.contains(relation.key)
                    )
                )
            }
    }
    return result
}

fun Relation.Format.toView() = when (this) {
    Relation.Format.SHORT_TEXT -> ColumnView.Format.SHORT_TEXT
    Relation.Format.LONG_TEXT -> ColumnView.Format.LONG_TEXT
    Relation.Format.NUMBER -> ColumnView.Format.NUMBER
    Relation.Format.STATUS -> ColumnView.Format.STATUS
    Relation.Format.DATE -> ColumnView.Format.DATE
    Relation.Format.FILE -> ColumnView.Format.FILE
    Relation.Format.CHECKBOX -> ColumnView.Format.CHECKBOX
    Relation.Format.URL -> ColumnView.Format.URL
    Relation.Format.EMAIL -> ColumnView.Format.EMAIL
    Relation.Format.PHONE -> ColumnView.Format.PHONE
    Relation.Format.EMOJI -> ColumnView.Format.EMOJI
    Relation.Format.OBJECT -> ColumnView.Format.OBJECT
    Relation.Format.TAG -> ColumnView.Format.TAG
    Relation.Format.RELATIONS -> ColumnView.Format.RELATIONS
}

fun List<ObjectType>.toObjectTypeView(): List<ObjectTypeView.Item> = map { oType ->
    ObjectTypeView.Item(
        id = oType.url,
        name = oType.name,
        emoji = oType.emoji,
        description = oType.description
    )
}

fun List<ObjectType.Layout>.toView(): List<ObjectLayoutView> = map { layout ->
    when (layout) {
        ObjectType.Layout.BASIC -> ObjectLayoutView.Basic(id = layout.code, isSelected = false)
        ObjectType.Layout.PROFILE -> ObjectLayoutView.Profile(id = layout.code, isSelected = false)
        ObjectType.Layout.TODO -> ObjectLayoutView.Todo(id = layout.code, isSelected = false)
        ObjectType.Layout.SET -> ObjectLayoutView.Set(id = layout.code, isSelected = false)
        ObjectType.Layout.OBJECT_TYPE -> ObjectLayoutView.ObjectType(id = layout.code, isSelected = false)
        ObjectType.Layout.RELATION -> ObjectLayoutView.Relation(id = layout.code, isSelected = false)
        ObjectType.Layout.FILE -> ObjectLayoutView.File(id = layout.code, isSelected = false)
        ObjectType.Layout.DASHBOARD -> ObjectLayoutView.Dashboard(id = layout.code, isSelected = false)
        ObjectType.Layout.IMAGE -> ObjectLayoutView.Image(id = layout.code, isSelected = false)
        ObjectType.Layout.NOTE -> ObjectLayoutView.Note(id = layout.code, isSelected = false)
        ObjectType.Layout.DATABASE -> ObjectLayoutView.Database(id = layout.code, isSelected = false)
        ObjectType.Layout.SPACE -> ObjectLayoutView.Space(id = layout.code, isSelected = false)
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
}