package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.addFilter
import com.anytypeio.anytype.analytics.base.EventsDictionary.addSort
import com.anytypeio.anytype.analytics.base.EventsDictionary.addView
import com.anytypeio.anytype.analytics.base.EventsDictionary.changeFilterValue
import com.anytypeio.anytype.analytics.base.EventsDictionary.changeSortValue
import com.anytypeio.anytype.analytics.base.EventsDictionary.changeViewType
import com.anytypeio.anytype.analytics.base.EventsDictionary.collectionScreenShow
import com.anytypeio.anytype.analytics.base.EventsDictionary.duplicateView
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectCreate
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectDuplicate
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectMoveToBin
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectScreenShow
import com.anytypeio.anytype.analytics.base.EventsDictionary.removeFilter
import com.anytypeio.anytype.analytics.base.EventsDictionary.removeSort
import com.anytypeio.anytype.analytics.base.EventsDictionary.removeView
import com.anytypeio.anytype.analytics.base.EventsDictionary.repositionView
import com.anytypeio.anytype.analytics.base.EventsDictionary.setScreenShow
import com.anytypeio.anytype.analytics.base.EventsDictionary.setSelectQuery
import com.anytypeio.anytype.analytics.base.EventsDictionary.switchView
import com.anytypeio.anytype.analytics.base.EventsDictionary.turnIntoCollection
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.features.WidgetAnalytics
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.analytics.props.Props.Companion.OBJ_LAYOUT_NONE
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.analytics.props.Props.Companion.OBJ_TYPE_CUSTOM
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.source.BundledWidgetSourceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
    Markup.Type.ITALIC -> "Italic"
    Markup.Type.BOLD -> "Bold"
    Markup.Type.STRIKETHROUGH -> "Strike"
    Markup.Type.TEXT_COLOR -> "Color"
    Markup.Type.BACKGROUND_COLOR -> "BgColor"
    Markup.Type.LINK -> "Link"
    Markup.Type.KEYBOARD -> "Code"
    Markup.Type.MENTION -> "Mention"
    Markup.Type.OBJECT -> "Object"
    Markup.Type.UNDERLINE -> "Underline"
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
    Block.Content.DataView.Filter.Condition.EXISTS -> "exists"
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
    Relation.Format.UNDEFINED -> "undefined"
}

/**
 *  ScreenObject - event code
 *  type - ObjectType
 */
fun CoroutineScope.sendAnalyticsObjectShowEvent(
    analytics: Analytics,
    startTime: Long
) {
    sendEvent(
        analytics = analytics,
        eventName = objectScreenShow,
        props = propsForObjectEvents(),
        startTime = startTime,
        middleTime = System.currentTimeMillis(),
        renderTime = System.currentTimeMillis()
    )
}

private fun propsForObjectEvents(
    layoutCode: Double? = null,
    route: String? = null,
    context: String? = null,
    originalId: String? = null,
    sourceObject: String? = null
): Props {
    val objType = sourceObject ?: OBJ_TYPE_CUSTOM
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
            EventsPropertiesKey.context to context,
            EventsPropertiesKey.originalId to originalId
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

fun CoroutineScope.sendAnalyticsBackLinkAddEvent(
    analytics: Analytics,
    startTime: Long
) {
    val props = Props(
        buildMap {
            analytics.getContext()?.let { put("context", it) }
            analytics.getOriginalId()?.let { put("originalId", it) }
            put("linkType", "Object")
        }
    )
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectLinkTo,
        props = props,
        startTime = startTime,
        middleTime = System.currentTimeMillis()
    )
}

