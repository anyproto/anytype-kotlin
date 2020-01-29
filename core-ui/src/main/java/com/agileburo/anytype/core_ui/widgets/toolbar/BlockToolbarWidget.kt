package com.agileburo.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.extensions.invisible
import com.agileburo.anytype.core_ui.extensions.visible
import com.agileburo.anytype.core_ui.reactive.clicks
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.state.ControlPanelState.Focus
import com.agileburo.anytype.core_ui.state.ControlPanelState.Toolbar.Block.Action.*
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
    fun turnIntoClicks() = turnIntoToggle.clicks()
    fun colorClicks() = color.clicks()

    fun setState(state: ControlPanelState.Toolbar.Block) {
        if (state.isVisible) visible() else invisible()
        add.isSelected = state.selectedAction == ADD
        actions.isSelected = state.selectedAction == BLOCK_ACTION
        turnIntoToggle.isSelected = state.selectedAction == TURN_INTO
        color.isSelected = state.selectedAction == COLOR
        arrow.isSelected = state.selectedAction == TURN_INTO
    }

    fun setTurnIntoTarget(
        target: Focus.Type
    ) {
        when (target) {
            Focus.Type.P -> {
                turnIntoToggle.setImageResource(R.drawable.ic_block_toolbar_turn_into_paragraph)
            }
            Focus.Type.H1 -> {
                turnIntoToggle.setImageResource(R.drawable.ic_block_toolbar_turn_into_header_one)
            }
            Focus.Type.H2 -> {
                turnIntoToggle.setImageResource(R.drawable.ic_block_toolbar_turn_into_header_two)
            }
            Focus.Type.H3 -> {
                turnIntoToggle.setImageResource(R.drawable.ic_block_toolbar_turn_into_header_three)
            }
            Focus.Type.CHECKBOX -> {
                turnIntoToggle.setImageResource(R.drawable.ic_block_toolbar_turn_into_checkbox)
            }
            Focus.Type.BULLET -> {
                turnIntoToggle.setImageResource(R.drawable.ic_block_toolbar_turn_into_bulleted)
            }
            Focus.Type.QUOTE -> {
                turnIntoToggle.setImageResource(R.drawable.ic_block_toolbar_turn_into_highlight)
            }
        }

        val color = if (turnIntoToggle.isSelected)
            context.color(R.color.toolbar_block_turn_into_toggle_selected)
        else
            context.color(R.color.toolbar_block_turn_into_toggle_default)

        turnIntoToggle.setColorFilter(color)
    }
}