package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.widgets.WidgetConfig.isValidObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map


class TreeWidgetContainer(
    private val widget: Widget.Tree,
    private val container: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    dispatchers: AppCoroutineDispatchers,
    expandedBranches: Flow<List<TreePath>>,
    isWidgetCollapsed: Flow<Boolean>
) : WidgetContainer {

    private val nodes = mutableMapOf<Id, List<Id>>()

    override val view: Flow<WidgetView> = combine(
        expandedBranches, isWidgetCollapsed
    ) { paths, isWidgetCollapsed ->
        paths to isWidgetCollapsed
    }.flatMapLatest { (paths, isWidgetCollapsed) ->
        container.subscribe(
            StoreSearchByIdsParams(
                subscription = widget.id,
                keys = keys,
                targets = if (!isWidgetCollapsed) {
                    getSubscriptionTargets(paths = paths)
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
                obj = widget.source,
                isExpanded = !isWidgetCollapsed,
                elements = buildTree(
                    links = widget.source.links,
                    level = ROOT_INDENT,
                    expanded = paths,
                    path = widget.id + SEPARATOR + widget.source.id + SEPARATOR,
                    data = data
                )
            )
        }
    }.flowOn(dispatchers.io)

    private fun getSubscriptionTargets(
        paths: List<TreePath>
    ) = buildList {
        addAll(widget.source.links)
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

    data class Node(
        val id: Id,
        val children: List<Id>
    )
}

/**
 * Path to an object inside a tree of objects inside a tree widget.
 * Example: widget-id/source-id/object-id/object-id ...
 * @see [Widget.Tree.id], [Widget.Tree.source]
 */
typealias TreePath = String