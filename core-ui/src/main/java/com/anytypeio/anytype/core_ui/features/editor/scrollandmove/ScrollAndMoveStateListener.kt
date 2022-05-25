package com.anytypeio.anytype.core_ui.features.editor.scrollandmove

import androidx.recyclerview.widget.RecyclerView

class ScrollAndMoveStateListener(
    val onStateChanged: (Int) -> Unit
) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        onStateChanged(dy)
    }
}