package com.agileburo.anytype.core_ui.features.page

import androidx.recyclerview.widget.DiffUtil
import com.agileburo.anytype.core_ui.common.Focusable
import com.agileburo.anytype.core_ui.common.Markup

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

        if (newBlock::class != oldBlock::class)
            return super.getChangePayload(oldItemPosition, newItemPosition)

        val changes = mutableListOf<Int>()

        if (newBlock is BlockView.Title && oldBlock is BlockView.Title) {
            if (newBlock.text != oldBlock.text)
                changes.add(TEXT_CHANGED)
        }

        if (newBlock is BlockView.Text && oldBlock is BlockView.Text) {
            if (newBlock.text != oldBlock.text)
                changes.add(TEXT_CHANGED)
            if (newBlock.color != oldBlock.color)
                changes.add(TEXT_COLOR_CHANGED)
        }

        if (newBlock is Markup && oldBlock is Markup) {
            if (newBlock.marks != oldBlock.marks)
                changes.add(MARKUP_CHANGED)
        }

        if (newBlock is Focusable && oldBlock is Focusable) {
            if (newBlock.focused != oldBlock.focused)
                changes.add(FOCUS_CHANGED)
        }

        return if (changes.isNotEmpty())
            Payload(changes)
        else
            super.getChangePayload(oldItemPosition, newItemPosition)
    }

    /**
     * Payload of changes to apply to a block view.
     */
    data class Payload(
        val changes: List<Int>
    )

    companion object {
        const val TEXT_CHANGED = 0
        const val MARKUP_CHANGED = 1
        const val FOCUS_CHANGED = 3
        const val TEXT_COLOR_CHANGED = 4
    }
}