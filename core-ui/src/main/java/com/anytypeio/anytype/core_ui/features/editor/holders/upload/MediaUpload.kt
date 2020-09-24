package com.anytypeio.anytype.core_ui.features.editor.holders.upload

import android.view.View
import com.anytypeio.anytype.core_ui.features.page.*
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_utils.ext.PopupExtensions

abstract class MediaUpload(view: View) : BlockViewHolder(view),
    BlockViewHolder.IndentableHolder {

    abstract val root: View
    abstract fun uploadClick(target: String, clicked: (ListenerType) -> Unit)
    abstract override fun indentize(item: BlockView.Indentable)
    abstract fun select(isSelected: Boolean)

    fun bind(
        item: BlockView.Upload,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        select(item.isSelected)
        with(itemView) {
            setOnClickListener { uploadClick(item.id, clicked) }
            setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = { uploadLongClick(itemView, it, clicked) }
                )
            )
        }
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.Upload) { "Expected upload block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                select(item.isSelected)
            }
        }
    }

    private fun uploadLongClick(root: View, target: String, clicked: (ListenerType) -> Unit) {
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