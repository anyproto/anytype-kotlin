package com.agileburo.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.isLinksPresent
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.features.page.TextBlockHolder
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import timber.log.Timber

abstract class Text(
    view: View
) : BlockViewHolder(view), TextBlockHolder, BlockViewHolder.IndentableHolder {

    fun bind(
        item: BlockView.TextBlockProps,
        clicked: (ListenerType) -> Unit,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        onEndLineEnterClicked: (String, Editable) -> Unit,
        onSplitLineEnterClicked: (String, Int, Editable) -> Unit,
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
                onSelectionChanged = onSelectionChanged,
                onFocusChanged = onFocusChanged,
                onEndLineEnterClicked = onEndLineEnterClicked,
                onSplitLineEnterClicked = onSplitLineEnterClicked,
                onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
                onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
                onTextInputClicked = onTextInputClicked
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
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        onEndLineEnterClicked: (String, Editable) -> Unit,
        onSplitLineEnterClicked: (String, Int, Editable) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onTextInputClicked: (String) -> Unit
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

            enableEnterKeyDetector(
                onEndLineEnterClicked = { editable ->
                    onEndLineEnterClicked(item.id, editable)
                },
                onSplitLineEnterClicked = { index ->
                    content.text?.let { editable ->
                        onSplitLineEnterClicked(
                            item.id,
                            index,
                            editable
                        )
                    }
                }
            )

            enableBackspaceDetector(
                onEmptyBlockBackspaceClicked = {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        onEmptyBlockBackspaceClicked(item.id)
                        Timber.d("Proceed onEmptyBlockBackspaceClicked for adapter position:${adapterPosition}")
                    } else {
                        Timber.e("Can't proceed with onEmptyBlockBackspaceClicked, because holder.adapter position is NO_POSITION")
                    }
                },
                onNonEmptyBlockBackspaceClicked = {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        content.text?.let { editable ->
                            onNonEmptyBlockBackspaceClicked(
                                item.id,
                                editable
                            )
                        }
                        Timber.d("Proceed onNonEmptyBlockBackspaceClicked for adapter position:${adapterPosition}")
                    } else {
                        Timber.e("Can't proceed with onNonEmptyBlockBackspaceClicked, because holder.adapter position is NO_POSITION")
                    }
                }
            )

            setOnClickListener { onTextInputClicked(item.id) }
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