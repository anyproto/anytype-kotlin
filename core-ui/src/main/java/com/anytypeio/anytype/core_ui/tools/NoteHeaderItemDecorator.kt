package com.anytypeio.anytype.core_ui.tools

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class NoteHeaderItemDecorator(private val offset: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == 0) {
            outRect.top = offset
        }
    }
}