package com.agileburo.anytype.core_ui.features.page.scrollandmove

import androidx.recyclerview.widget.RecyclerView

class ScrollAndMoveStateListener(val onStateChanged: (Int) -> Unit) :
    RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        onStateChanged(newState)
    }
}