package com.anytypeio.anytype.core_ui.layout

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import kotlin.math.roundToInt

class TableHorizontalItemDivider(
    private val drawable: Drawable
) : RecyclerView.ItemDecoration() {

    override fun onDraw(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        canvas.save()
        val top = 0
        val bottom = parent.height
        val childCount = parent.childCount
        val itemCount = parent.adapter?.itemCount ?: 0
        val rect = Rect()
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildLayoutPosition(child)
            parent.layoutManager?.getDecoratedBoundsWithMargins(child, rect)
            var right = rect.right + child.translationX.roundToInt()
            var left = right - drawable.intrinsicWidth

            if (position < itemCount) {
                drawable.setBounds(left, top, right, bottom)
                drawable.draw(canvas)
            }

            if (position == 0) {
                right = child.left
                left = right - drawable.intrinsicWidth

                drawable.setBounds(left, top, right, bottom)
                drawable.draw(canvas)
            }
        }
        canvas.restore()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildLayoutPosition(view)
        outRect.right = drawable.intrinsicWidth
        if (position == 0) {
            outRect.left = drawable.intrinsicWidth
        }
    }
}