package com.anytypeio.anytype.core_ui.features.editor.holders.`interface`

import android.view.Gravity
import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.text.BackspaceKeyDetector
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.Focusable
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
        val default = content.context.getColor(R.color.text_primary)
        if (value != null && value != ThemeColor.DEFAULT) {
            content.setTextColor(content.resources.dark(value, default))
        } else {
            content.setTextColor(default)
        }
    }

    fun setTextColor(color: Int) {
        content.setTextColor(color)
    }

    fun enableReadMode() {
        content.clearTextWatchers()
        content.enableReadMode()
        //content.selectionWatcher = null
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
            this.setFocus()
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

    fun setTextInputClickListener(onTextInputClicked: () -> Unit) {
        content.setOnClickListener { onTextInputClicked() }
    }
}