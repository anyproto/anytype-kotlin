package com.agileburo.anytype.core_ui.features.page.scrollandmove

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.features.page.SupportNesting
import com.agileburo.anytype.core_ui.features.page.scrollandmove.ScrollAndMoveTargetDescriptor.Companion.END_RANGE
import com.agileburo.anytype.core_ui.features.page.scrollandmove.ScrollAndMoveTargetDescriptor.Companion.INNER_RANGE
import com.agileburo.anytype.core_ui.features.page.scrollandmove.ScrollAndMoveTargetDescriptor.Companion.START_RANGE

class ScrollAndMoveTargetHighlighter(
    private val screen: Point,
    private val targeted: Drawable,
    private val disabled: Drawable,
    private val line: Drawable,
    private val padding: Int,
    private val descriptor: ScrollAndMoveTargetDescriptor
) : RecyclerView.ItemDecoration() {

    /**
     * Drawing footer and header.
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        parent.adapter?.itemCount?.let { size ->
            when (parent.getChildAdapterPosition(view)) {
                0 -> {
                    outRect.top = screen.y / 3
                }
                size - 1 -> {
                    outRect.bottom = screen.y / 2
                }
            }
        }
    }

    /**
     * Drawing below or above the target item, or highlighting the target.
     */
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            val item = descriptor.current()
            if (item == null || item.position != position) continue
            item.ratio.let { ratio ->
                when (ratio) {
                    in START_RANGE -> {
                        if (position != TITLE_POSITION) dropAboveTarget(parent, child, c)
                    }
                    in END_RANGE -> {
                        drawBelowTarget(parent, child, c)
                    }
                    in INNER_RANGE -> {
                        if (position != TITLE_POSITION) highlightTarget(parent, child, c)
                    }
                    else -> {
                        if (ratio > 1) drawBelowTarget(parent, child, c)
                    }
                }
            }
        }
    }

    private fun drawBelowTarget(
        parent: RecyclerView,
        child: View,
        c: Canvas
    ) {
        val left = padding
        val right = parent.width - padding
        val top = child.bottom
        val bottom = child.bottom + targeted.intrinsicHeight
        line.apply {
            setBounds(left, top, right, bottom)
            draw(c)
        }
    }

    private fun dropAboveTarget(
        parent: RecyclerView,
        child: View,
        c: Canvas
    ) {
        val left = padding
        val right = parent.width - padding
        val top = child.top
        val bottom = child.top + targeted.intrinsicHeight
        line.apply {
            setBounds(left, top, right, bottom)
            draw(c)
        }
    }

    private fun highlightTarget(
        parent: RecyclerView,
        child: View,
        c: Canvas
    ) {
        val left = padding
        val right = parent.width - padding
        val top = child.top
        val bottom = child.bottom

        val holder = parent.findContainingViewHolder(child)

        if (holder is SupportNesting)
            targeted.apply {
                setBounds(left, top, right, bottom)
                draw(c)
            }
        else
            disabled.apply {
                setBounds(left, top, right, bottom)
                draw(c)
            }
    }

    companion object {
        const val HIGHLIGHT_ALPHA = 255
        const val DEFAULT_ALPHA = 255
        const val TITLE_POSITION = 0
    }
}