package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * State holder for keeping track of expanded branches of a tree widget.
 * @see [TreeWidgetContainer]
 */
class TreeWidgetBranchStateHolder {

    private val expandedBranches = MutableStateFlow<List<TreePath>>(emptyList())

    /**
     * Stream of changes of expanded / collapsed state for a branch of a tree widget
     */
    fun stream(widget: Id): Flow<List<TreePath>> {
        return expandedBranches.map { paths ->
            paths.filter { path ->
                path.startsWith(widget)
            }
        }
    }

    fun onExpand(linkPath: String) {
        Timber.d("onExpand before update: ${expandedBranches.value}")
        val curr = expandedBranches.value
        val idx = curr.indexOf(linkPath)
        if (idx != -1) {
            expandedBranches.value = curr.filter { path ->
                if (path > linkPath) {
                    !path.startsWith(linkPath)
                } else {
                    path != linkPath
                }
            }
        } else {
            expandedBranches.value = buildList {
                addAll(curr)
                add(linkPath)
            }
        }
        Timber.d("onExpand after update: ${expandedBranches.value}")
    }
}