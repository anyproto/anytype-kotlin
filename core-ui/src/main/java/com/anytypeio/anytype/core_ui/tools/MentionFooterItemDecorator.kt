package com.anytypeio.anytype.core_ui.tools

import android.graphics.Point
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// TODO rename to DefaultFooterItemDecorator
open class MentionFooterItemDecorator(private val screen: Point) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        parent.adapter?.itemCount?.let { size ->
            when (parent.getChildAdapterPosition(view)) {
                size - 1 -> {
                    outRect.bottom = screen.y / 2
                }
            }
        }
    }
}

class MarkupColorToolbarFooter(screen: Point) : MentionFooterItemDecorator(screen)
class SlashWidgetFooterItemDecorator(screen: Point) : MentionFooterItemDecorator(screen)
class StyleToolbarItemDecorator(screen: Point) : MentionFooterItemDecorator(screen)