fun CoroutineScope.sendAnalyticsAddToCollectionEvent(
    analytics: Analytics,
    startTime: Long
) {
    val props = Props(
        buildMap {
            analytics.getContext()?.let { put("context", it) }
            analytics.getOriginalId()?.let { put("originalId", it) }
            put("linkType", "Collection")
        }
    )
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectLinkTo,
        props = props,
        startTime = startTime,
        middleTime = System.currentTimeMillis()
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

fun CoroutineScope.sendAnalyticsDuplicateEvent(
    analytics: Analytics,
    startTime: Long
) {
    sendEvent(
        analytics = analytics,
        eventName = objectDuplicate,
        startTime = startTime,
        middleTime = System.currentTimeMillis()
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
    objType: ObjectWrapper.Type?
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectTypeChanged,
        props = propsForObjectEvents(
            context = analytics.getContext(),
            originalId = analytics.getOriginalId(),
            sourceObject = objType?.sourceObject
        ),
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

fun CoroutineScope.sendAnalyticsObjectCreateEvent(
    analytics: Analytics,
    storeOfObjectTypes: StoreOfObjectTypes,
    type: String?,
    route: String,
    startTime: Long? = null
) {
    this.launch {
        val objType = type?.let { storeOfObjectTypes.get(it) }
        analytics.sendEvent(
            eventName = objectCreate,
            props = propsForObjectEvents(
                route = route,
                context = analytics.getContext(),
                originalId = analytics.getOriginalId(),
                sourceObject = objType?.sourceObject
            ),
            startTime = startTime,
            middleTime = System.currentTimeMillis()
        )
    }
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

fun CoroutineScope.sendAnalyticsBlockMoveToEvent(
    analytics: Analytics,
    count: Int
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.blockMove,
        props = Props(mapOf(EventsPropertiesKey.count to count))
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

fun CoroutineScope.sendAnalyticsBookmarkOpen(analytics: Analytics) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.bookmarkOpenUrl
    )
}

fun CoroutineScope.sendAnalyticsOpenAsObject(analytics: Analytics, type: String) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectOpenAs,
        props = Props(
            mapOf(EventsPropertiesKey.type to type)
        )
    )
}

fun CoroutineScope.sendAnalyticsObjectReload(analytics: Analytics) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectReload
    )
}

fun CoroutineScope.sendAnalyticsRelationUrlOpen(analytics: Analytics) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.relationUrlOpen
    )
}

fun CoroutineScope.sendAnalyticsRelationUrlCopy(analytics: Analytics) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.relationUrlCopy
    )
}

fun CoroutineScope.sendAnalyticsRelationUrlEdit(analytics: Analytics) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.relationUrlEdit
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

fun ObjectState.DataView.getAnalyticsParams(): Pair<String?, String?> {
    val block = blocks.firstOrNull { it.id == root }
    val analyticsContext = block?.fields?.analyticsContext
    val analyticsObjectId = if (analyticsContext != null) block.fields.analyticsOriginalId else null
    return Pair(analyticsContext, analyticsObjectId)
}

