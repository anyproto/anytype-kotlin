package com.anytypeio.anytype.core_ui.features.search

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter
import kotlin.math.roundToInt

class ObjectSearchDividerItemDecoration(
    context: Context,
    orientation: Int,
    private val drawableDivider: Drawable
) : DividerItemDecoration(context, orientation) {

    private val mBounds = Rect()

    override fun getDrawable() = drawableDivider

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawVertical(c, parent)
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()

        val left = 0
        val right: Int = parent.width
        val childCount = parent.childCount
        val startPosition =
            if (parent.getChildViewHolder(parent.getChildAt(0)) is DefaultObjectViewAdapter.RecentlyOpenedHolder) {
                POSITION_HEADER
            } else {
                POSITION_DATA
            }

        for (i in startPosition until childCount) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, mBounds)
            val bottom = mBounds.bottom + child.translationY.roundToInt()
            val top = bottom - drawableDivider.intrinsicHeight
            drawableDivider.setBounds(left, top, right, bottom)
            drawableDivider.draw(canvas)
        }

        canvas.restore()
    }

}

private const val POSITION_HEADER = 1
private const val POSITION_DATA = 0