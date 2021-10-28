package com.anytypeio.anytype.core_ui.features.editor

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Payload
import com.anytypeio.anytype.core_utils.ext.PopupExtensions
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

/**
 * Viewholder for rendering different type of blocks (i.e its UI-models).
 * @see BlockView
 * @see BlockAdapter
 */
open class BlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    interface DragAndDropHolder {
        fun setIsDragged(isDragged: Boolean) {
            // TODO
        }
        fun setIsTargeted(isTargeted: Boolean) {
            // TODO
        }
    }

    interface IndentableHolder {
        fun indentize(item: BlockView.Indentable)
        fun processIndentChange(
            item: BlockView,
            payloads: List<Payload>
        ) {
            for (payload in payloads) {
                if (payload.isIndentChanged && item is BlockView.Indentable)
                    indentize(item)
            }
        }
    }

    fun onBlockLongClick(root: View, target: String, clicked: (ListenerType) -> Unit) {
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
