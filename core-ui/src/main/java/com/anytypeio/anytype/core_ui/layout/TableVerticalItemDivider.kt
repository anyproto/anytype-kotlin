package com.anytypeio.anytype.core_ui.layout

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.CustomGridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.presentation.BuildConfig
import kotlin.math.roundToInt

class TableVerticalItemDivider(
    private val drawable: Drawable
) : RecyclerView.ItemDecoration() {

    override fun onDraw(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        canvas.save()
        val spanCount = getSpanCount(parent)
        val childCount = parent.childCount
        val itemCount = parent.adapter?.itemCount ?: 0
        val rect = Rect()
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildLayoutPosition(child)
            parent.getDecoratedBoundsWithMargins(child, rect)
            var bottom = rect.bottom + child.translationY.roundToInt()
            var top = bottom - drawable.intrinsicHeight
            if (position < itemCount - 1) {
                drawable.setBounds(rect.left, top, rect.right, bottom)
                drawable.draw(canvas)
            }

            if (position.rem(spanCount) == 0 && position < itemCount - 1) {
                bottom = child.top
                top = bottom - drawable.intrinsicHeight
                drawable.setBounds(rect.left, top, rect.right, bottom)
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
        val spanCount = getSpanCount(parent)
        val itemCount = parent.adapter?.itemCount ?: 0
        val position = parent.getChildLayoutPosition(view)
        outRect.bottom = drawable.intrinsicHeight
        if (position.rem(spanCount) == 0 && position < itemCount - 1) {
            outRect.top = drawable.intrinsicHeight
        }
    }

    private fun getSpanCount(parent: RecyclerView): Int {
        return if (BuildConfig.USE_SIMPLE_TABLES_IN_EDITOR_EDDITING) {
            (parent.layoutManager as CustomGridLayoutManager).spanCount
        } else {
            (parent.layoutManager as GridLayoutManager).spanCount
        }
    }
}