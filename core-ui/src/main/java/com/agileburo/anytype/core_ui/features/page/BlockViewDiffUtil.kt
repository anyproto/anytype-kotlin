package com.agileburo.anytype.core_ui.features.page

import androidx.recyclerview.widget.DiffUtil

class BlockViewDiffUtil(
    private val old: List<BlockView>,
    private val new: List<BlockView>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = new[newItemPosition].id == old[oldItemPosition].id

    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = new[newItemPosition] == old[oldItemPosition]
}