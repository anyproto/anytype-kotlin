package com.anytypeio.anytype.middleware.mappers

import anytype.ResponseEvent
import anytype.SmartBlockType
import anytype.model.ObjectInfo
import anytype.model.ObjectInfoWithLinks
import anytype.model.ObjectLinksInfo
import anytype.relation.RelationFormat
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.middleware.interactor.toCoreModels
import timber.log.Timber

// ---------------------- PAYLOAD ------------------------
fun ResponseEvent?.toPayload(): Payload {
    checkNotNull(this)
    val context = contextId

    return Payload(
        context = context,
        events = messages.mapNotNull { it.toCoreModels(context) }
    )
}


// ---------------------- BLOCKS ------------------------
fun List<MBlock>.toCoreModels(
    types: Map<String, SmartBlockType> = emptyMap()
): List<Block> = mapNotNull { block ->
    when {
        block.text != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreModelsText()
            )
        }
        block.layout != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreModelsLayout()
            )
        }
        block.link != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreModelsLink()
            )
        }
        block.div != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreModelsDivider()
            )
        }
        block.file_ != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreModelsFile()
            )
        }
        block.icon != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreModelsIcon()
            )
        }
        block.bookmark != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreModelsBookmark()
            )
        }
        block.smartblock != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = Block.Content.Smart(
                    type = types[block.id]?.toCoreModelsBlock()
                        ?: throw IllegalStateException("Type missing")
                )
            )
        }
        block.dataview != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreModelsDataView()
            )
        }
        block.relation != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreModelsRelationBlock()
            )
        }
        else -> {
            Timber.d("Ignoring content type: $block")
            null
        }
    }
}

fun Map<String, *>?.toCoreModel(): Block.Fields = Block.Fields(this?.toMap().orEmpty())

fun MBlock.toCoreModelsText(): Block.Content.Text {
    val content = checkNotNull(text)
    return Block.Content.Text(
        text = content.text,
        style = content.style.toCoreModels(),
        marks = content.marks?.marks?.map { it.toCoreModels() }.orEmpty(),
        isChecked = content.checked,
        color = content.color.ifEmpty { null },
        backgroundColor = backgroundColor.ifEmpty { null },
        align = align.toCoreModelsAlign()
    )
}

fun MBlock.toCoreModelsLayout(): Block.Content.Layout {
    val content = checkNotNull(layout)
    return Block.Content.Layout(
        type = content.style.toCoreModels()
    )
}

fun MBlock.toCoreModelsLink(): Block.Content.Link {
    val content = checkNotNull(link)
    return Block.Content.Link(
        target = content.targetBlockId,
        fields = Block.Fields(content.fields?.toMap().orEmpty()),
        type = content.style.toCoreModels()
    )
}

fun MBlock.toCoreModelsDivider(): Block.Content.Divider {
    val content = checkNotNull(div)
    return Block.Content.Divider(
        style = content.style.toCoreModels()
    )
}

fun MBlock.toCoreModelsFile(): Block.Content.File {
    val content = checkNotNull(file_)
    return Block.Content.File(
        hash = content.hash,
        name = content.name,
        mime = content.mime,
        size = content.size,
        type = content.type.toCoreModels(),
        state = content.state.toCoreModels()
    )
}

fun MBlock.toCoreModelsIcon(): Block.Content.Icon {
    val content = checkNotNull(icon)
    return Block.Content.Icon(
        name = content.name
    )
}

fun MBlock.toCoreModelsBookmark(): Block.Content.Bookmark {
    val content = checkNotNull(bookmark)
    return Block.Content.Bookmark(
        url = content.url.ifEmpty { null },
        title = content.title.ifEmpty { null },
        description = content.description.ifEmpty { null },
        image = content.description.ifEmpty { null },
        favicon = content.faviconHash.ifEmpty { null }
    )
}

fun MBlock.toCoreModelsDataView(): Block.Content.DataView {
    val content = checkNotNull(dataview)
    return Block.Content.DataView(
        source = content.source,
        viewers = content.views.map { it.toCoreModels() },
        relations = content.relations.map { it.toCoreModels() }
    )
}

fun MBlock.toCoreModelsRelationBlock() : Block.Content.RelationBlock {
    val content = checkNotNull(relation)
    return Block.Content.RelationBlock(
        key = content.key.ifEmpty { null },
        background = backgroundColor
    )
}

