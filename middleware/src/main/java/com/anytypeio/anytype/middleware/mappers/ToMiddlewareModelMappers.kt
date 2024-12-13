package com.anytypeio.anytype.middleware.mappers

import anytype.Rpc
import anytype.model.InternalFlag
import anytype.model.Range
import anytype.model.RelationFormat
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.BlockSplitMode
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVSortEmptyType
import com.anytypeio.anytype.core_models.DeviceNetworkType
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.NameServiceNameType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions


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
    is Block.Content.TableOfContents -> {
        MBlock(
            id = id,
            backgroundColor = backgroundColor.orEmpty(),
            fields = fields.toMiddlewareModel(),
            tableOfContents = MBTableOfContents()
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
    Block.Content.Layout.Type.TABLE_ROW -> MBLayout(style = MBLayoutStyle.TableRows)
    Block.Content.Layout.Type.TABLE_COLUMN -> MBLayout(style = MBLayoutStyle.TableColumns)
}

fun Block.Content.Bookmark.toMiddlewareModel(): MBBookmark = MBBookmark(
    description = description.orEmpty(),
    faviconHash = favicon.orEmpty(),
    title = title.orEmpty(),
    url = url.orEmpty(),
    imageHash = image.orEmpty(),
    state = state.toMiddlewareModel(),
    targetObjectId = targetObjectId.orEmpty()
)

fun Block.Content.Bookmark.State.toMiddlewareModel(): MBookmarkState = when (this) {
    Block.Content.Bookmark.State.EMPTY -> MBookmarkState.Empty
    Block.Content.Bookmark.State.DONE -> MBookmarkState.Done
    Block.Content.Bookmark.State.ERROR -> MBookmarkState.Error
    Block.Content.Bookmark.State.FETCHING -> MBookmarkState.Fetching
}

fun Block.Content.File.toMiddlewareModel(): MBFile = MBFile(
    targetObjectId = targetObjectId.orEmpty(),
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
        relations = relations.toList()
    )
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
    Block.Content.Text.Mark.Type.UNDERLINE -> {
        MBMark(
            range = range.range(),
            type = MBMarkType.Underscored
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
    Block.Content.Text.Mark.Type.EMOJI -> {
        MBMark(
            range = range.range(),
            type = MBMarkType.Emoji,
            param_ = param.orEmpty()
        )
    }
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
    Block.Align.AlignJustify -> MBAlign.AlignJustify
    null -> MBAlign.AlignLeft
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
        },
        defaultTemplateId = defaultTemplate.orEmpty(),
        defaultObjectTypeId = defaultObjectType.orEmpty(),
    )

fun Block.Content.DataView.Viewer.Type.toMiddlewareModel(): MDVViewType = when (this) {
    Block.Content.DataView.Viewer.Type.GRID -> MDVViewType.Table
    Block.Content.DataView.Viewer.Type.LIST -> MDVViewType.List
    Block.Content.DataView.Viewer.Type.GALLERY -> MDVViewType.Gallery
    Block.Content.DataView.Viewer.Type.BOARD -> MDVViewType.Kanban
    Block.Content.DataView.Viewer.Type.CALENDAR -> MDVViewType.Calendar
    Block.Content.DataView.Viewer.Type.GRAPH -> MDVViewType.Graph
}

fun Block.Content.DataView.Sort.toMiddlewareModel(): MDVSort {
    val emptyType = this.emptyType
    return if (emptyType != null) {
        MDVSort(
            id = id,
            RelationKey = relationKey,
            type = type.toMiddlewareModel(),
            includeTime = includeTime,
            customOrder = customOrder,
            format = relationFormat.toMiddlewareModel(),
            emptyPlacement = emptyType.toMiddlewareModel()
        )
    } else {
        MDVSort(
            id = id,
            RelationKey = relationKey,
            type = type.toMiddlewareModel(),
            includeTime = includeTime,
            customOrder = customOrder,
            format = relationFormat.toMiddlewareModel(),
        )
    }
}

fun DVSortEmptyType.toMiddlewareModel(): MDVSortEmptyType = when (this) {
    DVSortEmptyType.NOT_SPECIFIC -> MDVSortEmptyType.NotSpecified
    DVSortEmptyType.START -> MDVSortEmptyType.Start
    DVSortEmptyType.END -> MDVSortEmptyType.End
}

fun Block.Content.DataView.Sort.Type.toMiddlewareModel(): MDVSortType = when (this) {
    Block.Content.DataView.Sort.Type.ASC -> MDVSortType.Asc
    Block.Content.DataView.Sort.Type.DESC -> MDVSortType.Desc
    Block.Content.DataView.Sort.Type.CUSTOM -> MDVSortType.Custom
}

fun Block.Content.DataView.Filter.toMiddlewareModel(): MDVFilter =
    MDVFilter(
        id = id,
        RelationKey = relation,
        operator_ = operator.toMiddlewareModel(),
        condition = condition.toMiddlewareModel(),
        quickOption = quickOption.toMiddlewareModel(),
        value_ = value.validate(),
        format = relationFormat?.toMiddlewareModel() ?: RelationFormat.longtext
    )

fun Any?.validate() : Any? {
    return if (this is Int)
        this.toDouble()
    else
        this
}

fun Block.Content.DataView.Filter.Operator.toMiddlewareModel(): MDVFilterOperator = when (this) {
    Block.Content.DataView.Filter.Operator.AND -> MDVFilterOperator.And
    Block.Content.DataView.Filter.Operator.OR -> MDVFilterOperator.Or
    Block.Content.DataView.Filter.Operator.NO -> MDVFilterOperator.No
}

fun Block.Content.DataView.Filter.QuickOption.toMiddlewareModel(): MDVFilterQuickOption =
    when (this) {
        Block.Content.DataView.Filter.QuickOption.EXACT_DATE -> MDVFilterQuickOption.ExactDate
        Block.Content.DataView.Filter.QuickOption.YESTERDAY -> MDVFilterQuickOption.Yesterday
        Block.Content.DataView.Filter.QuickOption.TODAY -> MDVFilterQuickOption.Today
        Block.Content.DataView.Filter.QuickOption.TOMORROW -> MDVFilterQuickOption.Tomorrow
        Block.Content.DataView.Filter.QuickOption.LAST_WEEK -> MDVFilterQuickOption.LastWeek
        Block.Content.DataView.Filter.QuickOption.CURRENT_WEEK -> MDVFilterQuickOption.CurrentWeek
        Block.Content.DataView.Filter.QuickOption.NEXT_WEEK -> MDVFilterQuickOption.NextWeek
        Block.Content.DataView.Filter.QuickOption.LAST_MONTH -> MDVFilterQuickOption.LastMonth
        Block.Content.DataView.Filter.QuickOption.CURRENT_MONTH -> MDVFilterQuickOption.CurrentMonth
        Block.Content.DataView.Filter.QuickOption.NEXT_MONTH -> MDVFilterQuickOption.NextMonth
        Block.Content.DataView.Filter.QuickOption.DAYS_AGO -> MDVFilterQuickOption.NumberOfDaysAgo
        Block.Content.DataView.Filter.QuickOption.DAYS_AHEAD -> MDVFilterQuickOption.NumberOfDaysNow
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
    Block.Content.DataView.Filter.Condition.EXACT_IN -> MDVFilterCondition.ExactIn
    Block.Content.DataView.Filter.Condition.NOT_EXACT_IN -> MDVFilterCondition.NotExactIn
    Block.Content.DataView.Filter.Condition.EXISTS -> MDVFilterCondition.Exists
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
    ObjectType.Layout.IMAGE -> MOTypeLayout.image
    ObjectType.Layout.NOTE -> MOTypeLayout.note
    ObjectType.Layout.SPACE -> MOTypeLayout.space
    ObjectType.Layout.BOOKMARK -> MOTypeLayout.bookmark
    ObjectType.Layout.COLLECTION -> MOTypeLayout.collection
    ObjectType.Layout.DATE -> MOTypeLayout.date
    ObjectType.Layout.AUDIO -> MOTypeLayout.audio
    ObjectType.Layout.VIDEO -> MOTypeLayout.video
    ObjectType.Layout.RELATION_OPTION -> MOTypeLayout.relationOption
    ObjectType.Layout.RELATION_OPTION_LIST -> MOTypeLayout.relationOptionsList
    ObjectType.Layout.SPACE_VIEW -> MOTypeLayout.spaceView
    ObjectType.Layout.PARTICIPANT -> MOTypeLayout.participant
    ObjectType.Layout.PDF -> MOTypeLayout.pdf
    ObjectType.Layout.CHAT -> MOTypeLayout.chat
    ObjectType.Layout.CHAT_DERIVED -> MOTypeLayout.chatDerived
    ObjectType.Layout.TAG -> MOTypeLayout.tag
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
    Relation.Format.UNDEFINED -> throw IllegalStateException("Format was undefined")
}

fun List<InternalFlags>.toMiddlewareModel(): List<InternalFlag> = map {
    when (it) {
        InternalFlags.ShouldEmptyDelete -> InternalFlag(InternalFlag.Value.editorDeleteEmpty)
        InternalFlags.ShouldSelectTemplate -> InternalFlag(InternalFlag.Value.editorSelectTemplate)
        InternalFlags.ShouldSelectType -> InternalFlag(InternalFlag.Value.editorSelectType)
    }
}

fun Block.Content.Widget.Layout.mw() : MWidgetLayout = when(this) {
    Block.Content.Widget.Layout.TREE -> MWidgetLayout.Tree
    Block.Content.Widget.Layout.LINK -> MWidgetLayout.Link
    Block.Content.Widget.Layout.LIST -> MWidgetLayout.List
    Block.Content.Widget.Layout.COMPACT_LIST -> MWidgetLayout.CompactList
    Block.Content.Widget.Layout.VIEW -> MWidgetLayout.View
}

fun NetworkMode.toMiddlewareModel(): MNetworkMode = when (this) {
    NetworkMode.DEFAULT -> MNetworkMode.DefaultConfig
    NetworkMode.LOCAL -> MNetworkMode.LocalOnly
    NetworkMode.CUSTOM -> MNetworkMode.CustomConfig
}

fun SpaceMemberPermissions.toMw() : MParticipantPermission = when(this) {
    SpaceMemberPermissions.READER -> MParticipantPermission.Reader
    SpaceMemberPermissions.WRITER -> MParticipantPermission.Writer
    SpaceMemberPermissions.OWNER -> MParticipantPermission.Owner
    SpaceMemberPermissions.NO_PERMISSIONS -> MParticipantPermission.NoPermissions
}

fun NameServiceNameType.toMw(): MNameServiceNameType = when (this) {
    NameServiceNameType.ANY_NAME -> MNameServiceNameType.AnyName
}

fun MembershipPaymentMethod.toMw(): MMembershipPaymentMethod = when (this) {
    MembershipPaymentMethod.METHOD_NONE -> MMembershipPaymentMethod.MethodNone
    MembershipPaymentMethod.METHOD_STRIPE -> MMembershipPaymentMethod.MethodStripe
    MembershipPaymentMethod.METHOD_CRYPTO -> MMembershipPaymentMethod.MethodCrypto
    MembershipPaymentMethod.METHOD_INAPP_APPLE -> MMembershipPaymentMethod.MethodInappApple
    MembershipPaymentMethod.METHOD_INAPP_GOOGLE -> MMembershipPaymentMethod.MethodInappGoogle
}

fun Chat.Message.mw(): MChatMessage = MChatMessage(
    id = id,
    message = content?.mw(),
    orderId = order,
    attachments = attachments.map { it.mw() },
    createdAt = createdAt,
    modifiedAt = modifiedAt,
    creator = creator,
    replyToMessageId = replyToMessageId.orEmpty(),
    reactions = MChatMessageReactions(
        reactions = reactions.mapValues { (unicode, ids) ->
            MChatMessageReactionIdentity(
                ids = ids
            )
        }
    )

)

fun Chat.Message.Content.mw(): MChatMessageContent = MChatMessageContent(
    text = text,
    marks = marks.map { it.toMiddlewareModel() },
    style = style.toMiddlewareModel()
)

fun Chat.Message.Attachment.mw(): MChatMessageAttachment = MChatMessageAttachment(
    target = target,
    type = when(type) {
        Chat.Message.Attachment.Type.File -> MChatMessageAttachmentType.FILE
        Chat.Message.Attachment.Type.Image -> MChatMessageAttachmentType.IMAGE
        Chat.Message.Attachment.Type.Link -> MChatMessageAttachmentType.LINK
    }
)

fun Rpc.Object.SearchWithMeta.Response.toCoreModelSearchResults(): List<Command.SearchWithMeta.Result> {
    return results.map { result ->
        Command.SearchWithMeta.Result(
            obj = result.objectId,
            wrapper = ObjectWrapper.Basic(result.details.orEmpty()),
            metas = result.meta.map { meta ->
                val dependentObjectDetails = meta.relationDetails.orEmpty()
                val source = if (meta.blockId.isNotEmpty()) {
                    Command.SearchWithMeta.Result.Meta.Source.Block(meta.blockId)
                } else {
                    Command.SearchWithMeta.Result.Meta.Source.Relation(meta.relationKey)
                }
                Command.SearchWithMeta.Result.Meta(
                    highlight = meta.highlight,
                    source = source,
                    dependencies = if (dependentObjectDetails.isNotEmpty()) {
                        listOf(
                            ObjectWrapper.Basic(dependentObjectDetails)
                        )
                    } else {
                        emptyList()
                    },
                    ranges = meta.highlightRanges.map { range ->
                        IntRange(
                            start = range.from,
                            endInclusive = range.to
                        )
                    }
                )
            }
        )
    }
}

fun DeviceNetworkType.mw(): MDeviceNetworkType = when(this) {
    DeviceNetworkType.WIFI -> MDeviceNetworkType.WIFI
    DeviceNetworkType.CELLULAR -> MDeviceNetworkType.CELLULAR
    DeviceNetworkType.NOT_CONNECTED -> MDeviceNetworkType.NOT_CONNECTED
}

