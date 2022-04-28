package com.anytypeio.anytype.middleware.mappers

import anytype.ResponseEvent
import anytype.model.ObjectInfo
import anytype.model.ObjectInfoWithLinks
import anytype.model.ObjectLinksInfo
import anytype.model.Restrictions
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.middleware.interactor.toCoreModels

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
                content = block.toCoreModelsText(),
                backgroundColor = block.backgroundColor.ifEmpty { null }
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
                content = block.toCoreModelsLink(),
                backgroundColor = block.backgroundColor.ifEmpty { null }
            )
        }
        block.div != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreModelsDivider(),
                backgroundColor = block.backgroundColor.ifEmpty { null }
            )
        }
        block.file_ != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreModelsFile(),
                backgroundColor = block.backgroundColor.ifEmpty { null }
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
                content = block.toCoreModelsBookmark(),
                backgroundColor = block.backgroundColor.ifEmpty { null }
            )
        }
        block.smartblock != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = Block.Content.Smart(
                    type = types[block.id] ?: throw IllegalStateException("Type missing")
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
                content = block.toCoreModelsRelationBlock(),
                backgroundColor = block.backgroundColor.ifEmpty { null }
            )
        }
        block.featuredRelations != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = Block.Content.FeaturedRelations,
                backgroundColor = block.backgroundColor.ifEmpty { null }
            )
        }
        block.latex != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = Block.Content.Latex(
                    latex = block.latex?.text.orEmpty()
                ),
                backgroundColor = block.backgroundColor.ifEmpty { null }
            )
        }
        block.tableOfContents != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = Block.Content.TableOfContents,
                backgroundColor = block.backgroundColor.ifEmpty { null }
            )
        }
        else -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = Block.Content.Unsupported,
                backgroundColor = block.backgroundColor.ifEmpty { null }
            )
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
        image = content.imageHash.ifEmpty { null },
        favicon = content.faviconHash.ifEmpty { null }
    )
}

fun MBlock.toCoreModelsDataView(): Block.Content.DataView {
    val content = checkNotNull(dataview)
    return Block.Content.DataView(
        sources = content.source,
        viewers = content.views.map { it.toCoreModels() },
        relations = content.relations.map { it.toCoreModels() }
    )
}

fun MBlock.toCoreModelsRelationBlock(): Block.Content.RelationBlock {
    val content = checkNotNull(relation)
    return Block.Content.RelationBlock(
        key = content.key.ifEmpty { null }
    )
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
    MBFileType.Audio -> Block.Content.File.Type.AUDIO
    MBFileType.PDF -> Block.Content.File.Type.PDF
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
        param = param_.ifEmpty { null }
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
    MBTextStyle.Description -> Block.Content.Text.Style.DESCRIPTION
    MBTextStyle.Callout -> Block.Content.Text.Style.CALLOUT
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
    MBMarkType.Emoji -> Block.Content.Text.Mark.Type.EMOJI
    MBMarkType.Object -> Block.Content.Text.Mark.Type.OBJECT
}


// ---------------------- DATA VIEW ------------------------
fun MDVView.toCoreModels(): DVViewer = DVViewer(
    id = id,
    name = name,
    type = type.toCoreModels(),
    sorts = sorts.map { it.toCoreModels() },
    filters = filters.map { it.toCoreModels() },
    viewerRelations = relations.map { it.toCoreModels() },
    cardSize = when(cardSize) {
        MDVViewCardSize.Small -> DVViewerCardSize.SMALL
        MDVViewCardSize.Medium -> DVViewerCardSize.MEDIUM
        MDVViewCardSize.Large -> DVViewerCardSize.LARGE
    },
    hideIcon = hideIcon,
    coverFit = coverFit,
    coverRelationKey = coverRelationKey.ifEmpty { null }
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
    value = value_
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
    MDVFilterCondition.None -> DVFilterCondition.NONE
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
    layout = layout.toCoreModels(),
    smartBlockTypes = types.map { it.toCoreModel() },
    isArchived = isArchived,
    isReadOnly = readonly
)

