package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.os.Build.VERSION_CODES.N_MR1
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.getBlockTextColor
import com.anytypeio.anytype.core_ui.extensions.applyMovementMethod
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.TextBlockHolder
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_utils.ext.imm
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.Checkable

abstract class Text(
    view: View
) : BlockViewHolder(view), TextBlockHolder, BlockViewHolder.IndentableHolder {

    fun bind(
        item: BlockView.TextBlockProps,
        clicked: (ListenerType) -> Unit,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onTextInputClicked: (String) -> Unit,
        onBackPressedCallback: (() -> Boolean)? = null
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
                clicked = clicked,
                onTextChanged = onTextChanged,
                onSelectionChanged = onSelectionChanged,
                onBackPressedCallback = onBackPressedCallback
            )
        }

        content.apply {
            setOnClickListener {
                if (Build.VERSION.SDK_INT == N || Build.VERSION.SDK_INT == N_MR1) {
                    content.context.imm().showSoftInput(content, InputMethodManager.SHOW_FORCED)
                }
                onTextInputClicked(item.id)
            }
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

    private fun setContent(item: BlockView.TextBlockProps, clicked: (ListenerType) -> Unit) {
        setBlockText(
            text = item.text,
            markup = item,
            clicked = clicked,
            textColor = item.getBlockTextColor()
        )
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
        onBackPressedCallback: (() -> Boolean)? = null
    ) {

        content.apply {
            setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = {
                        // TODO do not call this method directly, ask permission from vm
                        enableReadMode()
                        onBlockLongClick(root, it, clicked)
                    }
                )
            )

            addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )

//            selectionWatcher = {
//                onSelectionChanged(item.id, it)
//            }

            backButtonWatcher = onBackPressedCallback
        }
    }

    fun selection(item: BlockView.Selectable) {
        select(item)
    }
}