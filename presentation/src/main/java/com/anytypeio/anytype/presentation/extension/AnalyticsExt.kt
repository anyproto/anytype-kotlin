package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectMoveToBin
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectScreenShow
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.analytics.props.Props.Companion.OBJ_LAYOUT_NONE
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.presentation.editor.editor.Markup
import kotlinx.coroutines.CoroutineScope

fun Block.Prototype.getAnalyticsEvent(
    eventName: String,
    startTime: Long,
    middlewareTime: Long,
    renderTime: Long
): EventAnalytics.Anytype {
    val props = when (this) {
        is Block.Prototype.Text -> {
            Props(
                mapOf(
                    EventsPropertiesKey.type to "text",
                    EventsPropertiesKey.style to getStyleName()
                )
            )
        }
        is Block.Prototype.Page -> {
            Props(
                mapOf(
                    EventsPropertiesKey.style to style.name
                )
            )
        }
        is Block.Prototype.File -> {
            Props(
                mapOf(
                    EventsPropertiesKey.type to getTypePropName(),
                    EventsPropertiesKey.style to getStyleName()
                )
            )
        }
        is Block.Prototype.Link -> {
            Props(mapOf(EventsPropertiesKey.type to "link"))
        }
        is Block.Prototype.Relation -> {
            Props(mapOf(EventsPropertiesKey.type to "relation"))
        }
        is Block.Prototype.DividerLine -> {
            Props(
                mapOf(
                    EventsPropertiesKey.type to "div",
                    EventsPropertiesKey.style to "line"
                )
            )
        }
        is Block.Prototype.DividerDots -> {
            Props(
                mapOf(
                    EventsPropertiesKey.type to "div",
                    EventsPropertiesKey.style to "dots"
                )
            )
        }
        is Block.Prototype.Bookmark -> {
            Props(mapOf(EventsPropertiesKey.type to "bookmark"))
        }
        is Block.Prototype.Latex -> {
            Props(mapOf(EventsPropertiesKey.type to "latex"))
        }
        is Block.Prototype.TableOfContents -> {
            Props(mapOf(EventsPropertiesKey.type to "table_of_contents"))
        }
        is Block.Prototype.SimpleTable -> {
            Props(mapOf(EventsPropertiesKey.type to "table"))
        }
    }

    return EventAnalytics.Anytype(
        name = eventName,
        props = props,
        duration = EventAnalytics.Duration(
            start = startTime,
            middleware = middlewareTime,
            render = renderTime
        )
    )
}

fun Block.Content.Text.Style.getStyleName(): String = when (this) {
    Block.Content.Text.Style.P -> "Paragraph"
    Block.Content.Text.Style.H1 -> "Header1"
    Block.Content.Text.Style.H2 -> "Header2"
    Block.Content.Text.Style.H3 -> "Header3"
    Block.Content.Text.Style.H4 -> "Header4"
    Block.Content.Text.Style.TITLE -> "Title"
    Block.Content.Text.Style.QUOTE -> "Highlighted"
    Block.Content.Text.Style.CODE_SNIPPET -> "Code"
    Block.Content.Text.Style.BULLET -> "Bulleted"
    Block.Content.Text.Style.NUMBERED -> "Numbered"
    Block.Content.Text.Style.TOGGLE -> "Toggle"
    Block.Content.Text.Style.CHECKBOX -> "Checkbox"
    Block.Content.Text.Style.DESCRIPTION -> "Description"
    Block.Content.Text.Style.CALLOUT -> "Callout"
}

fun Block.Prototype.Text.getStyleName() = this.style.getStyleName()

fun Block.Prototype.File.getStyleName() = when (this.type) {
    Block.Content.File.Type.NONE -> "None"
    Block.Content.File.Type.FILE -> "File"
    Block.Content.File.Type.IMAGE -> "Image"
    Block.Content.File.Type.VIDEO -> "Video"
    Block.Content.File.Type.AUDIO -> "Audio"
    Block.Content.File.Type.PDF -> "PDF"
}

fun Block.Prototype.File.getTypePropName() = this.type.getPropName()