fun CoroutineScope.logEvent(
    state: ObjectState,
    analytics: Analytics,
    event: ObjectStateAnalyticsEvent,
    startTime: Long? = null,
    type: String? = null,
    condition: DVFilterCondition? = null
) {
    if (state !is ObjectState.DataView) return
    val middleTime = System.currentTimeMillis()
    val embedTypeDefault = "object"
    val objectTypeDefault = when (state) {
        is ObjectState.DataView.Collection -> "ot-collection"
        is ObjectState.DataView.Set -> "ot-set"
    }
    val scope = this
    val params = state.getAnalyticsParams()
    val analyticsContext = params.first
    val analyticsObjectId = params.second
    when (event) {
        ObjectStateAnalyticsEvent.OPEN_OBJECT -> {
            when (state) {
                is ObjectState.DataView.Collection -> scope.sendEvent(
                    analytics = analytics,
                    eventName = collectionScreenShow,
                    startTime = startTime,
                    middleTime = middleTime,
                    props = buildProps(
                        analyticsContext = analyticsContext,
                        analyticsObjectId = analyticsObjectId
                    )
                )
                is ObjectState.DataView.Set -> scope.sendEvent(
                    analytics = analytics,
                    eventName = setScreenShow,
                    startTime = startTime,
                    middleTime = middleTime,
                    props = buildProps(
                        analyticsContext = analyticsContext,
                        analyticsObjectId = analyticsObjectId,
                        embedType = embedTypeDefault
                    )
                )
            }
        }
        ObjectStateAnalyticsEvent.TURN_INTO_COLLECTION -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = turnIntoCollection,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId
                )
            )
        }
        ObjectStateAnalyticsEvent.SELECT_QUERY -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = setSelectQuery,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    type = type
                )
            )
        }
        ObjectStateAnalyticsEvent.ADD_VIEW -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = addView,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    type = type,
                    embedType = embedTypeDefault,
                    objectType = objectTypeDefault
                )
            )
        }
        ObjectStateAnalyticsEvent.CHANGE_VIEW_TYPE -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = changeViewType,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    type = type,
                    embedType = embedTypeDefault,
                    objectType = objectTypeDefault
                )
            )
        }
        ObjectStateAnalyticsEvent.REMOVE_VIEW -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = removeView,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    embedType = embedTypeDefault,
                    objectType = objectTypeDefault
                )
            )
        }
        ObjectStateAnalyticsEvent.SWITCH_VIEW -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = switchView,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    type = type,
                    embedType = embedTypeDefault,
                    objectType = objectTypeDefault
                )
            )
        }
        ObjectStateAnalyticsEvent.REPOSITION_VIEW -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = repositionView,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    type = type,
                    embedType = embedTypeDefault,
                    objectType = objectTypeDefault
                )
            )
        }
        ObjectStateAnalyticsEvent.DUPLICATE_VIEW -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = duplicateView,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    type = type,
                    embedType = embedTypeDefault,
                    objectType = objectTypeDefault
                )
            )
        }
        ObjectStateAnalyticsEvent.ADD_FILTER -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = addFilter,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    embedType = embedTypeDefault,
                    objectType = objectTypeDefault,
                    condition = condition
                )
            )
        }
        ObjectStateAnalyticsEvent.CHANGE_FILTER_VALUE -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = changeFilterValue,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    embedType = embedTypeDefault,
                    objectType = objectTypeDefault,
                    condition = condition
                )
            )
        }
        ObjectStateAnalyticsEvent.REMOVE_FILTER -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = removeFilter,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    embedType = embedTypeDefault,
                    objectType = objectTypeDefault
                )
            )
        }
        ObjectStateAnalyticsEvent.ADD_SORT -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = addSort,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    embedType = embedTypeDefault,
                    objectType = objectTypeDefault,
                    type = type
                )
            )
        }
        ObjectStateAnalyticsEvent.CHANGE_SORT_VALUE -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = changeSortValue,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    embedType = embedTypeDefault,
                    objectType = objectTypeDefault,
                    type = type
                )
            )
        }
        ObjectStateAnalyticsEvent.REMOVE_SORT -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = removeSort,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    embedType = embedTypeDefault,
                    objectType = objectTypeDefault
                )
            )
        }
        ObjectStateAnalyticsEvent.OBJECT_CREATE -> {
            val route = when (state) {
                is ObjectState.DataView.Collection -> EventsDictionary.Routes.objCreateCollection
                is ObjectState.DataView.Set -> EventsDictionary.Routes.objCreateSet
            }
            scope.sendEvent(
                analytics = analytics,
                eventName = objectCreate,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    analyticsContext = analyticsContext,
                    analyticsObjectId = analyticsObjectId,
                    objectType = type ?: OBJ_TYPE_CUSTOM,
                    route = route
                )
            )
        }
    }
}

