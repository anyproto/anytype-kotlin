package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.applyMovementMethod
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.TextBlockHolder
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.Checkable

abstract class Text(
    view: View,
    protected val clicked: (ListenerType) -> Unit,
) : BlockViewHolder(view), TextBlockHolder, BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder {

    private val defTextColor: Int = itemView.context.resources.getColor(R.color.text_primary, null)

    fun bind(
        item: BlockView.TextBlockProps,
        onTextChanged: (String, Editable) -> Unit,
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onBackPressedCallback: (() -> Boolean)? = null
    ) {
        indentize(item)
        select(item)
        inputAction(item)

        if (item.mode == BlockView.Mode.READ) {
            enableReadMode()
            setContent(
                item = item,
                clicked = clicked
            )
            setStyle(item)
        } else {

            content.pauseTextWatchers {
                enableEditMode()
            }

            content.applyMovementMethod(item)

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
                onTextChanged = onTextChanged,
                onBackPressedCallback = onBackPressedCallback
            )
        }

        content.apply {
            enableEnterKeyDetector(
                onEnterClicked = { range ->
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

    private fun setContent(item: BlockView.TextBlockProps, clicked: (ListenerType) -> Unit) {
        content.pauseTextWatchers {
            setBlockText(
                text = item.text,
                markup = item,
                clicked = clicked,
                textColor = resolveTextBlockThemedColor(item.color)
            )
        }
        if (item is BlockView.Searchable) {
            applySearchHighlight(item)
        }
        if (item is BlockView.SupportGhostEditorSelection) {
            applyGhostEditorSelection(item)
        }
        if (item is Checkable) {
            applyCheckedCheckboxColorSpan(item.isChecked)
        }
    }

    private fun setStyle(item: BlockView.TextBlockProps) {
        setTextColor(item.color)
        setBackgroundColor(background = item.background)
    }

    fun observe(
        item: BlockView.TextBlockProps,
        onTextChanged: (String, Editable) -> Unit,
        onBackPressedCallback: (() -> Boolean)? = null
    ) {

        content.apply {
            addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )
            backButtonWatcher = onBackPressedCallback
        }
    }

    fun selection(item: BlockView.Selectable) {
        select(item)
    }

    fun inputAction(item: BlockView.TextBlockProps) {
        content.setInputAction(item.inputAction)
    }

    override fun getDefaultTextColor(): Int = defTextColor
}