package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.widgets.WidgetConfig.isValidObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map


class TreeWidgetContainer(
    private val widget: Widget.Tree,
    private val workspace: Id,
    private val container: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val expandedBranches: Flow<List<TreePath>>,
    private val isWidgetCollapsed: Flow<Boolean>,
    isSessionActive: Flow<Boolean>
) : WidgetContainer {

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
                container.subscribe(
                    ListWidgetContainer.params(
                        subscription = widget.source.id,
                        workspace = workspace,
                        keys = keys
                    )
                ).map { rootLevelObjects ->
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
                    with(nodes) {
                        clear()
                        putAll(valid.associate { obj -> obj.id to obj.links })
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
                    with(nodes) {
                        clear()
                        putAll(valid.associate { obj -> obj.id to obj.links })
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

    private fun getDefaultSubscriptionTargets(
        paths: List<TreePath>,
        source: Widget.Source.Default
    ) = buildList {
        if (source.obj.isArchived != true && source.obj.isDeleted != true) {
            addAll(source.obj.links)
            nodes.forEach { (id, links) ->
                if (paths.any { path -> path.contains(id) }) addAll(links)
            }
        }
    }.distinct()

    private fun getBundledSubscriptionTargets(
        paths: List<TreePath>,
        links: List<Id>,
    ) = buildList {
        addAll(links)
        nodes.forEach { (id, links) ->
            if (paths.any { path -> path.contains(id) }) addAll(links)
        }
    }.distinct()

    private fun buildTree(
        links: List<Id>,
        expanded: List<TreePath>,
        level: Int,
        path: TreePath,
        data: Map<Id, ObjectWrapper.Basic>
    ): List<WidgetView.Tree.Element> = buildList {
        links.forEach { link ->
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
                        objectIcon = ObjectIcon.from(
                            obj = obj,
                            layout = obj.layout,
                            builder = urlBuilder
                        ),
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
        }
    }
}

/**
 * Path to an object inside a tree of objects inside a tree widget.
 * Example: widget-id/source-id/object-id/object-id ...
 * @see [Widget.Tree.id], [Widget.Tree.source]
 */
typealias TreePath = String