private fun buildProps(
    analyticsContext: String? = null,
    analyticsObjectId: String? = null,
    embedType: String? = null,
    type: String? = null,
    objectType: String? = null,
    condition: DVFilterCondition? = null,
    route: String? = null
): Props {
    return Props(
        map = buildMap {
            if (analyticsContext != null) put("context", analyticsContext)
            if (analyticsObjectId != null) put("originalId", analyticsObjectId)
            if (embedType != null) put("embedType", embedType)
            if (type != null) put("type", type)
            if (objectType != null) put("objectType", objectType)
            if (condition != null) put("condition", condition.getPropName())
            if (route != null) put("route", route)
        }
    )
}

enum class ObjectStateAnalyticsEvent {
    OPEN_OBJECT,
    TURN_INTO_COLLECTION,
    SELECT_QUERY,
    ADD_VIEW,
    CHANGE_VIEW_TYPE,
    REMOVE_VIEW,
    SWITCH_VIEW,
    REPOSITION_VIEW,
    DUPLICATE_VIEW,
    ADD_FILTER,
    CHANGE_FILTER_VALUE,
    REMOVE_FILTER,
    ADD_SORT,
    CHANGE_SORT_VALUE,
    REMOVE_SORT,
    OBJECT_CREATE
}

fun CoroutineScope.sendEditWidgetsEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.editWidgets
    )
}

fun CoroutineScope.sendAddWidgetEvent(
    analytics: Analytics,
    isInEditMode: Boolean
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.addWidget,
        props = Props(
            map = buildMap {
                if (isInEditMode)
                    put(WidgetAnalytics.CONTEXT, WidgetAnalytics.CONTEXT_EDITOR)
                else
                    put(WidgetAnalytics.CONTEXT, WidgetAnalytics.CONTEXT_HOME)
            }
        )
    )
}

fun CoroutineScope.sendChangeWidgetLayoutEvent(
    analytics: Analytics,
    layout: WidgetLayout,
    isInEditMode: Boolean
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.changeWidgetLayout,
        props = Props(
            buildMap {
                when(layout) {
                    Block.Content.Widget.Layout.TREE -> {
                        put(WidgetAnalytics.LAYOUT, WidgetAnalytics.WIDGET_LAYOUT_TREE)
                    }
                    Block.Content.Widget.Layout.LINK -> {
                        put(WidgetAnalytics.LAYOUT, WidgetAnalytics.WIDGET_LAYOUT_LINK)
                    }
                    Block.Content.Widget.Layout.LIST -> {
                        put(WidgetAnalytics.LAYOUT, WidgetAnalytics.WIDGET_LAYOUT_LIST)
                    }
                    Block.Content.Widget.Layout.COMPACT_LIST -> {
                        put(WidgetAnalytics.LAYOUT, WidgetAnalytics.WIDGET_LAYOUT_COMPACT_LIST)
                    }
                }
                if (isInEditMode)
                    put(WidgetAnalytics.CONTEXT, WidgetAnalytics.CONTEXT_EDITOR)
                else
                    put(WidgetAnalytics.CONTEXT, WidgetAnalytics.CONTEXT_HOME)
            }
        )
    )
}

fun CoroutineScope.sendChangeWidgetSourceEvent(
    analytics: Analytics,
    view: BundledWidgetSourceView,
    isForNewWidget: Boolean,
    isInEditMode: Boolean
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.changeWidgetSource,
        props = Props(
            buildMap {
                when(view) {
                    BundledWidgetSourceView.Collections -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_COLLECTIONS)
                    }
                    BundledWidgetSourceView.Favorites -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_FAVORITES)
                    }
                    BundledWidgetSourceView.Recent -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_RECENT)
                    }
                    BundledWidgetSourceView.Sets -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_SETS)
                    }
                }
                if (isForNewWidget)
                    put(WidgetAnalytics.ROUTE, WidgetAnalytics.ROUTE_ADD_WIDGET)
                if (isInEditMode)
                    put(WidgetAnalytics.CONTEXT, WidgetAnalytics.CONTEXT_EDITOR)
                else
                    put(WidgetAnalytics.CONTEXT, WidgetAnalytics.CONTEXT_HOME)
            }
        )
    )
}

