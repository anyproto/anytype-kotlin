package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.reactive.clicks
import kotlinx.android.synthetic.main.widget_block_toolbar.view.*

class BlockToolbarWidget  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_block_toolbar, this)
        orientation = HORIZONTAL
    }

    fun hideKeyboardClicks() = hideKeyboardButton.clicks()
    fun enterScrollAndMoveButton() = enterScrollAndMoveButton.clicks()
    fun openSlashWidgetClicks() = slashWidgetButton.clicks()
    fun changeStyleClicks() = changeStyleButton.clicks()
    fun mentionClicks() = blockMentionButton.clicks()
}