fun SmartBlockType.toCoreModelsBlock(): Block.Content.Smart.Type = when (this) {
    SmartBlockType.Page -> Block.Content.Smart.Type.PAGE
    SmartBlockType.Home -> Block.Content.Smart.Type.HOME
    SmartBlockType.ProfilePage -> Block.Content.Smart.Type.PROFILE
    SmartBlockType.Archive -> Block.Content.Smart.Type.ARCHIVE
    SmartBlockType.Breadcrumbs -> Block.Content.Smart.Type.BREADCRUMBS
    SmartBlockType.Set -> Block.Content.Smart.Type.SET
    else -> throw IllegalStateException("Unexpected smart block type: $this")
}

fun SmartBlockType.toCoreModelsEvent(): Event.Command.ShowBlock.Type = when (this) {
    SmartBlockType.Page -> Event.Command.ShowBlock.Type.PAGE
    SmartBlockType.Home -> Event.Command.ShowBlock.Type.HOME
    SmartBlockType.ProfilePage -> Event.Command.ShowBlock.Type.PROFILE_PAGE
    SmartBlockType.Archive -> Event.Command.ShowBlock.Type.ARCHIVE
    SmartBlockType.Breadcrumbs -> Event.Command.ShowBlock.Type.BREADCRUMBS
    SmartBlockType.Set -> Event.Command.ShowBlock.Type.SET
    else -> throw IllegalStateException("Unexpected smart block type: $this")
}

fun MBFileState.toCoreModels(): Block.Content.File.State = when (this) {
    MBFileState.Empty -> Block.Content.File.State.EMPTY
    MBFileState.Uploading -> Block.Content.File.State.UPLOADING
    MBFileState.Done -> Block.Content.File.State.DONE
    MBFileState.Error -> Block.Content.File.State.ERROR
}

fun MBFileType.toCoreModels(): Block.Content.File.Type = when (this) {
    MBFileType.None -> Block.Content.File.Type.NONE
    MBFileType.File -> Block.Content.File.Type.FILE
    MBFileType.Image -> Block.Content.File.Type.IMAGE
    MBFileType.Video -> Block.Content.File.Type.VIDEO
}

fun MBDivStyle.toCoreModels(): Block.Content.Divider.Style = when (this) {
    MBDivStyle.Line -> Block.Content.Divider.Style.LINE
    MBDivStyle.Dots -> Block.Content.Divider.Style.DOTS
}

fun MBLinkStyle.toCoreModels(): Block.Content.Link.Type = when (this) {
    MBLinkStyle.Page -> Block.Content.Link.Type.PAGE
    MBLinkStyle.Dataview -> Block.Content.Link.Type.DATA_VIEW
    MBLinkStyle.Dashboard -> Block.Content.Link.Type.DASHBOARD
    MBLinkStyle.Archive -> Block.Content.Link.Type.ARCHIVE
}

fun MBlock.toCoreModelsFields(): Block.Fields = Block.Fields(fields?.toMap().orEmpty())

fun MBMark.toCoreModels(): Block.Content.Text.Mark {
    return Block.Content.Text.Mark(
        range = IntRange(
            start = range?.from ?: 0,
            endInclusive = range?.to ?: 0
        ),
        type = type.toCoreModels(),
        param = param_
    )
}

fun MBLayoutStyle.toCoreModels(): Block.Content.Layout.Type = when (this) {
    MBLayoutStyle.Row -> Block.Content.Layout.Type.ROW
    MBLayoutStyle.Column -> Block.Content.Layout.Type.COLUMN
    MBLayoutStyle.Div -> Block.Content.Layout.Type.DIV
    MBLayoutStyle.Header -> Block.Content.Layout.Type.HEADER
}

fun MBAlign.toCoreModelsAlign(): Block.Align = when (this) {
    MBAlign.AlignLeft -> Block.Align.AlignLeft
    MBAlign.AlignCenter -> Block.Align.AlignCenter
    MBAlign.AlignRight -> Block.Align.AlignRight
}

fun MBTextStyle.toCoreModels(): Block.Content.Text.Style = when (this) {
    MBTextStyle.Paragraph -> Block.Content.Text.Style.P
    MBTextStyle.Header1 -> Block.Content.Text.Style.H1
    MBTextStyle.Header2 -> Block.Content.Text.Style.H2
    MBTextStyle.Header3 -> Block.Content.Text.Style.H3
    MBTextStyle.Header4 -> Block.Content.Text.Style.H4
    MBTextStyle.Quote -> Block.Content.Text.Style.QUOTE
    MBTextStyle.Code -> Block.Content.Text.Style.CODE_SNIPPET
    MBTextStyle.Title -> Block.Content.Text.Style.TITLE
    MBTextStyle.Checkbox -> Block.Content.Text.Style.CHECKBOX
    MBTextStyle.Numbered -> Block.Content.Text.Style.NUMBERED
    MBTextStyle.Toggle -> Block.Content.Text.Style.TOGGLE
    MBTextStyle.Marked -> Block.Content.Text.Style.BULLET
}

