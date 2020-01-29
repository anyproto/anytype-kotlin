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

        // TODO refactoring needed. Return list of changes instead of one change.

        val oldBlock = old[oldItemPosition]
        val newBlock = new[newItemPosition]

        return if (oldBlock is BlockView.Paragraph && newBlock is BlockView.Paragraph) {
            if (oldBlock.text != newBlock.text) {
                if (oldBlock.marks != newBlock.marks)
                    if (oldBlock.color != newBlock.color) {
                        TEXT_MARKUP_AND_COLOR_CHANGED
                    } else {
                        TEXT_AND_MARKUP_CHANGED
                    }
                else if (oldBlock.color != newBlock.color) {
                    TEXT_AND_COLOR_CHANGED
                } else
                    TEXT_CHANGED
            } else {
                when {
                    oldBlock.marks != newBlock.marks && oldBlock.color != newBlock.color -> MARKUP_AND_COLOR_CHANGED
                    oldBlock.focused != newBlock.focused && oldBlock.color != newBlock.color -> FOCUS_AND_COLOR_CHANGED
                    oldBlock.marks != newBlock.marks -> MARKUP_CHANGED
                    oldBlock.focused != newBlock.focused -> FOCUS_CHANGED
                    oldBlock.color != newBlock.color -> TEXT_COLOR_CHANGED
                    else -> throw IllegalStateException("Unexpected change payload scenario:\n$oldBlock\n$newBlock")
                }
            }
        } else if (oldBlock is BlockView.Bulleted && newBlock is BlockView.Bulleted) {
            if (oldBlock.text != newBlock.text) {
                if (oldBlock.marks != newBlock.marks)
                    if (oldBlock.color != newBlock.color) {
                        TEXT_MARKUP_AND_COLOR_CHANGED
                    } else {
                        TEXT_AND_MARKUP_CHANGED
                    }
                else if (oldBlock.color != newBlock.color) {
                    TEXT_AND_COLOR_CHANGED
                } else
                    TEXT_CHANGED
            } else {
                when {
                    oldBlock.marks != newBlock.marks && oldBlock.color != newBlock.color -> MARKUP_AND_COLOR_CHANGED
                    oldBlock.focused != newBlock.focused && oldBlock.color != newBlock.color -> FOCUS_AND_COLOR_CHANGED
                    oldBlock.marks != newBlock.marks -> MARKUP_CHANGED
                    oldBlock.focused != newBlock.focused -> FOCUS_CHANGED
                    oldBlock.color != newBlock.color -> TEXT_COLOR_CHANGED
                    else -> throw IllegalStateException("Unexpected change payload scenario:\n$oldBlock\n$newBlock")
                }
            }
        } else if (oldBlock is BlockView.Checkbox && newBlock is BlockView.Checkbox) {
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
        const val TEXT_COLOR_CHANGED = 4
        const val TEXT_MARKUP_AND_COLOR_CHANGED = 5
        const val TEXT_AND_COLOR_CHANGED = 6
        const val FOCUS_AND_COLOR_CHANGED = 7
        const val MARKUP_AND_COLOR_CHANGED = 8
    }
}