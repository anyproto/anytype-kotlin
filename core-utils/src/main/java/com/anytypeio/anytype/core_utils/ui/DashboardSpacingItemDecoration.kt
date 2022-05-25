package com.anytypeio.anytype.core_utils.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class DashboardSpacingItemDecoration(
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % SPAN_COUNT

        if (column == 0) {
            outRect.left = spacing - column * spacing / SPAN_COUNT
            outRect.right = ((column + 1) * spacing / SPAN_COUNT) / 2
        }

        if (column > 0) {
            outRect.left = (spacing - column * spacing / SPAN_COUNT) / 2
            outRect.right = (column + 1) * spacing / SPAN_COUNT
        }

        outRect.bottom = spacing / 2
    }

    companion object {
        const val SPAN_COUNT = 2
    }
}
