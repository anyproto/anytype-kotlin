package com.anytypeio.anytype.core_ui.widgets.dv.board

import android.os.Bundle
import androidx.compose.foundation.lazy.LazyListState
import com.anytypeio.anytype.presentation.sets.model.Viewer

/** Different grouping relations may publish identical column ids, but own separate anchors. */
internal fun Viewer.Board.scrollKey(): String =
    groupingKey?.let { "${id.length}:$id$it" } ?: id

internal fun boardStructureChanged(previous: Viewer.Board?, next: Viewer.Board): Boolean =
    previous?.scrollKey() != next.scrollKey() ||
        previous?.columns?.map { it.id } != next.columns.map { it.id }

internal fun boardAnchorResolved(anchor: BoardScrollAnchor?, column: Viewer.Board.Column): Boolean =
    anchor?.id == null || column.cards.any { it.objectId == anchor.id } ||
        column.hasLoadedRecords && isColumnFullyLoaded(column.cards.size, column.count)

/** Small UI-only anchors; no card models, coordinates, Views, or animation jobs are saved. */
internal data class BoardScrollAnchor(
    val id: String?,
    val nextId: String?,
    val previousId: String?,
    val index: Int,
    val offset: Int
)

internal fun resolveBoardAnchor(anchor: BoardScrollAnchor, ids: List<String>): Int {
    if (ids.isEmpty()) return 0
    for (id in listOf(anchor.id, anchor.nextId, anchor.previousId)) {
        val index = if (id == null) -1 else ids.indexOf(id)
        if (index >= 0) return index
    }
    return anchor.index.coerceIn(ids.indices)
}

/** Persisted by BoardViewWidget before clear(), with live readers only while lists compose. */
internal class BoardScrollStore {
    private data class ViewerState(
        var row: BoardScrollAnchor? = null,
        val columns: LinkedHashMap<String, BoardScrollAnchor> = linkedMapOf()
    )

    private val viewers = linkedMapOf<String, ViewerState>()
    private val liveReaders = mutableMapOf<Pair<String, String?>, () -> BoardScrollAnchor>()
    private val knownColumns = linkedMapOf<String, Set<String>>()
    var inputGeneration: Long = 0
        private set

    fun onUserInput() { inputGeneration++ }

    fun anchor(viewerId: String, columnId: String?): BoardScrollAnchor? =
        viewers[viewerId]?.let { if (columnId == null) it.row else it.columns[columnId] }

    fun register(viewerId: String, columnId: String?, reader: () -> BoardScrollAnchor) {
        liveReaders[viewerId to columnId] = reader
    }

    fun unregister(viewerId: String, columnId: String?) {
        liveReaders.remove(viewerId to columnId)?.let { put(viewerId, columnId, it()) }
    }

    fun capture() {
        liveReaders.toMap().forEach { (key, reader) -> put(key.first, key.second, reader()) }
    }

    private fun put(viewerId: String, columnId: String?, anchor: BoardScrollAnchor) {
        if (columnId != null && knownColumns[viewerId]?.contains(columnId) == false) return
        val viewer = viewers.remove(viewerId) ?: ViewerState()
        if (columnId == null) {
            viewer.row = anchor
        } else {
            viewer.columns.remove(columnId)
            viewer.columns[columnId] = anchor
            while (viewer.columns.size > MAX_COLUMNS) viewer.columns.remove(viewer.columns.keys.first())
        }
        viewers[viewerId] = viewer
        trimViewers()
    }

    private fun trimViewers() {
        while (viewers.size > MAX_VIEWERS) {
            val oldest = viewers.keys.first()
            viewers.remove(oldest)
            knownColumns.remove(oldest)
        }
    }

    fun retainColumns(viewerId: String, columnIds: Set<String>) {
        knownColumns[viewerId] = columnIds
        // A viewer can be replaced before composition registers a live reader. Bound its
        // metadata together with snapshots instead of waiting for a later capture().
        val viewer = viewers.remove(viewerId) ?: ViewerState()
        viewer.columns.keys.retainAll(columnIds)
        viewers[viewerId] = viewer
        trimViewers()
    }

