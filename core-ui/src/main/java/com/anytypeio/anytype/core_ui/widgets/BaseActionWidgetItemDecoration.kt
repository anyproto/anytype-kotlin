package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_utils.ext.dp

class BaseActionWidgetItemDecoration(
    private val context: Context,
    private val itemWidth: Int = 72.dp,
    private val minSpaceWidth: Int = 4.dp,
    private val horizontalPadding: Int = 32.dp
) : RecyclerView.ItemDecoration() {

    private val screenWidth = screenWidth() - horizontalPadding

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val totalWidth = (itemWidth * state.itemCount) + (minSpaceWidth * (state.itemCount - 1))

        if (totalWidth < screenWidth) {
            proceedItemsLtScreen(outRect)
        } else {
            proceedItemsGtScreen(position, outRect, state, screenWidth)
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
        screenWidth: Int
    ) {
        when (position) {
            0 -> {
                outRect.left = minSpaceWidth * 2
            }
            state.itemCount - 1 -> {
                outRect.right = minSpaceWidth * 2
            }
            else -> {
                val space = (screenWidth - itemWidth) / (state.itemCount - 1)

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
    private fun screenWidth(): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return displayMetrics.widthPixels
    }

}