fun CoroutineScope.sendChangeWidgetSourceEvent(
    analytics: Analytics,
    sourceObjectTypeId: Id,
    isCustomObjectType: Boolean = false,
    isForNewWidget: Boolean,
    isInEditMode: Boolean
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.changeWidgetSource,
        props = Props(
            buildMap {
                if (isCustomObjectType)
                    put(WidgetAnalytics.TYPE, WidgetAnalytics.CUSTOM_OBJECT_TYPE)
                else
                    put(WidgetAnalytics.TYPE, sourceObjectTypeId)
                if (isForNewWidget)
                    put(WidgetAnalytics.ROUTE, WidgetAnalytics.ROUTE_ADD_WIDGET)
                if (isInEditMode)
                    put(WidgetAnalytics.CONTEXT, WidgetAnalytics.CONTEXT_EDITOR)
                else
                    put(WidgetAnalytics.CONTEXT, WidgetAnalytics.CONTEXT_HOME)
            }
        )
    )
}

fun CoroutineScope.sendDeleteWidgetEvent(
    analytics: Analytics,
    sourceObjectTypeId: Id,
    isCustomObjectType: Boolean = false,
    isInEditMode: Boolean
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.deleteWidget,
        props = Props(
            buildMap {
                if (isCustomObjectType)
                    put(WidgetAnalytics.TYPE, WidgetAnalytics.CUSTOM_OBJECT_TYPE)
                else
                    put(WidgetAnalytics.TYPE, sourceObjectTypeId)
                if (isInEditMode)
                    put(WidgetAnalytics.CONTEXT, WidgetAnalytics.CONTEXT_EDITOR)
                else
                    put(WidgetAnalytics.CONTEXT, WidgetAnalytics.CONTEXT_HOME)
            }
        )
    )
}

fun CoroutineScope.sendDeleteWidgetEvent(
    analytics: Analytics,
    bundled: Widget.Source.Bundled,
    isInEditMode: Boolean
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.deleteWidget,
        props = Props(
            buildMap {
                when (bundled) {
                    Widget.Source.Bundled.Collections -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_COLLECTIONS)
                    }
                    Widget.Source.Bundled.Favorites -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_FAVORITES)
                    }
                    Widget.Source.Bundled.Recent -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_RECENT)
                    }
                    Widget.Source.Bundled.Sets -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_SETS)
                    }
                }
                if (isInEditMode)
                    put(WidgetAnalytics.CONTEXT, WidgetAnalytics.CONTEXT_EDITOR)
                else
                    put(WidgetAnalytics.CONTEXT, WidgetAnalytics.CONTEXT_HOME)
            }
        )
    )
}

fun CoroutineScope.sendSelectHomeTabEvent(
    analytics: Analytics,
    bundled: Widget.Source.Bundled
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.selectHomeTab,
        props = Props(
            buildMap {
                put(WidgetAnalytics.VIEW, WidgetAnalytics.VIEW_WIDGET)
                when (bundled) {
                    Widget.Source.Bundled.Collections -> {
                        put(WidgetAnalytics.TAB, WidgetAnalytics.WIDGET_SOURCE_COLLECTIONS)
                    }
                    Widget.Source.Bundled.Favorites -> {
                        put(WidgetAnalytics.TAB, WidgetAnalytics.WIDGET_SOURCE_FAVORITES)
                    }
                    Widget.Source.Bundled.Recent -> {
                        put(WidgetAnalytics.TAB, WidgetAnalytics.WIDGET_SOURCE_RECENT)
                    }
                    Widget.Source.Bundled.Sets -> {
                        put(WidgetAnalytics.TAB, WidgetAnalytics.WIDGET_SOURCE_SETS)
                    }
                }
            }
        )
    )
}