    fun save(): Bundle {
        capture()
        return Bundle().apply {
            putStringArrayList("viewers", ArrayList(viewers.keys))
            viewers.forEach { (viewerId, viewer) ->
                putBundle(viewerId, Bundle().apply {
                    viewer.row?.let { putBundle("row", it.toBundle()) }
                    putStringArrayList("columns", ArrayList(viewer.columns.keys))
                    putBundle("anchors", Bundle().apply {
                        viewer.columns.forEach { (columnId, anchor) -> putBundle(columnId, anchor.toBundle()) }
                    })
                })
            }
        }
    }

    fun restore(bundle: Bundle) {
        onUserInput()
        viewers.clear()
        knownColumns.clear()
        bundle.getStringArrayList("viewers")?.takeLast(MAX_VIEWERS)?.forEach { viewerId ->
            val viewer = bundle.getBundle(viewerId) ?: return@forEach
            val state = ViewerState(row = viewer.getBundle("row")?.toAnchor())
            val anchors = viewer.getBundle("anchors")
            viewer.getStringArrayList("columns")?.takeLast(MAX_COLUMNS)?.forEach { columnId ->
                anchors?.getBundle(columnId)?.toAnchor()?.let { state.columns[columnId] = it }
            }
            viewers[viewerId] = state
        }
    }

    private fun BoardScrollAnchor.toBundle() = Bundle().apply {
        putString("id", id)
        putString("next", nextId)
        putString("previous", previousId)
        putInt("index", index)
        putInt("offset", offset)
    }

    private fun Bundle.toAnchor() = BoardScrollAnchor(
        id = getString("id"),
        nextId = getString("next"),
        previousId = getString("previous"),
        index = getInt("index").coerceAtLeast(0),
        offset = getInt("offset")
    )

    companion object {
        private const val MAX_VIEWERS = 16
        private const val MAX_COLUMNS = 64
    }
}

/** Ignore the sticky label when selecting the stable card anchor. */
internal fun columnScrollAnchor(state: LazyListState, cardIds: List<String>): BoardScrollAnchor {
    if (state.firstVisibleItemIndex == 0) {
        return BoardScrollAnchor(null, cardIds.firstOrNull(), null, 0, state.firstVisibleItemScrollOffset)
    }
    val visible = state.layoutInfo.visibleItemsInfo.firstOrNull {
        (it.key as? String)?.removePrefix(BOARD_CARD_KEY_PREFIX) in cardIds
    }
    val cardId = (visible?.key as? String)?.removePrefix(BOARD_CARD_KEY_PREFIX)
    val index = cardIds.indexOf(cardId).takeIf { it >= 0 }
        ?: (state.firstVisibleItemIndex - 1).coerceIn(0, (cardIds.size - 1).coerceAtLeast(0))
    return BoardScrollAnchor(
        id = cardIds.getOrNull(index),
        nextId = cardIds.getOrNull(index + 1),
        previousId = cardIds.getOrNull(index - 1),
        index = index,
        offset = visible?.let { -it.offset } ?: state.firstVisibleItemScrollOffset
    )
}

internal fun rowScrollAnchor(state: LazyListState, columnIds: List<String>): BoardScrollAnchor =
    rowScrollAnchor(
        state.firstVisibleItemIndex,
        state.firstVisibleItemScrollOffset,
        state.layoutInfo.visibleItemsInfo.mapNotNull { item ->
            (item.key as? String)?.let { it to item.offset }
        },
        columnIds
    )

internal fun rowScrollAnchor(
    firstVisibleIndex: Int,
    firstVisibleOffset: Int,
    visibleItemPositions: List<Pair<String, Int>>,
    columnIds: List<String>
): BoardScrollAnchor {
    // The latest model can arrive before LazyRow remeasures. Its old numeric index must
    // not be interpreted against a reordered/filtered model when a measured key survives.
    val visible = visibleItemPositions.firstOrNull { it.first in columnIds }
    val index = visible?.let { columnIds.indexOf(it.first) }
        ?: firstVisibleIndex.coerceIn(0, (columnIds.size - 1).coerceAtLeast(0))
    return BoardScrollAnchor(
        id = columnIds.getOrNull(index),
        nextId = columnIds.getOrNull(index + 1),
        previousId = columnIds.getOrNull(index - 1),
        index = index,
        offset = visible?.let { -it.second } ?: firstVisibleOffset
    )
}

internal const val BOARD_CARD_KEY_PREFIX = "board-card:"
