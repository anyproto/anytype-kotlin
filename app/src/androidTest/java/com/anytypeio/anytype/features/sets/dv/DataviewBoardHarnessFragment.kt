package com.anytypeio.anytype.features.sets.dv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.FrameLayout
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.widgets.dv.board.BoardViewWidget
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.DataviewScrollHost
import com.anytypeio.anytype.presentation.sets.model.Viewer

/** Production BoardViewWidget with deterministic models and no backend dependencies. */
class DataviewBoardHarnessFragment : Fragment() {
    lateinit var host: DataviewScrollHost
    lateinit var board: BoardViewWidget
    lateinit var header: TextView
    lateinit var model: Viewer.Board
    val pageRequests = mutableListOf<Int>()
    var persistedMoves = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val count = arguments?.getInt(ARG_COUNT, 100) ?: 100
        model = Viewer.Board("runtime-viewer", "Runtime board", listOf(column(count)))
        header = TextView(requireContext()).apply { text = "Runtime object header" }
        board = BoardViewWidget(requireContext()).apply {
            tag = BOARD_TAG
            canCreateObject = false
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            onCardMoved = { _, _, _, _ -> persistedMoves++ }
            onCardReordered = { _, _ -> persistedMoves++ }
            onColumnLoadMore = {
                val loaded = model.columns.single().cards.size
                pageRequests.add(loaded)
                if (arguments?.getBoolean(ARG_PAGINATE) == true && loaded < 3) {
                    model = model.copy(columns = listOf(column(loaded + 1, total = 3)))
                    setBoard(model)
                }
            }
        }
        host = DataviewScrollHost(requireContext()).apply {
            pinHeight = px(56)
            addView(header, ViewGroup.LayoutParams(-1, px(240)))
            addView(TextView(context).apply { text = "Runtime controls" }, ViewGroup.LayoutParams(-1, px(48)))
            addView(FrameLayout(context).apply { addView(board, ViewGroup.LayoutParams(-1, -1)) }, ViewGroup.LayoutParams(-1, -1))
            excludeNestedScrollTarget = board
            onGeometryChanging = { board.cancelDrag() }
        }
        board.scrollCoordinator = host.coordinator
        host.setStableViewportChild(board, board::setViewportTopInset)
        if (arguments?.getBoolean(ARG_PAGINATE) == true) model = model.copy(columns = listOf(column(count, total = 3)))
        board.setBoard(model)
        return host
    }

    fun column(count: Int, total: Int = count) = Viewer.Board.Column(
        id = COLUMN_ID,
        label = "Runtime column A",
        cards = (0 until count).map { card("A-$it") },
        count = total
    )

    fun card(id: String) = Viewer.Board.Card(id, "Runtime card $id", ObjectIcon.None, emptyList(), true)
    fun px(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    fun anchor(): Bundle? = board.saveScrollState().getBundle(model.id)?.getBundle("anchors")?.getBundle(COLUMN_ID)

    companion object {
        const val BOARD_TAG = "production-board-runtime"
        const val COLUMN_ID = "runtime-column-a"
        const val ARG_COUNT = "card-count"
        const val ARG_PAGINATE = "paginate"
    }
}
