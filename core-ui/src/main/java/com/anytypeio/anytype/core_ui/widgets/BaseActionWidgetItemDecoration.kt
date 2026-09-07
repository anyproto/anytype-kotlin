package com.anytypeio.anytype.core_ui.widgets

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.extensions.contentWidth
import com.anytypeio.anytype.core_utils.ext.dp

class BaseActionWidgetItemDecoration(
    private val itemWidth: Int = 72.dp,
    private val minSpaceWidth: Int = 4.dp,
    private val horizontalPadding: Int = 32.dp
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        // The list, not the display, owns the space. The content column is narrower than the
        // window on a tablet and on a phone in landscape.
        val availableWidth = parent.contentWidth() - horizontalPadding
        val totalWidth = (itemWidth * state.itemCount) + (minSpaceWidth * (state.itemCount - 1))

        if (totalWidth < availableWidth) {
            proceedItemsLtScreen(outRect)
        } else {
            proceedItemsGtScreen(position, outRect, state, availableWidth)
        }
    }

    private fun proceedItemsLtScreen(outRect: Rect) {
        outRect.right = minSpaceWidth
        outRect.left = minSpaceWidth
    }

    private fun proceedItemsGtScreen(
        position: Int,
        outRect: Rect,
        state: RecyclerView.State,
        availableWidth: Int
    ) {
        when (position) {
            0 -> {
                outRect.left = minSpaceWidth * 2
            }
            state.itemCount - 1 -> {
                outRect.right = minSpaceWidth * 2
            }
            else -> {
                val space = (availableWidth - itemWidth) / (state.itemCount - 1)

                val leftSpace = space * position
                val rightSpace = space * (state.itemCount - position - 1)
                if (leftSpace < minSpaceWidth / 2) {
                    outRect.left = minSpaceWidth / 2 - leftSpace
                }
                if (rightSpace < minSpaceWidth / 2) {
                    outRect.right = minSpaceWidth / 2 - rightSpace
                }
            }
        }
    }
}