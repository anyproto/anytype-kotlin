package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.widgets.WidgetConfig.isValidObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import timber.log.Timber

class TreeWidgetContainer(
    private val widget: Widget.Tree,
    private val container: ObjectSearchSubscriptionContainer,
    private val expandedBranches: Flow<List<TreePath>>
) : WidgetContainer {

    private val store = mutableMapOf<Id, ObjectWrapper.Basic>()

    override val view: Flow<WidgetView.Tree> = expandedBranches.mapLatest {
        container.get(
            subscription = widget.id,
            keys = keys,
            targets = getSubscriptionTargets(paths = it).also { targets ->
                Timber.d("Subscription targets: $targets")
            }
        ).also { result ->
            val valid = result.filter { obj -> isValidObject(obj) }
            store.clear()
            store.putAll(valid.associateBy { r -> r.id })
        }
        WidgetView.Tree(
            id = widget.id,
            obj = widget.source,
            elements = buildTree(
                links = widget.source.links,
                level = ROOT_INDENT,
                expanded = it,
                path = widget.id + SEPARATOR + widget.source.id + SEPARATOR
            )
        )
    }

    private fun getSubscriptionTargets(paths: List<TreePath>) = buildList {
        addAll(widget.source.links)
        store.forEach { (id, obj) ->
            if (paths.any { path -> path.contains(id) }) addAll(obj.links)
        }
    }.distinct()

    private fun buildTree(
        links: List<Id>,
        expanded: List<TreePath>,
        level: Int,
        path: TreePath
    ): List<WidgetView.Tree.Element> = buildList {
        links.forEach { link ->
            val obj = store[link]
            if (obj != null) {
                val currentLinkPath = path + link
                val isExpandable = level < MAX_INDENT
                add(
                    WidgetView.Tree.Element(
                        icon = resolveObjectIcon(
                            obj = obj,
                            isExpandable = isExpandable,
                            expanded = expanded,
                            currentLinkPath = currentLinkPath
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
                            path = currentLinkPath + SEPARATOR
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
        obj.type.contains(ObjectTypeIds.SET) -> WidgetView.Tree.Icon.Set
        !isExpandable -> WidgetView.Tree.Icon.Leaf
        obj.links.isEmpty() -> WidgetView.Tree.Icon.Leaf
        else -> WidgetView.Tree.Icon.Branch(
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