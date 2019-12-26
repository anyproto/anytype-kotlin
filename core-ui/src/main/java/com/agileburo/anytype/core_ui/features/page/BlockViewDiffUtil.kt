package com.agileburo.anytype.core_ui.features.page

import androidx.recyclerview.widget.DiffUtil

class BlockViewDiffUtil(
    private val old: List<BlockView>,
    private val new: List<BlockView>
) : DiffUtil.Callback() {

    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = new[newItemPosition].id == old[oldItemPosition].id

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = new[newItemPosition] == old[oldItemPosition]

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {

        val oldBlock = old[oldItemPosition]
        val newBlock = new[newItemPosition]

        return if (oldBlock is BlockView.Text && newBlock is BlockView.Text) {
            if (oldBlock.text != newBlock.text) {
                if (oldBlock.marks != newBlock.marks)
                    TEXT_AND_MARKUP_CHANGED
                else
                    TEXT_CHANGED
            } else {
                when {
                    oldBlock.marks != newBlock.marks -> MARKUP_CHANGED
                    oldBlock.focused != newBlock.focused -> FOCUS_CHANGED
                    else -> throw IllegalStateException("Unexpected change payload scenario:\n$oldBlock\n$newBlock")
                }
            }
        } else
            super.getChangePayload(oldItemPosition, newItemPosition)
    }

    companion object {
        const val TEXT_CHANGED = 0
        const val MARKUP_CHANGED = 1
        const val TEXT_AND_MARKUP_CHANGED = 2
        const val FOCUS_CHANGED = 3
    }
}