package com.anytypeio.anytype.core_ui.features.editor.holders.media

import android.view.View
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_utils.ext.PopupExtensions
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

abstract class Media(view: View) : BlockViewHolder(view), BlockViewHolder.IndentableHolder {

    abstract val root: View
    abstract val clickContainer: View
    abstract fun onMediaBlockClicked(item: BlockView.Media, clicked: (ListenerType) -> Unit)
    abstract override fun indentize(item: BlockView.Indentable)
    abstract fun select(isSelected: Boolean)

    fun bind(
        item: BlockView.Media,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        select(item.isSelected)
        with(clickContainer) {
            setOnClickListener { onMediaBlockClicked(item, clicked) }
            setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = { mediaLongClick(root, it, clicked) }
                )
            )
        }
    }

    open fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.Media) { "Expected a media, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                select(item.isSelected)
            }
        }
    }

    private fun mediaLongClick(root: View, target: String, clicked: (ListenerType) -> Unit) {
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