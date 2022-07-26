package com.anytypeio.anytype.core_ui.widgets.toolbar.table

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemSimpleTableWidgetRecentBinding

class SimpleTableSettingAdapter(
    private val cellAdapter: SimpleTableWidgetAdapter,
    private val columnAdapter: SimpleTableWidgetAdapter,
    private val rowAdapter: SimpleTableWidgetAdapter
) : RecyclerView.Adapter<SimpleTableSettingAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSimpleTableWidgetRecentBinding.inflate(inflater, parent, false)
        return when (viewType) {
            TYPE_CELL -> {
                VH.Cell(binding).apply {
                    binding.recyclerCell.apply {
                        layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        setHasFixedSize(true)
                        adapter = cellAdapter
                    }
                }
            }
            TYPE_COLUMN -> {
                VH.Column(binding).apply {
                    binding.recyclerCell.apply {
                        layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        setHasFixedSize(true)
                        adapter = columnAdapter
                    }
                }
            }
            TYPE_ROW -> {
                VH.Row(binding).apply {
                    binding.recyclerCell.apply {
                        layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        setHasFixedSize(true)
                        adapter = rowAdapter
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {}
    override fun getItemCount(): Int = DEFAULT_TABS_COUNT

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_CELL
        1 -> TYPE_COLUMN
        2 -> TYPE_ROW
        else -> throw IllegalStateException("Unexpected position: $position")
    }

    companion object {
        const val DEFAULT_TABS_COUNT = 3
        const val TYPE_CELL = 1
        const val TYPE_COLUMN = 2
        const val TYPE_ROW = 3
    }

    sealed class VH(view: View) : RecyclerView.ViewHolder(view) {
        class Cell(val binding: ItemSimpleTableWidgetRecentBinding) : VH(binding.root)
        class Column(val binding: ItemSimpleTableWidgetRecentBinding) : VH(binding.root)
        class Row(val binding: ItemSimpleTableWidgetRecentBinding) : VH(binding.root)
    }
}