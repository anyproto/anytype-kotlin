package com.anytypeio.anytype.core_ui.layout

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Item decoration that adds empty space between items.
 */
class SpacingItemDecoration(
    private val spacingStart: Int = 0,
    private val spacingEnd: Int = 0,
    private val spacingTop: Int = 0,
    private val spacingBottom: Int = 0,
    private val firstItemSpacingStart: Int? = null,
    private val lastItemSpacingEnd: Int? = null,
    private val firstItemSpacingTop: Int? = null
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        outRect.left = spacingStart
        outRect.right = spacingEnd
        outRect.bottom = spacingBottom
        outRect.top = spacingTop

        val position = parent.getChildAdapterPosition(view)

        if (firstItemSpacingStart != null && position == 0) {
            outRect.left = firstItemSpacingStart
        }

        if (lastItemSpacingEnd != null && position == parent.adapter?.itemCount?.dec()) {
            outRect.right = lastItemSpacingEnd
        }

        if (firstItemSpacingTop != null && position == 0) {
            outRect.top = firstItemSpacingTop
        }
    }
}