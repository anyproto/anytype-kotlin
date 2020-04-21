package com.agileburo.anytype.core_ui.features.page

import android.text.Editable
import android.view.View
import android.widget.TextView
import com.agileburo.anytype.core_ui.common.*
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.text.BackspaceKeyDetector
import com.agileburo.anytype.core_utils.text.DefaultEnterKeyDetector
import timber.log.Timber

/**
 * Provides default implementation for common behavior for text blocks.
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

    fun enableEnterKeyDetector(
        onEndLineEnterClicked: (Editable) -> Unit,
        onSplitLineEnterClicked: (Int) -> Unit
    ) {
        content.filters = arrayOf(
            DefaultEnterKeyDetector(
                onSplitLineEnterClicked = onSplitLineEnterClicked,
                onEndLineEnterClicked = { onEndLineEnterClicked(content.editableText) }
            )
        )
    }

    fun setOnClickListener(onTextInputClicked: () -> Unit) {
        content.setOnClickListener { onTextInputClicked() }
    }

    fun enableBackspaceDetector(
        onEmptyBlockBackspaceClicked: () -> Unit,
        onNonEmptyBlockBackspaceClicked: () -> Unit
    ) {
        content.setOnKeyListener(
            BackspaceKeyDetector {
                if (content.text.toString().isEmpty()) {
                    // Refactoring needed, there are cases when we shouldn't clear text watchers
                    //content.clearTextWatchers()
                    //content.setOnKeyListener(null)
                    onEmptyBlockBackspaceClicked()
                } else {
                    // Refactoring needed, there are cases when we shouldn't clear text watchers
                    //content.clearTextWatchers()
                    //content.setOnKeyListener(null)
                    onNonEmptyBlockBackspaceClicked()
                }
            }
        )
    }

    fun setTextColor(color: String) {
        content.setTextColor(
            ThemeColor.values().first { value ->
                value.title == color
            }.text
        )
    }

    fun setTextColor(color: Int) {
        content.setTextColor(color)
    }

    fun setBackgroundColor(color: String? = null) {
        Timber.d("Setting background color: $color")
        if (color != null) {
            root.setBackgroundColor(
                ThemeColor.values().first { value ->
                    value.title == color
                }.background
            )
        } else {
            root.background = null
        }
    }

    fun setFocus(item: Focusable) {
        if (item.focused && !content.hasFocus())
            focus()
        else
            content.clearFocus()
    }

    fun setMarkup(markup: Markup) {
        content.text?.setMarkup(markup)
    }

    fun setupTextWatcher(
        onTextChanged: (String, Editable) -> Unit,
        item: BlockView
    ) {
        content.addTextChangedListener(
            DefaultTextWatcher { text ->
                onTextChanged(item.id, text)
            }
        )
    }

    private fun focus() {
        content.apply {
            postDelayed(
                { requestFocus() }
                , BlockViewHolder.FOCUS_TIMEOUT_MILLIS
            )
        }
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView
    ) = payloads.forEach { payload ->

        Timber.d("Processing $payload for new view:\n$item")

        if (item is BlockView.Text) {

            if (payload.textChanged()) {
                val cursor = content.selectionEnd
                content.pauseTextWatchers {
                    if (item is Markup)
                        content.setText(item.toSpannable(), TextView.BufferType.SPANNABLE)
                    else
                        content.setText(item.text)
                }
                try {
                    content.setSelection(cursor)
                } catch (e: Throwable) {
                    Timber.e(e, "Error while setting focus: ")
                }
            } else if (payload.markupChanged()) {
                if (item is Markup) setMarkup(item)
            }

            if (payload.textColorChanged()) {
                item.color?.let { setTextColor(it) }
            }

            if (payload.backgroundColorChanged()) {
                setBackgroundColor(item.backgroundColor)
            }
        }

        if (item is Focusable) {
            if (payload.focusChanged())
                setFocus(item)
        }
    }
}