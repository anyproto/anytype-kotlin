package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.ext.isValidObject
import com.anytypeio.anytype.core_models.getSingleValue
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.cover
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.subscription.updateWithRelationFormat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import timber.log.Timber

/**
 * Container for chat list widgets that handles async data loading,
 * collapsed state management, and chat preview integration.
 * 
 * Inspired by DataViewListWidgetContainer but specialized for chat objects.
 */
class ChatListWidgetContainer(
    private val space: SpaceId,
    private val widget: Widget,
    private val getObject: GetObject,
    private val storage: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val activeView: Flow<Id?>,
    private val isWidgetCollapsed: Flow<Boolean>,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val storeOfRelations: StoreOfRelations,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val chatPreviewContainer: ChatPreviewContainer,
    isSessionActiveFlow: Flow<Boolean>,
    onRequestCache: () -> WidgetView? = { null },
) : WidgetContainer {

    companion object {
        const val DEFAULT_FALLBACK_WIDGET_LIMIT = 6
        // Hard-coded flag to control display mode (can be changed later)
        private const val USE_PREVIEW_MODE = false
    }

    /**
     * Reactive flow that emits widget view states based on session activity and collapsed state.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override val view: Flow<WidgetView> =
        isSessionActiveFlow
            .flatMapLatest { isActive ->
                if (isActive)
                    buildViewFlow().onStart {
                        isWidgetCollapsed
                            .take(1)
                            .collect { isCollapsed ->
                                val cached = onRequestCache()
                                if (cached != null) {
                                    val adjustedCache = when (cached) {
                                        is WidgetView.ChatList -> cached.copy(
                                            isExpanded = !isCollapsed
                                        )
                                        else -> cached
                                    }
                                    emit(adjustedCache)
                                } else {
                                    emit(
                                        createWidgetView(
                                            isCollapsed = isCollapsed
                                        )
                                    )
                                }
                            }
                    }
                else
                    emptyFlow()
            }

    /**
     * Helper function that emits an empty or loading WidgetView when collapsed,
     * or subscribes to the actual data flow when expanded.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun dataOrEmptyWhenCollapsed(
        isCollapsedFlow: Flow<Boolean>,
        buildData: () -> Flow<WidgetView>
    ): Flow<WidgetView> =
        isCollapsedFlow.distinctUntilChanged().flatMapLatest { isCollapsed ->
            if (isCollapsed) {
                flowOf(createWidgetView(isCollapsed = true))
            } else {
                buildData()
            }
        }

    /**
     * Builds the main widget view flow combining active view and collapsed state.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun buildViewFlow(): Flow<WidgetView> =
        activeView.distinctUntilChanged()
            .flatMapLatest { activeView ->
                when (val source = widget.source) {
                    is Widget.Source.Bundled -> throw IllegalStateException("Bundled widgets do not support chat list layout")
                    is Widget.Source.Default -> {

                        val isCompact = widget is Widget.List && widget.isCompact

                        val widgetSourceObj = source.obj
                        if (!widgetSourceObj.isValid || !widgetSourceObj.notDeletedNorArchived) {
                            Timber.w("Widget source object is invalid or deleted/archived for widget ${widget.id}")
                            return@flatMapLatest isWidgetCollapsed.map { isCollapsed ->
                                defaultEmptyState(isCollapsed)
                            }
                        }

                        dataOrEmptyWhenCollapsed(isWidgetCollapsed) {
                            flow {
                                val ctx = computeViewerContext(
                                    widgetSourceObjId = widgetSourceObj.id,
                                    activeView = activeView,
                                    isCompact = isCompact
                                )

                                if (ctx.params != null) {
                                    val view = defaultWidgetSubscribe(
                                        obj = ctx.obj,
                                        activeView = activeView,
                                        params = ctx.params,
                                        isCompact = isCompact,
                                        displayLimit = ctx.displayLimit,
                                        storeOfObjectTypes = storeOfObjectTypes
                                    )
                                    val previews = chatPreviewContainer
                                        .observePreviewsBySpaceId(space)
                                        .distinctUntilChanged()

                                    val chats = view.flatMapLatest { view ->
                                        val chats = view.elements.map { it.obj.id }
                                        previews
                                            .map { list ->
                                                list.filter { preview ->
                                                    chats.contains(preview.chat)
                                                }
                                            }
                                            .distinctUntilChanged()
                                            .map { previewList ->
                                                view.copy(
                                                    elements = view.elements.map { element ->
                                                        val preview = previewList.find { p ->
                                                            p.chat == element.obj.id
                                                        }
                                                        val state = preview?.state
                                                        if (preview != null && state != null) {
                                                            WidgetView.SetOfObjects.Element.Chat(
                                                                obj = element.obj,
                                                                objectIcon = element.objectIcon,
                                                                name = element.name,
                                                                cover = element.cover,
                                                                counter = WidgetView.ChatCounter(
                                                                    unreadMentionCount = state.unreadMentions?.counter ?: 0,
                                                                    unreadMessageCount = state.unreadMessages?.counter ?: 0
                                                                )
                                                            )
                                                        } else {
                                                            element
                                                        }
                                                    }
                                                )
                                            }
                                    }
                                    emitAll(chats)
                                } else {
                                    emit(defaultEmptyState(isCollapsed = false))
                                }
                            }
                        }
                    }

                    Widget.Source.Other -> {
                        isWidgetCollapsed.map { isCollapsed ->
                            defaultEmptyState(isCollapsed)
                        }
                    }
                }
            }.catch { e ->
                Timber.e(e, "Error in chat list container flow")
                when (widget) {
                    is Widget.List -> {
                        isWidgetCollapsed.take(1).collect { isCollapsed ->
                            emit(defaultEmptyState(isCollapsed))
                        }
                    }

                    is Widget.View -> {
                        isWidgetCollapsed.take(1).collect { isCollapsed ->
                            emit(defaultEmptyState(isCollapsed))
                        }
                    }

                    else -> {
                        Timber.e(e, "Error in chat list container flow")
                    }
                }
            }

    /**
     * Internal data class containing viewer context information for widget data subscription.
     */
    private data class ViewerContext(
        val obj: ObjectView,
        val target: Block.Content.DataView.Viewer?,
        val params: StoreSearchParams?,
        val displayLimit: Int
    )

    /**
     * Asynchronously fetches the object view for the widget's source.
     */
    private suspend fun getObjectViewOrEmpty(objectId: Id, spaceId: SpaceId): ObjectView {
        Timber.d("Fetching object by id:${objectId} for chat widget")
        val objResult = getObject.async(
            GetObject.Params(
                target = objectId,
                space = spaceId
            )
        )
        return objResult.getOrNull() ?: run {
            Timber.e(objResult.exceptionOrNull(), "Failed to get object $objectId for chat widget")
            ObjectView(
                root = "",
                blocks = emptyList(),
                details = emptyMap(),
                objectRestrictions = emptyList(),
                dataViewRestrictions = emptyList()
            )
        }
    }

    /**
     * Computes ViewerContext for chat list widget.
     */
    private suspend fun computeViewerContext(
        widgetSourceObjId: Id,
        activeView: Id?,
        isCompact: Boolean
    ): ViewerContext {
        val obj = getObjectViewOrEmpty(objectId = widgetSourceObjId, spaceId = space)
        return buildViewerContextCommon(
            obj = obj,
            activeViewerId = activeView,
            isCompact = isCompact
        )
    }

    /**
     * Builds a ViewerContext containing object data, data view configuration, and search parameters.
     */
    private fun buildViewerContextCommon(
        obj: ObjectView,
        activeViewerId: Id?,
        isCompact: Boolean
    ): ViewerContext {

        val dv = obj.blocks.find { it.content is DV }?.content as? DV
        val targetView = dv?.viewers?.find { it.id == activeViewerId } ?: dv?.viewers?.firstOrNull()

        val displayLimit = WidgetConfig.resolveListWidgetLimit(
            isCompact = isCompact,
            isGallery = false,
            limit = when (widget) {
                is Widget.List -> widget.limit
                is Widget.View -> widget.limit
                else -> DEFAULT_FALLBACK_WIDGET_LIMIT
            }
        )

        // Fetch one extra item to determine if there are more
        val subscriptionLimit = displayLimit + 1

        val struct = obj.details[obj.root] ?: emptyMap()
        val params = if (struct.isValidObject()) {
            val dataViewKeys = dv?.relationLinks?.map { it.key }.orEmpty()
            val sorts = targetView?.sorts?.updateWithRelationFormat(dv?.relationLinks.orEmpty()).orEmpty()
            val defaultKeys = ObjectSearchConstants.defaultDataViewKeys

            // Use collection or set subscription logic
            if (obj.isCollection()) {
                StoreSearchParams(
                    space = space,
                    subscription = obj.root,
                    sorts = sorts,
                    keys = buildList {
                        addAll(defaultKeys)
                        addAll(dataViewKeys)
                    }.distinct(),
                    filters = buildList {
                        addAll(targetView?.filters.orEmpty())
                        addAll(ObjectSearchConstants.defaultDataViewFilters())
                    },
                    limit = subscriptionLimit,
                    source = emptyList(),
                    collection = obj.root
                )
            } else {
                val setOf = struct.getSingleValue<String>(Relations.SET_OF).orEmpty()
                if (setOf.isEmpty()) {
                    Timber.w("Widget ${widget.id}: setOf is empty, cannot create subscription parameters")
                    null
                } else {
                    StoreSearchParams(
                        space = space,
                        subscription = obj.root,
                        sorts = sorts,
                        keys = buildList {
                            addAll(defaultKeys)
                            addAll(dataViewKeys)
                        }.distinct(),
                        filters = buildList {
                            addAll(targetView?.filters.orEmpty())
                            addAll(ObjectSearchConstants.defaultDataViewFilters())
                        },
                        limit = subscriptionLimit,
                        source = listOf(setOf),
                        collection = null
                    )
                }
            }
        } else {
            null
        }

        return ViewerContext(obj = obj, target = targetView, params = params, displayLimit = displayLimit)
    }

    /**
     * Creates a reactive flow for chat list widget views.
     */
    private fun defaultWidgetSubscribe(
        obj: ObjectView,
        activeView: Id?,
        params: StoreSearchParams,
        isCompact: Boolean,
        displayLimit: Int,
        storeOfObjectTypes: StoreOfObjectTypes
    ): Flow<WidgetView.ChatList> {
        return storage.subscribe(params).map { results ->
            val objects = resolveObjectOrder(
                searchResults = results,
                obj = obj,
                activeView = activeView
            )
            val hasMore = objects.size > displayLimit
            val displayObjects = objects.take(displayLimit)

            WidgetView.ChatList(
                id = widget.id,
                source = widget.source,
                tabs = obj.tabs(viewer = activeView),
                elements = displayObjects.map { obj ->
                    // Initially create regular elements; chat counters will be added
                    // when combined with chat preview flow
                    WidgetView.SetOfObjects.Element.Regular(
                        obj = obj,
                        objectIcon = obj.objectIcon(
                            builder = urlBuilder,
                            objType = storeOfObjectTypes.getTypeOfObject(obj)
                        ),
                        name = WidgetView.Name.Default(
                            prettyPrintName = fieldParser.getObjectPluralName(obj, false)
                        )
                    )
                },
                isExpanded = true,
                isCompact = isCompact,
                hasMore = hasMore,
                icon = widget.icon,
                name = widget.source.getPrettyName(fieldParser),
                sectionType = widget.sectionType,
                displayMode = if (USE_PREVIEW_MODE) {
                    WidgetView.ChatList.DisplayMode.Preview
                } else {
                    WidgetView.ChatList.DisplayMode.Compact
                }
            )
        }
    }

    /**
     * Factory method to create appropriate WidgetView instances.
     */
    private fun createWidgetView(
        isCollapsed: Boolean = false
    ): WidgetView {
        return when (widget) {
            is Widget.List -> WidgetView.ChatList(
                id = widget.id,
                source = widget.source,
                tabs = emptyList(),
                elements = emptyList(),
                isExpanded = !isCollapsed,
                isCompact = widget.isCompact,
                icon = widget.icon,
                name = widget.source.getPrettyName(fieldParser),
                sectionType = widget.sectionType,
                displayMode = if (USE_PREVIEW_MODE) {
                    WidgetView.ChatList.DisplayMode.Preview
                } else {
                    WidgetView.ChatList.DisplayMode.Compact
                }
            )

            is Widget.View -> {
                WidgetView.ChatList(
                    id = widget.id,
                    source = widget.source,
                    tabs = emptyList(),
                    elements = emptyList(),
                    isExpanded = !isCollapsed,
                    isCompact = false,
                    icon = widget.icon,
                    name = widget.source.getPrettyName(fieldParser),
                    sectionType = widget.sectionType,
                    displayMode = if (USE_PREVIEW_MODE) {
                        WidgetView.ChatList.DisplayMode.Preview
                    } else {
                        WidgetView.ChatList.DisplayMode.Compact
                    }
                )
            }

            else -> {
                throw IllegalStateException("Incompatible widget type.")
            }
        }
    }

    /**
     * Returns a default empty widget view state.
     */
    private fun defaultEmptyState(isCollapsed: Boolean = false): WidgetView {
        return createWidgetView(isCollapsed = isCollapsed)
    }
}

