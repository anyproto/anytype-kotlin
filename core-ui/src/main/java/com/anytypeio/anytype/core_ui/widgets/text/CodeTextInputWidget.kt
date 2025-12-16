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
import android.view.ViewConfiguration
import android.widget.HorizontalScrollView
import androidx.appcompat.widget.AppCompatEditText
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.tools.TextInputTextWatcher
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

    private val watchers: MutableList<TextInputTextWatcher> = mutableListOf()

    var selectionWatcher: ((IntRange) -> Unit)? = null

    val editorTouchProcessor by lazy {
        EditorTouchProcessor(
            touchSlop = ViewConfiguration.get(context).scaledTouchSlop,
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
        super.addTextChangedListener(newlineScrollResetWatcher)
    }

    private val newlineScrollResetWatcher = NewlineScrollResetWatcher {
        (parent as? HorizontalScrollView)?.scrollTo(0, 0)
    }

    private fun setupSyntaxHighlighter() {
        runCatching {
            addRules(context.obtainSyntaxRules(Syntaxes.KOTLIN))
            highlight()
        }.onFailure {
            Timber.e(it, "Error while setting up syntax highlighter")
        }
        super.addTextChangedListener(syntaxTextWatcher)
    }

    fun enableEditMode() {
        setRawInputType(TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_MULTI_LINE or TYPE_TEXT_FLAG_NO_SUGGESTIONS)
        setHorizontallyScrolling(false)
        maxLines = Integer.MAX_VALUE
        setTextIsSelectable(true)
        isCursorVisible = hasFocus()
    }

    fun enableReadMode() {
        setRawInputType(TYPE_NULL)
        maxLines = Integer.MAX_VALUE
        setHorizontallyScrolling(false)
        setTextIsSelectable(false)
    }

    override fun addTextChangedListener(watcher: TextWatcher) {
        if (watcher is TextInputTextWatcher) {
            watchers.add(watcher)
        }
        super.addTextChangedListener(watcher)
    }

    override fun removeTextChangedListener(watcher: TextWatcher) {
        if (watcher is TextInputTextWatcher) {
            watchers.remove(watcher)
        }
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
            watcher.lock()
        }
    }

    private fun unlockTextWatchers() {
        watchers.forEach { watcher ->
            watcher.unlock()
        }
    }

    /**
     * Send selection event only for blocks in focus state
     */

    @Volatile
    private var isSelectionWatcherBlocked = false

    fun pauseSelectionWatcher(block: () -> Unit) {
        isSelectionWatcherBlocked = true
        try {
            block()
        } finally {
            isSelectionWatcherBlocked = false
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (isFocused && !isSelectionWatcherBlocked) {
            selectionWatcher?.invoke(selStart..selEnd)
        }
        super.onSelectionChanged(selStart, selEnd)
    }

    override fun setupSyntax(lang: String?) {
        runCatching {
            if (lang == null || lang.equals("plain", ignoreCase = true)) {
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
        }.onFailure {
            Timber.e(it, "Error while setting syntax rules.")
        }
    }

    override fun onDragEvent(event: DragEvent?): Boolean = true

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (hasFocus()) return super.onTouchEvent(event)
        return editorTouchProcessor.process(this, event)
    }
}

private const val MEASURING_CHAR = " "