fun Block.Content.File.Type?.getPropName() = when (this) {
    Block.Content.File.Type.NONE -> "none"
    Block.Content.File.Type.FILE -> "file"
    Block.Content.File.Type.IMAGE -> "image"
    Block.Content.File.Type.VIDEO -> "video"
    Block.Content.File.Type.AUDIO -> "audio"
    Block.Content.File.Type.PDF -> "pdf"
    else -> ""
}

fun Block.Align.getPropName() = when (this) {
    Block.Align.AlignCenter -> "center"
    Block.Align.AlignLeft -> "left"
    Block.Align.AlignRight -> "right"
}

fun Block.Content.Text.Mark.Type.getPropName() = when (this) {
    Block.Content.Text.Mark.Type.STRIKETHROUGH -> "strikethrough"
    Block.Content.Text.Mark.Type.KEYBOARD -> "code"
    Block.Content.Text.Mark.Type.ITALIC -> "italic"
    Block.Content.Text.Mark.Type.BOLD -> "bold"
    Block.Content.Text.Mark.Type.UNDERLINE -> "underline"
    Block.Content.Text.Mark.Type.LINK -> "linkURL"
    Block.Content.Text.Mark.Type.TEXT_COLOR -> "color"
    Block.Content.Text.Mark.Type.BACKGROUND_COLOR -> "bgcolor"
    Block.Content.Text.Mark.Type.MENTION -> "mention"
    Block.Content.Text.Mark.Type.EMOJI -> "emoji"
    Block.Content.Text.Mark.Type.OBJECT -> "linkObject"
}

fun Markup.Type.getPropName() = when (this) {
    Markup.Type.ITALIC -> "italic"
    Markup.Type.BOLD -> "bold"
    Markup.Type.STRIKETHROUGH -> "strikethrough"
    Markup.Type.TEXT_COLOR -> "color"
    Markup.Type.BACKGROUND_COLOR -> "bgcolor"
    Markup.Type.LINK -> "linkURL"
    Markup.Type.KEYBOARD -> "code"
    Markup.Type.MENTION -> "mention"
    Markup.Type.OBJECT -> "linkObject"
    Markup.Type.UNDERLINE -> "underline"
}

fun DVViewerType.getPropName() = when (this) {
    Block.Content.DataView.Viewer.Type.GRID -> "table"
    Block.Content.DataView.Viewer.Type.LIST -> "list"
    Block.Content.DataView.Viewer.Type.GALLERY -> "gallery"
    Block.Content.DataView.Viewer.Type.BOARD -> "kanban"
}

fun DVFilterCondition.getPropName() = when (this) {
    Block.Content.DataView.Filter.Condition.EQUAL -> "equal"
    Block.Content.DataView.Filter.Condition.NOT_EQUAL -> "notequal"
    Block.Content.DataView.Filter.Condition.GREATER -> "greater"
    Block.Content.DataView.Filter.Condition.LESS -> "less"
    Block.Content.DataView.Filter.Condition.GREATER_OR_EQUAL -> "greaterorequal"
    Block.Content.DataView.Filter.Condition.LESS_OR_EQUAL -> "lessorequal"
    Block.Content.DataView.Filter.Condition.LIKE -> "like"
    Block.Content.DataView.Filter.Condition.NOT_LIKE -> "notlike"
    Block.Content.DataView.Filter.Condition.IN -> "in"
    Block.Content.DataView.Filter.Condition.NOT_IN -> "notin"
    Block.Content.DataView.Filter.Condition.EMPTY -> "empty"
    Block.Content.DataView.Filter.Condition.NOT_EMPTY -> "notempty"
    Block.Content.DataView.Filter.Condition.ALL_IN -> "allin"
    Block.Content.DataView.Filter.Condition.NOT_ALL_IN -> "notallin"
    Block.Content.DataView.Filter.Condition.NONE -> "none"
    Block.Content.DataView.Filter.Condition.EXACT_IN -> "exactin"
    Block.Content.DataView.Filter.Condition.NOT_EXACT_IN -> "notexactin"
}

