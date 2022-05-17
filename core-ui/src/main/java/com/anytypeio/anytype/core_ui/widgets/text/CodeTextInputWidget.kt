package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.text.Editable
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_NULL
import android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.DragEvent
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.library_syntax_highlighter.Syntax
import com.anytypeio.anytype.library_syntax_highlighter.SyntaxHighlighter
import com.anytypeio.anytype.library_syntax_highlighter.SyntaxTextWatcher
import com.anytypeio.anytype.library_syntax_highlighter.Syntaxes
import com.anytypeio.anytype.library_syntax_highlighter.obtainGenericSyntaxRules
import com.anytypeio.anytype.library_syntax_highlighter.obtainSyntaxRules
import timber.log.Timber

class CodeTextInputWidget : AppCompatEditText, SyntaxHighlighter {

    override val rules: MutableList<Syntax> = mutableListOf()
    override val source: Editable get() = editableText

    private val syntaxTextWatcher = SyntaxTextWatcher { highlight() }

    private val watchers: MutableList<TextWatcher> = mutableListOf()

    var selectionWatcher: ((IntRange) -> Unit)? = null

    val editorTouchProcessor by lazy {
        EditorTouchProcessor(
            fallback = { e -> super.onTouchEvent(e) }
        )
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup()
        setupSyntaxHighlighter()
        setOnLongClickListener { view -> view != null && !view.hasFocus() }
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        setup()
        setupSyntaxHighlighter()
        setOnLongClickListener { view -> view != null && !view.hasFocus() }
    }

    private fun setup() {
        enableEditMode()
        super.addTextChangedListener(MonospaceTabTextWatcher(paint.measureText(MEASURING_CHAR)))
    }

    private fun setupSyntaxHighlighter() {
        addRules(context.obtainSyntaxRules(Syntaxes.KOTLIN))
        highlight()
        super.addTextChangedListener(syntaxTextWatcher)
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
        watchers.add(watcher)
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

    override fun onDragEvent(event: DragEvent?): Boolean = true

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (hasFocus()) return super.onTouchEvent(event)
        return editorTouchProcessor.process(this, event)
    }
}

private const val MEASURING_CHAR = " "