package com.agileburo.anytype.core_ui.features.editor.holders.text

import android.content.Context
import android.text.Editable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updatePadding
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.features.page.SupportNesting
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_checkbox.view.*

class Checkbox(
    view: View,
    onMarkupActionClicked: (Markup.Type, IntRange) -> Unit
) : Text(view), SupportNesting {

    var mode = BlockView.Mode.EDIT

    val checkbox: ImageView = itemView.checkboxIcon
    private val container = itemView.checkboxBlockContentContainer
    override val content: TextInputWidget = itemView.checkboxContent
    override val root: View = itemView

    init {
        setup(onMarkupActionClicked, ContextMenuType.TEXT)
    }

    fun bind(
        item: BlockView.Checkbox,
        onTextChanged: (String, Editable) -> Unit,
        onCheckboxClicked: (String) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        clicked: (ListenerType) -> Unit,
        onEndLineEnterClicked: (String, Editable) -> Unit,
        onSplitLineEnterClicked: (String, Int, Editable) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onTextInputClicked: (String) -> Unit
    ) = super.bind(
        item = item,
        onTextChanged = onTextChanged,
        onFocusChanged = onFocusChanged,
        onSelectionChanged = onSelectionChanged,
        clicked = clicked,
        onEndLineEnterClicked = onEndLineEnterClicked,
        onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
        onSplitLineEnterClicked = onSplitLineEnterClicked,
        onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
        onTextInputClicked = onTextInputClicked
    ).also {
        checkbox.isActivated = item.isChecked
        updateTextColor(
            context = itemView.context,
            view = content,
            isSelected = checkbox.isActivated
        )
        if (item.mode == BlockView.Mode.EDIT) {
            checkbox.setOnClickListener {
                checkbox.isActivated = !checkbox.isActivated
                updateTextColor(
                    context = itemView.context,
                    view = content,
                    isSelected = checkbox.isActivated
                )
                onCheckboxClicked(item.id)
            }
        }
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }

    override fun indentize(item: BlockView.Indentable) {
        checkbox.updatePadding(left = item.indent * dimen(R.dimen.indent))
    }

    override fun enableEditMode() {
        super.enableEditMode()
        mode = BlockView.Mode.EDIT
    }

    override fun enableReadOnlyMode() {
        super.enableReadOnlyMode()
        mode = BlockView.Mode.READ
    }

    override fun select(item: BlockView.Selectable) {
        container.isSelected = item.isSelected
    }

    private fun updateTextColor(context: Context, view: TextView, isSelected: Boolean) =
        view.setTextColor(
            context.color(
                if (isSelected) R.color.checkbox_state_checked else R.color.black
            )
        )
}