package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.os.Build.VERSION_CODES.N_MR1
import android.text.Editable
import android.view.View
import android.view.ViewConfiguration
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockCodeSnippetBinding
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.provide
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.widgets.text.CodeTextInputWidget
import com.anytypeio.anytype.core_utils.ext.imm
import com.anytypeio.anytype.library_syntax_highlighter.Syntaxes
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.Focusable
import timber.log.Timber

class Code(
    val binding: ItemBlockCodeSnippetBinding
) : BlockViewHolder(binding.root),
    BlockViewHolder.DragAndDropHolder,
    DecoratableViewHolder
{

    val menu: TextView
        get() = binding.codeMenu
    val root: View
        get() = itemView
    val content: CodeTextInputWidget
        get() = binding.snippet

    val editorTouchProcessor = EditorTouchProcessor(
        touchSlop = ViewConfiguration.get(itemView.context).scaledTouchSlop,
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    override val decoratableContainer: EditorDecorationContainer
        get() = binding.decorationContainer

    init {
        content.setOnTouchListener { v, e ->
            if (content.hasFocus()) {
                false // Let the widget handle it via onTouchEvent for proper text selection
            } else {
                editorTouchProcessor.process(v, e)
            }
        }
    }

    fun setupViewHolder(
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit
    ) {
        content.clearTextWatchers()
        content.addTextChangedListener(createTextWatcher(onTextChanged))

        content.setOnFocusChangeListener { _, focused ->
            content.isCursorVisible = focused
            provide<BlockView.Code>()?.let { item ->
                onFocusChanged(item.id, focused)
            }
        }

        content.selectionWatcher = { selection ->
            provide<BlockView.Code>()?.let { item ->
                onSelectionChanged(item.id, selection)
            }
        }
    }

    fun bind(
        item: BlockView.Code,
        clicked: (ListenerType) -> Unit,
        onTextInputClicked: (String) -> Unit
    ) {
        if (item.mode == BlockView.Mode.READ) {
            content.setText(item.text)
            content.enableReadMode()
            select(item)
            setBackgroundColor(item.background)
        } else {
            content.pauseTextWatchers {
                content.enableEditMode()
            }

            select(item)

            content.pauseSelectionWatcher {
                content.pauseTextWatchers {
                    content.setText(item.text)
                }
            }

            setBackgroundColor(item.background)

            setCursor(item)
            setFocus(item)
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

        handleCodeLanguageChange(item)
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.Code,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
    ) = payloads.forEach { payload ->

        Timber.d("Processing $payload for new view:\n$item")

        if (payload.textChanged()) {
            content.pauseSelectionWatcher {
                content.pauseTextWatchers {
                    content.setText(item.text)
                    content.clearHighlights()
                    content.highlight()
                }
            }
        }

        if (payload.readWriteModeChanged()) {
            content.pauseTextWatchers {
                if (item.mode == BlockView.Mode.EDIT) {
                    content.apply {
                        clearTextWatchers()
                        addTextChangedListener(createTextWatcher(onTextChanged))
                        selectionWatcher = { onSelectionChanged(item.id, it) }
                    }
                    content.enableEditMode()
                } else {
                    content.enableReadMode()
                }
            }
            setBackgroundColor(item.background)
        }

        if (payload.selectionChanged()) {
            select(item)
            setBackgroundColor(item.background)
        }

        if (payload.focusChanged()) {
            setFocus(item)
        }

        if (payload.isCursorChanged) {
            setCursor(item = item)
        }

        if (payload.backgroundColorChanged()) {
            setBackgroundColor(item.background)
        }

        if (payload.codeLanguageChanged()) {
            handleCodeLanguageChange(item)
        }
    }

    /**
     * Handles setting up the syntax highlighting and menu text based on the code language.
     */
    private fun handleCodeLanguageChange(item: BlockView.Code) {
        val lang = item.lang
        if (lang.isNullOrEmpty() || lang.equals("plain", ignoreCase = true)) {
            content.setupSyntax(Syntaxes.PLAIN)
            menu.setText(R.string.block_code_plain_text)
        } else {
            content.setupSyntax(lang)
            menu.text = lang.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
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

    private fun createTextWatcher(
        onTextChanged: (String, Editable) -> Unit
    ): DefaultTextWatcher = DefaultTextWatcher { text ->
        provide<BlockView.Code>()?.let { item ->
            if (item.mode == BlockView.Mode.EDIT) {
                item.text = text.toString()
                onTextChanged(item.id, text)
            }
        }
    }

    private fun setBackgroundColor(background: ThemeColor) {
        if (background != ThemeColor.DEFAULT) {
            (binding.content.background as? ColorDrawable)?.color = root.resources.veryLight(background, 0)
        } else {
            val defaultBackgroundColor =
                content.context.resources.getColor(R.color.shape_tertiary, null)
            (binding.content.background as? ColorDrawable)?.color = defaultBackgroundColor
        }
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        decoratableContainer.decorate(decorations) { rect ->
            binding.content.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = rect.left
                marginEnd = rect.right
                bottomMargin = rect.bottom
            }
        }
    }
}