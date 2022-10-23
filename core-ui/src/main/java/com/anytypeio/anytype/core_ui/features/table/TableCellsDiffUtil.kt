package com.anytypeio.anytype.core_ui.features.table

import androidx.recyclerview.widget.DiffUtil
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

object TableCellsDiffUtil : DiffUtil.ItemCallback<BlockView.Table.Cell>() {

    override fun areItemsTheSame(
        oldItem: BlockView.Table.Cell,
        newItem: BlockView.Table.Cell
    ): Boolean {
        return oldItem.getId() == newItem.getId()
    }

    override fun areContentsTheSame(
        oldItem: BlockView.Table.Cell,
        newItem: BlockView.Table.Cell
    ): Boolean {
        return oldItem.block == newItem.block
    }

    override fun getChangePayload(
        oldItem: BlockView.Table.Cell,
        newItem: BlockView.Table.Cell
    ): Any? {
        val changes = mutableListOf<Int>()

        val oldBlock = oldItem.block
        val newBlock = newItem.block

        if (newBlock?.text != oldBlock?.text) {
            changes.add(TEXT_CHANGED)
        }
        if (newBlock?.color != oldBlock?.color) {
            changes.add(TEXT_COLOR_CHANGED)
        }
        if (newBlock?.background != oldBlock?.background) {
            changes.add(BACKGROUND_COLOR_CHANGED)
        }
        if (newBlock?.marks != oldBlock?.marks) {
            changes.add(MARKUP_CHANGED)
        }
        if (newBlock?.alignment != oldBlock?.alignment) {
            changes.add(ALIGN_CHANGED)
        }

        return if (changes.isEmpty()) {
            super.getChangePayload(oldItem, newItem)
        } else {
            Payload(changes)
        }
    }

    data class Payload(
        val changes: List<Int>
    ) {
        val isBordersChanged: Boolean = changes.contains(SETTING_BORDER_CHANGED)
        val isWidthChanged: Boolean = changes.contains(SETTING_WIDTH_CHANGED)
        val isTextChanged = changes.contains(TEXT_CHANGED)
        val isBackgroundChanged = changes.contains(BACKGROUND_COLOR_CHANGED)
        val isTextColorChanged = changes.contains(TEXT_COLOR_CHANGED)
        val isMarkupChanged = changes.contains(MARKUP_CHANGED)
        val isAlignChanged = changes.contains(ALIGN_CHANGED)
    }

    const val TEXT_CHANGED = 1
    const val TEXT_COLOR_CHANGED = 2
    const val BACKGROUND_COLOR_CHANGED = 3
    const val MARKUP_CHANGED = 4
    const val ALIGN_CHANGED = 5
    const val SETTING_WIDTH_CHANGED = 6
    const val SETTING_BORDER_CHANGED = 7
}