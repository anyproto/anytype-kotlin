package com.agileburo.anytype.feature_editor.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-06-25.
 */
class SpaceItemDecoration(private val space: Int, private val addSpaceBelowLastItem : Boolean) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.bottom = space
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = space
        }

        if (addSpaceBelowLastItem && parent.getChildAdapterPosition(view) == parent.adapter?.itemCount?.dec()) {
            outRect.bottom = space * 10
        }
    }
}