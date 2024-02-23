package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.addFilter
import com.anytypeio.anytype.analytics.base.EventsDictionary.addSort
import com.anytypeio.anytype.analytics.base.EventsDictionary.addView
import com.anytypeio.anytype.analytics.base.EventsDictionary.changeDefaultTemplate
import com.anytypeio.anytype.analytics.base.EventsDictionary.changeFilterValue
import com.anytypeio.anytype.analytics.base.EventsDictionary.changeSortValue
import com.anytypeio.anytype.analytics.base.EventsDictionary.changeViewType
import com.anytypeio.anytype.analytics.base.EventsDictionary.clickNewOption
import com.anytypeio.anytype.analytics.base.EventsDictionary.collectionScreenShow
import com.anytypeio.anytype.analytics.base.EventsDictionary.createTemplate
import com.anytypeio.anytype.analytics.base.EventsDictionary.defaultTypeChanged
import com.anytypeio.anytype.analytics.base.EventsDictionary.duplicateTemplate
import com.anytypeio.anytype.analytics.base.EventsDictionary.duplicateView
import com.anytypeio.anytype.analytics.base.EventsDictionary.editTemplate
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectCreate
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectCreateLink
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectDuplicate
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectMoveToBin
import com.anytypeio.anytype.analytics.base.EventsDictionary.objectScreenShow
import com.anytypeio.anytype.analytics.base.EventsDictionary.removeFilter
import com.anytypeio.anytype.analytics.base.EventsDictionary.removeSort
import com.anytypeio.anytype.analytics.base.EventsDictionary.removeView
import com.anytypeio.anytype.analytics.base.EventsDictionary.repositionView
import com.anytypeio.anytype.analytics.base.EventsDictionary.selectTemplate
import com.anytypeio.anytype.analytics.base.EventsDictionary.setScreenShow
import com.anytypeio.anytype.analytics.base.EventsDictionary.setSelectQuery
import com.anytypeio.anytype.analytics.base.EventsDictionary.switchView
import com.anytypeio.anytype.analytics.base.EventsDictionary.turnIntoCollection
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.features.WidgetAnalytics
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.analytics.props.Props.Companion.OBJ_TYPE_CUSTOM
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.sets.isChangingDefaultTypeAvailable
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.viewerByIdOrFirst
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.source.BundledWidgetSourceView
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
        is Block.Prototype.File -> {
            Props(
                mapOf(
                    EventsPropertiesKey.type to getTypePropName(),
                    EventsPropertiesKey.style to "Embed"
                )
            )
        }
        is Block.Prototype.Link -> {
            return EventAnalytics.Anytype(
                name = objectCreateLink,
                duration = EventAnalytics.Duration(
                    start = startTime,
                    middleware = middlewareTime,
                    render = renderTime
                )
            )
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

fun Block.Prototype.File.getTypePropName() = this.type.getPropName()

fun Block.Content.File.Type?.getPropName() = when (this) {
    Block.Content.File.Type.NONE -> "None"
    Block.Content.File.Type.FILE -> "File"
    Block.Content.File.Type.IMAGE -> "Image"
    Block.Content.File.Type.VIDEO -> "Video"
    Block.Content.File.Type.AUDIO -> "Audio"
    Block.Content.File.Type.PDF -> "Pdf"
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

fun CoroutineScope.sendAnalyticsObjectShowEvent(
    ctx: Id,
    details: Map<Id, Block.Fields>?,
    analytics: Analytics,
    startTime: Long
) {
    val objType = getAnalyticsObjectType(
        details = details,
        ctx = ctx
    )
    val props = Props(
        mapOf(
            EventsPropertiesKey.objectType to objType,
        )
    )
    sendEvent(
        analytics = analytics,
        eventName = objectScreenShow,
        props = props,
        startTime = startTime,
        middleTime = System.currentTimeMillis(),
        renderTime = System.currentTimeMillis()
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
    length: Int
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.searchResult,
        props = Props(
            mapOf(
                EventsPropertiesKey.index to pos,
                EventsPropertiesKey.length to length
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsSearchWordsEvent(
    analytics: Analytics,
    length: Int
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.searchWords,
        props = Props(
            mapOf(
                EventsPropertiesKey.length to length
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
    details: Map<Id, Block.Fields>?,
    ctx: Id,
    startTime: Long,
    count: Int = 1
) {
    val objType = getAnalyticsObjectType(
        details = details,
        ctx = ctx
    )
    val props = Props(
        mapOf(
            EventsPropertiesKey.count to count,
            EventsPropertiesKey.objectType to objType
        )
    )
    sendEvent(
        analytics = analytics,
        eventName = objectDuplicate,
        props = props,
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
    count: Int
) {
    val event = EventAnalytics.Anytype(
        name = EventsDictionary.blockChangeBlockStyle,
        props = Props(
            mapOf(
                EventsPropertiesKey.type to "Text",
                EventsPropertiesKey.style to style.getStyleName(),
                EventsPropertiesKey.count to count
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
    count: Int,
    align: Block.Align
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.blockChangeBlockAlign,
        props = Props(
            mapOf(
                EventsPropertiesKey.align to align.getPropName(),
                EventsPropertiesKey.count to count
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsObjectTypeSelectOrChangeEvent(
    analytics: Analytics,
    startTime: Long,
    sourceObject: Id? = null,
    containsFlagType: Boolean,
    route: String? = null
) {
    val objType = sourceObject ?: OBJ_TYPE_CUSTOM
    val props = Props(
        mapOf(
            EventsPropertiesKey.objectType to objType,
            EventsPropertiesKey.route to route
        )
    )
    val event = if (containsFlagType) {
        EventsDictionary.selectObjectType
    } else {
        EventsDictionary.objectTypeChanged
    }
    sendEvent(
        analytics = analytics,
        eventName = event,
        props = props,
        startTime = startTime,
        middleTime = System.currentTimeMillis()
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
    type: String = ""
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.relationChangeValue,
        props = Props(
            mapOf(
                EventsPropertiesKey.type to type
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
    route: String,
    startTime: Long? = null,
    view: String? = null,
    objType: String?
) {
    val props = Props(
        mapOf(
            EventsPropertiesKey.objectType to objType,
            EventsPropertiesKey.route to route,
            EventsPropertiesKey.view to view
        )
    )
    sendEvent(
        analytics = analytics,
        eventName = objectCreate,
        props = props,
        startTime = startTime,
        middleTime = System.currentTimeMillis()
    )
}

fun CoroutineScope.sendAnalyticsObjectCreateEvent(
    analytics: Analytics,
    route: String,
    startTime: Long? = null,
    view: String? = null,
    objType: ObjectWrapper.Type?
) {
    val objTypeParam = if (objType == null) null
    else objType.sourceObject ?: OBJ_TYPE_CUSTOM
    val props = Props(
        mapOf(
            EventsPropertiesKey.objectType to objTypeParam,
            EventsPropertiesKey.route to route,
            EventsPropertiesKey.view to view
        )
    )
    sendEvent(
        analytics = analytics,
        eventName = objectCreate,
        props = props,
        startTime = startTime,
        middleTime = System.currentTimeMillis()
    )
}

fun CoroutineScope.sendAnalyticsSetTitleEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectSetTitle,
        props = Props.empty()
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
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectSetDescription
    )
}

fun CoroutineScope.sendAnalyticsUpdateTextMarkupEvent(
    analytics: Analytics,
    type: Block.Content.Text.Mark.Type
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.blockChangeTextStyle,
        props = Props(
            mapOf(
                EventsPropertiesKey.type to type.getPropName()
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsUpdateTextMarkupEvent(
    analytics: Analytics,
    type: Markup.Type
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.blockChangeTextStyle,
        props = Props(
            mapOf(
                EventsPropertiesKey.type to type.getPropName()
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsBlockReorder(
    analytics: Analytics,
    count: Int
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.blockReorder,
        props = Props(
            mapOf(
                EventsPropertiesKey.count to "$count"
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
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.goBack
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

fun CoroutineScope.logEvent(
    state: ObjectState,
    analytics: Analytics,
    event: ObjectStateAnalyticsEvent,
    startTime: Long? = null,
    type: String? = null,
    condition: DVFilterCondition? = null,
    currentViewId: Id? = null
) {
    if (state !is ObjectState.DataView) return
    val middleTime = System.currentTimeMillis()
    val embedTypeDefault = "object"
    val (objectTypeDefault, viewerType) = when (state) {
        is ObjectState.DataView.Collection -> {
            Pair("ot-collection", state.viewerByIdOrFirst(currentViewId)?.type?.formattedName)
        }

        is ObjectState.DataView.Set -> {
            Pair("ot-set", state.viewerByIdOrFirst(currentViewId)?.type?.formattedName)
        }
    }
    val scope = this
    when (event) {
        ObjectStateAnalyticsEvent.OPEN_OBJECT -> {
            when (state) {
                is ObjectState.DataView.Collection -> scope.sendEvent(
                    analytics = analytics,
                    eventName = collectionScreenShow,
                    startTime = startTime,
                    middleTime = middleTime,
                    props = buildProps(
                        type = viewerType,
                    )
                )
                is ObjectState.DataView.Set -> scope.sendEvent(
                    analytics = analytics,
                    eventName = setScreenShow,
                    startTime = startTime,
                    middleTime = middleTime,
                    props = buildProps(
                        embedType = embedTypeDefault,
                        type = viewerType,
                    )
                )
            }
        }
        ObjectStateAnalyticsEvent.TURN_INTO_COLLECTION -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = turnIntoCollection,
                startTime = startTime,
                middleTime = middleTime
            )
        }
        ObjectStateAnalyticsEvent.SELECT_QUERY -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = setSelectQuery,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(type = type)
            )
        }
        ObjectStateAnalyticsEvent.ADD_VIEW -> {
            scope.sendEvent(
                analytics = analytics,
                eventName = addView,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
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
                    objectType = type,
                    route = route
                )
            )
        }

        ObjectStateAnalyticsEvent.SELECT_TEMPLATE -> {
            val route = when (state) {
                is ObjectState.DataView.Collection -> EventsDictionary.Routes.objCreateCollection
                is ObjectState.DataView.Set -> EventsDictionary.Routes.objCreateSet
            }
            scope.sendEvent(
                analytics = analytics,
                eventName = selectTemplate,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    route = route
                )
            )
        }
        ObjectStateAnalyticsEvent.SHOW_TEMPLATES -> {
            val route = when (state) {
                is ObjectState.DataView.Collection -> EventsDictionary.Routes.objCreateCollection
                is ObjectState.DataView.Set -> EventsDictionary.Routes.objCreateSet
            }
            scope.sendEvent(
                analytics = analytics,
                eventName = clickNewOption,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    route = route
                )
            )
        }
        ObjectStateAnalyticsEvent.CREATE_TEMPLATE -> {
            val route = when (state) {
                is ObjectState.DataView.Collection -> EventsDictionary.Routes.objCreateCollection
                is ObjectState.DataView.Set -> EventsDictionary.Routes.objCreateSet
            }
            scope.sendEvent(
                analytics = analytics,
                eventName = createTemplate,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    route = route,
                    objectType = type ?: OBJ_TYPE_CUSTOM
                )
            )
        }

        ObjectStateAnalyticsEvent.EDIT_TEMPLATE -> {
            val route = when (state) {
                is ObjectState.DataView.Collection -> EventsDictionary.Routes.objCreateCollection
                is ObjectState.DataView.Set -> EventsDictionary.Routes.objCreateSet
            }
            scope.sendEvent(
                analytics = analytics,
                eventName = editTemplate,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    route = route,
                    objectType = type ?: OBJ_TYPE_CUSTOM
                )
            )
        }

        ObjectStateAnalyticsEvent.DUPLICATE_TEMPLATE -> {
            val route = when (state) {
                is ObjectState.DataView.Collection -> EventsDictionary.Routes.objCreateCollection
                is ObjectState.DataView.Set -> EventsDictionary.Routes.objCreateSet
            }
            scope.sendEvent(
                analytics = analytics,
                eventName = duplicateTemplate,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    route = route,
                    objectType = type ?: OBJ_TYPE_CUSTOM
                )
            )
        }

        ObjectStateAnalyticsEvent.DELETE_TEMPLATE -> {
            val route = when (state) {
                is ObjectState.DataView.Collection -> EventsDictionary.Routes.objCreateCollection
                is ObjectState.DataView.Set -> EventsDictionary.Routes.objCreateSet
            }
            scope.sendEvent(
                analytics = analytics,
                eventName = objectMoveToBin,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(
                    route = route,
                    objectType = type ?: OBJ_TYPE_CUSTOM
                )
            )
        }

        ObjectStateAnalyticsEvent.SET_AS_DEFAULT_TYPE -> {
            val route = when (state) {
                is ObjectState.DataView.Collection -> EventsDictionary.Routes.objCreateCollection
                is ObjectState.DataView.Set -> EventsDictionary.Routes.objCreateSet
            }
            if (state.isChangingDefaultTypeAvailable()) {
                scope.sendEvent(
                    analytics = analytics,
                    eventName = defaultTypeChanged,
                    startTime = startTime,
                    middleTime = middleTime,
                    props = buildProps(
                        route = route,
                        objectType = type ?: OBJ_TYPE_CUSTOM
                    )
                )
            }
        }

        ObjectStateAnalyticsEvent.CHANGE_DEFAULT_TEMPLATE -> {
            val route = when (state) {
                is ObjectState.DataView.Collection -> EventsDictionary.Routes.objCreateCollection
                is ObjectState.DataView.Set -> EventsDictionary.Routes.objCreateSet
            }
            scope.sendEvent(
                analytics = analytics,
                eventName = changeDefaultTemplate,
                startTime = startTime,
                middleTime = middleTime,
                props = buildProps(route = route)
            )
        }
    }
}

private fun buildProps(
    embedType: String? = null,
    type: String? = null,
    objectType: String? = null,
    condition: DVFilterCondition? = null,
    route: String? = null
): Props {
    return Props(
        map = buildMap {
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
    OBJECT_CREATE,
    SELECT_TEMPLATE,
    SHOW_TEMPLATES,
    CREATE_TEMPLATE,
    EDIT_TEMPLATE,
    DUPLICATE_TEMPLATE,
    DELETE_TEMPLATE,
    SET_AS_DEFAULT_TYPE,
    CHANGE_DEFAULT_TEMPLATE
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
                when (view) {
                    BundledWidgetSourceView.Collections -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_COLLECTIONS)
                    }

                    BundledWidgetSourceView.Favorites -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_FAVORITES)
                    }

                    BundledWidgetSourceView.Recent -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_RECENT)
                    }

                    BundledWidgetSourceView.RecentLocal -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_RECENT_LOCAL)
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

                    Widget.Source.Bundled.RecentLocal -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_RECENT_LOCAL)
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

                    Widget.Source.Bundled.RecentLocal -> {
                        put(WidgetAnalytics.TAB, WidgetAnalytics.WIDGET_SOURCE_RECENT_LOCAL)
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

                    Widget.Source.Bundled.RecentLocal -> {
                        put(WidgetAnalytics.TYPE, WidgetAnalytics.WIDGET_SOURCE_RECENT_LOCAL)
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

suspend fun Analytics.sendSettingsSpaceStorageManageEvent() {
    sendEvent(
        eventName = EventsDictionary.screenSettingsSpaceStorageManager
    )
}

suspend fun Analytics.sendSettingsOffloadEvent() {
    sendEvent(
        eventName = EventsDictionary.screenSettingsStorageOffload
    )
}

suspend fun Analytics.sendGetMoreSpaceEvent() {
    sendEvent(
        eventName = EventsDictionary.getMoreSpace
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
    eventName: String,
    lang: String? = null
) {
    val analyticsId = configStorage.get().analytics
    val userProperty = UserProperty.AccountId(analyticsId)
    updateUserProperty(userProperty)
    if (lang != null) {
        updateUserProperty(UserProperty.InterfaceLanguage(lang))
    }
    sendEvent(
        startTime = startTime,
        middleTime = System.currentTimeMillis(),
        eventName = eventName,
        props = Props(map = mapOf("accountId" to analyticsId))
    )
}

suspend fun Analytics.sendOpenAccountEvent(
    analytics: Id
) {
    val userProperty = UserProperty.AccountId(analytics)
    updateUserProperty(userProperty)
    sendEvent(
        startTime = null,
        middleTime = System.currentTimeMillis(),
        eventName = EventsDictionary.openAccount,
        props = Props(map = mapOf("accountId" to analytics))
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

fun CoroutineScope.sendAnalyticsOnboardingScreenEvent(
    analytics: Analytics,
    step: EventsDictionary.ScreenOnboardingStep
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.screenOnboarding,
        props = Props(
            buildMap {
                put(EventsPropertiesKey.step, step.value)
            }
        )
    )
}

fun CoroutineScope.sendAnalyticsOnboardingClickEvent(
    analytics: Analytics,
    type: EventsDictionary.ClickOnboardingButton,
    step: EventsDictionary.ScreenOnboardingStep
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.clickOnboarding,
        props = Props(
            buildMap {
                put(EventsPropertiesKey.type, type.value)
                put(EventsPropertiesKey.step, step.value)
            }
        )
    )
}

fun CoroutineScope.sendAnalyticsOnboardingLoginEvent(
    analytics: Analytics,
    type: EventsDictionary.ClickLoginButton
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.clickLogin,
        props = Props(
            buildMap {
                put(EventsPropertiesKey.type, type.value)
            }
        )
    )
}

fun CoroutineScope.sendAnalyticsSelectTemplateEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = selectTemplate,
        props = Props(
            buildMap {
                put(EventsPropertiesKey.route, "Navigation")
            }
        )
    )
}

fun CoroutineScope.sendAnalyticsCreateTemplateEvent(
    analytics: Analytics,
    details: Map<Id, Block.Fields>,
    ctx: Id,
    startTime: Long
) {
    val objType = getAnalyticsObjectType(
        details = details,
        ctx = ctx
    )
    sendEvent(
        analytics = analytics,
        eventName = createTemplate,
        props = Props(
            buildMap {
                put(EventsPropertiesKey.route, "MenuObject")
                put(EventsPropertiesKey.objectType, objType)
            }
        ),
        startTime = startTime,
        middleTime = System.currentTimeMillis()
    )
}

fun CoroutineScope.sendAnalyticsDefaultTemplateEvent(
    analytics: Analytics,
    objType: ObjectWrapper.Type?,
    startTime: Long,
    route: String? = null
) {
    val objectType = objType?.sourceObject ?: OBJ_TYPE_CUSTOM
    sendEvent(
        analytics = analytics,
        eventName = changeDefaultTemplate,
        props = Props(
            buildMap {
                put(EventsPropertiesKey.type, objectType)
                put(EventsPropertiesKey.route, route)
            }
        ),
        startTime = startTime,
        middleTime = System.currentTimeMillis()
    )
}

private fun getAnalyticsObjectType(
    details: Map<Id, Block.Fields>?,
    ctx: Id
): String? {
    if (details == null) return null
    val objTypeId = details[ctx]?.type?.firstOrNull()
    val typeStruct = details[objTypeId]?.map
    val objType = typeStruct?.mapToObjectWrapperType()
    return objType?.sourceObject ?: OBJ_TYPE_CUSTOM
}

fun CoroutineScope.sendAnalyticsCreateLink(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.objectCreateLink
    )
}


//region Self-Hosting
fun CoroutineScope.sendAnalyticsSelectNetworkEvent(
    analytics: Analytics,
    type: String,
    route: String
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.selectNetwork,
        props = Props(
            mapOf(
                EventsPropertiesKey.type to type,
                EventsPropertiesKey.route to route
            )
        )
    )
}

fun CoroutineScope.sendAnalyticsUploadConfigFileEvent(
    analytics: Analytics
) {
    sendEvent(
        analytics = analytics,
        eventName = EventsDictionary.uploadNetworkConfiguration
    )
}

//endregion