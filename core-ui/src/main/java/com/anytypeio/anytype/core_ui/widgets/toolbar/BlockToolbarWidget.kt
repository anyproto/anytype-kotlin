package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.databinding.WidgetBlockToolbarBinding
import com.anytypeio.anytype.core_ui.reactive.clicks

class BlockToolbarWidget  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    val binding = WidgetBlockToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        orientation = HORIZONTAL
    }

    fun hideKeyboardClicks() = binding.hideKeyboardButton.clicks()
    fun blockActionsClick() = binding.btnBlockActions.clicks()
    fun openSlashWidgetClicks() = binding.slashWidgetButton.clicks()
    fun changeStyleClicks() = binding.changeStyleButton.clicks()
    fun mentionClicks() = binding.blockMentionButton.clicks()
}