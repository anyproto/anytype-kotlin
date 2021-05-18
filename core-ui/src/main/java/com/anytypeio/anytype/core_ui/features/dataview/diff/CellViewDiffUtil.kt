package com.anytypeio.anytype.core_ui.features.dataview.diff

import androidx.recyclerview.widget.DiffUtil
import com.anytypeio.anytype.presentation.sets.model.CellView
import timber.log.Timber

class CellViewDiffUtil(
    private val old: List<CellView>,
    private val new: List<CellView>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]
        return (oldItem.id == newItem.id).also { Timber.d("areItemsTheSame: $it") }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]
        return oldItem == newItem.also { Timber.d("areContentsTheSame: $it") }
    }

    override fun getOldListSize(): Int = old.size
    override fun getNewListSize(): Int = new.size
}