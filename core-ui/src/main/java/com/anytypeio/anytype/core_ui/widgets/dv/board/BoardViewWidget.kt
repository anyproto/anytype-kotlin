package com.anytypeio.anytype.core_ui.widgets.dv.board

import android.content.Context
import android.util.AttributeSet
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.DataviewScrollCoordinator

/**
 * Hosts the Kanban [BoardScreen] as a single, persistent composition — the same pattern
 * the other data-view viewers use (e.g. `GalleryViewWidget`).
 *
 * The board is fed via [setBoard], which updates state and lets Compose *diff* the new
 * view-state into the existing composition. This replaces calling `setContent` on every
 * view-state emission, which tore the whole composition down — resetting scroll and
 * cancelling any in-flight drag. Callbacks are set once by the host; the composition is
 * never rebuilt from outside.
 */
class BoardViewWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private var boardState by mutableStateOf<Viewer.Board?>(null)
    private val scrollStore = BoardScrollStore()
    private var cancelActiveDrag: () -> Unit = {}
    private var validateActiveDrag: (Viewer.Board) -> Unit = {}
    private var viewportInsetPixels by mutableIntStateOf(0)
    private var bottomContentInsetPixels by mutableIntStateOf(0)

    /** Null in embedded mode: the external header receives the single View interop stream. */
    var scrollCoordinator: DataviewScrollCoordinator? by mutableStateOf(null)

    var onCardClick: (Id) -> Unit = {}
    var onCardMoved: (cardId: Id, sourceColumnId: String, targetColumnId: String, targetOrderedIds: List<Id>?) -> Unit =
        { _, _, _, _ -> }
    var onCardReordered: (columnId: String, orderedCardIds: List<Id>) -> Unit = { _, _ -> }
    var onColumnLoadMore: (columnId: String) -> Unit = {}
    var onCreateInColumn: (String) -> Unit = {}

    /** Whether the current permission allows creating objects; gates the per-column "＋ New" item. */
    // Fail closed: stay hidden until the host sets it from permission state, so a
    // read-only viewer can't briefly see the "+ New" affordance.
    var canCreateObject: Boolean by mutableStateOf(false)

    @Composable
    override fun Content() {
        // The Android owner stays at a fixed screen origin so native Compose velocity
        // tracking sees physical pointer movement. Only this finite inner viewport moves.
        // The board keeps the full height: the bottom strip still scrolls the board, and
        // [BoardScreen] reserves that strip inside each column instead.
        Box(Modifier.fillMaxSize().padding(top = with(LocalDensity.current) { viewportInsetPixels.toDp() })) {
            boardState?.let { current ->
                key(current.scrollKey()) { BoardScreen(
                    board = current,
                    onCardClick = onCardClick,
                    onCardMoved = onCardMoved,
                    onCardReordered = onCardReordered,
                    onColumnLoadMore = onColumnLoadMore,
                    canCreateObject = canCreateObject,
                    onCreateInColumn = onCreateInColumn,
                    scrollCoordinator = scrollCoordinator,
                    scrollStore = scrollStore,
                    bottomContentInset = with(LocalDensity.current) { bottomContentInsetPixels.toDp() },
                    registerDragCancellation = { cancelActiveDrag = it },
                    registerDragValidation = { validateActiveDrag = it }
                ) }
            }
        }
    }

    /** Paired with DataviewScrollHost's fixed Android-origin child geometry. */
    fun setViewportTopInset(pixels: Int) {
        viewportInsetPixels = pixels.coerceAtLeast(0)
    }

    fun setBottomContentInset(pixels: Int) {
        bottomContentInsetPixels = pixels.coerceAtLeast(0)
    }

    fun setBoard(board: Viewer.Board) {
        if (boardStructureChanged(boardState, board)) {
            cancelDrag()
            scrollStore.capture()
            scrollCoordinator?.cancel()
        }
        validateActiveDrag(board)
        // The visible subset cannot distinguish a hidden group from a removed group.
        // Keep bounded snapshots during group loading; prune only against backend membership.
        board.knownColumnIds?.let { scrollStore.retainColumns(board.scrollKey(), it) }
        this.boardState = board
    }

    /** Releases the held board state when another view is shown, so its cards aren't retained. */
    fun clear() {
        if (boardState == null) return
        cancelDrag()
        scrollStore.capture()
        scrollCoordinator?.cancel()
        this.boardState = null
    }

    fun cancelDrag() { cancelActiveDrag() }

    fun saveScrollState(): Bundle = scrollStore.save()

    fun restoreScrollState(state: Bundle) { scrollStore.restore(state) }

    override fun onDetachedFromWindow() {
        cancelDrag()
        scrollStore.capture()
        scrollCoordinator?.cancel()
        super.onDetachedFromWindow()
    }
}
