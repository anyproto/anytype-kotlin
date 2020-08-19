package com.agileburo.anytype.core_ui.features.editor.holders

import android.text.Editable
import android.view.View
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.isLinksPresent
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.features.page.TextHolder
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener

abstract class Text(
    view: View
) : BlockViewHolder(view), TextHolder, BlockViewHolder.IndentableHolder {

    fun bind(
        item: BlockView.TXT,
        clicked: (ListenerType) -> Unit,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
    ) {

        indentize(item)
        select(item)

        if (item.mode == BlockView.Mode.READ) {
            enableReadOnlyMode()
            setContent(
                item = item,
                clicked = clicked
            )
        } else {

            enableEditMode()

            makeLinkClickable(item)

            setContent(
                item = item,
                clicked = clicked
            )

            if (item.isFocused) setCursor(item)

            setFocus(item)

            observe(
                item = item,
                clicked = clicked,
                onTextChanged = onTextChanged,
                onSelectionChanged = onSelectionChanged,
                onFocusChanged = onFocusChanged
            )
        }
    }

    fun setContent(item: BlockView.TXT, clicked: (ListenerType) -> Unit) {
        setBlockText(text = item.text, markup = item, clicked = clicked)
        val color = item.color
        if (color != null) {
            setTextColor(color)
        } else {
            setTextColor(content.context.color(R.color.black))
        }
    }

    fun observe(
        item: BlockView.TXT,
        clicked: (ListenerType) -> Unit,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
    ) {

        content.apply {

            clearTextWatchers()

            setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = { onBlockLongClick(root, it, clicked) }
                )
            )

            addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )

            setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
            }

            selectionWatcher = {
                onSelectionChanged(item.id, it)
            }
        }
    }

    fun selection(item: BlockView.Selectable) {
        select(item)
    }

    fun makeLinkClickable(item: BlockView.TXT) {
        if (item.marks.isLinksPresent()) {
            content.setLinksClickable()
        }
    }
}