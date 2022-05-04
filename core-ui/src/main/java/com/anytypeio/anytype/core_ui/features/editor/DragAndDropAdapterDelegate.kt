package com.anytypeio.anytype.core_ui.features.editor

import java.lang.ref.WeakReference

interface DragAndDropSelector{

    fun selectDraggedViewHolder(position: Int)

    fun unSelectDraggedViewHolder()

    fun trySetDesiredAppearanceForDraggedItem(holder: BlockViewHolder, position: Int)

}

class DragAndDropAdapterDelegate : DragAndDropSelector {

    private var draggedItem: DraggedItem = DraggedItem.Default
    private var draggedHolder: WeakReference<BlockViewHolder>? = null

    override fun selectDraggedViewHolder(position: Int) {
        draggedItem = DraggedItem.SelectedDraggedItem(position)
    }

    override fun unSelectDraggedViewHolder() {
        if (draggedItem is DraggedItem.SelectedDraggedItem) {
            draggedItem =
                DraggedItem.UnselectedDraggedItem(draggedItem.position, draggedItem.itemOriginalAlpha)
            draggedHolder?.get()?.let {
                restoreView(it)
            }
        }
    }

    override fun trySetDesiredAppearanceForDraggedItem(holder: BlockViewHolder, position: Int) {
        if (draggedItem !is DraggedItem.Default && position != draggedItem.position) {
            draggedHolder?.get()?.let {
                if (holder == it) {
                    restoreView(it)
                }
            }
        } else if (position == draggedItem.position) {
            if (draggedItem is DraggedItem.SelectedDraggedItem) {
                if (!draggedItem.isAlphaSet()) {
                    draggedHolder = WeakReference(holder)
                    draggedItem = DraggedItem.SelectedDraggedItem(
                        draggedItem.position,
                        holder.itemView.alpha
                    )
                }
                holder.itemView.alpha = DRAGGED_ITEM_ALPHA
            } else if (draggedItem is DraggedItem.UnselectedDraggedItem) {
                restoreView(holder)
            }
        }
    }

    private fun restoreView(holder: BlockViewHolder) {
        holder.itemView.alpha = draggedItem.itemOriginalAlpha
        if (draggedItem is DraggedItem.UnselectedDraggedItem) {
            draggedItem = DraggedItem.Default
        }
    }
}

private sealed class DraggedItem {

    abstract val position: Int
    abstract val itemOriginalAlpha: Float

    fun isAlphaSet(): Boolean {
        return itemOriginalAlpha != NONE
    }

    object Default : DraggedItem() {
        override val position: Int = -1
        override val itemOriginalAlpha: Float = NONE
    }

    data class SelectedDraggedItem(
        override val position: Int,
        override val itemOriginalAlpha: Float = NONE
    ) : DraggedItem()

    data class UnselectedDraggedItem(
        override val position: Int,
        override val itemOriginalAlpha: Float = NONE
    ) : DraggedItem()
}
private const val NONE = -1f
private const val DRAGGED_ITEM_ALPHA = 0.4f