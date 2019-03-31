package com.agileburo.anytype.feature_editor.presentation.util

import androidx.recyclerview.widget.DiffUtil
import com.agileburo.anytype.feature_editor.presentation.model.BlockView

class BlockViewDiffUtil(
    private val old : List<BlockView>,
    private val new : List<BlockView>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition].id == new[newItemPosition].id
    }

    override fun getOldListSize(): Int = old.size
    override fun getNewListSize(): Int = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return new[newItemPosition] == old[oldItemPosition]
    }
}