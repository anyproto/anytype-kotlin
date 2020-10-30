package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.text.Editable
import android.text.InputType.*
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.library_syntax_highlighter.*
import timber.log.Timber

class CodeTextInputWidget : AppCompatEditText, SyntaxHighlighter {

    override val rules: MutableList<Syntax> = mutableListOf()
    override val source: Editable get() = editableText

    private val syntaxTextWatcher = SyntaxTextWatcher { highlight() }

    private val watchers: MutableList<TextWatcher> = mutableListOf()

    var selectionWatcher: ((IntRange) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup()
        setupSyntaxHighlighter()
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        setup()
        setupSyntaxHighlighter()
    }

    private fun setup() {
        enableEditMode()
    }

    private fun setupSyntaxHighlighter() {
        addRules(context.obtainSyntaxRules(Syntaxes.KOTLIN))
        highlight()
        addTextChangedListener(syntaxTextWatcher)
    }

    fun enableEditMode() {
        setRawInputType(TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_MULTI_LINE or TYPE_TEXT_FLAG_NO_SUGGESTIONS)
        setHorizontallyScrolling(false)
        maxLines = Integer.MAX_VALUE
        setTextIsSelectable(true)
    }

    fun enableReadMode() {
        setRawInputType(TYPE_NULL)
        maxLines = Integer.MAX_VALUE
        setHorizontallyScrolling(false)
        setTextIsSelectable(false)
    }

    override fun addTextChangedListener(watcher: TextWatcher) {
        if (watcher !is SyntaxTextWatcher) watchers.add(watcher)
        super.addTextChangedListener(watcher)
    }

    override fun removeTextChangedListener(watcher: TextWatcher) {
        watchers.remove(watcher)
        super.removeTextChangedListener(watcher)
    }

    fun clearTextWatchers() {
        watchers.forEach { super.removeTextChangedListener(it) }
        watchers.clear()
    }

    fun pauseTextWatchers(block: () -> Unit) = synchronized(this) {
        lockTextWatchers()
        block()
        unlockTextWatchers()
    }

    private fun lockTextWatchers() {
        watchers.forEach { watcher ->
            if (watcher is DefaultTextWatcher) watcher.lock()
        }
    }

    private fun unlockTextWatchers() {
        watchers.forEach { watcher ->
            if (watcher is DefaultTextWatcher) watcher.unlock()
        }
    }

    /**
     * Send selection event only for blocks in focus state
     */
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (isFocused) {
            Timber.d("New selection: $selStart - $selEnd")
            selectionWatcher?.invoke(selStart..selEnd)
        }
        super.onSelectionChanged(selStart, selEnd)
    }

    override fun setupSyntax(lang: String?) {
        if (lang == null) {
            rules.clear()
            clearHighlights()
        } else {
            val result = context.obtainSyntaxRules(lang)
            if (result.isEmpty()) {
                addRules(context.obtainGenericSyntaxRules())
            } else {
                addRules(result)
            }
            highlight()
        }
    }
}