package com.anytypeio.anytype.core_ui.features.table.holders

import android.widget.FrameLayout
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
import com.anytypeio.anytype.core_ui.layout.TableHorizontalItemDivider
import com.anytypeio.anytype.core_ui.layout.TableVerticalItemDivider
import com.anytypeio.anytype.core_ui.features.table.TableEditableCellsAdapter
import com.anytypeio.anytype.core_ui.layout.TableCellSelectionDecoration
import com.anytypeio.anytype.core_utils.ext.containsItemDecoration
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.selection.TableCellsSelectionState

class TableBlockHolder(
    binding: ItemBlockTableBinding,
    clickListener: (ListenerType) -> Unit,
    onTextBlockTextChanged: (BlockView.Text) -> Unit,
    onMentionEvent: (MentionEvent) -> Unit,
    onSlashEvent: (SlashEvent) -> Unit,
    onSelectionChanged: (Id, IntRange) -> Unit,
    onFocusChanged: (Id, Boolean) -> Unit
) : BlockViewHolder(binding.root) {

    private val root: FrameLayout = binding.root
    private val recycler: RecyclerView = binding.recyclerTable
    private val selectView = binding.selected

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
        onSlashEvent = onSlashEvent,
        onSelectionChanged = onSelectionChanged,
        onFocusChanged = onFocusChanged
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
        applySelection(item)
        applyBackground(item)
        applyRowCount(item)
        applyCells(item)
        updateCellsSelection(item)
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.Table
    ) {
        payloads.forEach { payload ->
            if (payload.selectionChanged()) applySelection(item)
            if (payload.backgroundColorChanged()) applyBackground(item)
            if (payload.tableRowCountChanged()) applyRowCount(item)
            if (payload.tableCellsChanged()) applyCells(item)
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
        lm.spanCount = item.rowCount
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
            cellsSelectionState.set(selectedCells)
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
}