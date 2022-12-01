package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.anytypeio.anytype.core_ui.databinding.WidgetBlockToolbarBinding
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.core_utils.ext.visible

class BlockToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    sealed interface State {

        /**
         * Toolbar is shown for Any block type except Title or Cells
         */
        object Any : State

        /**
         * Toolbar is shown specially for Title block
         */
        object Title : State

        /**
         * Toolbar is shown specially for Simple Table cells
         */
        object Cell : State
    }

    private val binding = WidgetBlockToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )
    private val done = binding.done
    private val blockActions = binding.btnBlockActions
    private val blockMentionButton = binding.blockMentionButton
    private val slashWidgetButton = binding.slashWidgetButton
    private val changeStyleButton = binding.changeStyleButton

    var state: State = State.Any
        set(value) {
            if(field == value) return
            field = value
            when (value) {
                State.Any -> {
                    done.visible()
                    blockActions.visible()
                    blockMentionButton.visible()
                    slashWidgetButton.visible()
                    changeStyleButton.visible()
                }
                State.Title -> {
                    done.visible()
                    changeStyleButton.visible()
                    blockActions.gone()
                    blockMentionButton.gone()
                    slashWidgetButton.gone()
                }
                State.Cell -> {
                    done.visible()
                    blockActions.visible()
                    blockMentionButton.visible()
                    slashWidgetButton.gone()
                    changeStyleButton.gone()
                }
            }
        }

    init {
        orientation = HORIZONTAL
    }

    fun hideKeyboardClicks() = done.clicks().throttleFirst()
    fun blockActionsClick() = blockActions.clicks().throttleFirst()
    fun openSlashWidgetClicks() = slashWidgetButton.clicks().throttleFirst()
    fun changeStyleClicks() = changeStyleButton.clicks().throttleFirst()
    fun mentionClicks() = blockMentionButton.clicks().throttleFirst()
}