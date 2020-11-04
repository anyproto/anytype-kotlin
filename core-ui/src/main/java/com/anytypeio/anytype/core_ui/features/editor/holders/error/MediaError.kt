package com.anytypeio.anytype.core_ui.features.editor.holders.error

import android.view.View
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_utils.ext.PopupExtensions
import com.anytypeio.anytype.presentation.page.editor.BlockDimensions
import com.anytypeio.anytype.presentation.page.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.page.editor.model.BlockView

abstract class MediaError(view: View) : BlockViewHolder(view),
    BlockViewHolder.IndentableHolder {

    abstract val root: View
    abstract fun errorClick(item: BlockView.Error, clicked: (ListenerType) -> Unit)
    abstract override fun indentize(item: BlockView.Indentable)
    abstract fun select(isSelected: Boolean)

    fun bind(
        item: BlockView.Error,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        select(item.isSelected)
        with(itemView) {
            setOnClickListener { errorClick(item, clicked) }
            setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = { errorLongClick(itemView, it, clicked) }
                )
            )
        }
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.Error) { "Expected error block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                itemView.isSelected = item.isSelected
            }
        }
    }

    private fun errorLongClick(root: View, target: String, clicked: (ListenerType) -> Unit) {
        val rect = PopupExtensions.calculateRectInWindow(root)
        val dimensions = BlockDimensions(
            left = rect.left,
            top = rect.top,
            bottom = rect.bottom,
            right = rect.right,
            height = root.height,
            width = root.width
        )
        clicked(ListenerType.LongClick(target, dimensions))
    }
}