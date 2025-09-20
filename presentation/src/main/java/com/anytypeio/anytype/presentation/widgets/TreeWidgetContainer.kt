package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.widgets.BundledWidgetSourceIds
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.spaces.GetSpaceView
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.widgets.WidgetConfig.isValidObject
import com.anytypeio.anytype.presentation.widgets.WidgetView.Name.Bundled
import com.anytypeio.anytype.presentation.widgets.WidgetView.Name.Default
import com.anytypeio.anytype.presentation.widgets.WidgetView.Tree
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber


class TreeWidgetContainer(
    private val widget: Widget.Tree,
    private val container: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val expandedBranches: Flow<List<TreePath>>,
    private val isWidgetCollapsed: Flow<Boolean>,
    private val objectWatcher: ObjectWatcher,
    private val getSpaceView: GetSpaceView,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    isSessionActive: Flow<Boolean>,
    onRequestCache: () -> WidgetView.Tree? = { null }
) : WidgetContainer {

    private val mutex = Mutex()

    private val rootLevelLimit = WidgetConfig.resolveTreeWidgetLimit(widget.limit)

    private val nodes = mutableMapOf<Id, List<Id>>()

    override val view : Flow<WidgetView> = isSessionActive.flatMapLatest { isActive ->
        if (isActive)
            buildViewFlow().onStart {
                isWidgetCollapsed.take(1).collect { isCollapsed ->
                    val loadingStateView = WidgetView.Tree(
                        id = widget.id,
                        source = widget.source,
                        isExpanded = !isCollapsed,
                        elements = emptyList(),
                        isLoading = true,
                        icon = widget.icon,
                        name = widget.source.getPrettyName(fieldParser)
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

    private fun buildViewFlow() = combine(
        expandedBranches,
        isWidgetCollapsed
    ) { paths, isWidgetCollapsed ->
        paths to isWidgetCollapsed
    }.flatMapLatest { (paths, isWidgetCollapsed) ->
        when (val source = widget.source) {
            is Widget.Source.Bundled -> {
                fetchRootLevelBundledSourceObjects().map { rootLevelObjects ->
                    rootLevelObjects.map { it.id }
                }.flatMapLatest { rootLevelObjects ->
                    container.subscribe(
                        StoreSearchByIdsParams(
                            space = SpaceId(widget.config.space),
                            subscription = widget.id,
                            keys = keys,
                            targets = if (!isWidgetCollapsed) {
                                getBundledSubscriptionTargets(
                                    paths = paths,
                                    links = rootLevelObjects
                                )
                            } else {
                                emptyList()
                            }
                        )
                    ).map { data ->
                        rootLevelObjects to data
                    }
                }.map { (rootLevelLinks, objectWrappers) ->
                    val valid = objectWrappers.filter { obj -> isValidObject(obj) }
                    val data = valid.associateBy { r -> r.id }
                    mutex.withLock {
                        with(nodes) {
                            clear()
                            putAll(valid.associate { obj -> obj.id to obj.links })
                        }
                    }
                    Tree(
                        id = widget.id,
                        source = widget.source,
                        icon = widget.icon,
                        isExpanded = !isWidgetCollapsed,
                        elements = buildTree(
                            links = rootLevelLinks,
                            level = ROOT_INDENT,
                            expanded = paths,
                            path = widget.id + SEPARATOR + widget.source.id + SEPARATOR,
                            data = data,
                            rootLimit = rootLevelLimit,
                            storeOfObjectTypes = storeOfObjectTypes
                        ),
                        name = Bundled(source = source)
                    )
                }
            }
            is Widget.Source.Default -> {
                container.subscribe(
                    StoreSearchByIdsParams(
                        space = SpaceId(widget.config.space),
                        subscription = widget.id,
                        keys = keys,
                        targets = if (!isWidgetCollapsed) {
                            getDefaultSubscriptionTargets(
                                paths = paths,
                                source = source
                            )
                        } else {
                            emptyList()
                        }
                    )
                ).map { results ->
                    val valid = results.filter { obj -> isValidObject(obj) }
                    val data = valid.associateBy { r -> r.id }
                    mutex.withLock {
                        with(nodes) {
                            clear()
                            putAll(valid.associate { obj -> obj.id to obj.links })
                        }
                    }
                    Tree(
                        id = widget.id,
                        source = widget.source,
                        isExpanded = !isWidgetCollapsed,
                        elements = buildTree(
                            links = source.obj.links,
                            level = ROOT_INDENT,
                            expanded = paths,
                            path = widget.id + SEPARATOR + widget.source.id + SEPARATOR,
                            data = data,
                            rootLimit = WidgetConfig.NO_LIMIT,
                            storeOfObjectTypes = storeOfObjectTypes
                        ),
                        icon = widget.icon,
                        name = Default(
                            prettyPrintName = fieldParser.getObjectName(source.obj)
                        )
                    )
                }
            }
            else -> {
                Timber.w("Unsupported source type for tree widget: ${widget.source}")
                emptyFlow()
            }
        }
    }

    private suspend fun fetchRootLevelBundledSourceObjects(): Flow<List<ObjectWrapper.Basic>> {
        return when (widget.source.id) {
            BundledWidgetSourceIds.FAVORITE -> {
                objectWatcher
                    .watch(
                        target = widget.config.home,
                        space = SpaceId(widget.config.space)
                    )
                    .map { obj -> obj.orderOfRootObjects(obj.root) }
                    .catch { emit(emptyMap()) }
                    .flatMapLatest { order ->
                        container.subscribe(
                            StoreSearchByIdsParams(
                                space = SpaceId(widget.config.space),
                                subscription = widget.source.id,
                                targets = order.keys.toList(),
                                keys = keys
                            )
                        ).map { favorites ->
                            favorites
                                .filter { obj -> obj.notDeletedNorArchived }
                                .sortedBy { obj -> order[obj.id] }
                                .take(rootLevelLimit)
                        }
                    }
            }
            BundledWidgetSourceIds.RECENT -> {
                val spaceView = getSpaceView.async(
                    GetSpaceView.Params.BySpaceViewId(widget.config.spaceView)
                ).getOrNull()
                val spaceViewCreationDate = spaceView
                    ?.getValue<Double?>(Relations.CREATED_DATE)
                    ?.toLong()
                container.subscribe(
                    ListWidgetContainer.params(
                        subscription = widget.source.id,
                        space = widget.config.space,
                        keys = keys,
                        limit = rootLevelLimit,
                        spaceCreationDateInSeconds = spaceViewCreationDate
                    )
                )
            }
            else -> {
                container.subscribe(
                    ListWidgetContainer.params(
                        subscription = widget.source.id,
                        space = widget.config.space,
                        keys = keys,
                        limit = rootLevelLimit
                    )
                )
            }
        }
    }

    private suspend fun getDefaultSubscriptionTargets(
        paths: List<TreePath>,
        source: Widget.Source.Default
    ) = buildList {
        if (source.obj.isArchived != true && source.obj.isDeleted != true) {
            addAll(source.obj.links)
            mutex.withLock {
                nodes.forEach { (id, links) ->
                    if (paths.any { path -> path.contains(id) }) addAll(links)
                }
            }
        }
    }.distinct()

    private suspend fun getBundledSubscriptionTargets(
        paths: List<TreePath>,
        links: List<Id>,
    ) = buildList {
        addAll(links)
        mutex.withLock {
            nodes.forEach { (id, links) ->
                if (paths.any { path -> path.contains(id) }) addAll(links)
            }
        }
    }.distinct()

    private suspend fun buildTree(
        links: List<Id>,
        expanded: List<TreePath>,
        level: Int,
        path: TreePath,
        data: Map<Id, ObjectWrapper.Basic>,
        rootLimit: Int,
        storeOfObjectTypes: StoreOfObjectTypes
    ): List<WidgetView.Tree.Element> = buildList {
        links.forEachIndexed { index, link ->
            // Applying limit only for root level:
            if (level == 0 && rootLimit > 0 && index == rootLimit) {
                return@buildList
            }
            val obj = data[link]
            if (obj != null) {
                val currentLinkPath = path + link
                val isExpandable = level < MAX_INDENT
                add(
                    WidgetView.Tree.Element(
                        id = obj.id,
                        obj = obj,
                        elementIcon = resolveObjectIcon(
                            obj = obj,
                            isExpandable = isExpandable,
                            expanded = expanded,
                            currentLinkPath = currentLinkPath
                        ),
                        objectIcon = obj.objectIcon(
                            builder = urlBuilder,
                            objType = storeOfObjectTypes.getTypeOfObject(obj)
                        ),
                        indent = level,
                        path = path + link,
                        name = buildWidgetName(
                            obj = obj,
                            fieldParser = fieldParser
                        )
                    )
                )
                if (isExpandable && expanded.contains(currentLinkPath)) {
                    addAll(
                        buildTree(
                            links = obj.links,
                            level = level.inc(),
                            expanded = expanded,
                            path = currentLinkPath + SEPARATOR,
                            data = data,
                            rootLimit = rootLimit,
                            storeOfObjectTypes = storeOfObjectTypes
                        )
                    )
                }
            }
        }
    }

    private fun resolveObjectIcon(
        obj: ObjectWrapper.Basic,
        isExpandable: Boolean,
        expanded: List<TreePath>,
        currentLinkPath: String
    ) = when {
        obj.type.contains(ObjectTypeIds.SET) -> WidgetView.Tree.ElementIcon.Set
        obj.type.contains(ObjectTypeIds.COLLECTION) -> WidgetView.Tree.ElementIcon.Collection
        !isExpandable -> WidgetView.Tree.ElementIcon.Leaf
        obj.links.isEmpty() -> WidgetView.Tree.ElementIcon.Leaf
        else -> WidgetView.Tree.ElementIcon.Branch(
            isExpanded = expanded.contains(currentLinkPath)
        )
    }

    companion object {
        const val ROOT_INDENT = 0
        const val MAX_INDENT = 3
        const val SEPARATOR = "/"
        val keys = buildList {
            addAll(ObjectSearchConstants.defaultKeys)
            add(Relations.LINKS)
            add(Relations.LAST_MODIFIED_DATE)
            add(Relations.LAST_OPENED_DATE)
        }
    }
}

/**
 * Path to an object inside a tree of objects inside a tree widget.
 * Example: widget-id/source-id/object-id/object-id ...
 * @see [Widget.Tree.id], [Widget.Tree.source]
 */
typealias TreePath = String