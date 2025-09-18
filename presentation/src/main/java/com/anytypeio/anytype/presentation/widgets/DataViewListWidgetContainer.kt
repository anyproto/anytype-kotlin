package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.GetObject.*
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.cover
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.widgets.WidgetView.*
import com.anytypeio.anytype.presentation.widgets.WidgetView.Name.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import timber.log.Timber

/**
 * Container for data view widgets (List and View) that handles async data loading,
 * collapsed state management, and caching to optimize performance.
 */
class DataViewListWidgetContainer(
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
    isSessionActiveFlow: Flow<Boolean>,
    onRequestCache: () -> WidgetView.SetOfObjects? = { null },
) : WidgetContainer {

    // Cache to prevent duplicate computeViewerContext calls
    private var cachedContext: ViewerContext? = null
    private var cachedContextKey: Triple<String, Id?, Boolean>? = null

    init {
        Timber.d("Creating DataViewListWidgetContainer for widget with id ${widget.id}")
    }

    /**
     * Reactive flow that emits widget view states based on session activity and collapsed state.
     * Provides loading states initially, then switches to actual data when available.
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
                                val loadingStateView = createWidgetView(
                                    isCollapsed = isCollapsed,
                                    isLoading = true
                                )
                                if (isCollapsed) {
                                    emit(loadingStateView)
                                } else {
                                    emit(onRequestCache() ?: loadingStateView)
                                }
                            }
                    }
                else
                    emptyFlow()
            }

    /**
     * Builds the main widget view flow combining active view and collapsed state.
     * Handles different widget source types and optimizes by skipping data subscriptions when collapsed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun buildViewFlow(): Flow<WidgetView> =
        combine(
            activeView.distinctUntilChanged(),
            isWidgetCollapsed
        ) { view, isCollapsed -> view to isCollapsed }
            .flatMapLatest { (view, isCollapsed) ->
                Timber.d("Subscribing to data view widget with id ${widget.id} with view $view (collapsed = $isCollapsed)")
                when (val source = widget.source) {
                    is Widget.Source.Bundled -> throw IllegalStateException("Bundled widgets do not support data view layout")
                    is Widget.Source.Default -> {
                        Timber.d("Processing Widget.Source.Default for widget ${widget.id}")
                        val isCompact = widget is Widget.List && widget.isCompact
                        if (isCollapsed) {
                            flowOf(createWidgetView(isCollapsed = true, isLoading = false))
                        } else {

                            val setOf = source.obj.setOf.firstOrNull()

                            if (setOf == null) {
                                Timber.w("Widget source setOf is empty for widget ${widget.id}")
                                return@flatMapLatest flowOf(defaultEmptyState(isCollapsed))
                            }

                            val ctx = computeViewerContext(
                                sourceParams = WidgetSourceParams(
                                    sourceId = source.obj.id,
                                    isArchived = source.obj.isArchived,
                                    isDeleted = source.obj.isDeleted
                                ),
                                activeViewerId = view,
                                isCompact = isCompact
                            )

                            if (ctx.params != null) {
                                if (widget is Widget.View && ctx.target?.type == DVViewerType.GALLERY) {
                                    galleryWidgetSubscribe(
                                        obj = ctx.obj,
                                        activeView = view,
                                        params = ctx.params,
                                        target = ctx.target,
                                        storeOfObjectTypes = storeOfObjectTypes
                                    )
                                } else {
                                    defaultWidgetSubscribe(
                                        obj = ctx.obj,
                                        activeView = view,
                                        params = ctx.params,
                                        isCompact = isCompact,
                                        storeOfObjectTypes = storeOfObjectTypes
                                    )
                                }
                            } else {
                                flowOf(defaultEmptyState(isCollapsed))
                            }
                        }
                    }

                    is Widget.Source.ObjectType -> {
                        Timber.d("Processing Widget.Source.ObjectType for widget ${widget.id}")
                        isWidgetCollapsed.flatMapLatest { isCollapsed ->
                            if (isCollapsed) {
                                // When collapsed, don't subscribe to data - just show empty collapsed state
                                flowOf(defaultEmptyState(isCollapsed = true))
                            } else {
                                val isCompact = widget is Widget.List && widget.isCompact
                                val ctx = computeViewerContext(
                                    sourceParams = WidgetSourceParams(
                                        sourceId = source.obj.id,
                                        isArchived = source.obj.isArchived,
                                        isDeleted = source.obj.isDeleted
                                    ),
                                    activeViewerId = view,
                                    isCompact = isCompact
                                )
                                if (ctx.params != null) {
                                    if (widget is Widget.View && ctx.target?.type == DVViewerType.GALLERY) {
                                        galleryWidgetSubscribe(
                                            obj = ctx.obj,
                                            activeView = view,
                                            params = ctx.params,
                                            target = ctx.target,
                                            storeOfObjectTypes = storeOfObjectTypes
                                        )
                                    } else {
                                        defaultWidgetSubscribe(
                                            obj = ctx.obj,
                                            activeView = view,
                                            params = ctx.params,
                                            isCompact = isCompact,
                                            storeOfObjectTypes = storeOfObjectTypes
                                        )
                                    }
                                } else {
                                    flowOf(defaultEmptyState(isCollapsed = false))
                                }
                            }
                        }
                    }

                    Widget.Source.Other -> {
                        flowOf(defaultEmptyState(isCollapsed))
                    }
                }
            }.catch { e ->
                Timber.e(e, "Error in data view container flow")
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
                        Timber.e(e, "Error in data view container flow")
                    }
                }
            }

    /**
     * Internal data class containing viewer context information for widget data subscription.
     * Bundles object data, data view configuration, and search parameters together.
     */
    private data class ViewerContext(
        val obj: ObjectView,
        val target: Block.Content.DataView.Viewer?,
        val params: StoreSearchParams?
    )

    /**
     * Asynchronously fetches the object view for the widget's source.
     * Returns an empty ObjectView if the fetch fails to prevent crashes.
     */
    private suspend fun getObjectViewOrEmpty(objectId: Id, spaceId: SpaceId): ObjectView {
        Timber.d("Fetching object by id:${objectId} for widget")
        val objResult = getObject.async(
            Params(
                target = objectId,
                space = spaceId
            )
        )
        return objResult.getOrNull() ?: run {
            Timber.e(objResult.exceptionOrNull(), "Failed to get object $objectId for widget")
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
     * Builds a ViewerContext containing object data, data view configuration, and search parameters.
     * Handles viewer selection, limit resolution, and parameter parsing for widget data subscription.
     */
    private fun buildViewerContextCommon(
        obj: ObjectView,
        sourceParams: WidgetSourceParams,
        activeViewerId: Id?,
        isCompact: Boolean
    ): ViewerContext {
        val dv = obj.blocks.find { it.content is DV }?.content as? DV
        val target = dv?.viewers?.find { it.id == activeViewerId } ?: dv?.viewers?.firstOrNull()

        val limit = WidgetConfig.resolveListWidgetLimit(
            isCompact = isCompact,
            isGallery = target?.type == DVViewerType.GALLERY,
            limit = when (widget) {
                is Widget.List -> widget.limit
                is Widget.View -> widget.limit
                is Widget.Tree, is Widget.Link, is Widget.AllObjects, is Widget.Chat, is Widget.Section -> {
                    throw IllegalStateException("Incompatible widget type.")
                }
            }
        )

        val params = obj.parseDataViewStoreSearchParams(
            space = space,
            subscription = obj.root,
            viewer = activeViewerId,
            sourceParams = sourceParams,
            limit = limit
        )

        return ViewerContext(obj = obj, target = target, params = params)
    }

    /**
     * Computes and caches ViewerContext to avoid duplicate object fetches and processing.
     * Uses caching based on source ID, active viewer, and compact state to optimize performance.
     */
    private suspend fun computeViewerContext(
        sourceParams: WidgetSourceParams,
        activeViewerId: Id?,
        isCompact: Boolean
    ): ViewerContext {
        val contextKey = Triple(widget.source.id, activeViewerId, isCompact)
        if (cachedContextKey == contextKey && cachedContext != null) {
            Timber.d("Using cached ViewerContext for widget ${widget.id}")
            return cachedContext!!
        }
        Timber.d("Computing ViewerContext for widget ${widget.id}")
        val obj = getObjectViewOrEmpty(objectId = sourceParams.sourceId, spaceId = space)
        val result = buildViewerContextCommon(
            obj = obj,
            sourceParams = sourceParams,
            activeViewerId = activeViewerId,
            isCompact = isCompact
        )
        cachedContext = result
        cachedContextKey = contextKey
        return result
    }

    /**
     * Creates a reactive flow for gallery widget views with dependency tracking.
     * Handles cover images, icons, and object ordering for gallery-style data display.
     */
    private fun galleryWidgetSubscribe(
        obj: ObjectView,
        activeView: Id?,
        target: Block.Content.DataView.Viewer,
        params: StoreSearchParams,
        storeOfObjectTypes: StoreOfObjectTypes
    ): Flow<WidgetView.Gallery> {
        return storage.subscribeWithDependencies(params).map { response ->
            val objects = resolveObjectOrder(
                searchResults = response.results,
                obj = obj,
                activeView = activeView
            )
            val withCover = !target.coverRelationKey.isNullOrEmpty()
            val withIcon = !target.hideIcon
            WidgetView.Gallery(
                id = widget.id,
                source = widget.source,
                view = target.id,
                tabs = obj.tabs(viewer = activeView),
                elements = objects.filter { obj -> obj.isValid }.map { obj ->
                    WidgetView.SetOfObjects.Element(
                        obj = obj,
                        objectIcon = if (withIcon) {
                            obj.objectIcon(
                                builder = urlBuilder,
                                objType = storeOfObjectTypes.getTypeOfObject(obj)
                            )
                        } else {
                            ObjectIcon.None
                        },
                        cover = if (withCover) {
                            obj.cover(
                                urlBuilder = urlBuilder,
                                coverImageHashProvider = coverImageHashProvider,
                                storeOfRelations = storeOfRelations,
                                dependedObjects = response.dependencies,
                                dvViewer = target,
                                isMedium = true
                            )
                        } else {
                            null
                        },
                        name = Default(prettyPrintName = fieldParser.getObjectPluralName(obj, false))
                    )
                },
                isExpanded = true,
                showIcon = withIcon,
                showCover = withCover,
                icon = widget.icon,
                name = widget.source.getPrettyName(fieldParser)
            )
        }
    }

    /**
     * Creates a reactive flow for standard list widget views.
     * Subscribes to object data and transforms it into widget elements with icons and names.
     */
    private fun defaultWidgetSubscribe(
        obj: ObjectView,
        activeView: Id?,
        params: StoreSearchParams,
        isCompact: Boolean,
        storeOfObjectTypes: StoreOfObjectTypes
    ): Flow<WidgetView> {
        return storage.subscribe(params).map { results ->
            val objects = resolveObjectOrder(
                searchResults = results,
                obj = obj,
                activeView = activeView
            )
            WidgetView.SetOfObjects(
                id = widget.id,
                source = widget.source,
                tabs = obj.tabs(viewer = activeView),
                elements = objects.map { obj ->
                    WidgetView.SetOfObjects.Element(
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
                icon = widget.icon,
                name = widget.source.getPrettyName(fieldParser)
            )
        }
    }

    /**
     * Factory method to create appropriate WidgetView instances based on widget type.
     * Handles collapsed and loading states for List, View, and Section widgets.
     */
    private fun createWidgetView(
        isCollapsed: Boolean = false,
        isLoading: Boolean = false
    ): WidgetView {
        return when (widget) {
            is Widget.List -> SetOfObjects(
                id = widget.id,
                source = widget.source,
                tabs = emptyList(),
                elements = emptyList(),
                isExpanded = !isCollapsed,
                isCompact = widget.isCompact,
                icon = widget.icon,
                isLoading = isLoading,
                name = widget.source.getPrettyName(fieldParser)
            )

            is Widget.View -> Gallery(
                id = widget.id,
                source = widget.source,
                icon = widget.icon,
                tabs = emptyList(),
                elements = emptyList(),
                isExpanded = !isCollapsed,
                isLoading = isLoading,
                view = null,
                name = widget.source.getPrettyName(fieldParser)
            )

            is Widget.Section.ObjectType -> Section.ObjectTypes
            is Widget.Section.Pinned -> Section.Pinned

            is Widget.Link, is Widget.Tree, is Widget.AllObjects, is Widget.Chat -> {
                throw IllegalStateException("Incompatible widget type.")
            }
        }
    }

    /**
     * Returns a default empty widget view state for error handling and initial states.
     */
    private fun defaultEmptyState(isCollapsed: Boolean = false): WidgetView {
        return createWidgetView(isCollapsed = isCollapsed, isLoading = false)
    }
}

/**
 * Extension function to check if an ObjectView represents a collection.
 * Collections have special ordering behavior in data views.
 */
fun ObjectView.isCollection(): Boolean {
    val wrapper = ObjectWrapper.Basic(details.getOrDefault(root, emptyMap()))
    return wrapper.layout == ObjectType.Layout.COLLECTION
}

/**
 * Data class representing common parameters extracted from widget sources.
 * Used to unify parameter handling across different source types.
 */
data class WidgetSourceParams(
    val isArchived: Boolean?,
    val isDeleted: Boolean?,
    val sourceId: Id
)

/**
 * Extension function to parse ObjectView data into StoreSearchParams for widget subscriptions.
 * Extracts data view configuration, filters, sorts, and keys for database queries.
 */
private fun ObjectView.parseDataViewStoreSearchParams(
    space: SpaceId,
    subscription: Id,
    limit: Int,
    sourceParams: WidgetSourceParams,
    viewer: Id?
): StoreSearchParams? {
    if (sourceParams.isArchived == true || sourceParams.isDeleted == true) return null
    val block = blocks.find { it.content is DV } ?: return null
    val dv = block.content<DV>()
    val view = dv.viewers.find { it.id == viewer } ?: dv.viewers.firstOrNull() ?: return null
    val dataViewKeys = dv.relationLinks.map { it.key }
    val defaultKeys = ObjectSearchConstants.defaultDataViewKeys
    return StoreSearchParams(
        space = space,
        subscription = subscription,
        sorts = view.sorts,
        keys = buildList {
            addAll(defaultKeys)
            addAll(dataViewKeys)
            add(Relations.DESCRIPTION)
        }.distinct(),
        filters = buildList {
            addAll(view.filters)
            addAll(
                ObjectSearchConstants.defaultDataViewFilters()
            )
        },
        limit = limit,
        source = listOf(sourceParams.sourceId),
        collection = if (isCollection())
            root
        else
            null
    )
}

/**
 * Extension function to extract tabs from ObjectView data view configuration.
 * Creates tab list for multi-view widgets with selection state.
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
 * Applies custom ordering from data view configuration if available.
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
            objects = objects.sortedBy { order.ids.indexOf(it.id) }
        }
    }
    return objects
}