package com.anytypeio.anytype.core_ui.features.table.holders

import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager2
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.ThemeColor
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
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class TableBlockHolder(
    binding: ItemBlockTableBinding,
    clickListener: (ListenerType) -> Unit
) : BlockViewHolder(binding.root) {

    val root: FrameLayout = binding.root
    val recycler: RecyclerView = binding.recyclerTable
    private val selected = binding.selected

    private val tableAdapter = TableBlockAdapter(
        differ = TableCellsDiffUtil,
        clickListener = clickListener
    )
    private val lm = GridLayoutManager2(itemView.context, 1, GridLayoutManager.HORIZONTAL, false)

    private val mSpanSizeLookup = object : GridLayoutManager2.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return when (recycler.adapter?.getItemViewType(position)) {
                TableBlockAdapter.TYPE_CELL -> 1
                else -> lm.spanCount
            }
        }
    }

    init {
        val drawable = itemView.context.drawable(R.drawable.divider_dv_grid)
        val verticalDecorator = TableVerticalItemDivider(drawable)
        val horizontalDecorator = TableHorizontalItemDivider(drawable)

        recycler.apply {
            layoutManager = lm
            lm.spanSizeLookup = mSpanSizeLookup
            adapter = tableAdapter
            addItemDecoration(verticalDecorator)
            addItemDecoration(horizontalDecorator)
            setHasFixedSize(true)
        }
    }

    fun bind(item: BlockView.Table) {
        selected.isSelected = item.isSelected
        lm.spanCount = item.rowCount
        tableAdapter.setTableBlockId(item.id)
        tableAdapter.submitList(item.cells)
        recycler.postDelayed(lm::clearBorders, 300)
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.Table
    ) {
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                selected.isSelected = item.isSelected
            }
            if (payload.changes.contains(BlockViewDiffUtil.BACKGROUND_COLOR_CHANGED)) {
                applyBackground(item.background)
            }
        }
    }

    private fun applyBackground(background: ThemeColor) {
        root.setBlockBackgroundColor(background)
    }

    fun recycle() {
        tableAdapter.submitList(emptyList())
    }
}