package com.anytypeio.anytype.middleware.mappers

import anytype.ResponseEvent
import anytype.Rpc
import anytype.model.Account
import anytype.model.NameserviceNameType
import anytype.model.ParticipantPermissions
import anytype.model.Restrictions
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.CreateBlockLinkWithObjectResult
import com.anytypeio.anytype.core_models.CreateObjectResult
import com.anytypeio.anytype.core_models.DVDateFormat
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterOperator
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortEmptyType
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.DVTimeFormat
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerCardSize
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ImportErrorCode
import com.anytypeio.anytype.core_models.ManifestInfo
import com.anytypeio.anytype.core_models.NodeUsage
import com.anytypeio.anytype.core_models.NodeUsageInfo
import com.anytypeio.anytype.core_models.Notification
import com.anytypeio.anytype.core_models.NotificationActionType
import com.anytypeio.anytype.core_models.NotificationPayload
import com.anytypeio.anytype.core_models.NotificationStatus
import com.anytypeio.anytype.core_models.ObjectOrder
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Process
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.SpaceUsage
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.history.DiffVersionResponse
import com.anytypeio.anytype.core_models.history.ShowVersionResponse
import com.anytypeio.anytype.core_models.history.Version
import com.anytypeio.anytype.core_models.membership.EmailVerificationStatus
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.core_models.membership.NameServiceNameType
import com.anytypeio.anytype.core_models.multiplayer.P2PStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncNetwork
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TimeInSeconds
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.middleware.interactor.toCoreModels
import com.google.gson.GsonBuilder

// ---------------------- PAYLOAD ------------------------
fun ResponseEvent?.toPayload(): Payload {
    checkNotNull(this)
    val context = contextId

    return Payload(
        context = context,
        events = messages.mapNotNull { it.toCoreModels(context) }
    )
}

fun MObjectView.toPayload(): Payload {
    return Payload(
        context = rootId,
        events = listOf(
            Event.Command.ShowObject(
                context = rootId,
                root = rootId,
                blocks = blocks.toCoreModels(),
                details = Block.Details(
                    details.associate { details ->
                        details.id to details.details.toCoreModel()
                    }
                ),
                relationLinks = relationLinks.map { it.toCoreModels() },
                objectRestrictions = restrictions?.object_?.map { it.toCoreModel() }.orEmpty(),
                dataViewRestrictions = restrictions?.dataview?.map { it.toCoreModel() }.orEmpty()
            )
        )
    )
}

fun MObjectView.toCore(): ObjectView {
    return ObjectView(
        root = rootId,
        blocks = blocks.toCoreModels(),
        details = details.associate { d -> d.id to d.details.orEmpty() },
        relations = relationLinks.map { it.toCoreModels() },
        objectRestrictions = restrictions?.object_?.map { it.toCoreModel() }.orEmpty(),
        dataViewRestrictions = restrictions?.dataview?.map { it.toCoreModel() }.orEmpty(),
    )
}


// ---------------------- BLOCKS ------------------------
fun List<MBlock>.toCoreModels(): List<Block> = mapNotNull { block ->
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
                content = Block.Content.Smart
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
            val latex = checkNotNull(block.latex)
            if (latex.processor.value == MBLatexProcessor.Latex.value) {
                Block(
                    id = block.id,
                    fields = block.toCoreModelsFields(),
                    children = block.childrenIds,
                    content = Block.Content.Latex(
                        latex = block.latex?.text.orEmpty()
                    ),
                    backgroundColor = block.backgroundColor.ifEmpty { null }
                )
            } else {
                Block(
                    id = block.id,
                    fields = block.toCoreModelsFields(),
                    children = block.childrenIds,
                    content = Block.Content.Unsupported,
                    backgroundColor = block.backgroundColor.ifEmpty { null }
                )
            }
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
        block.table != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = Block.Content.Table,
                backgroundColor = block.backgroundColor
            )
        }
        block.tableColumn != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = Block.Content.TableColumn,
                backgroundColor = block.backgroundColor
            )
        }
        block.tableRow != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreModelsTableRowBlock(),
                backgroundColor = block.backgroundColor
            )
        }
        block.widget != null -> {
            Block(
                id = block.id,
                fields = block.toCoreModelsFields(),
                children = block.childrenIds,
                content = block.toCoreWidget(),
                backgroundColor = block.backgroundColor
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
        align = align.toCoreModelsAlign(),
        iconEmoji = content.iconEmoji,
        iconImage = content.iconImage,
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
        type = content.style.toCoreModels(),
        iconSize = content.iconSize.toCoreModel(),
        cardStyle = content.cardStyle.toCoreModel(),
        description = content.description.toCoreModel(),
        relations = content.relations.toSet(),
    )
}