fun Relation.Format.getPropName() = when (this) {
    Relation.Format.SHORT_TEXT -> "shorttext"
    Relation.Format.LONG_TEXT -> "longtext"
    Relation.Format.NUMBER -> "number"
    Relation.Format.STATUS -> "status"
    Relation.Format.TAG -> "tag"
    Relation.Format.DATE -> "date"
    Relation.Format.FILE -> "file"
    Relation.Format.CHECKBOX -> "checkbox"
    Relation.Format.URL -> "url"
    Relation.Format.EMAIL -> "email"
    Relation.Format.PHONE -> "phone"
    Relation.Format.EMOJI -> "emoji"
    Relation.Format.OBJECT -> "object"
    Relation.Format.RELATIONS -> "relations"
}

/**
 *  ScreenObject - event code
 *  type - ObjectType
 */
fun CoroutineScope.sendAnalyticsObjectShowEvent(
    analytics: Analytics,
    startTime: Long,
    middleTime: Long,
    type: String?,
    layoutCode: Double?,
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = objectScreenShow,
        props = propsForObjectEvents(type = type, layoutCode = layoutCode, context = context),
        startTime = startTime,
        middleTime = middleTime,
        renderTime = System.currentTimeMillis()
    )
}

private fun propsForObjectEvents(
    type: String?,
    layoutCode: Double?,
    route: String? = null,
    context: String? = null
): Props {
    val objType = when {
        type == null -> null
        type.startsWith(Props.CHAR_TYPE_BUNDLED, ignoreCase = true) -> type
        else -> Props.OBJ_TYPE_CUSTOM
    }
    val layout = layoutCode?.toInt()?.let { code ->
        ObjectType.Layout.values().find { layout ->
            layout.code == code
        }
    }?.name ?: OBJ_LAYOUT_NONE
    return Props(
        mapOf(
            EventsPropertiesKey.objectType to objType,
            EventsPropertiesKey.layout to layout,
            EventsPropertiesKey.route to route,
            EventsPropertiesKey.context to context
        )
    )
}

/**
 *  SearchResult - event code
 *  index - number of position of chosen item, started from 1
 *  length - the number of characters entered in the search string
 */
fun CoroutineScope.sendAnalyticsSearchResultEvent(
    analytics: Analytics,
    pos: Int,
    length: Int,
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.searchResult,
        props = Props(
            mapOf(
                EventsPropertiesKey.index to pos,
                EventsPropertiesKey.length to length,
                EventsPropertiesKey.context to context
            )
        )
    )
}

/**
 *  SearchQuery - event code
 *  route - ScreenSearch or MenuMention or MenuSearch
 *  length - the number of characters entered in the search string
 */
fun CoroutineScope.sendAnalyticsSearchQueryEvent(
    analytics: Analytics,
    route: String,
    length: Int,
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.searchQuery,
        props = Props(
            mapOf(
                EventsPropertiesKey.route to route,
                EventsPropertiesKey.length to length,
                EventsPropertiesKey.context to context
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsSearchWordsEvent(
    analytics: Analytics,
    length: Int,
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.searchWords,
        props = Props(
            mapOf(
                EventsPropertiesKey.length to length,
                EventsPropertiesKey.context to context
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsSetCoverEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectSetCover
    )
}

fun CoroutineScope.sendAnalyticsRemoveCoverEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectRemoveCover
    )
}

fun CoroutineScope.sendAnalyticsAddToFavoritesEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectAddToFavorites
    )
}

fun CoroutineScope.sendAnalyticsRemoveFromFavoritesEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectRemoveFromFavorites
    )
}

fun CoroutineScope.sendAnalyticsMoveToBinEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = objectMoveToBin
    )
}

