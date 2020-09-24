package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.isLinksPresent
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.page.ListenerType
import com.anytypeio.anytype.core_ui.features.page.TextBlockHolder
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener

abstract class Text(
    view: View
) : BlockViewHolder(view), TextBlockHolder, BlockViewHolder.IndentableHolder {

    fun bind(
        item: BlockView.TextBlockProps,
        clicked: (ListenerType) -> Unit,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onTextInputClicked: (String) -> Unit
    ) {

        indentize(item)
        select(item)

        if (item.mode == BlockView.Mode.READ) {
            enableReadMode()
            setContent(
                item = item,
                clicked = clicked
            )
            setStyle(item)
        } else {

            enableEditMode()

            makeLinkClickable(item)

            clearTextWatchers()

            setContent(
                item = item,
                clicked = clicked
            )
            setStyle(item)

            if (item.isFocused) setCursor(item)

            setFocus(item)

            observe(
                item = item,
                clicked = clicked,
                onTextChanged = onTextChanged,
                onSelectionChanged = onSelectionChanged
            )
        }

        content.apply {
            setOnFocusChangeListener { _, hasFocus -> onFocusChanged(item.id, hasFocus) }
            setOnClickListener { onTextInputClicked(item.id) }
            enableEnterKeyDetector(
                onSplitLineEnterClicked = { range ->
                    content.text?.let { editable ->
                        onSplitLineEnterClicked(
                            item.id,
                            editable,
                            range
                        )
                    }
                }
            )
            enableBackspaceDetector(
                onEmptyBlockBackspaceClicked = {
                    onEmptyBlockBackspaceClicked(item.id)
                },
                onNonEmptyBlockBackspaceClicked = {
                    content.text?.let { editable ->
                        onNonEmptyBlockBackspaceClicked(
                            item.id,
                            editable
                        )
                    }
                }
            )
        }
    }

    fun setContent(item: BlockView.TextBlockProps, clicked: (ListenerType) -> Unit) {
        setBlockText(text = item.text, markup = item, clicked = clicked)
    }

    fun setStyle(item: BlockView.TextBlockProps) {
        val color = item.color
        if (color != null) {
            setTextColor(color)
        } else {
            setTextColor(content.context.color(R.color.black))
        }
        setBackgroundColor(color = item.backgroundColor)
    }

    fun observe(
        item: BlockView.TextBlockProps,
        clicked: (ListenerType) -> Unit,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit
    ) {

        content.apply {

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

            selectionWatcher = {
                onSelectionChanged(item.id, it)
            }
        }
    }

    fun selection(item: BlockView.Selectable) {
        select(item)
    }

    fun makeLinkClickable(item: BlockView.TextBlockProps) {
        if (item.marks.isLinksPresent()) {
            content.setLinksClickable()
        }
    }
}