fun MBLinkIconSize.toCoreModel(): Block.Content.Link.IconSize {
    return when (this) {
        MBLinkIconSize.SizeNone -> Block.Content.Link.IconSize.NONE
        MBLinkIconSize.SizeSmall -> Block.Content.Link.IconSize.SMALL
        MBLinkIconSize.SizeMedium -> Block.Content.Link.IconSize.MEDIUM
    }
}

fun MBLinkCardStyle.toCoreModel(): Block.Content.Link.CardStyle {
    return when (this) {
        MBLinkCardStyle.Text -> Block.Content.Link.CardStyle.TEXT
        MBLinkCardStyle.Card -> Block.Content.Link.CardStyle.CARD
        MBLinkCardStyle.Inline -> Block.Content.Link.CardStyle.INLINE
    }
}

fun MBLinkDescription.toCoreModel(): Block.Content.Link.Description {
    return when (this) {
        MBLinkDescription.None -> Block.Content.Link.Description.NONE
        MBLinkDescription.Added -> Block.Content.Link.Description.ADDED
        MBLinkDescription.Content -> Block.Content.Link.Description.CONTENT
    }
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
        targetObjectId = content.targetObjectId,
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
        favicon = content.faviconHash.ifEmpty { null },
        targetObjectId = content.targetObjectId.ifEmpty { null },
        state = content.state.toCoreModelsBookmarkState()
    )
}

fun MBookmarkState.toCoreModelsBookmarkState(): Block.Content.Bookmark.State {
    return when (this) {
        MBookmarkState.Empty -> Block.Content.Bookmark.State.EMPTY
        MBookmarkState.Fetching -> Block.Content.Bookmark.State.FETCHING
        MBookmarkState.Done -> Block.Content.Bookmark.State.DONE
        MBookmarkState.Error -> Block.Content.Bookmark.State.ERROR
    }
}

fun MBlock.toCoreModelsDataView(): Block.Content.DataView {
    val content = checkNotNull(dataview)
    return Block.Content.DataView(
        viewers = content.views.map { it.toCoreModels() },
        relationLinks = content.relationLinks.map { it.toCoreModels() },
        targetObjectId = content.TargetObjectId,
        isCollection = content.isCollection,
        objectOrders = content.objectOrders.map { it.toCoreModelsObjectOrder() }
    )
}

fun MDVObjectOrder.toCoreModelsObjectOrder(): ObjectOrder {
    return ObjectOrder(
        view = viewId,
        group = groupId,
        ids = objectIds
    )
}

fun MBlock.toCoreModelsRelationBlock(): Block.Content.RelationBlock {
    val content = checkNotNull(relation)
    return Block.Content.RelationBlock(
        key = content.key.ifEmpty { null }
    )
}

fun MBlock.toCoreModelsTableRowBlock(): Block.Content.TableRow {
    val content = checkNotNull(tableRow)
    return Block.Content.TableRow(
        isHeader = content.isHeader
    )
}

