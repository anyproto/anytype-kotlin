package com.agileburo.anytype.core_ui.features.page

import android.text.Editable
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.TextView
import com.agileburo.anytype.core_ui.common.*
import com.agileburo.anytype.core_ui.extensions.preserveSelection
import com.agileburo.anytype.core_ui.menu.TextBlockContextMenu
import com.agileburo.anytype.core_ui.tools.DefaultSpannableFactory
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget.Companion.TEXT_INPUT_WIDGET_ACTION_GO
import com.agileburo.anytype.core_utils.ext.hideKeyboard
import com.agileburo.anytype.core_utils.ext.imm
import com.agileburo.anytype.core_utils.text.BackspaceKeyDetector
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

    fun setup(onMarkupActionClicked: (Markup.Type) -> Unit) {
        with(content) {
            setSpannableFactory(DefaultSpannableFactory())
            customSelectionActionModeCallback = TextBlockContextMenu(
                onTextColorClicked = { mode ->
                    preserveSelection {
                        content.hideKeyboard()
                        onMarkupActionClicked(Markup.Type.TEXT_COLOR)
                        mode.finish()
                    }
                    false
                },
                onBackgroundColorClicked = { mode ->
                    preserveSelection {
                        content.hideKeyboard()
                        onMarkupActionClicked(Markup.Type.BACKGROUND_COLOR)
                        mode.finish()
                    }
                    false
                },
                onMenuItemClicked = { onMarkupActionClicked(it) }
            )
        }
    }

    fun enableEnterKeyDetector(
        onEndLineEnterClicked: (Editable) -> Unit,
        onSplitLineEnterClicked: (Int) -> Unit
    ) {
        content.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == TEXT_INPUT_WIDGET_ACTION_GO) {
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
        if (item.focused)
            focus()
        else
            content.clearFocus()
    }

    fun setAlignment(alignment: BlockView.Alignment) {
        content.gravity = when (alignment) {
            BlockView.Alignment.START -> Gravity.START
            BlockView.Alignment.CENTER -> Gravity.CENTER
            BlockView.Alignment.END -> Gravity.END
        }
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
        Timber.d("Requesting focus")
        content.apply {
            post {
                if (!hasFocus() && requestFocus())
                    context.imm().showSoftInput(this, SHOW_IMPLICIT)
                else
                    Timber.d("Couldn't gain focus")
            }
        }
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit
    ) = payloads.forEach { payload ->

        Timber.d("Processing $payload for new view:\n$item")

        if (item is BlockView.Text) {
            if (payload.textChanged()) {
                val cursor = content.length()
                content.pauseTextWatchers {
                    if (item is Markup)
                        content.setText(item.toSpannable(), TextView.BufferType.SPANNABLE)
                    else
                        content.setText(item.text)
                }
                try {
                    content.setSelection(cursor)
                } catch (e: Throwable) {
                    Timber.e(e, "Error while setting selection")
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

            if (payload.alignmentChanged()) {
                if (item is BlockView.Alignable) {
                    item.alignment?.let { setAlignment(it) }
                }
            }
        }

        if (item is BlockView.Permission && payload.readWriteModeChanged()) {
            if (item.mode == BlockView.Mode.EDIT) {
                content.clearTextWatchers()
                setupTextWatcher(onTextChanged, item)
                content.selectionDetector = { onSelectionChanged(item.id, it) }
                enableEditMode()
            } else {
                enableReadOnlyMode()
            }
        }

        if (item is BlockView.Selectable && payload.selectionChanged()) {
            select(item)
        }

        if (item is Focusable && payload.focusChanged()) {
            setFocus(item)
        }
    }

    fun select(item: BlockView.Selectable) {
        content.isSelected = item.isSelected
    }

    fun enableReadOnlyMode() {
        content.enableReadMode()
        content.selectionDetector = null
        content.clearTextWatchers()
    }

    fun enableEditMode() {
        content.enableEditMode()
    }

    fun enableTitleReadOnlyMode() {
        content.enableReadMode()
    }
}