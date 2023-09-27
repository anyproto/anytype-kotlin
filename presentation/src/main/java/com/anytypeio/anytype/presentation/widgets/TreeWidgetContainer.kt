package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.widgets.WidgetConfig.isValidObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class TreeWidgetContainer(
    private val widget: Widget.Tree,
    private val workspace: Id,
    private val config: Config,
    private val container: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val expandedBranches: Flow<List<TreePath>>,
    private val isWidgetCollapsed: Flow<Boolean>,
    private val objectWatcher: ObjectWatcher,
    isSessionActive: Flow<Boolean>
) : WidgetContainer {

    private val mutex = Mutex()

    private val rootLevelLimit = WidgetConfig.resolveTreeWidgetLimit(widget.limit)

    private val nodes = mutableMapOf<Id, List<Id>>()

    override val view = isSessionActive.flatMapLatest { isActive ->
        if (isActive)
            buildViewFlow()
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
                    WidgetView.Tree(
                        id = widget.id,
                        source = widget.source,
                        isExpanded = !isWidgetCollapsed,
                        elements = buildTree(
                            links = rootLevelLinks,
                            level = ROOT_INDENT,
                            expanded = paths,
                            path = widget.id + SEPARATOR + widget.source.id + SEPARATOR,
                            data = data
                        )
                    )
                }
            }
            is Widget.Source.Default -> {
                container.subscribe(
                    StoreSearchByIdsParams(
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
                    WidgetView.Tree(
                        id = widget.id,
                        source = widget.source,
                        isExpanded = !isWidgetCollapsed,
                        elements = buildTree(
                            links = source.obj.links,
                            level = ROOT_INDENT,
                            expanded = paths,
                            path = widget.id + SEPARATOR + widget.source.id + SEPARATOR,
                            data = data
                        )
                    )
                }
            }
        }
    }

    private suspend fun fetchRootLevelBundledSourceObjects(): Flow<List<ObjectWrapper.Basic>> {
        return if (widget.source.id == BundledWidgetSourceIds.FAVORITE) {
            combine(
                objectWatcher
                    .watch(config.home)
                    .map { obj -> obj.orderOfRootObjects(obj.root) }
                    .catch { emit(emptyMap()) },
                container.subscribe(
                    ListWidgetContainer.params(
                        subscription = widget.source.id,
                        workspace = workspace,
                        keys = keys,
                        limit = resolveLimit()
                    )
                ).map { favorites ->
                    favorites.filter { obj -> obj.isArchived != true && obj.isDeleted != true }
                }
            ) { order, rootLevelObjects ->
                rootLevelObjects.sortedBy { obj -> order[obj.id] }
            }
        } else {
            container.subscribe(
                ListWidgetContainer.params(
                    subscription = widget.source.id,
                    workspace = workspace,
                    keys = keys,
                    limit = resolveLimit()
                )
            )
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

    private fun buildTree(
        links: List<Id>,
        expanded: List<TreePath>,
        level: Int,
        path: TreePath,
        data: Map<Id, ObjectWrapper.Basic>
    ): List<WidgetView.Tree.Element> = buildList {
        links.forEachIndexed { index, link ->
            // Applying limit only for root level:
            if (level == 0 && rootLevelLimit > 0 && index == rootLevelLimit) {
                return@buildList
            }
            val obj = data[link]
            if (obj != null) {
                val currentLinkPath = path + link
                val isExpandable = level < MAX_INDENT
                add(
                    WidgetView.Tree.Element(
                        elementIcon = resolveObjectIcon(
                            obj = obj,
                            isExpandable = isExpandable,
                            expanded = expanded,
                            currentLinkPath = currentLinkPath
                        ),
                        objectIcon = obj.widgetElementIcon(urlBuilder),
                        indent = level,
                        obj = obj,
                        path = path + link
                    )
                )
                if (isExpandable && expanded.contains(currentLinkPath)) {
                    addAll(
                        buildTree(
                            links = obj.links,
                            level = level.inc(),
                            expanded = expanded,
                            path = currentLinkPath + SEPARATOR,
                            data = data
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

    private fun resolveLimit(): Int = NO_LIMIT

    companion object {
        const val ROOT_INDENT = 0
        const val MAX_INDENT = 3
        const val SEPARATOR = "/"
        const val NO_LIMIT = 0
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