package com.agileburo.anytype.core_ui.features.page.scrollandmove

import androidx.recyclerview.widget.RecyclerView

class ScrollAndMoveStateListener(
    val onStateChanged: (Int) -> Unit
) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        onStateChanged(dy)
    }
}