fun MBMarkType.toCoreModels(): Block.Content.Text.Mark.Type = when (this) {
    MBMarkType.Strikethrough -> Block.Content.Text.Mark.Type.STRIKETHROUGH
    MBMarkType.Keyboard -> Block.Content.Text.Mark.Type.KEYBOARD
    MBMarkType.Italic -> Block.Content.Text.Mark.Type.ITALIC
    MBMarkType.Bold -> Block.Content.Text.Mark.Type.BOLD
    MBMarkType.Underscored -> Block.Content.Text.Mark.Type.UNDERSCORED
    MBMarkType.Link -> Block.Content.Text.Mark.Type.LINK
    MBMarkType.TextColor -> Block.Content.Text.Mark.Type.TEXT_COLOR
    MBMarkType.BackgroundColor -> Block.Content.Text.Mark.Type.BACKGROUND_COLOR
    MBMarkType.Mention -> Block.Content.Text.Mark.Type.MENTION
}


// ---------------------- DATA VIEW ------------------------
fun MDVView.toCoreModels(): DVViewer = DVViewer(
    id = id,
    name = name,
    type = type.toCoreModels(),
    sorts = sorts.map { it.toCoreModels() },
    filters = filters.map { it.toCoreModels() },
    viewerRelations = relations.map { it.toCoreModels() }
)

fun MDVRelation.toCoreModels(): DVViewerRelation = DVViewerRelation(
    key = key,
    isDateIncludeTime = dateIncludeTime,
    isVisible = isVisible,
    width = width,
    dateFormat = dateFormat.toCoreModels(),
    timeFormat = timeFormat.toCoreModels()
)

fun MDVDateFormat.toCoreModels(): DVDateFormat = when (this) {
    MDVDateFormat.MonthAbbrBeforeDay -> DVDateFormat.MONTH_ABBR_BEFORE_DAY
    MDVDateFormat.MonthAbbrAfterDay -> DVDateFormat.MONTH_ABBR_AFTER_DAY
    MDVDateFormat.Short -> DVDateFormat.SHORT
    MDVDateFormat.ShortUS -> DVDateFormat.SHORTUS
    MDVDateFormat.ISO -> DVDateFormat.ISO
}

fun MDVTimeFormat.toCoreModels(): DVTimeFormat = when (this) {
    MDVTimeFormat.Format12 -> DVTimeFormat.H12
    MDVTimeFormat.Format24 -> DVTimeFormat.H24
}

fun MDVViewType.toCoreModels(): DVViewerType = when (this) {
    MDVViewType.Table -> DVViewerType.GRID
    MDVViewType.List -> DVViewerType.LIST
    MDVViewType.Gallery -> DVViewerType.GALLERY
    MDVViewType.Kanban -> DVViewerType.BOARD
}

fun MDVFilter.toCoreModels(): DVFilter = DVFilter(
    relationKey = RelationKey,
    operator = operator_.toCoreModels(),
    condition = condition.toCoreModels(),
    value = value
)

fun MDVFilterCondition.toCoreModels(): DVFilterCondition = when (this) {
    MDVFilterCondition.Equal -> DVFilterCondition.EQUAL
    MDVFilterCondition.NotEqual -> DVFilterCondition.NOT_EQUAL
    MDVFilterCondition.Greater -> DVFilterCondition.GREATER
    MDVFilterCondition.Less -> DVFilterCondition.LESS
    MDVFilterCondition.GreaterOrEqual -> DVFilterCondition.GREATER_OR_EQUAL
    MDVFilterCondition.LessOrEqual -> DVFilterCondition.LESS_OR_EQUAL
    MDVFilterCondition.Like -> DVFilterCondition.LIKE
    MDVFilterCondition.NotLike -> DVFilterCondition.NOT_LIKE
    MDVFilterCondition.In -> DVFilterCondition.IN
    MDVFilterCondition.NotIn -> DVFilterCondition.NOT_IN
    MDVFilterCondition.Empty -> DVFilterCondition.EMPTY
    MDVFilterCondition.NotEmpty -> DVFilterCondition.NOT_EMPTY
    MDVFilterCondition.AllIn -> DVFilterCondition.ALL_IN
    MDVFilterCondition.NotAllIn -> DVFilterCondition.NOT_ALL_IN
}

fun MDVFilterOperator.toCoreModels(): DVFilterOperator = when (this) {
    MDVFilterOperator.And -> DVFilterOperator.AND
    MDVFilterOperator.Or -> DVFilterOperator.OR
}

fun MDVSort.toCoreModels(): Block.Content.DataView.Sort = DVSort(
    relationKey = RelationKey,
    type = type.toCoreModels()
)

