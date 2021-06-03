package com.anytypeio.anytype.core_ui.features.editor.holders.`interface`

import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.imm
import com.anytypeio.anytype.core_utils.text.BackspaceKeyDetector
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.page.editor.model.Alignment
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.page.editor.model.Focusable
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
        Timber.d("Setting cursor: $item")
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
        if (value != null) {
            content.setTextColor(value.text)
        } else {
            Timber.e("Could not find value for text color: $color, setting default text color")
            content.setTextColor(ThemeColor.DEFAULT.text)
        }
    }

    fun setTextColor(color: Int) {
        content.setTextColor(color)
    }

    fun enableReadMode() {
        content.enableReadMode()
        //content.selectionWatcher = null
        content.clearTextWatchers()
    }

    fun enableEditMode() {
        content.enableEditMode()
    }

    fun select(item: BlockView.Selectable) {
        content.isSelected = item.isSelected
    }

    fun setFocus(item: Focusable) {
        if (item.isFocused) {
            focus()
        } else {
            content.clearFocus()
        }
    }

    fun focus() {
        Timber.d("Requesting focus")
        content.apply {
            // Sheduling a runnable that shows the keyboard in the next UI loop.
            post {
                content.pauseFocusChangeListener {
                    if (!hasFocus()) {
                        if (requestFocus()) {
                            context.imm().showSoftInput(this, InputMethodManager.SHOW_FORCED)
                        } else {
                            Timber.d("Couldn't gain focus")
                        }
                    } else {
                        Timber.d("Already had focus")
                    }
                }
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
        onSplitLineEnterClicked: (IntRange) -> Unit
    ) {
        content.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == TextInputWidget.TEXT_INPUT_WIDGET_ACTION_GO) {
                onSplitLineEnterClicked.invoke(v.selectionStart..v.selectionEnd)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    fun setTextInputClickListener(onTextInputClicked: () -> Unit) {
        content.setOnClickListener { onTextInputClicked() }
    }
}