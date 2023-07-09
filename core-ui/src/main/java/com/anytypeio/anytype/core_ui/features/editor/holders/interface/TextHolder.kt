package com.anytypeio.anytype.core_ui.features.editor.holders.`interface`

import android.view.Gravity
import android.view.View
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.extensions.setTextColor
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.text.BackspaceKeyDetector
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

    val selectionView: View

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

    fun setTextColor(color: ThemeColor) {
        content.setTextColor(color)
    }

    fun setTextColor(color: Int) {
        content.setTextColor(color)
    }

    fun enableReadMode() {
        content.enableReadMode()
        //content.selectionWatcher = null
    }

    fun enableEditMode() {
        content.enableEditMode()
    }

    fun select(item: BlockView.Selectable) {
        selectionView.isSelected = item.isSelected
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