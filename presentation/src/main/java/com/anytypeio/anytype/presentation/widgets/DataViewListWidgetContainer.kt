package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.cover
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
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

class DataViewListWidgetContainer(
    private val widget: Widget,
    private val getObject: GetObject,
    private val storage: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val activeView: Flow<Id?>,
    private val isWidgetCollapsed: Flow<Boolean>,
    private val coverImageHashProvider: CoverImageHashProvider,
    isSessionActive: Flow<Boolean>,
    onRequestCache: () -> WidgetView.SetOfObjects? = { null }
) : WidgetContainer {

    init {
        if (BuildConfig.DEBUG) {
            assert(widget is Widget.List || widget is Widget.View) { "Incompatible container." }
        }
    }

    override val view : Flow<WidgetView> = isSessionActive.flatMapLatest { isActive ->
        if (isActive)
            buildViewFlow().onStart {
                isWidgetCollapsed.take(1).collect { isCollapsed ->
                    val loadingStateView = when(widget) {
                        is Widget.List -> {
                            WidgetView.SetOfObjects(
                                id = widget.id,
                                source = widget.source,
                                tabs = emptyList(),
                                elements = emptyList(),
                                isExpanded = !isCollapsed,
                                isCompact = widget.isCompact,
                                isLoading = true
                            )
                        }
                        is Widget.View -> {
                            WidgetView.Gallery(
                                id = widget.id,
                                source = widget.source,
                                tabs = emptyList(),
                                elements = emptyList(),
                                isExpanded = !isCollapsed,
                                isLoading = true
                            )
                        }
                        is Widget.Link, is Widget.Tree -> {
                            throw IllegalStateException("Incompatible widget type.")
                        }
                    }
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

    private fun buildViewFlow() : Flow<WidgetView> = combine(
        activeView.distinctUntilChanged(),
        isWidgetCollapsed
    ) { view, isCollapsed -> view to isCollapsed }.flatMapLatest { (view, isCollapsed) ->
        when (val source = widget.source) {
            is Widget.Source.Bundled -> throw IllegalStateException("Bundled widgets do not support data view layout")
            is Widget.Source.Default -> {
                val isCompact = widget is Widget.List && widget.isCompact
                if (isCollapsed) {
                    when(val w = widget) {
                        is Widget.List -> {
                            flowOf(
                                WidgetView.SetOfObjects(
                                    id = widget.id,
                                    source = widget.source,
                                    tabs = emptyList(),
                                    elements = emptyList(),
                                    isExpanded = false,
                                    isCompact = isCompact
                                )
                            )
                        }
                        is Widget.View -> {
                            flowOf(
                                WidgetView.Gallery(
                                    id = widget.id,
                                    source = widget.source,
                                    tabs = emptyList(),
                                    elements = emptyList(),
                                    isExpanded = false
                                )
                            )
                        }
                        is Widget.Tree, is Widget.Link -> {
                            throw IllegalStateException("Incompatible widget type.")
                        }
                    }
                } else {
                    val obj = getObject.run(widget.source.id)
                    val dv = obj.blocks.find { it.content is DV }?.content
                    val target = if (dv is DV) {
                        dv.viewers.find { it.id == view } ?: dv.viewers.firstOrNull()
                    } else {
                        null
                    }
                    val params = obj.parseDataViewStoreSearchParams(
                        subscription = widget.id,
                        viewer = view,
                        source = source.obj,
                        config = widget.config,
                        limit = WidgetConfig.resolveListWidgetLimit(
                            isCompact = isCompact,
                            isGallery = target?.type == DVViewerType.GALLERY,
                            limit = when(widget) {
                                is Widget.List -> widget.limit
                                is Widget.View -> widget.limit
                                is Widget.Tree, is Widget.Link -> {
                                    throw IllegalStateException("Incompatible widget type.")
                                }
                            }
                        )
                    )
                    if (params != null) {
                        storage.subscribeWithDependencies(params).map { results ->
                            val objects = resolveObjectOrder(
                                searchResults = results.results,
                                obj = obj,
                                activeView = view
                            )
                            if (target != null && target.type == DVViewerType.GALLERY) {
                                val withCover = !target.coverRelationKey.isNullOrEmpty()
                                val withIcon = !target.hideIcon
                                WidgetView.Gallery(
                                    id = widget.id,
                                    source = widget.source,
                                    view = target.id,
                                    tabs = obj.tabs(viewer = view),
                                    elements = objects.map { obj ->
                                        WidgetView.SetOfObjects.Element(
                                            obj = obj,
                                            objectIcon = if (withIcon) {
                                                obj.widgetElementIcon(
                                                    builder = urlBuilder
                                                )
                                            } else {
                                                ObjectIcon.None
                                            },
                                            cover = if (withCover) {
                                                obj.cover(
                                                    urlBuilder = urlBuilder,
                                                    coverImageHashProvider = coverImageHashProvider,
                                                    isMedium = true
                                                )
                                            } else {
                                                null
                                            }
                                        )
                                    },
                                    isExpanded = true,
                                    showIcon = withIcon,
                                    showCover = withCover
                                )
                            } else {
                                WidgetView.SetOfObjects(
                                    id = widget.id,
                                    source = widget.source,
                                    tabs = obj.tabs(viewer = view),
                                    elements = objects.map { obj ->
                                        WidgetView.SetOfObjects.Element(
                                            obj = obj,
                                            objectIcon = obj.widgetElementIcon(builder = urlBuilder)
                                        )
                                    },
                                    isExpanded = true,
                                    isCompact = isCompact
                                )
                            }
                        }
                    } else {
                        flowOf(defaultEmptyState())
                    }
                }
            }
        }
    }.catch { e ->
        when(widget) {
            is Widget.List -> {
                emit(defaultEmptyState())
            }
            is Widget.View -> {
                emit(defaultEmptyState())
            }
            else -> {
                Timber.e(e, "Error in data view container flow")
            }
        }
    }

    private fun defaultEmptyState() : WidgetView {
        return when(widget) {
            is Widget.List -> WidgetView.SetOfObjects(
                id = widget.id,
                source = widget.source,
                tabs = emptyList(),
                elements = emptyList(),
                isExpanded = true,
                isCompact = (widget as? Widget.List)?.isCompact ?: false
            )
            is Widget.View -> WidgetView.Gallery(
                id = widget.id,
                source = widget.source,
                tabs = emptyList(),
                elements = emptyList(),
                isExpanded = true,
                view = null
            )
            is Widget.Link, is Widget.Tree -> {
                throw IllegalStateException("Incompatible widget type.")
            }
        }
    }
}

fun ObjectView.isCollection(): Boolean {
    val wrapper = ObjectWrapper.Basic(details.getOrDefault(root, emptyMap()))
    return wrapper.layout == ObjectType.Layout.COLLECTION
}

fun ObjectView.parseDataViewStoreSearchParams(
    subscription: Id,
    limit: Int,
    config: Config,
    source: ObjectWrapper.Basic,
    viewer: Id?
): StoreSearchParams? {
    if (source.isArchived == true || source.isDeleted == true) return null
    val block = blocks.find { it.content is DV } ?: return null
    val dv = block.content<DV>()
    val view = dv.viewers.find { it.id == viewer } ?: dv.viewers.firstOrNull() ?: return null
    val dataViewKeys = dv.relationLinks.map { it.key }
    val defaultKeys = ObjectSearchConstants.defaultDataViewKeys
    return StoreSearchParams(
        subscription =subscription,
        sorts = view.sorts,
        keys = buildList {
            addAll(defaultKeys)
            addAll(dataViewKeys)
            add(Relations.DESCRIPTION)
        }.distinct(),
        filters = buildList {
            addAll(view.filters)
            addAll(
                ObjectSearchConstants.defaultDataViewFilters(
                    spaces = buildList {
                        add(config.space)
                        add(config.techSpace)
                    }
                )
            )
            add(
                DVFilter(
                    relation = Relations.TYPE_UNIQUE_KEY,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(
                        ObjectTypeIds.OBJECT_TYPE,
                        ObjectTypeIds.RELATION,
                        ObjectTypeIds.TEMPLATE,
                        ObjectTypeIds.DASHBOARD,
                        ObjectTypeIds.RELATION_OPTION,
                        ObjectTypeIds.DASHBOARD,
                        ObjectTypeIds.DATE
                    )
                ),
            )
        },
        limit = limit,
        source = source.setOf,
        collection = if (isCollection())
            root
        else
            null
    )
}

fun ObjectView.tabs(viewer: Id?): List<WidgetView.SetOfObjects.Tab> = buildList {
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

fun resolveObjectOrder(
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