fun CoroutineScope.sendSelectHomeTabEvent(
    analytics: Analytics,
    sourceObjectTypeId: Id,
    isCustomObjectType: Boolean = false
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.selectHomeTab,
        props = Props(
            buildMap {
                put(WidgetAnalytics.VIEW, WidgetAnalytics.VIEW_WIDGET)
                if (isCustomObjectType)
                    put(WidgetAnalytics.TAB, WidgetAnalytics.CUSTOM_OBJECT_TYPE)
                else
                    put(WidgetAnalytics.TAB, sourceObjectTypeId)
            }
        )
    )
}

fun CoroutineScope.sendReorderWidgetEvent(
    analytics: Analytics,
    sourceObjectTypeId: Id,
    isCustomObjectType: Boolean = false
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.reorderWidget,
        props = Props(
            buildMap {
                if (isCustomObjectType)
                    put(WidgetAnalytics.TYPE, WidgetAnalytics.CUSTOM_OBJECT_TYPE)
                else
                    put(WidgetAnalytics.TYPE, sourceObjectTypeId)
            }
        )
    )
}

fun CoroutineScope.sendReorderWidgetEvent(
    analytics: Analytics,
    bundled: Widget.Source.Bundled
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.reorderWidget,
        props = Props(
            buildMap {
                when (bundled) {
                    Widget.Source.Bundled.Collections -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_COLLECTIONS)
                    }
                    Widget.Source.Bundled.Favorites -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_FAVORITES)
                    }
                    Widget.Source.Bundled.Recent -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_RECENT)
                    }
                    Widget.Source.Bundled.Sets -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_SETS)
                    }
                }
            }
        )
    )
}

suspend fun Analytics.sendScreenHomeEvent() {
    sendEvent(
        eventName = EventsDictionary.screenHome,
        props = Props(
            buildMap {
                put(WidgetAnalytics.VIEW, WidgetAnalytics.VIEW)
            }
        )
    )
}

suspend fun Analytics.sendSettingsStorageEvent() {
    sendEvent(
        eventName = EventsDictionary.screenSettingsStorage
    )
}

suspend fun Analytics.sendSettingsStorageManageEvent() {
    sendEvent(
        eventName = EventsDictionary.screenSettingsStorageManage
    )
}

suspend fun Analytics.sendSettingsOffloadEvent() {
    sendEvent(
        eventName = EventsDictionary.screenSettingsStorageOffload
    )
}

suspend fun Analytics.sendSettingsStorageOffloadEvent() {
    sendEvent(
        eventName = EventsDictionary.settingsStorageOffload
    )
}

suspend fun Analytics.proceedWithAccountEvent(
    configStorage: ConfigStorage,
    startTime: Long,
    eventName: String
) {
    val analyticsId = configStorage.get().analytics
    val userProperty = UserProperty.AccountId(analyticsId)
    updateUserProperty(userProperty)
    sendEvent(
        startTime = startTime,
        middleTime = System.currentTimeMillis(),
        eventName = eventName,
        props = Props(map = mapOf("accountId" to analyticsId))
    )
}

suspend fun Analytics.sendDeletionWarning() {
    sendEvent(
        eventName = EventsDictionary.deletionWarningShow
    )
}

suspend fun Analytics.sendScreenSettingsDeleteEvent() {
    sendEvent(
        eventName = EventsDictionary.screenSettingsDelete
    )
}

suspend fun Analytics.sendChangeThemeEvent(theme: ThemeMode) {
    val name = when (theme) {
        ThemeMode.Light -> "light"
        ThemeMode.Night -> "dark"
        ThemeMode.System -> "system"
    }
    sendEvent(
        eventName = EventsDictionary.changeTheme,
        props = Props(map = mapOf("id" to name))
    )
}

suspend fun Analytics.sendHideKeyboardEvent() {
    sendEvent(
        eventName = EventsDictionary.hideKeyboard
    )
}