fun MOTypeLayout.toCoreModels(): ObjectType.Layout = when (this) {
    MOTypeLayout.basic -> ObjectType.Layout.BASIC
    MOTypeLayout.profile -> ObjectType.Layout.PROFILE
    MOTypeLayout.todo -> ObjectType.Layout.TODO
    MOTypeLayout.set_ -> ObjectType.Layout.SET
    MOTypeLayout.objectType -> ObjectType.Layout.OBJECT_TYPE
    MOTypeLayout.file_ -> ObjectType.Layout.FILE
    MOTypeLayout.relation -> ObjectType.Layout.RELATION
    MOTypeLayout.dashboard -> ObjectType.Layout.DASHBOARD
    MOTypeLayout.database -> ObjectType.Layout.DATABASE
    MOTypeLayout.image -> ObjectType.Layout.IMAGE
    MOTypeLayout.note -> ObjectType.Layout.NOTE
    MOTypeLayout.space -> ObjectType.Layout.SPACE
}

fun MRelationDataSource.source(): Relation.Source = when (this) {
    MRelationDataSource.details -> Relation.Source.DETAILS
    MRelationDataSource.derived -> Relation.Source.DERIVED
    MRelationDataSource.account -> Relation.Source.ACCOUNT
    MRelationDataSource.local -> Relation.Source.LOCAL
}

fun MRelationFormat.format(): Relation.Format = when (this) {
    MRelationFormat.shorttext -> Relation.Format.SHORT_TEXT
    MRelationFormat.longtext -> Relation.Format.LONG_TEXT
    MRelationFormat.number -> Relation.Format.NUMBER
    MRelationFormat.date -> Relation.Format.DATE
    MRelationFormat.file_ -> Relation.Format.FILE
    MRelationFormat.checkbox -> Relation.Format.CHECKBOX
    MRelationFormat.url -> Relation.Format.URL
    MRelationFormat.email -> Relation.Format.EMAIL
    MRelationFormat.phone -> Relation.Format.PHONE
    MRelationFormat.emoji -> Relation.Format.EMOJI
    MRelationFormat.object_ -> Relation.Format.OBJECT
    MRelationFormat.status -> Relation.Format.STATUS
    MRelationFormat.tag -> Relation.Format.TAG
    MRelationFormat.relations -> Relation.Format.RELATIONS
}

fun MRelationOption.option(): Relation.Option = Relation.Option(
    id = id,
    text = text,
    color = color,
    scope = scope.scope()
)

fun MRelationOptionScope.scope(): Relation.OptionScope = when (this) {
    MRelationOptionScope.local -> Relation.OptionScope.LOCAL
    MRelationOptionScope.relation -> Relation.OptionScope.RELATION
    MRelationOptionScope.format -> Relation.OptionScope.FORMAT
}

// ---------------------- NAVIGATION & SEARCH ------------------------
fun ObjectInfoWithLinks.toCoreModel(): com.anytypeio.anytype.core_models.ObjectInfoWithLinks {
    val i = info
    checkNotNull(i)
    return ObjectInfoWithLinks(
        id = id,
        links = links?.toCoreModel() ?: ObjectLinks(emptyList(), emptyList()),
        documentInfo = i.toCoreModel()
    )
}

fun ObjectLinksInfo.toCoreModel(): ObjectLinks = ObjectLinks(
    inbound = inbound.map { it.toCoreModel() },
    outbound = outbound.map { it.toCoreModel() }
)

fun ObjectInfo.toCoreModel(): DocumentInfo = DocumentInfo(
    id = id,
    obj = ObjectWrapper.Basic(details?.toMap() ?: mapOf()),
    snippet = snippet,
    hasInboundLinks = hasInboundLinks,
    smartBlockType = objectType.toCoreModel()
)

