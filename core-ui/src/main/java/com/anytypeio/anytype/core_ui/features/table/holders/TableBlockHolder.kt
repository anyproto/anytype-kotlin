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
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent

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
                        override fun getSpanSize(position: Int): Int {
                            return when (recycler.adapter?.getItemViewType(position)) {
                                TableEditableCellsAdapter.TYPE_CELL, TableEditableCellsAdapter.TYPE_EMPTY -> 1
                                else -> lm.spanCount
                            }
                        }
                    }
                adapter = tableEditableCellsAdapter
                setHasFixedSize(true)
            } else {
                (lm as GridLayoutManager).spanSizeLookup =
                    object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return when (recycler.adapter?.getItemViewType(position)) {
                                TableBlockAdapter.TYPE_CELL -> 1
                                else -> lm.spanCount
                            }
                        }
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