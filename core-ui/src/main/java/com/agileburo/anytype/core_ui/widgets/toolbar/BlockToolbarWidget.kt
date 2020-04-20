package com.agileburo.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.reactive.clicks
import kotlinx.android.synthetic.main.widget_block_toolbar_new.view.*

class BlockToolbarWidget : ConstraintLayout {

    constructor(
        context: Context
    ) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        inflate()
    }

    fun unfocusClicks() = unfocus.clicks()
    fun enterDocumentStructureClicks() = structure.clicks()
    fun addBlockClicks() = add.clicks()

    private fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.widget_block_toolbar_new, this)
    }
}