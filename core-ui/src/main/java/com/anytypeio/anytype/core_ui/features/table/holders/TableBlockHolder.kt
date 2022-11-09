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
import com.anytypeio.anytype.core_ui.features.table.TableBlockAdapter
import com.anytypeio.anytype.core_ui.features.table.TableCellsDiffUtil
import com.anytypeio.anytype.core_ui.layout.TableHorizontalItemDivider
import com.anytypeio.anytype.core_ui.layout.TableVerticalItemDivider
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.features.table.TableEditableCellsAdapter
import com.anytypeio.anytype.core_ui.layout.TableCellSelectionDecoration
import com.anytypeio.anytype.core_utils.ext.containsItemDecoration
import com.anytypeio.anytype.presentation.BuildConfig
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

    val root: FrameLayout = binding.root
    val recycler: RecyclerView = binding.recyclerTable
    private val selected = binding.selected

    private val cellsSelectionState = TableCellsSelectionState()

    private val cellSelectionDecoration: TableCellSelectionDecoration = TableCellSelectionDecoration(
        drawable = binding.root.context.drawable(R.drawable.cell_top_border)
    )

    private val tableAdapter = TableBlockAdapter(
        differ = TableCellsDiffUtil,
        clickListener = clickListener
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

    private val lm = if (BuildConfig.USE_SIMPLE_TABLES_IN_EDITOR_EDDITING) {
        CustomGridLayoutManager(itemView.context, 1, GridLayoutManager.HORIZONTAL, false)
    } else {
        GridLayoutManager(itemView.context, 1, GridLayoutManager.HORIZONTAL, false)
    }

    init {
        val drawable = itemView.context.drawable(R.drawable.divider_dv_grid)
        val verticalDecorator = TableVerticalItemDivider(drawable)
        val horizontalDecorator = TableHorizontalItemDivider(drawable)

        recycler.apply {
            layoutManager = lm
            if (BuildConfig.USE_SIMPLE_TABLES_IN_EDITOR_EDDITING) {
                (lm as CustomGridLayoutManager).spanSizeLookup =
                    object : CustomGridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int = 1
                    }
                adapter = tableEditableCellsAdapter
                setHasFixedSize(true)
            } else {
                (lm as GridLayoutManager).spanSizeLookup =
                    object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int = 1
                    }
                adapter = tableAdapter
            }
            addItemDecoration(verticalDecorator)
            addItemDecoration(horizontalDecorator)
        }
    }

    fun bind(item: BlockView.Table) {
        selected.isSelected = item.isSelected
        if (BuildConfig.USE_SIMPLE_TABLES_IN_EDITOR_EDDITING) {
            (lm as CustomGridLayoutManager).spanCount = item.rowCount
            tableEditableCellsAdapter.setTableBlockId(item.id)
            tableEditableCellsAdapter.updateWithDiffUtil(item.cells)
            updateCellsSelection(item)
        } else {
            (lm as GridLayoutManager).spanCount = item.rowCount
            tableAdapter.setTableBlockId(item.id)
            tableAdapter.submitList(item.cells)
        }
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.Table
    ) {
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.TABLE_CELLS_CHANGED)) {
                bind(item)
            }
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                selected.isSelected = item.isSelected
            }
            if (payload.changes.contains(BlockViewDiffUtil.BACKGROUND_COLOR_CHANGED)) {
                applyBackground(item.background)
            }
            if (payload.changes.contains(BlockViewDiffUtil.TABLE_CELLS_SELECTION_CHANGED)) {
                updateCellsSelection(item)
            }
        }
    }

    private fun applyBackground(background: ThemeColor) {
        root.setBlockBackgroundColor(background)
    }

    fun recycle() {
        tableAdapter.submitList(emptyList())
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
            cellsSelectionState.set(cells = selectedCells)
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