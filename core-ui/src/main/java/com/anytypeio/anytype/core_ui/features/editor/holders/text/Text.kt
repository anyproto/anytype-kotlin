package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.os.Build
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.getBlockTextColor
import com.anytypeio.anytype.core_ui.extensions.applyMovementMethod
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.page.ListenerType
import com.anytypeio.anytype.core_ui.features.page.TextBlockHolder
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_utils.ext.imm

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
            setOnFocusChangeListener { _, hasFocus ->
                item.isFocused = hasFocus
                onFocusChanged(item.id, hasFocus)
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                    if (hasFocus) {
                        imm().showSoftInput(content, InputMethodManager.SHOW_FORCED)
                    }
                }
            }
            setOnClickListener {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
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
            applySearchTargetHighlight(item)
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
                        // TODO do not call this methed directly, ask permission from vm
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

            selectionWatcher = {
                onSelectionChanged(item.id, it)
            }

            backButtonWatcher = onBackPressedCallback
        }
    }

    fun selection(item: BlockView.Selectable) {
        select(item)
    }
}