fun MBlock.toCoreWidget(): Block.Content.Widget {
    val content = checkNotNull(widget)
    return Block.Content.Widget(
        layout = when (content.layout) {
            MWidgetLayout.Link -> Block.Content.Widget.Layout.LINK
            MWidgetLayout.Tree -> Block.Content.Widget.Layout.TREE
            MWidgetLayout.List -> Block.Content.Widget.Layout.LIST
            MWidgetLayout.CompactList -> Block.Content.Widget.Layout.COMPACT_LIST
            MWidgetLayout.View -> Block.Content.Widget.Layout.VIEW
        },
        limit = content.limit,
        activeView = content.viewId.ifEmpty { null }
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
    MBLayoutStyle.TableRows -> Block.Content.Layout.Type.TABLE_ROW
    MBLayoutStyle.TableColumns -> Block.Content.Layout.Type.TABLE_COLUMN
}

fun MBAlign.toCoreModelsAlign(): Block.Align = when (this) {
    MBAlign.AlignLeft -> Block.Align.AlignLeft
    MBAlign.AlignCenter -> Block.Align.AlignCenter
    MBAlign.AlignRight -> Block.Align.AlignRight
    MBAlign.AlignJustify -> Block.Align.AlignJustify
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
    MBMarkType.Underscored -> Block.Content.Text.Mark.Type.UNDERLINE
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
    cardSize = when (cardSize) {
        MDVViewCardSize.Small -> DVViewerCardSize.SMALL
        MDVViewCardSize.Medium -> DVViewerCardSize.MEDIUM
        MDVViewCardSize.Large -> DVViewerCardSize.LARGE
    },
    hideIcon = hideIcon,
    coverFit = coverFit,
    coverRelationKey = coverRelationKey.ifEmpty { null },
    defaultTemplate = defaultTemplateId.ifEmpty { null },
    defaultObjectType = defaultObjectTypeId.ifEmpty { ObjectTypeIds.PAGE }
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
    MDVViewType.Calendar -> DVViewerType.CALENDAR
    MDVViewType.Graph -> DVViewerType.GRAPH
}

fun MDVFilter.toCoreModels(): DVFilter = DVFilter(
    id = id,
    relation = RelationKey,
    operator = operator_.toCoreModels(),
    condition = condition.toCoreModels(),
    quickOption = quickOption.toCoreModels(),
    value = value_
)

fun MDVFilterCondition.toCoreModels(): DVFilterCondition = when (this) {
    MDVFilterCondition.None -> DVFilterCondition.NONE
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
    MDVFilterCondition.ExactIn -> DVFilterCondition.EXACT_IN
    MDVFilterCondition.NotExactIn -> DVFilterCondition.NOT_EXACT_IN
    MDVFilterCondition.Exists -> DVFilterCondition.EXISTS
}

fun MDVFilterQuickOption.toCoreModels(): DVFilterQuickOption = when (this) {
    MDVFilterQuickOption.ExactDate -> DVFilterQuickOption.EXACT_DATE
    MDVFilterQuickOption.Yesterday -> DVFilterQuickOption.YESTERDAY
    MDVFilterQuickOption.Today -> DVFilterQuickOption.TODAY
    MDVFilterQuickOption.Tomorrow -> DVFilterQuickOption.TOMORROW
    MDVFilterQuickOption.LastWeek -> DVFilterQuickOption.LAST_WEEK
    MDVFilterQuickOption.CurrentWeek -> DVFilterQuickOption.CURRENT_WEEK
    MDVFilterQuickOption.NextWeek -> DVFilterQuickOption.NEXT_WEEK
    MDVFilterQuickOption.LastMonth -> DVFilterQuickOption.LAST_MONTH
    MDVFilterQuickOption.CurrentMonth -> DVFilterQuickOption.CURRENT_MONTH
    MDVFilterQuickOption.NextMonth -> DVFilterQuickOption.NEXT_MONTH
    MDVFilterQuickOption.NumberOfDaysAgo -> DVFilterQuickOption.DAYS_AGO
    MDVFilterQuickOption.NumberOfDaysNow -> DVFilterQuickOption.DAYS_AHEAD
}

fun MDVFilterOperator.toCoreModels(): DVFilterOperator = when (this) {
    MDVFilterOperator.And -> DVFilterOperator.AND
    MDVFilterOperator.Or -> DVFilterOperator.OR
    MDVFilterOperator.No -> DVFilterOperator.NO
}

fun MDVSort.toCoreModels(): Block.Content.DataView.Sort = DVSort(
    id = id,
    relationKey = RelationKey,
    type = type.toCoreModels(),
    includeTime = includeTime,
    customOrder = customOrder.mapNotNull { value ->
        if (value is Id) value else null
    },
    relationFormat = format.format(),
    emptyType = emptyPlacement.toCoreModels()
)

fun MDVSortEmptyType.toCoreModels(): DVSortEmptyType = when (this) {
    MDVSortEmptyType.NotSpecified -> DVSortEmptyType.NOT_SPECIFIC
    MDVSortEmptyType.Start -> DVSortEmptyType.START
    MDVSortEmptyType.End -> DVSortEmptyType.END
}

fun MDVSortType.toCoreModels(): DVSortType = when (this) {
    MDVSortType.Asc -> DVSortType.ASC
    MDVSortType.Desc -> DVSortType.DESC
    MDVSortType.Custom -> DVSortType.CUSTOM
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

fun MOTypeLayout.toCoreModels(): ObjectType.Layout = when (this) {
    MOTypeLayout.basic -> ObjectType.Layout.BASIC
    MOTypeLayout.profile -> ObjectType.Layout.PROFILE
    MOTypeLayout.todo -> ObjectType.Layout.TODO
    MOTypeLayout.set_ -> ObjectType.Layout.SET
    MOTypeLayout.objectType -> ObjectType.Layout.OBJECT_TYPE
    MOTypeLayout.file_ -> ObjectType.Layout.FILE
    MOTypeLayout.relation -> ObjectType.Layout.RELATION
    MOTypeLayout.dashboard -> ObjectType.Layout.DASHBOARD
    MOTypeLayout.image -> ObjectType.Layout.IMAGE
    MOTypeLayout.note -> ObjectType.Layout.NOTE
    MOTypeLayout.space -> ObjectType.Layout.SPACE
    MOTypeLayout.bookmark -> ObjectType.Layout.BOOKMARK
    MOTypeLayout.audio -> ObjectType.Layout.AUDIO
    MOTypeLayout.video -> ObjectType.Layout.VIDEO
    MOTypeLayout.date -> ObjectType.Layout.DATE
    MOTypeLayout.collection -> ObjectType.Layout.COLLECTION
    MOTypeLayout.relationOption -> ObjectType.Layout.RELATION_OPTION
    MOTypeLayout.participant -> ObjectType.Layout.PARTICIPANT
    MOTypeLayout.relationOptionsList -> ObjectType.Layout.RELATION_OPTION_LIST
    MOTypeLayout.spaceView -> ObjectType.Layout.SPACE_VIEW
    MOTypeLayout.pdf -> ObjectType.Layout.PDF
    MOTypeLayout.chat -> ObjectType.Layout.CHAT
    MOTypeLayout.chatDerived -> ObjectType.Layout.CHAT_DERIVED
    MOTypeLayout.tag -> ObjectType.Layout.TAG
}

fun MRelationDataSource.source(): Relation.Source = when (this) {
    MRelationDataSource.details -> Relation.Source.DETAILS
    MRelationDataSource.derived -> Relation.Source.DERIVED
    MRelationDataSource.account -> Relation.Source.ACCOUNT
    MRelationDataSource.local -> Relation.Source.LOCAL
}

fun MRelationFormat.format(): Relation.Format = when (this) {
    MRelationFormat.shorttext -> RelationFormat.SHORT_TEXT
    MRelationFormat.longtext -> RelationFormat.LONG_TEXT
    MRelationFormat.number -> RelationFormat.NUMBER
    MRelationFormat.date -> RelationFormat.DATE
    MRelationFormat.file_ -> RelationFormat.FILE
    MRelationFormat.checkbox -> RelationFormat.CHECKBOX
    MRelationFormat.url -> RelationFormat.URL
    MRelationFormat.email -> RelationFormat.EMAIL
    MRelationFormat.phone -> RelationFormat.PHONE
    MRelationFormat.emoji -> RelationFormat.EMOJI
    MRelationFormat.object_ -> RelationFormat.OBJECT
    MRelationFormat.status -> RelationFormat.STATUS
    MRelationFormat.tag -> RelationFormat.TAG
    MRelationFormat.relations -> RelationFormat.RELATIONS
}

fun MRelationOption.option(): Relation.Option = Relation.Option(
    id = id,
    text = text,
    color = color
)

fun MRelationLink.toCoreModels() = RelationLink(
    key = key,
    format = format.format()
)

// ---------------------- RESTRICTIONS ------------------------
fun MObjectRestriction.toCoreModel(): ObjectRestriction = when (this) {
    MObjectRestriction.Delete -> ObjectRestriction.DELETE
    MObjectRestriction.Relations -> ObjectRestriction.RELATIONS
    MObjectRestriction.Details -> ObjectRestriction.DETAILS
    MObjectRestriction.Blocks -> ObjectRestriction.BLOCKS
    MObjectRestriction.TypeChange -> ObjectRestriction.TYPE_CHANGE
    MObjectRestriction.LayoutChange -> ObjectRestriction.LAYOUT_CHANGE
    MObjectRestriction.Template -> ObjectRestriction.TEMPLATE
    MObjectRestriction.None -> ObjectRestriction.NONE
    MObjectRestriction.Duplicate -> ObjectRestriction.DUPLICATE
    MObjectRestriction.CreateObjectOfThisType -> ObjectRestriction.CREATE_OBJECT_OF_THIS_TYPE
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

fun Rpc.Object.Create.Response.toCoreModel(): CreateObjectResult {
    return CreateObjectResult(
        id = objectId,
        event = event.toPayload(),
        details = details.orEmpty()
    )
}

fun Rpc.BlockLink.CreateWithObject.Response.toCoreModel(): CreateBlockLinkWithObjectResult {
    return CreateBlockLinkWithObjectResult(
        blockId = blockId,
        objectId = targetId,
        event = event.toPayload()
    )
}

fun MDVViewCardSize.toCodeModels(): DVViewerCardSize = when (this) {
    MDVViewCardSize.Small -> DVViewerCardSize.SMALL
    MDVViewCardSize.Medium -> DVViewerCardSize.MEDIUM
    MDVViewCardSize.Large -> DVViewerCardSize.LARGE
}

fun Rpc.File.NodeUsage.Response.toCoreModel(): NodeUsageInfo {
    return NodeUsageInfo(
        nodeUsage = NodeUsage(
            filesCount = usage?.filesCount,
            cidsCount = usage?.cidsCount,
            bytesUsage = usage?.bytesUsage,
            bytesLeft = usage?.bytesLeft,
            bytesLimit = usage?.bytesLimit,
            localBytesUsage = usage?.localBytesUsage
        ),
        spaces = spaces.map {
            SpaceUsage(
                space = it.spaceId,
                filesCount = it.filesCount,
                cidsCount = it.cidsCount,
                bytesUsage = it.bytesUsage
            )
        }
    )
}

fun List<Rpc.Debug.TreeInfo>.toCoreModel(): String {
    return GsonBuilder().setPrettyPrinting().create().toJson(this)
}

fun Account.Info.config(): Config = Config(
    home = homeObjectId,
    profile = profileObjectId,
    gateway = gatewayUrl,
    space = accountSpaceId,
    techSpace = techSpaceId,
    spaceView = spaceViewId,
    widgets = widgetsId,
    analytics = analyticsId,
    device = deviceId,
    network = networkId
)

fun MManifestInfo.toCoreModel(): ManifestInfo {
    return ManifestInfo(
        schema = schema,
        id = id,
        name = name,
        author = author,
        license = license,
        title = title,
        description = description,
        screenshots = screenshots,
        downloadLink = downloadLink,
        fileSize = fileSize,
        categories = categories,
        language = language
    )
}

fun MProcess.toCoreModel(): Process {
    return Process(
        id = id,
        type = type.toCoreModel(),
        state = state.toCoreModel(),
        progress = progress?.toCoreModel()
    )
}

fun MProcessType.toCoreModel(): Process.Type {
    return when (this) {
        MProcessType.DropFiles -> Process.Type.DROP_FILES
        MProcessType.Import -> Process.Type.IMPORT
        MProcessType.Export -> Process.Type.EXPORT
        MProcessType.SaveFile -> Process.Type.SAVE_FILE
        MProcessType.RecoverAccount -> Process.Type.RECOVER_ACCOUNT
        MProcessType.Migration -> Process.Type.MIGRATION
    }
}

fun MProcessState.toCoreModel(): Process.State {
    return when (this) {
        MProcessState.None -> Process.State.NONE
        MProcessState.Running -> Process.State.RUNNING
        MProcessState.Done -> Process.State.DONE
        MProcessState.Canceled -> Process.State.CANCELED
        MProcessState.Error -> Process.State.ERROR
    }
}

fun MProcessProgress.toCoreModel(): Process.Progress {
    return Process.Progress(
        total = total,
        done = done,
        message = message
    )
}

//region IMPORT
fun MImportErrorCode.toCoreModel(): ImportErrorCode {
    return when (this) {
        MImportErrorCode.NULL -> ImportErrorCode.NULL
        MImportErrorCode.UNKNOWN_ERROR -> ImportErrorCode.UNKNOWN_ERROR
        MImportErrorCode.BAD_INPUT -> ImportErrorCode.BAD_INPUT
        MImportErrorCode.INTERNAL_ERROR -> ImportErrorCode.INTERNAL_ERROR
        MImportErrorCode.NO_OBJECTS_TO_IMPORT -> ImportErrorCode.NO_OBJECTS_TO_IMPORT
        MImportErrorCode.IMPORT_IS_CANCELED -> ImportErrorCode.IMPORT_IS_CANCELED
        MImportErrorCode.LIMIT_OF_ROWS_OR_RELATIONS_EXCEEDED -> ImportErrorCode.LIMIT_OF_ROWS_OR_RELATIONS_EXCEEDED
        MImportErrorCode.FILE_LOAD_ERROR -> ImportErrorCode.FILE_LOAD_ERROR
        MImportErrorCode.INSUFFICIENT_PERMISSIONS -> ImportErrorCode.INSUFFICIENT_PERMISSIONS
    }
}
//endregion

//region NOTIFICATIONS
fun MNotificationStatus.toCoreModel(): NotificationStatus {
    return when (this) {
        MNotificationStatus.Created -> NotificationStatus.CREATED
        MNotificationStatus.Shown -> NotificationStatus.SHOWN
        MNotificationStatus.Read -> NotificationStatus.READ
        MNotificationStatus.Replied -> NotificationStatus.REPLIED
    }
}

fun MNotificationActionType.toCoreModel(): NotificationActionType {
    return when (this) {
        MNotificationActionType.CLOSE -> NotificationActionType.CLOSE
    }
}

fun MParticipantPermission.toCore(): SpaceMemberPermissions {
    return when (this) {
        ParticipantPermissions.Reader -> SpaceMemberPermissions.READER
        ParticipantPermissions.Writer -> SpaceMemberPermissions.WRITER
        ParticipantPermissions.Owner -> SpaceMemberPermissions.OWNER
        ParticipantPermissions.NoPermissions -> SpaceMemberPermissions.NO_PERMISSIONS
    }
}

fun MNotification.toCoreModel(): Notification {
    return Notification(
        id = id,
        createTime = createTime,
        status = status.toCoreModel(),
        isLocal = isLocal,
        space = SpaceId(space),
        aclHeadId = aclHeadId,
        payload = when {
            participantPermissionsChange != null -> {
                NotificationPayload.ParticipantPermissionsChange(
                    spaceId = SpaceId(participantPermissionsChange!!.spaceId),
                    spaceName = participantPermissionsChange!!.spaceName,
                    permissions = participantPermissionsChange!!.permissions.toCore()
                )
            }
            requestToJoin != null -> {
                NotificationPayload.RequestToJoin(
                    spaceId = SpaceId(requestToJoin!!.spaceId),
                    spaceName = requestToJoin!!.spaceName,
                    identity = requestToJoin!!.identity,
                    identityName = requestToJoin!!.identityName,
                    identityIcon = requestToJoin!!.identityIcon
                )
            }
            requestToLeave != null -> {
                NotificationPayload.RequestToLeave(
                    spaceId = SpaceId(requestToLeave!!.spaceId),
                    spaceName = requestToLeave!!.spaceName,
                    identity = requestToLeave!!.identity,
                    identityName = requestToLeave!!.identityName,
                    identityIcon = requestToLeave!!.identityIcon
                )
            }
            participantRequestApproved != null -> {
                NotificationPayload.ParticipantRequestApproved(
                    spaceId = SpaceId(participantRequestApproved!!.spaceId),
                    permissions = participantRequestApproved!!.permissions.toCore(),
                    spaceName = participantRequestApproved!!.spaceName
                )
            }
            participantRemove != null -> {
                NotificationPayload.ParticipantRemove(
                    identity = participantRemove!!.identity,
                    identityName = participantRemove!!.identityName,
                    identityIcon = participantRemove!!.identityIcon,
                    spaceId = SpaceId(participantRemove!!.spaceId),
                    spaceName = participantRemove!!.spaceName
                )
            }
            participantRequestDecline != null -> {
                NotificationPayload.ParticipantRequestDecline(
                    spaceId = SpaceId(participantRequestDecline!!.spaceId),
                    spaceName = participantRequestDecline!!.spaceName
                )
            }
            galleryImport != null -> {
                NotificationPayload.GalleryImport(
                    processId = galleryImport!!.processId,
                    errorCode = galleryImport!!.errorCode.toCoreModel(),
                    spaceId = SpaceId(galleryImport!!.spaceId),
                    name = galleryImport!!.name
                )
            }
            test != null -> NotificationPayload.Test
            else -> NotificationPayload.Unsupported("Unsupported notification payload :${this.javaClass.simpleName}")
        },
    )
}
//endregion

//region MEMBERSHIP
fun MMembershipStatus.toCoreModel(): Membership.Status {
    return when (this) {
        MMembershipStatus.StatusUnknown -> Membership.Status.STATUS_UNKNOWN
        MMembershipStatus.StatusPending -> Membership.Status.STATUS_PENDING
        MMembershipStatus.StatusActive -> Membership.Status.STATUS_ACTIVE
        MMembershipStatus.StatusPendingRequiresFinalization -> Membership.Status.STATUS_PENDING_FINALIZATION
    }
}

fun MMembershipPaymentMethod.toCoreModel(): MembershipPaymentMethod {
    return when (this) {
        MMembershipPaymentMethod.MethodNone -> MembershipPaymentMethod.METHOD_NONE
        MMembershipPaymentMethod.MethodStripe -> MembershipPaymentMethod.METHOD_STRIPE
        MMembershipPaymentMethod.MethodCrypto -> MembershipPaymentMethod.METHOD_CRYPTO
        MMembershipPaymentMethod.MethodInappApple -> MembershipPaymentMethod.METHOD_INAPP_APPLE
        MMembershipPaymentMethod.MethodInappGoogle -> MembershipPaymentMethod.METHOD_INAPP_GOOGLE
    }
}

fun MMembership.toCoreModel(): Membership {
    return Membership(
        tier = tier,
        membershipStatusModel = status.toCoreModel(),
        dateStarted = dateStarted,
        dateEnds = dateEnds,
        isAutoRenew = isAutoRenew,
        paymentMethod = paymentMethod.toCoreModel(),
        nameServiceName = nsName,
        nameServiceType = nsNameType.toCoreModel(),
        userEmail = userEmail,
        subscribeToNewsletter = subscribeToNewsletter
    )
}

fun MMembershipTierDataPeriodType.toCoreModel(): MembershipPeriodType {
    return when (this) {
        MMembershipTierDataPeriodType.PeriodTypeUnknown -> MembershipPeriodType.PERIOD_TYPE_UNKNOWN
        MMembershipTierDataPeriodType.PeriodTypeUnlimited -> MembershipPeriodType.PERIOD_TYPE_UNLIMITED
        MMembershipTierDataPeriodType.PeriodTypeDays -> MembershipPeriodType.PERIOD_TYPE_DAYS
        MMembershipTierDataPeriodType.PeriodTypeWeeks -> MembershipPeriodType.PERIOD_TYPE_WEEKS
        MMembershipTierDataPeriodType.PeriodTypeMonths -> MembershipPeriodType.PERIOD_TYPE_MONTHS
        MMembershipTierDataPeriodType.PeriodTypeYears -> MembershipPeriodType.PERIOD_TYPE_YEARS
    }
}

fun MMembershipTierData.toCoreModel() : MembershipTierData {
    return MembershipTierData(
        id = id,
        name = name,
        description = description,
        isTest = isTest,
        periodType = periodType.toCoreModel(),
        periodValue = periodValue,
        priceStripeUsdCents = priceStripeUsdCents,
        anyNamesCountIncluded = anyNamesCountIncluded,
        anyNameMinLength = anyNameMinLength,
        features = features,
        colorStr = colorStr,
        stripeProductId = stripeProductId.ifEmpty { null },
        stripeManageUrl = stripeManageUrl.ifEmpty { null },
        iosProductId = iosProductId.ifEmpty { null },
        iosManageUrl = iosManageUrl.ifEmpty { null },
        androidProductId = androidProductId.ifEmpty { null },
        androidManageUrl = androidManageUrl.ifEmpty { null },
    )
}

fun MEmailVerificationStatus.toCoreModel(): EmailVerificationStatus {
    return when (this) {
        MEmailVerificationStatus.StatusNotVerified -> EmailVerificationStatus.STATUS_VERIFIED
        MEmailVerificationStatus.StatusCodeSent -> EmailVerificationStatus.STATUS_CODE_SENT
        MEmailVerificationStatus.StatusVerified -> EmailVerificationStatus.STATUS_VERIFIED
    }
}

fun MNameServiceNameType.toCoreModel(): NameServiceNameType = when (this) {
    NameserviceNameType.AnyName -> NameServiceNameType.ANY_NAME
}
//endregion

//region SYNC STATUS
fun MSpaceSyncStatus.toCoreModel(): SpaceSyncStatus = when (this) {
    MSpaceSyncStatus.Synced -> SpaceSyncStatus.SYNCED
    MSpaceSyncStatus.Syncing -> SpaceSyncStatus.SYNCING
    MSpaceSyncStatus.Error -> SpaceSyncStatus.ERROR
    MSpaceSyncStatus.Offline -> SpaceSyncStatus.OFFLINE
    MSpaceSyncStatus.NetworkNeedsUpdate -> SpaceSyncStatus.NETWORK_UPDATE_NEEDED
}

fun MSpaceSyncNetwork.toCoreModel(): SpaceSyncNetwork = when (this) {
    anytype.Event.Space.Network.Anytype -> SpaceSyncNetwork.ANYTYPE
    anytype.Event.Space.Network.SelfHost -> SpaceSyncNetwork.SELF_HOST
    anytype.Event.Space.Network.LocalOnly -> SpaceSyncNetwork.LOCAL_ONLY
}

fun MSpaceSyncError.toCoreModel(): SpaceSyncError = when (this) {
    MSpaceSyncError.Null -> SpaceSyncError.NULL
    MSpaceSyncError.StorageLimitExceed -> SpaceSyncError.STORAGE_LIMIT_EXCEED
    MSpaceSyncError.IncompatibleVersion -> SpaceSyncError.INCOMPATIBLE_VERSION
    MSpaceSyncError.NetworkError -> SpaceSyncError.NETWORK_ERROR
}

fun MP2PStatus.toCoreModel(): P2PStatus = when (this) {
    MP2PStatus.NotConnected -> P2PStatus.NOT_CONNECTED
    MP2PStatus.NotPossible -> P2PStatus.NOT_POSSIBLE
    MP2PStatus.Connected -> P2PStatus.CONNECTED
    MP2PStatus.Restricted -> P2PStatus.RESTRICTED
}

fun MChatMessage.core(): Chat.Message = Chat.Message(
    id = id,
    content = message?.core(),
    creator = creator,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
    replyToMessageId = replyToMessageId.ifEmpty { null },
    attachments = attachments.map { attachment ->
        Chat.Message.Attachment(
            target = attachment.target,
            type = when(attachment.type) {
                MChatMessageAttachmentType.FILE -> Chat.Message.Attachment.Type.File
                MChatMessageAttachmentType.IMAGE -> Chat.Message.Attachment.Type.Image
                MChatMessageAttachmentType.LINK -> Chat.Message.Attachment.Type.Link
            }
        )
    },
    order = orderId,
    reactions = reactions?.reactions?.mapValues { (unicode, identities) ->
        identities.ids
    } ?: emptyMap()
)

fun MChatMessageContent.core(): Chat.Message.Content = Chat.Message.Content(
    text = text,
    style = style.toCoreModels(),
    marks = marks.map { it.toCoreModels() }
)

fun Rpc.History.Version.toCoreModel(): Version {
    return Version(
        id = id,
        previousIds = previousIds,
        spaceMember = authorId,
        spaceMemberName = authorName,
        timestamp = TimeInSeconds(time),
        groupId = groupId
    )
}

fun Rpc.History.ShowVersion.Response.toCoreModel(): ShowVersionResponse {
    return ShowVersionResponse(
        payload = objectView?.toPayload(),
        version = version?.toCoreModel(),
        traceId = traceId
    )
}

fun Rpc.History.DiffVersions.Response.toCoreModel(
    context: Id
): DiffVersionResponse {
    return DiffVersionResponse(
        historyEvents = historyEvents.mapNotNull { it.toCoreModels(context) },
        objectView = objectView?.toCore()
    )
}