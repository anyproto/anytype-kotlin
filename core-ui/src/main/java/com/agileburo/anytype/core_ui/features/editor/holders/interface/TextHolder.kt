package com.agileburo.anytype.core_ui.features.editor.holders.`interface`

import android.text.Editable
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Focusable
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.imm
import com.agileburo.anytype.core_utils.text.BackspaceKeyDetector
import timber.log.Timber

/**
 * Provides contract and default implementation for for editable blocks' common behavior.
 * @see [BlockView.Text], [BlockView.Title], [BlockView.Code]
 */
interface TextHolder {

    /**
     * Block's parent view.
     */
    val root: View

    /**
     * Block's content widget.
     * Common behavior is applied to this widget.
     */
    val content: TextInputWidget

    fun setCursor(item: BlockView.Cursor) {
        item.cursor?.let {
            val length = content.text?.length ?: 0
            if (it in 0..length) {
                content.setSelection(it)
            }
        }
    }

    fun setAlignment(alignment: Alignment) {
        content.gravity = when (alignment) {
            Alignment.START -> Gravity.START
            Alignment.CENTER -> Gravity.CENTER
            Alignment.END -> Gravity.END
        }
    }

    fun setTextColor(color: String) {
        val value = ThemeColor.values().find { value -> value.title == color }
        if (value != null)
            content.setTextColor(value.text)
        else
            Timber.e("Could not find value for text color: $color")
    }

    fun setTextColor(color: Int) {
        content.setTextColor(color)
    }

    fun enableReadMode() {
        content.enableReadMode()
        content.selectionWatcher = null
        content.clearTextWatchers()
    }

    fun enableEditMode() {
        content.enableEditMode()
    }

    fun select(item: BlockView.Selectable) {
        content.isSelected = item.isSelected
    }

    fun setFocus(item: Focusable) {
        if (item.isFocused)
            focus()
        else
            content.clearFocus()
    }

    fun focus() {
        Timber.d("Requesting focus")
        content.apply {
            post {
                if (!hasFocus()) {
                    if (requestFocus()) {
                        context.imm().showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                    } else {
                        Timber.d("Couldn't gain focus")
                    }
                } else
                    Timber.d("Already had focus")
            }
        }
    }

    fun enableBackspaceDetector(
        onEmptyBlockBackspaceClicked: () -> Unit,
        onNonEmptyBlockBackspaceClicked: () -> Unit
    ) {
        content.setOnKeyListener(
            BackspaceKeyDetector {
                if (content.text.toString().isEmpty()) {
                    onEmptyBlockBackspaceClicked()
                } else {
                    onNonEmptyBlockBackspaceClicked()
                }
            }
        )
    }

    fun enableEnterKeyDetector(
        onEndLineEnterClicked: (Editable) -> Unit,
        onSplitLineEnterClicked: (Int) -> Unit
    ) {
        content.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == TextInputWidget.TEXT_INPUT_WIDGET_ACTION_GO) {
                if (v.selectionEnd < v.text.length) {
                    onSplitLineEnterClicked(v.selectionEnd)
                } else {
                    onEndLineEnterClicked(content.editableText)
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    fun setTextInputClickListener(onTextInputClicked: () -> Unit) {
        content.setOnClickListener { onTextInputClicked() }
    }
}