fun MDVSortType.toCoreModels(): DVSortType = when (this) {
    MDVSortType.Asc -> DVSortType.ASC
    MDVSortType.Desc -> DVSortType.DESC
}


// ---------------------- OBJECT & RELATIONS ------------------------
fun MRelation.toCoreModels(): Relation = Relation(
    key = key,
    name = name,
    isHidden = hidden,
    isReadOnly = readOnly,
    isMulti = multi,
    defaultValue = defaultValue,
    selections = selectDict.map { it.option() },
    objectTypes = objectTypes,
    source = dataSource.source(),
    format = format.format()
)

fun MObjectType.toCoreModels(): ObjectType = ObjectType(
    url = url,
    name = name,
    emoji = iconEmoji,
    description = description,
    isHidden = hidden,
    relations = relations.map { it.toCoreModels() },
    layout = layout.toCoreModels()
)

fun MOTypeLayout.toCoreModels(): ObjectType.Layout = when (this) {
    MOTypeLayout.basic -> ObjectType.Layout.PAGE
    MOTypeLayout.profile -> ObjectType.Layout.PROFILE
    MOTypeLayout.todo -> ObjectType.Layout.TODO
    MOTypeLayout.set_ -> ObjectType.Layout.SET
    MOTypeLayout.objectType -> ObjectType.Layout.OBJECT
    MOTypeLayout.file_ -> ObjectType.Layout.FILE
    MOTypeLayout.relation -> ObjectType.Layout.RELATION
    MOTypeLayout.dashboard -> ObjectType.Layout.DASHBOARD
    MOTypeLayout.database -> ObjectType.Layout.DATABASE
    MOTypeLayout.image -> ObjectType.Layout.IMAGE
}

fun MRelationDataSource.source(): Relation.Source = when (this) {
    MRelationDataSource.details -> Relation.Source.DETAILS
    MRelationDataSource.derived -> Relation.Source.DERIVED
    MRelationDataSource.account -> Relation.Source.ACCOUNT
    else -> throw IllegalStateException()
}

fun RelationFormat.format(): Relation.Format = when (this) {
    RelationFormat.shorttext -> Relation.Format.SHORT_TEXT
    RelationFormat.longtext -> Relation.Format.LONG_TEXT
    RelationFormat.number -> Relation.Format.NUMBER
    RelationFormat.date -> Relation.Format.DATE
    RelationFormat.file_ -> Relation.Format.FILE
    RelationFormat.checkbox -> Relation.Format.CHECKBOX
    RelationFormat.url -> Relation.Format.URL
    RelationFormat.email -> Relation.Format.EMAIL
    RelationFormat.phone -> Relation.Format.PHONE
    RelationFormat.emoji -> Relation.Format.EMOJI
    RelationFormat.object_ -> Relation.Format.OBJECT
    RelationFormat.status -> Relation.Format.STATUS
    RelationFormat.tag -> Relation.Format.TAG
    RelationFormat.relations -> Relation.Format.RELATIONS
}

fun MRelationOption.option(): Relation.Option = Relation.Option(
    id = id,
    text = text,
    color = color
)

// ---------------------- NAVIGATION & SEARCH ------------------------
fun ObjectInfoWithLinks.toCoreModel(): PageInfoWithLinks {
    val i = info
    checkNotNull(i)
    return PageInfoWithLinks(
        id = id,
        links = links?.toCoreModel() ?: PageLinks(emptyList(), emptyList()),
        documentInfo = i.toCoreModel()
    )
}

fun ObjectLinksInfo.toCoreModel(): PageLinks = PageLinks(
    inbound = inbound.map { it.toCoreModel() },
    outbound = outbound.map { it.toCoreModel() }
)

fun ObjectInfo.toCoreModel(): DocumentInfo = DocumentInfo(
    id = id,
    fields = details.toCoreModel(),
    snippet = snippet,
    hasInboundLinks = hasInboundLinks,
    type = objectType.toCoreModel()
)

fun ObjectInfo.Type.toCoreModel(): DocumentInfo.Type = when (this) {
    ObjectInfo.Type.Page -> DocumentInfo.Type.PAGE
    ObjectInfo.Type.Home -> DocumentInfo.Type.HOME
    ObjectInfo.Type.ProfilePage -> DocumentInfo.Type.PROFILE_PAGE
    ObjectInfo.Type.Archive -> DocumentInfo.Type.ARCHIVE
    ObjectInfo.Type.Set -> DocumentInfo.Type.SET
    ObjectInfo.Type.File -> DocumentInfo.Type.FILE
    ObjectInfo.Type.ObjectType -> DocumentInfo.Type.OBJECT_TYPE
    ObjectInfo.Type.Relation -> DocumentInfo.Type.RELATION
}
