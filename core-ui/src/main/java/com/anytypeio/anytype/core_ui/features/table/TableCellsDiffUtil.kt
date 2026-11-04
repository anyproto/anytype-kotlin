package com.anytypeio.anytype.core_ui.features.table

import androidx.recyclerview.widget.DiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.Focusable
import timber.log.Timber

class TableCellsDiffUtil(
    private val old: List<BlockView.Table.Cell>,
    private val new: List<BlockView.Table.Cell>
) : DiffUtil.Callback() {

    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val newItem = new[newItemPosition]
        val oldItem = old[oldItemPosition]
        return newItem.getId() == oldItem.getId()
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]
        return oldItem.block == newItem.block && oldItem.column.width == newItem.column.width
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {

        val oldBlock = old[oldItemPosition].block
        val newBlock = new[newItemPosition].block

        val changes = mutableListOf<Int>()

        if (old[oldItemPosition].column.width != new[newItemPosition].column.width) {
            changes.add(BlockViewDiffUtil.TABLE_CELL_WIDTH_CHANGED)
        }

        if (oldBlock == null && newBlock == null) {
            return if (changes.isNotEmpty())
                BlockViewDiffUtil.Payload(changes)
            else
                super.getChangePayload(oldItemPosition, newItemPosition)
        }

        if (oldBlock == null && newBlock != null) {
            changes.add(BlockViewDiffUtil.TEXT_CHANGED)
            changes.add(BlockViewDiffUtil.TEXT_COLOR_CHANGED)
            changes.add(BlockViewDiffUtil.BACKGROUND_COLOR_CHANGED)
            changes.add(BlockViewDiffUtil.MARKUP_CHANGED)
            changes.add(BlockViewDiffUtil.FOCUS_CHANGED)
            changes.add(BlockViewDiffUtil.CURSOR_CHANGED)
            changes.add(BlockViewDiffUtil.ALIGNMENT_CHANGED)
            return BlockViewDiffUtil.Payload(changes)
        }

        if (newBlock is BlockView.TextSupport && oldBlock is BlockView.TextSupport) {
            if (newBlock.text != oldBlock.text)
                changes.add(BlockViewDiffUtil.TEXT_CHANGED)
            if (newBlock.color != oldBlock.color)
                changes.add(BlockViewDiffUtil.TEXT_COLOR_CHANGED)
            if (newBlock.background != oldBlock.background) {
                changes.add(BlockViewDiffUtil.BACKGROUND_COLOR_CHANGED)
            }
        }

        if (newBlock is Markup && oldBlock is Markup) {
            if (newBlock.marks != oldBlock.marks)
                changes.add(BlockViewDiffUtil.MARKUP_CHANGED)
        }

        if (newBlock is Focusable && oldBlock is Focusable) {
            if (newBlock.isFocused != oldBlock.isFocused)
                changes.add(BlockViewDiffUtil.FOCUS_CHANGED)
        }

        if (newBlock is BlockView.Cursor && oldBlock is BlockView.Cursor) {
            if (newBlock.cursor != oldBlock.cursor)
                changes.add(BlockViewDiffUtil.CURSOR_CHANGED)
        }

        if (newBlock is BlockView.Permission && oldBlock is BlockView.Permission) {
            if (newBlock.mode != oldBlock.mode)
                changes.add(BlockViewDiffUtil.READ_WRITE_MODE_CHANGED)
        }

        if (newBlock is BlockView.Alignable && oldBlock is BlockView.Alignable) {
            if (newBlock.alignment != oldBlock.alignment)
                changes.add(BlockViewDiffUtil.ALIGNMENT_CHANGED)
        }

        return if (changes.isNotEmpty())
            BlockViewDiffUtil.Payload(changes).also { Timber.d("Returning TableCellsDiffUtil payload: $it") }
        else
            super.getChangePayload(oldItemPosition, newItemPosition)
    }
}