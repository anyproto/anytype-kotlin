package com.anytypeio.anytype.core_ui.features.table.holders

import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.CustomGridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableBinding
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.layout.TableHorizontalItemDivider
import com.anytypeio.anytype.core_ui.layout.TableVerticalItemDivider
import com.anytypeio.anytype.core_ui.features.table.TableEditableCellsAdapter
import com.anytypeio.anytype.core_ui.layout.TableCellSelectionDecoration
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.core_utils.ext.containsItemDecoration
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.selection.TableCellsSelectionState

class TableBlockHolder(
    binding: ItemBlockTableBinding,
    clickListener: (ListenerType) -> Unit,
    onTextBlockTextChanged: (BlockView.Text) -> Unit,
    onMentionEvent: (MentionEvent) -> Unit,
    onCellSelectionChanged: (Id, IntRange) -> Unit,
    onFocusChanged: (Id, Boolean) -> Unit,
    clipboardInterceptor: ClipboardInterceptor,
    onDragAndDropTrigger: (RecyclerView.ViewHolder, event: MotionEvent?) -> Boolean
) : BlockViewHolder(binding.root),
    BlockViewHolder.DragAndDropHolder,
    DecoratableViewHolder {

    private val root: FrameLayout = binding.root
    private val recycler: RecyclerView = binding.recyclerTable
    private val container: NestedScrollView = binding.container
    private val selectView = binding.selected
    override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

    private val cellsSelectionState = TableCellsSelectionState()

    private val cellSelectionDecoration: TableCellSelectionDecoration =
        TableCellSelectionDecoration(
            drawable = binding.root.context.drawable(R.drawable.cell_top_border)
        )

    private val tableEditableCellsAdapter = TableEditableCellsAdapter(
        items = listOf(),
        clicked = clickListener,
        onTextBlockTextChanged = onTextBlockTextChanged,
        onMentionEvent = onMentionEvent,
        onCellSelectionChanged = onCellSelectionChanged,
        onFocusChanged = onFocusChanged,
        clipboardInterceptor = clipboardInterceptor,
        onDragAndDropTrigger = { _, event -> onDragAndDropTrigger(this, event) }
    )

    private val lm =
        CustomGridLayoutManager(itemView.context, 1, GridLayoutManager.HORIZONTAL, false).apply {
            spanSizeLookup = object : CustomGridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = 1
            }
        }

    init {
        val drawable = itemView.context.drawable(R.drawable.divider_dv_grid)
        val verticalDecorator = TableVerticalItemDivider(drawable)
        val horizontalDecorator = TableHorizontalItemDivider(drawable)

        recycler.apply {
            layoutManager = lm
            adapter = tableEditableCellsAdapter
            setHasFixedSize(true)
            addItemDecoration(verticalDecorator)
            addItemDecoration(horizontalDecorator)
        }
    }

    fun bind(item: BlockView.Table) {
        applyRowCount(item)
        applySelection(item)
        applyBackground(item)
        applyCells(item)
        updateCellsSelection(item)
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.Table
    ) {
        payloads.forEach { payload ->
            if (payload.tableCellsChanged()) applyCells(item)
            if (payload.selectionChanged()) applySelection(item)
            if (payload.backgroundColorChanged()) applyBackground(item)
            if (payload.tableCellsSelectionChanged()) updateCellsSelection(item)
        }
    }

    private fun applyBackground(item: BlockView.Table) {
        root.setBlockBackgroundColor(item.background)
    }

    private fun applySelection(item: BlockView.Table) {
        selectView.isSelected = item.isSelected
    }

    private fun applyCells(item: BlockView.Table) {
        tableEditableCellsAdapter.updateWithDiffUtil(item.cells)
    }

    private fun applyRowCount(item: BlockView.Table) {
        lm.spanCount = item.rows.size
    }

    private fun updateCellsSelection(item: BlockView.Table) {
        if (item.selectedCellsIds.isEmpty()) {
            cellsSelectionState.clear()
            if (recycler.containsItemDecoration(cellSelectionDecoration)) {
                recycler.removeItemDecoration(cellSelectionDecoration)
            }
        } else {
            val selectedCells = item.cells.filter { cell ->
                item.selectedCellsIds.contains(cell.getId())
            }
            cellsSelectionState.clear()
            cellsSelectionState.set(cells = selectedCells, rowsSize = item.rows.size)
            if (cellsSelectionState.current().isNotEmpty()) {
                cellSelectionDecoration.setSelectionState(cellsSelectionState.current())
                if (!recycler.containsItemDecoration(cellSelectionDecoration)) {
                    recycler.addItemDecoration(cellSelectionDecoration)
                } else {
                    recycler.invalidateItemDecorations()
                }
            } else {
                recycler.removeItemDecoration(cellSelectionDecoration)
            }
        }
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        decoratableContainer.decorate(decorations) { rect ->
            container.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = dimen(R.dimen.dp_8) + rect.left
                marginEnd = dimen(R.dimen.dp_8) + rect.right
                bottomMargin = rect.bottom + dimen(R.dimen.item_block_table_margin_bottom)
            }
            selectView.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = container.marginStart
                marginEnd = container.marginEnd
                topMargin = container.marginTop
                bottomMargin = container.marginBottom
            }
        }
    }
}