suspend fun Analytics.sendAnalyticsSplitBlockEvent(
    style: TextStyle, startTime: Long, middlewareTime: Long
) {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.blockCreate,
        props = Props(
            mapOf(
                EventsPropertiesKey.type to "text",
                EventsPropertiesKey.style to style.getStyleName()
            )
        ),
        duration = EventAnalytics.Duration(
            start = startTime,
            middleware = middlewareTime,
            render = System.currentTimeMillis()
        )
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsCreateBlockEvent(
    prototype: Block.Prototype, startTime: Long, middlewareTime: Long
) {
    val event = prototype.getAnalyticsEvent(
        eventName = EventsDictionary.blockCreate,
        startTime = startTime,
        middlewareTime = middlewareTime,
        renderTime = System.currentTimeMillis()
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsDeleteBlockEvent(
    count: Int
) {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.blockDelete,
        props = Props(mapOf(EventsPropertiesKey.count to count))
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsRestoreFromBin(
    count: Int
) {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.restoreFromBin,
        props = Props(mapOf(EventsPropertiesKey.count to count))
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsRemoveObjects(
    count: Int
) {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.objectListDelete,
        props = Props(mapOf(EventsPropertiesKey.count to count))
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsUpdateTextMarkupEvent(type: Block.Content.Text.Mark.Type) {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.blockChangeTextStyle,
        props = Props(mapOf(EventsPropertiesKey.type to type.getPropName()))
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsChangeTextBlockStyleEvent(
    style: Block.Content.Text.Style,
    count: Int,
    analyticsContext: String?
) {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.blockChangeBlockStyle,
        props = Props(
            mapOf(
                EventsPropertiesKey.type to "Text",
                EventsPropertiesKey.style to style.getStyleName(),
                EventsPropertiesKey.count to count,
                EventsPropertiesKey.context to analyticsContext
            )
        )
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsDuplicateBlockEvent(count: Int) {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.blockDuplicate,
        props = Props(mapOf(EventsPropertiesKey.count to count))
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsCopyBlockEvent() {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.blockCopy
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsPasteBlockEvent() {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.blockPaste
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsReorderBlockEvent(count: Int) {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.blockReorder,
        props = Props(mapOf(EventsPropertiesKey.count to count))
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsUploadMediaEvent(mediaType: Mimetype) {
    val type = when (mediaType) {
        Mimetype.MIME_FILE_ALL -> "file"
        Mimetype.MIME_IMAGE_ALL -> "image"
        Mimetype.MIME_VIDEO_ALL -> "video"
        else -> ""
    }
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.blockUpload,
        props = Props(mapOf(EventsPropertiesKey.type to type))
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsDownloadMediaEvent(type: Block.Content.File.Type?) {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.blockDownload,
        props = Props(mapOf(EventsPropertiesKey.type to type.getPropName()))
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsUndoEvent() {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.objectUndo
    )
    registerEvent(event)
}

suspend fun Analytics.sendAnalyticsRedoEvent() {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.objectRedo
    )
    registerEvent(event)
}

fun CoroutineScope.sendAnalyticsBlockAlignEvent(
    analytics: Analytics,
    context: String?,
    count: Int,
    align: Block.Align
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.blockChangeBlockAlign,
        props = Props(
            mapOf(
                EventsPropertiesKey.align to align.getPropName(),
                EventsPropertiesKey.context to context,
                EventsPropertiesKey.count to count
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsObjectTypeChangeEvent(
    analytics: Analytics,
    typeId: Id,
    context: String? = null
) {
    val objType = Props.mapType(typeId)
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectTypeChanged,
        props = Props(
            mapOf(
                EventsPropertiesKey.objectType to objType,
                EventsPropertiesKey.context to context
            )
        )
    )
}

suspend fun Analytics.sendAnalyticsObjectLayoutChangeEvent(name: String) {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.objectLayoutChange,
        props = Props(mapOf(EventsPropertiesKey.layout to name))
    )
    registerEvent(event)
}

fun CoroutineScope.sendAnalyticsCreateRelationEvent(
    analytics: Analytics,
    format: String,
    type: String
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.relationCreate,
        props = Props(
            mapOf(
                EventsPropertiesKey.format to format,
                EventsPropertiesKey.type to type
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsAddRelationEvent(
    analytics: Analytics,
    format: String,
    type: String
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.relationAdd,
        props = Props(
            mapOf(
                EventsPropertiesKey.format to format,
                EventsPropertiesKey.type to type
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsRelationValueEvent(
    analytics: Analytics,
    type: String = "",
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.relationChangeValue,
        props = Props(
            mapOf(
                EventsPropertiesKey.type to type,
                EventsPropertiesKey.context to context
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsRelationDeleteEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.relationDelete
    )
}

fun CoroutineScope.sendAnalyticsShowSetEvent(
    analytics: Analytics,
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.setScreenShow,
        props = Props(mapOf(EventsPropertiesKey.context to context))
    )
}

fun CoroutineScope.sendAnalyticsAddViewEvent(
    analytics: Analytics,
    type: String
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.setAddView,
        props = Props(mapOf(EventsPropertiesKey.type to type))
    )
}

fun CoroutineScope.sendAnalyticsSwitchViewEvent(
    analytics: Analytics,
    type: String
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.setSwitchView,
        props = Props(mapOf(EventsPropertiesKey.type to type))
    )
}

fun CoroutineScope.sendAnalyticsRemoveViewEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.setRemoveView,
        props = Props.empty()
    )
}

fun CoroutineScope.sendAnalyticsAddFilterEvent(
    analytics: Analytics,
    condition: DVFilterCondition
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.setAddFilter,
        props = Props(
            mapOf(
                EventsPropertiesKey.condition to condition.getPropName()
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsChangeFilterValueEvent(
    analytics: Analytics,
    condition: DVFilterCondition
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.setChangeFilterValue,
        props = Props(
            mapOf(
                EventsPropertiesKey.condition to condition.getPropName()
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsRemoveFilterEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.setRemoveFilter,
        props = Props.empty()
    )
}

fun CoroutineScope.sendAnalyticsAddSortEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.setAddSort,
        props = Props.empty()
    )
}

fun CoroutineScope.sendAnalyticsChangeSortValueEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.setChangeSortValue,
        props = Props.empty()
    )
}

fun CoroutineScope.sendAnalyticsRemoveSortEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.setRemoveSort,
        props = Props.empty()
    )
}

fun CoroutineScope.sendAnalyticsObjectCreateEvent(
    analytics: Analytics,
    objType: String?,
    layout: Double?,
    route: String,
    startTime: Long? = null,
    middleTime: Long? = null,
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectCreate,
        props = propsForObjectEvents(
            type = objType,
            layoutCode = layout,
            route = route,
            context = context
        ),
        startTime = startTime,
        middleTime = middleTime,
        renderTime = System.currentTimeMillis()
    )
}

fun CoroutineScope.sendAnalyticsSetTitleEvent(
    analytics: Analytics,
    context: String? = null
) {
    val props = if (context != null) {
        Props(mapOf(EventsPropertiesKey.context to context))
    } else {
        Props.empty()
    }
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectSetTitle,
        props = props
    )
}

fun CoroutineScope.sendAnalyticsSetDescriptionEvent(
    analytics: Analytics,
    context: String? = null
) {
    val props = if (context != null) {
        Props(mapOf(EventsPropertiesKey.context to context))
    } else {
        Props.empty()
    }
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectSetDescription,
        props = props
    )
}

fun CoroutineScope.sendAnalyticsUpdateTextMarkupEvent(
    analytics: Analytics,
    type: Block.Content.Text.Mark.Type,
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.blockChangeTextStyle,
        props = Props(
            mapOf(
                EventsPropertiesKey.type to type.getPropName(),
                EventsPropertiesKey.context to context
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsUpdateTextMarkupEvent(
    analytics: Analytics,
    type: Markup.Type,
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.blockChangeTextStyle,
        props = Props(
            mapOf(
                EventsPropertiesKey.type to type.getPropName(),
                EventsPropertiesKey.context to context
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsBlockReorder(
    analytics: Analytics,
    count: Int,
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.blockReorder,
        props = Props(
            mapOf(
                EventsPropertiesKey.count to "$count",
                EventsPropertiesKey.context to context
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsBlockBackgroundEvent(
    analytics: Analytics,
    count: Int = 1,
    color: String,
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.blockChangeBackground,
        props = Props(
            mapOf(
                EventsPropertiesKey.color to color,
                EventsPropertiesKey.count to count,
                EventsPropertiesKey.context to context
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsGoBackEvent(
    analytics: Analytics,
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.goBack,
        props = Props(
            mapOf(
                EventsPropertiesKey.context to context
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsBlockActionEvent(
    analytics: Analytics,
    type: String,
    context: String? = null
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.blockAction,
        props = Props(
            mapOf(
                EventsPropertiesKey.context to context,
                EventsPropertiesKey.type to type
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsSlashMenuEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.slashMenu
    )
}

fun CoroutineScope.sendAnalyticsStyleMenuEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.styleMenu
    )
}

fun CoroutineScope.sendAnalyticsSelectionMenuEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.selectionMenu
    )
}

fun CoroutineScope.sendAnalyticsMentionMenuEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.mentionMenu
    )
}