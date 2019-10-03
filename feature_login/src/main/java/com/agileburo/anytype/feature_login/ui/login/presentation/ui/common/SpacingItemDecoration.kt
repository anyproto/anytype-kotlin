package com.agileburo.anytype.feature_login.ui.login.presentation.ui.common

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
    private val spacingBottom: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = spacingStart
        outRect.right = spacingEnd
        outRect.bottom = spacingBottom
        outRect.top = spacingTop
    }
}