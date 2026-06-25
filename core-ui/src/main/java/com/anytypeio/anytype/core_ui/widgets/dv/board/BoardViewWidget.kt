package com.anytypeio.anytype.core_ui.widgets.dv.board

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.sets.model.Viewer

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

    var onCardClick: (Id) -> Unit = {}
    var onCardMoved: (cardId: Id, sourceColumnId: String, targetColumnId: String) -> Unit =
        { _, _, _ -> }
    var onCardReordered: (columnId: String, orderedCardIds: List<Id>) -> Unit = { _, _ -> }

    @Composable
    override fun Content() {
        boardState?.let { current ->
            BoardScreen(
                board = current,
                onCardClick = onCardClick,
                onCardMoved = onCardMoved,
                onCardReordered = onCardReordered
            )
        }
    }

    fun setBoard(board: Viewer.Board) {
        this.boardState = board
    }
}
