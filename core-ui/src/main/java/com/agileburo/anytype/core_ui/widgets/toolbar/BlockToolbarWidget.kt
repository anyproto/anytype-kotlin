package com.agileburo.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.extensions.invisible
import com.agileburo.anytype.core_ui.extensions.visible
import com.agileburo.anytype.core_ui.reactive.clicks
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.state.ControlPanelState.Toolbar.Block.Action.ADD
import com.agileburo.anytype.core_ui.state.ControlPanelState.Toolbar.Block.Action.BLOCK_ACTION
import kotlinx.android.synthetic.main.widget_block_toolbar.view.*

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

    private fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.widget_block_toolbar, this)
    }

    fun keyboardClicks() = keyboard.clicks()
    fun addButtonClicks() = add.clicks()
    fun actionClicks() = actions.clicks()

    fun setState(state: ControlPanelState.Toolbar.Block) {
        if (state.isVisible) visible() else invisible()
        add.isSelected = state.selectedAction == ADD
        actions.isSelected = state.selectedAction == BLOCK_ACTION
    }
}