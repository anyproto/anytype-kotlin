package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.os.Build.VERSION_CODES.N_MR1
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockCodeSnippetBinding
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.widgets.text.CodeTextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.imm
import com.anytypeio.anytype.core_utils.text.BackspaceKeyDetector
import com.anytypeio.anytype.library_syntax_highlighter.Syntaxes
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.Focusable
import timber.log.Timber

class Code(val binding: ItemBlockCodeSnippetBinding) : BlockViewHolder(binding.root),
    BlockViewHolder.DragAndDropHolder {

    val menu: TextView
        get() = binding.codeMenu
    val root: View
        get() = itemView
    val content: CodeTextInputWidget
        get() = binding.snippet

    val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        content.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.Code,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        clicked: (ListenerType) -> Unit,
        onTextInputClicked: (String) -> Unit
    ) {
        indentize(item)
        if (item.mode == BlockView.Mode.READ) {
            content.setText(item.text)
            content.enableReadMode()
            select(item)
            setBackgroundColor(item.backgroundColor)
        } else {
            content.enableEditMode()

            select(item)

            content.clearTextWatchers()

            content.setText(item.text)

            setBackgroundColor(item.backgroundColor)

            setCursor(item)
            setFocus(item)

            content.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )

            content.setOnFocusChangeListener { _, focused ->
                item.isFocused = focused
                onFocusChanged(item.id, focused)
                if (Build.VERSION.SDK_INT == N || Build.VERSION.SDK_INT == N_MR1) {
                    if (focused) {
                        imm().showSoftInput(content, InputMethodManager.SHOW_FORCED)
                    }
                }
            }

            // TODO add backspace detector

            content.selectionWatcher = { onSelectionChanged(item.id, it) }
        }

        content.setOnClickListener {
            onTextInputClicked(item.id)
            if (Build.VERSION.SDK_INT == N || Build.VERSION.SDK_INT == N_MR1) {
                content.context.imm().showSoftInput(content, InputMethodManager.SHOW_FORCED)
            }
        }

        menu.setOnClickListener {
            clicked(ListenerType.Code.SelectLanguage(item.id))
        }

        if (!item.lang.isNullOrEmpty()) {
            content.setupSyntax(item.lang)
            menu.text = item.lang!!.capitalize()
        } else {
            content.setupSyntax(Syntaxes.GENERIC)
            menu.setText(R.string.block_code_menu_title)
        }
    }

    fun indentize(item: BlockView.Indentable) {
        root.updateLayoutParams<RecyclerView.LayoutParams> {
            leftMargin = item.indent * dimen(R.dimen.indent)
        }
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.Code,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
    ) = payloads.forEach { payload ->

        Timber.d("Processing $payload for new view:\n$item")

        if (payload.textChanged()) {
            content.pauseTextWatchers { content.setText(item.text) }
        }

        if (payload.readWriteModeChanged()) {
            content.pauseTextWatchers {
                if (item.mode == BlockView.Mode.EDIT) {
                    content.apply {
                        clearTextWatchers()
                        addTextChangedListener(
                            DefaultTextWatcher { text -> onTextChanged(item.id, text) }
                        )
                        selectionWatcher = { onSelectionChanged(item.id, it) }
                    }
                    content.enableEditMode()
                } else {
                    content.enableReadMode()
                }
            }
        }

        if (payload.selectionChanged()) {
            select(item)
        }

        if (payload.focusChanged()) {
            setFocus(item)
        }

        if (payload.isCursorChanged) {
            setCursor(item = item)
        }

        if (payload.backgroundColorChanged()) {
            setBackgroundColor(item.backgroundColor)
        }

        if (payload.isIndentChanged) {
            indentize(item)
        }
    }

    fun select(item: BlockView.Selectable) {
        binding.selected.isSelected = item.isSelected
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
            post {
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

    private fun setCursor(item: BlockView.Code) {
        if (item.isFocused) {
            Timber.d("Setting cursor: $item")
            item.cursor?.let {
                val length = content.text?.length ?: 0
                if (it in 0..length) {
                    content.setSelection(it)
                }
            }
        }
    }

    private fun setBackgroundColor(color: String? = null) {
        val value = ThemeColor.values().find { value -> value.code == color }
        if (value != null && value != ThemeColor.DEFAULT) {
            (root.background as? ColorDrawable)?.color = root.resources.veryLight(value, 0)
        } else {
            val defaultBackgroundColor =
                content.context.resources.getColor(R.color.shape_tertiary, null)
            (root.background as? ColorDrawable)?.color = defaultBackgroundColor
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
}