package com.anytypeio.anytype.core_ui.features.dataview.diff

import androidx.recyclerview.widget.DiffUtil
import com.anytypeio.anytype.presentation.sets.model.ColumnView

class ColumnViewDiffUtil(
    private val old: List<ColumnView>,
    private val new: List<ColumnView>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]
        return (oldItem.key == newItem.key)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]
        return oldItem == newItem
    }

    override fun getOldListSize(): Int = old.size
    override fun getNewListSize(): Int = new.size
}