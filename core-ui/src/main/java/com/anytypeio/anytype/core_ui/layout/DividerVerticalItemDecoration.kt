package com.anytypeio.anytype.core_ui.layout

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView

class DividerVerticalItemDecoration(
    divider: Drawable,
    isShowInLastItem: Boolean
) : RecyclerView.ItemDecoration() {

    private val mIsShowInLastItem: Boolean = isShowInLastItem
    private val mDiv: Drawable = divider

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val divLeft = parent.paddingLeft
        val divRight = parent.width - parent.paddingRight

        val childCount = if (mIsShowInLastItem) {
            parent.childCount - 1
        } else {
            parent.childCount - 2
        }

        for (i in 0..childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val divTop = child.bottom + params.bottomMargin
            val divBottom = divTop + (mDiv.intrinsicHeight ?: 0)
            mDiv.setBounds(divLeft, divTop, divRight, divBottom)
            mDiv.draw(c)
        }
    }
}