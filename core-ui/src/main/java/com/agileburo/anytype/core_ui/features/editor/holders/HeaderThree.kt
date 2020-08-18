package com.agileburo.anytype.core_ui.features.editor.holders

import android.text.Editable
import android.view.View
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import kotlinx.android.synthetic.main.item_block_header_three.view.*

class HeaderThree(
    view: View,
    onMarkupActionClicked: (Markup.Type, IntRange) -> Unit
) : Header(view) {

    override val header: TextInputWidget = itemView.headerThree
    override val content: TextInputWidget get() = header
    override val root: View = itemView

    init {
        setup(onMarkupActionClicked, ContextMenuType.HEADER)
    }

    fun bind(
        block: BlockView.HeaderThree,
        onTextChanged: (String, Editable) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit
    ) {
        if (block.mode == BlockView.Mode.READ) {
            enableReadOnlyMode()
            select(block)
            setBlockText(text = block.text, markup = block, clicked = clicked)
            setBlockTextColor(block.color)
        } else {
            enableEditMode()
            select(block)
            setLinksClickable(block)
            setBlockText(text = block.text, markup = block, clicked = clicked)
            setBlockTextColor(block.color)
            setFocus(block)
            if (block.isFocused) setCursor(block)
            with(header) {
                clearTextWatchers()
                setOnFocusChangeListener { _, hasFocus ->
                    onFocusChanged(block.id, hasFocus)
                }
                addTextChangedListener(
                    DefaultTextWatcher { text ->
                        onTextChanged(block.id, text)
                    }
                )
                selectionWatcher = {
                    onSelectionChanged(block.id, it)
                }
            }
        }
        header.setOnLongClickListener(
            EditorLongClickListener(
                t = block.id,
                click = { onBlockLongClick(root, it, clicked) }
            )
        )
        indentize(block)
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_three),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }
}