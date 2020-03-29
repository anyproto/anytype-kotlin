package com.agileburo.anytype.core_ui.tools

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.properties.Delegates

class FirstItemInvisibilityDetector(
    onVisibilityChanged: (Boolean) -> Unit
) : RecyclerView.OnScrollListener() {

    private var visible: Boolean by Delegates.observable(true) { _, old, new ->
        if (new != old) onVisibilityChanged(new)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (recyclerView.layoutManager is LinearLayoutManager) {
            val mng = (recyclerView.layoutManager as LinearLayoutManager)
            visible = mng.findFirstVisibleItemPosition() == 0
        } else {
            throw IllegalStateException("This detector support only LinearLayoutManager")
        }
    }
}