fun MSmartBlockType.toCoreModel(): SmartBlockType = when (this) {
    MSmartBlockType.Breadcrumbs -> SmartBlockType.BREADCRUMBS
    MSmartBlockType.Page -> SmartBlockType.PAGE
    MSmartBlockType.ProfilePage -> SmartBlockType.PROFILE_PAGE
    MSmartBlockType.Home -> SmartBlockType.HOME
    MSmartBlockType.Archive -> SmartBlockType.ARCHIVE
    MSmartBlockType.Database -> SmartBlockType.DATABASE
    MSmartBlockType.Set -> SmartBlockType.SET
    MSmartBlockType.STObjectType -> SmartBlockType.CUSTOM_OBJECT_TYPE
    MSmartBlockType.File -> SmartBlockType.FILE
    MSmartBlockType.Template -> SmartBlockType.TEMPLATE
    MSmartBlockType.MarketplaceType -> SmartBlockType.MARKETPLACE_TYPE
    MSmartBlockType.MarketplaceRelation -> SmartBlockType.MARKETPLACE_RELATION
    MSmartBlockType.MarketplaceTemplate -> SmartBlockType.MARKETPLACE_TEMPLATE
    MSmartBlockType.BundledRelation -> SmartBlockType.BUNDLED_RELATION
    MSmartBlockType.IndexedRelation -> SmartBlockType.INDEXED_RELATION
    MSmartBlockType.BundledObjectType -> SmartBlockType.BUNDLED_OBJECT_TYPE
    MSmartBlockType.AnytypeProfile -> SmartBlockType.ANYTYPE_PROFILE
    MSmartBlockType.BundledTemplate -> SmartBlockType.BUNDLED_TEMPLATE
    MSmartBlockType.Date -> SmartBlockType.DATE
    MSmartBlockType.Workspace -> SmartBlockType.WORKSPACE
    MSmartBlockType.WorkspaceOld -> SmartBlockType.WORKSPACE_OLD
    MSmartBlockType.AccountOld -> SmartBlockType.ACCOUNT_OLD
}

// ---------------------- RESTRICTIONS ------------------------
fun MObjectRestriction.toCoreModel(): ObjectRestriction? = when (this) {
    MObjectRestriction.Delete -> ObjectRestriction.DELETE
    MObjectRestriction.Relations -> ObjectRestriction.RELATIONS
    MObjectRestriction.Details -> ObjectRestriction.DETAILS
    MObjectRestriction.Blocks -> ObjectRestriction.BLOCKS
    MObjectRestriction.TypeChange -> ObjectRestriction.TYPE_CHANGE
    MObjectRestriction.LayoutChange -> ObjectRestriction.LAYOUT_CHANGE
    MObjectRestriction.Template -> ObjectRestriction.TEMPLATE
    MObjectRestriction.None -> null
}

fun MDVRestrictions.toCoreModel(): DataViewRestrictions {
    return DataViewRestrictions(
        block = this.blockId,
        restrictions = this.restrictions.mapNotNull { it.toCoreModel() }
    )
}

fun MDVRestriction.toCoreModel(): DataViewRestriction? = when (this) {
    Restrictions.DataviewRestriction.DVViews -> DataViewRestriction.VIEWS
    Restrictions.DataviewRestriction.DVRelation -> DataViewRestriction.RELATION
    Restrictions.DataviewRestriction.DVCreateObject -> DataViewRestriction.CREATE_OBJECT
    Restrictions.DataviewRestriction.DVNone -> null
}

fun MAccountStatus.core(): AccountStatus = when (statusType) {
    MAccountStatusType.Active -> AccountStatus.Active
    MAccountStatusType.PendingDeletion -> AccountStatus.PendingDeletion(
        deadline = deletionDate
    )
    MAccountStatusType.StartedDeletion -> AccountStatus.Deleted
    MAccountStatusType.Deleted -> AccountStatus.Deleted
}