/**
 * Extension function to extract tabs from ObjectView data view configuration.
 */
private fun ObjectView.tabs(viewer: Id?): List<WidgetView.SetOfObjects.Tab> = buildList {
    val block = blocks.find { it.content is DV }
    block?.content<DV>()?.viewers?.forEachIndexed { idx, view ->
        add(
            WidgetView.SetOfObjects.Tab(
                id = view.id,
                name = view.name,
                isSelected = if (viewer != null) view.id == viewer else idx == 0
            )
        )
    }
}

/**
 * Helper function to resolve object ordering for collection-based widgets.
 */
private fun resolveObjectOrder(
    searchResults: List<ObjectWrapper.Basic>,
    obj: ObjectView,
    activeView: Id?
): List<ObjectWrapper.Basic> {
    var objects = searchResults
    val dv = obj.blocks.find { b -> b.content is DV }
    val content = dv?.content as? DV
    if (content?.isCollection == true) {
        val targetView = activeView ?: content.viewers.firstOrNull()?.id
        val order = content.objectOrders.find { order -> order.view == targetView }
        if (order != null && order.ids.isNotEmpty()) {
            val indexMap = order.ids.withIndex().associate { it.value to it.index }
            objects = objects.sortedBy { obj -> indexMap[obj.id] ?: Int.MAX_VALUE }
        }
    }
    return objects
}
