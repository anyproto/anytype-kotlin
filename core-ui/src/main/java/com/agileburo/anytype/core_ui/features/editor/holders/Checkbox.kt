package com.agileburo.anytype.core_ui.features.editor.holders

import android.content.Context
import android.text.Editable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updatePadding
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.isLinksPresent
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.*
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_checkbox.view.*

class Checkbox(
    view: View,
    onMarkupActionClicked: (Markup.Type, IntRange) -> Unit
) : BlockViewHolder(view), TextHolder, BlockViewHolder.IndentableHolder, SupportNesting {

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
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)

        if (item.mode == BlockView.Mode.READ) {
            enableReadOnlyMode()

            select(item)

            updateTextColor(
                context = itemView.context,
                view = content,
                isSelected = checkbox.isActivated
            )
            checkbox.isActivated = item.isChecked
            setBlockText(text = item.text, markup = item, clicked = clicked)
        } else {

            enableEditMode()

            select(item)

            content.setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = { onBlockLongClick(root, it, clicked) }
                )
            )

            content.clearTextWatchers()
            checkbox.isActivated = item.isChecked

            updateTextColor(
                context = itemView.context,
                view = content,
                isSelected = checkbox.isActivated
            )

            if (item.marks.isLinksPresent()) {
                content.setLinksClickable()
            }

            setBlockText(text = item.text, markup = item, clicked = clicked)

            if (item.isFocused) setCursor(item)

            setFocus(item)

            checkbox.setOnClickListener {
                if (mode == BlockView.Mode.EDIT) {
                    checkbox.isActivated = !checkbox.isActivated
                    updateTextColor(
                        context = itemView.context,
                        view = content,
                        isSelected = checkbox.isActivated
                    )
                    onCheckboxClicked(item.id)
                }
            }

            content.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
            }

            content.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )

            content.selectionWatcher = {
                onSelectionChanged(item.id, it)
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