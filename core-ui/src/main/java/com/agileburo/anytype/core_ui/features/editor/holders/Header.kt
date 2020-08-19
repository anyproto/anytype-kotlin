package com.agileburo.anytype.core_ui.features.editor.holders

import android.text.Editable
import android.view.View
import androidx.core.view.updatePadding
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.isLinksPresent
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.features.page.TextHolder
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.dimen

abstract class Header(
    view: View
) : BlockViewHolder(view), TextHolder, BlockViewHolder.IndentableHolder {

    abstract val header: TextInputWidget

    fun bind(
        block: BlockView.Header,
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

    fun setBlockTextColor(color: String?) {
        if (color != null)
            setTextColor(color)
        else
            setTextColor(content.context.color(R.color.black))
    }

    fun setLinksClickable(block: BlockView.Header) {
        if (block.marks.isLinksPresent()) {
            content.setLinksClickable()
        }
    }

    override fun indentize(item: BlockView.Indentable) {
        header.updatePadding(
            left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
        )
    }
}