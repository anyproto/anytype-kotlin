package com.anytypeio.anytype.core_ui.features.dataview.diff

import androidx.recyclerview.widget.DiffUtil
import com.anytypeio.anytype.presentation.sets.model.Viewer

class GridRowDiffUtil(
    private val old: List<Viewer.GridView.Row>,
    private val new: List<Viewer.GridView.Row>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean = false

    override fun getOldListSize(): Int = old.size
    override fun getNewListSize(): Int = new.size
}