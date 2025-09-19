package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.ext.isValidObject
import com.anytypeio.anytype.core_models.getSingleValue
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.GetObject.Params
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.cover
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.widgets.WidgetView.Gallery
import com.anytypeio.anytype.presentation.widgets.WidgetView.Name.Default
import com.anytypeio.anytype.presentation.widgets.WidgetView.Section
import com.anytypeio.anytype.presentation.widgets.WidgetView.SetOfObjects
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private var cachedContextKey: ContextKey? = null
    private val ctxMutex = Mutex()

    private data class ContextKey(
        val widgetSourceId: String,
        val activeViewerId: Id?,
        val isCompact: Boolean,
        val dvFingerprint: String = ""
    )

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
                                val cached = onRequestCache()
                                if (cached != null) {
                                    // Adjust cached state to reflect current collapsed flag
                                    emit(
                                        cached.copy(
                                            isExpanded = !isCollapsed,
                                            isLoading = false
                                        )
                                    )
                                } else {
                                    emit(
                                        createWidgetView(
                                            isCollapsed = isCollapsed,
                                            isLoading = true
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
                flowOf(createWidgetView(isCollapsed = true, isLoading = false))
            } else {
                buildData()
            }
        }

    /**
     * Builds the main widget view flow combining active view and collapsed state.
     * Handles different widget source types and optimizes by skipping data subscriptions when collapsed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun buildViewFlow(): Flow<WidgetView> =
        activeView.distinctUntilChanged()
            .flatMapLatest { activeView ->
                when (val source = widget.source) {
                    is Widget.Source.Bundled -> throw IllegalStateException("Bundled widgets do not support data view layout")
                    is Widget.Source.Default -> {

                        val isCompact = widget is Widget.List && widget.isCompact

                        val widgetSourceObj = source.obj
                        if (!widgetSourceObj.isValid || !widgetSourceObj.notDeletedNorArchived) {
                            Timber.w("Widget source object is invalid or deleted/archived for widget ${widget.id}")
                            return@flatMapLatest isWidgetCollapsed.map { isCollapsed ->
                                defaultEmptyState(isCollapsed)
                            }
                        }

                        if (!widgetSourceObj.layout.isDataView()) {
                            Timber.w("Widget source object has unsupported layout ${widgetSourceObj.layout} for widget ${widget.id}")
                            return@flatMapLatest isWidgetCollapsed.map { isCollapsed ->
                                defaultEmptyState(isCollapsed)
                            }
                        }

                        dataOrEmptyWhenCollapsed(isWidgetCollapsed) {
                            flow {
                                val ctx = computeViewerContext(
                                    widgetSourceObj = widgetSourceObj,
                                    activeView = activeView,
                                    isCompact = isCompact
                                )

                                if (ctx.params != null) {
                                    if (widget is Widget.View && ctx.target?.type == DVViewerType.GALLERY) {
                                        emitAll(
                                            galleryWidgetSubscribe(
                                                obj = ctx.obj,
                                                activeView = activeView,
                                                params = ctx.params,
                                                target = ctx.target,
                                                storeOfObjectTypes = storeOfObjectTypes
                                            )
                                        )
                                    } else {
                                        emitAll(
                                            defaultWidgetSubscribe(
                                                obj = ctx.obj,
                                                activeView = activeView,
                                                params = ctx.params,
                                                isCompact = isCompact,
                                                storeOfObjectTypes = storeOfObjectTypes
                                            )
                                        )
                                    }
                                } else {
                                    emit(defaultEmptyState(isCollapsed = false))
                                }
                            }
                        }
                    }

                    is Widget.Source.ObjectType -> {
                        val isCompact = widget is Widget.List && widget.isCompact

                        val widgetSourceObj = source.obj

                        if (!widgetSourceObj.isValid) {
                            Timber.w("Widget source object is invalid for widget ${widget.id}")
                            return@flatMapLatest isWidgetCollapsed.map { isCollapsed ->
                                defaultEmptyState(isCollapsed)
                            }
                        }

                        dataOrEmptyWhenCollapsed(isWidgetCollapsed) {
                            flow {
                                val ctx = computeViewerContext(
                                    widgetSourceObj = source.obj,
                                    activeView = activeView,
                                    isCompact = isCompact
                                )
                                if (ctx.params != null) {
                                    if (widget is Widget.View && ctx.target?.type == DVViewerType.GALLERY) {
                                        emitAll(
                                            galleryWidgetSubscribe(
                                                obj = ctx.obj,
                                                activeView = activeView,
                                                params = ctx.params,
                                                target = ctx.target,
                                                storeOfObjectTypes = storeOfObjectTypes
                                            )
                                        )
                                    } else {
                                        emitAll(
                                            defaultWidgetSubscribe(
                                                obj = ctx.obj,
                                                activeView = activeView,
                                                params = ctx.params,
                                                isCompact = isCompact,
                                                storeOfObjectTypes = storeOfObjectTypes
                                            )
                                        )
                                    }
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
        activeViewerId: Id?,
        isCompact: Boolean
    ): ViewerContext {

        val dv = obj.blocks.find { it.content is DV }?.content as? DV
        val targetView = dv?.viewers?.find { it.id == activeViewerId } ?: dv?.viewers?.firstOrNull()

        val limit = WidgetConfig.resolveListWidgetLimit(
            isCompact = isCompact,
            isGallery = targetView?.type == DVViewerType.GALLERY,
            limit = when (widget) {
                is Widget.List -> widget.limit
                is Widget.View -> widget.limit
                is Widget.Tree, is Widget.Link, is Widget.AllObjects, is Widget.Chat, is Widget.Section -> {
                    throw IllegalStateException("Incompatible widget type.")
                }
            }
        )

        val struct = obj.details[obj.root] ?: emptyMap()
        val params = if (struct.isValidObject()) {
            val setOf = struct.getSingleValue<String>(Relations.SET_OF).orEmpty()
            val dataViewKeys = dv?.relationLinks?.map { it.key }.orEmpty()
            val defaultKeys = ObjectSearchConstants.defaultDataViewKeys
            StoreSearchParams(
                space = space,
                subscription = obj.root,
                sorts = targetView?.sorts.orEmpty(),
                keys = buildList {
                    addAll(defaultKeys)
                    addAll(dataViewKeys)
                }.distinct(),
                filters = buildList {
                    addAll(targetView?.filters.orEmpty())
                    addAll(ObjectSearchConstants.defaultDataViewFilters())
                },
                limit = limit,
                source = listOf(setOf),
                collection = if (obj.isCollection())
                    obj.root
                else
                    null
            )
        } else {
            null
        }

        return ViewerContext(obj = obj, target = targetView, params = params)
    }

    /**
     * Computes a lightweight fingerprint of the DV configuration to invalidate cache
     * when viewers/filters/sorts/objectOrders change.
     */
    private fun ObjectView.dataViewFingerprint(): String {
        val dv = blocks.find { it.content is DV }?.content as? DV ?: return "no-dv"
        val viewersPart = dv.viewers.joinToString("|") { v ->
            buildString {
                append(v.id)
                append(":")
                append(v.type.name)
                append(":")
                append(v.name)
                append(":f=")
                append(v.filters.hashCode())
                append(":s=")
                append(v.sorts.hashCode())
                append(":hideIcon=")
                append(v.hideIcon)
                append(":cover=")
                append(v.coverRelationKey ?: "")
            }
        }
        val relsPart = dv.relationLinks.joinToString(",") { it.key }
        val ordersPart = dv.objectOrders.joinToString("|") { o ->
            o.view + ":" + o.ids.hashCode()
        }
        return buildString {
            append("isCollection=")
            append(dv.isCollection)
            append("|viewers=")
            append(viewersPart)
            append("|rels=")
            append(relsPart)
            append("|orders=")
            append(ordersPart)
        }
    }

    /**
     * Computes and caches ViewerContext to avoid duplicate object fetches and processing.
     * Uses caching based on source ID, active viewer, compact state, and a DV fingerprint to optimize performance.
     */
    private suspend fun computeViewerContext(
        widgetSourceObj: ObjectWrapper.Basic,
        activeView: Id?,
        isCompact: Boolean
    ): ViewerContext {
        return ctxMutex.withLock {
            // Always fetch the ObjectView inside the lock to ensure sequential cache updates
            val obj = getObjectViewOrEmpty(objectId = widgetSourceObj.id, spaceId = space)
            val fp = obj.dataViewFingerprint()
            val key = ContextKey(
                widgetSourceId = widgetSourceObj.id,
                activeViewerId = activeView,
                isCompact = isCompact,
                dvFingerprint = fp
            )

            if (cachedContextKey == key && cachedContext != null) {
                Timber.d("Using cached ViewerContext for widget ${widget.id}")
                return@withLock cachedContext!!
            }

            Timber.d("Computing ViewerContext for widget ${widget.id} (cache miss or DV changed)")
            val result = buildViewerContextCommon(
                obj = obj,
                activeViewerId = activeView,
                isCompact = isCompact
            )
            cachedContext = result
            cachedContextKey = key
            result
        }
    }

    /**
     * Computes and caches ViewerContext to avoid duplicate object fetches and processing.
     * Uses caching based on source ID, active viewer, compact state, and a DV fingerprint to optimize performance.
     */
    private suspend fun computeViewerContext(
        widgetSourceObj: ObjectWrapper.Type,
        activeView: Id?,
        isCompact: Boolean
    ): ViewerContext {
        return ctxMutex.withLock {
            // Always fetch the ObjectView inside the lock to ensure sequential cache updates
            val obj = getObjectViewOrEmpty(objectId = widgetSourceObj.id, spaceId = space)
            val fp = obj.dataViewFingerprint()
            val key = ContextKey(
                widgetSourceId = widgetSourceObj.id,
                activeViewerId = activeView,
                isCompact = isCompact,
                dvFingerprint = fp
            )

            if (cachedContextKey == key && cachedContext != null) {
                Timber.d("Using cached ViewerContext for widget ${widget.id}")
                return@withLock cachedContext!!
            }

            Timber.d("Computing ViewerContext for widget ${widget.id} (cache miss or DV changed)")
            val result = buildViewerContextCommon(
                obj = obj,
                activeViewerId = activeView,
                isCompact = isCompact
            )
            cachedContext = result
            cachedContextKey = key
            result
        }
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
            val indexMap = order.ids.withIndex().associate { it.value to it.index }
            objects = objects.sortedBy { obj -> indexMap[obj.id] ?: Int.MAX_VALUE }
        }
    }
    return objects
}