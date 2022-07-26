package com.anytypeio.anytype.core_ui.features.table

import androidx.recyclerview.widget.DiffUtil
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

object TableCellsDiffUtil : DiffUtil.ItemCallback<BlockView.Table.Cell>() {

    override fun areItemsTheSame(
        oldItem: BlockView.Table.Cell,
        newItem: BlockView.Table.Cell
    ): Boolean {
        if (oldItem is BlockView.Table.Cell.Empty && newItem is BlockView.Table.Cell.Empty) {
            return oldItem.rowId == newItem.rowId && oldItem.columnId == newItem.columnId
        }
        if (oldItem is BlockView.Table.Cell.Empty && newItem is BlockView.Table.Cell.Text) {
            return oldItem.rowId == newItem.rowId && oldItem.columnId == newItem.columnId
        }
        if (oldItem is BlockView.Table.Cell.Text && newItem is BlockView.Table.Cell.Text) {
            return oldItem.rowId == newItem.rowId && oldItem.columnId == newItem.columnId
        }
        return false
    }

    override fun areContentsTheSame(
        oldItem: BlockView.Table.Cell,
        newItem: BlockView.Table.Cell
    ): Boolean {
        if (oldItem is BlockView.Table.Cell.Empty && newItem is BlockView.Table.Cell.Empty) {
            return oldItem.settings == newItem.settings
        }
        if (oldItem is BlockView.Table.Cell.Empty && newItem is BlockView.Table.Cell.Text) {
            return false
        }
        if (oldItem is BlockView.Table.Cell.Text && newItem is BlockView.Table.Cell.Text) {
            return oldItem.block == newItem.block && oldItem.settings == newItem.settings
        }
        return false
    }

    override fun getChangePayload(
        oldItem: BlockView.Table.Cell,
        newItem: BlockView.Table.Cell
    ): Any? {
        val changes = mutableListOf<Int>()
        var oldSettings: BlockView.Table.CellSettings? = null
        var newSettings: BlockView.Table.CellSettings? = null

        if (oldItem is BlockView.Table.Cell.Empty && newItem is BlockView.Table.Cell.Empty) {
            oldSettings = oldItem.settings
            newSettings = newItem.settings
        }

        if (oldItem is BlockView.Table.Cell.Empty && newItem is BlockView.Table.Cell.Text) {
            oldSettings = oldItem.settings
            newSettings = newItem.settings
        }

        if (oldItem is BlockView.Table.Cell.Text && newItem is BlockView.Table.Cell.Text) {
            val oldBlock = oldItem.block
            val newBlock = newItem.block
            oldSettings = oldItem.settings
            newSettings = newItem.settings
            if (newBlock.text != oldBlock.text) {
                changes.add(TEXT_CHANGED)
            }
            if (newBlock.color != oldBlock.color) {
                changes.add(TEXT_COLOR_CHANGED)
            }
            if (newBlock.backgroundColor != oldBlock.backgroundColor) {
                changes.add(BACKGROUND_COLOR_CHANGED)
            }
            if (newBlock.marks != oldBlock.marks) {
                changes.add(MARKUP_CHANGED)
            }
            if (newBlock.alignment != oldBlock.alignment) {
                changes.add(ALIGN_CHANGED)
            }
        }

        if (oldSettings != null && newSettings != null) {
            if (oldSettings.width != newSettings.width) {
                changes.add(SETTING_WIDTH_CHANGED)
            }
            if (oldSettings.left != newSettings.left
                || oldSettings.top != newSettings.top
                || oldSettings.right != newSettings.right
                || oldSettings.bottom != newSettings.bottom
            ) {
                changes.add(SETTING_BORDER_CHANGED)
            }
            if (oldSettings.isHeader != newSettings.isHeader) {
                changes.add(BACKGROUND_COLOR_CHANGED)
            }
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