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
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.mapper.objectIcon
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
    private val storeOfRelations: StoreOfRelations,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    isSessionActive: Flow<Boolean>,
    onRequestCache: () -> WidgetView.SetOfObjects? = { null },
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
                                isLoading = true,
                                name = when(val source = widget.source) {
                                    is Widget.Source.Bundled -> WidgetView.Name.Bundled(source = source)
                                    is Widget.Source.Default ->  WidgetView.Name.Default(
                                        prettyPrintName = fieldParser.getObjectName(source.obj)
                                    )
                                }
                            )
                        }
                        is Widget.View -> {
                            WidgetView.Gallery(
                                id = widget.id,
                                source = widget.source,
                                tabs = emptyList(),
                                elements = emptyList(),
                                isExpanded = !isCollapsed,
                                isLoading = true,
                                name =  WidgetView.Name.Default(
                                    prettyPrintName = fieldParser.getObjectName(widget.source.obj)
                                )
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
                    when(widget) {
                        is Widget.List -> {
                            flowOf(
                                WidgetView.SetOfObjects(
                                    id = widget.id,
                                    source = widget.source,
                                    tabs = emptyList(),
                                    elements = emptyList(),
                                    isExpanded = false,
                                    isCompact = isCompact,
                                    name = when(val source = widget.source) {
                                        is Widget.Source.Bundled -> WidgetView.Name.Bundled(source = source)
                                        is Widget.Source.Default ->  WidgetView.Name.Default(
                                            prettyPrintName = fieldParser.getObjectName(source.obj)
                                        )
                                    }
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
                                    isExpanded = false,
                                    name =  WidgetView.Name.Default(
                                        prettyPrintName = fieldParser.getObjectName(source.obj)
                                    )
                                )
                            )
                        }
                        is Widget.Tree, is Widget.Link -> {
                            throw IllegalStateException("Incompatible widget type.")
                        }
                    }
                } else {
                    if (source.obj.layout == ObjectType.Layout.SET && source.obj.setOf.isEmpty()) {
                        flowOf(defaultEmptyState())
                    } else {
                        val obj = getObject.run(
                            GetObject.Params(
                                target = widget.source.id,
                                space = SpaceId(widget.config.space)
                            )
                        )
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
                                limit = when (widget) {
                                    is Widget.List -> widget.limit
                                    is Widget.View -> widget.limit
                                    is Widget.Tree, is Widget.Link -> {
                                        throw IllegalStateException("Incompatible widget type.")
                                    }
                                }
                            )
                        )
                        if (params != null) {
                            if (target?.type == DVViewerType.GALLERY) {
                                galleryWidgetSubscribe(
                                    obj = obj,
                                    activeView = view,
                                    params = params,
                                    target = target,
                                    storeOfObjectTypes = storeOfObjectTypes
                                )
                            } else {
                                defaultWidgetSubscribe(
                                    obj = obj,
                                    activeView = view,
                                    params = params,
                                    isCompact = isCompact,
                                    storeOfObjectTypes = storeOfObjectTypes
                                )
                            }
                        } else {
                            flowOf(defaultEmptyState())
                        }
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
                        name = WidgetView.Name.Default(
                            prettyPrintName = fieldParser.getObjectName(obj)
                        )
                    )
                },
                isExpanded = true,
                showIcon = withIcon,
                showCover = withCover,
                name = when(val source = widget.source) {
                    is Widget.Source.Bundled -> WidgetView.Name.Bundled(source = source)
                    is Widget.Source.Default -> WidgetView.Name.Default(
                        prettyPrintName = fieldParser.getObjectName(source.obj)
                    )
                }
            )
        }
    }

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
                            prettyPrintName = fieldParser.getObjectName(obj)
                        )
                    )
                },
                isExpanded = true,
                isCompact = isCompact,
                name = when(val source = widget.source) {
                    is Widget.Source.Bundled -> WidgetView.Name.Bundled(source = source)
                    is Widget.Source.Default ->  WidgetView.Name.Default(
                        prettyPrintName = fieldParser.getObjectName(source.obj)
                    )
                }
            )
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
                isCompact = (widget as? Widget.List)?.isCompact ?: false,
                name = when(val source = widget.source) {
                    is Widget.Source.Bundled -> WidgetView.Name.Bundled(source = source)
                    is Widget.Source.Default ->  WidgetView.Name.Default(
                        prettyPrintName = fieldParser.getObjectName(source.obj)
                    )
                }
            )
            is Widget.View -> WidgetView.Gallery(
                id = widget.id,
                source = widget.source,
                tabs = emptyList(),
                elements = emptyList(),
                isExpanded = true,
                view = null,
                name =  WidgetView.Name.Default(
                    prettyPrintName = fieldParser.getObjectName(widget.source.obj)
                )
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
        space = SpaceId(config.space),
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
                ObjectSearchConstants.defaultDataViewFilters()
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