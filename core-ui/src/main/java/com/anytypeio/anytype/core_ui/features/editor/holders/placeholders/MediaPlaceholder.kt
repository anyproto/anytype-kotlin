package com.anytypeio.anytype.core_ui.features.editor.holders.placeholders

import android.view.View
import com.anytypeio.anytype.core_ui.features.page.*
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_utils.ext.PopupExtensions

abstract class MediaPlaceholder(view: View) : BlockViewHolder(view),
    BlockViewHolder.IndentableHolder {

    abstract val root: View
    abstract fun placeholderClick(target: String, clicked: (ListenerType) -> Unit)
    abstract override fun indentize(item: BlockView.Indentable)
    abstract fun select(isSelected: Boolean)

    fun bind(
        item: BlockView.MediaPlaceholder,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        select(item.isSelected)
        with(root) {
            setOnClickListener { placeholderClick(item.id, clicked) }
            setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = { placeholderLongClick(root, it, clicked) }
                )
            )
        }
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.MediaPlaceholder) { "Expected a media placeholder, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                select(item.isSelected)
            }
        }
    }

    private fun placeholderLongClick(root: View, target: String, clicked: (ListenerType) -> Unit) {
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

