package com.anytypeio.anytype.core_ui.widgets.text

import android.R.id.paste
import android.R.id.copy
import android.content.Context
import android.graphics.Canvas
import android.text.InputType
import android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
import android.text.Spanned
import android.text.TextWatcher
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.DragEvent
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.graphics.withTranslation
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.tools.*
import com.anytypeio.anytype.core_ui.widgets.text.highlight.HighlightAttributeReader
import com.anytypeio.anytype.core_ui.widgets.text.highlight.HighlightDrawer
import com.anytypeio.anytype.core_utils.ext.imm
import com.anytypeio.anytype.core_utils.ext.multilineIme
import timber.log.Timber

class TextInputWidget : AppCompatEditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup()
        setupHighlightHelpers(context, attrs)
        setOnLongClickListener { view -> view != null && !view.hasFocus() }
        context.obtainStyledAttributes(attrs, R.styleable.TextInputWidget).apply {
            ignoreDragAndDrop = getBoolean(R.styleable.TextInputWidget_ignoreDragAndDrop, false)
            recycle()
        }
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        setup()
        setupHighlightHelpers(context, attrs)
        setOnLongClickListener { view -> view != null && !view.hasFocus() }
        context.obtainStyledAttributes(attrs, R.styleable.TextInputWidget).apply {
            ignoreDragAndDrop = getBoolean(R.styleable.TextInputWidget_ignoreDragAndDrop, false)
            recycle()
        }
    }

    private var ignoreDragAndDrop = false

    val editorTouchProcessor by lazy {
        EditorTouchProcessor(
            fallback = { e -> super.onTouchEvent(e) }
        )
    }

    private val watchers: MutableList<TextWatcher> = mutableListOf()

    private var highlightDrawer: HighlightDrawer? = null

    var selectionWatcher: ((IntRange) -> Unit)? = null

    var clipboardInterceptor: ClipboardInterceptor? = null

    /**
     * Returns a bool value, indicating whether or not to absorb the click
     */
    var backButtonWatcher: (() -> Boolean)? = null

    private var isSelectionWatcherBlocked = false

    private fun setup() {
        enableEditMode()
    }

    fun enableEditMode() {
        multilineIme(
            action = TEXT_INPUT_WIDGET_ACTION_GO,
            inputType = TEXT_INPUT_WIDGET_INPUT_TYPE
        )
        setTextIsSelectable(true)
    }

    override fun dispatchDragEvent(event: DragEvent?): Boolean {
        return super.dispatchDragEvent(event)
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        return if (event != null
            && event.keyCode == KeyEvent.KEYCODE_BACK
            && event.action == KeyEvent.ACTION_UP
            && backButtonWatcher?.invoke() == true
        ) {
            true
        } else {
            super.onKeyPreIme(keyCode, event)
        }
    }

    fun enableReadMode() {
        pauseTextWatchers {
            inputType = InputType.TYPE_NULL
            setRawInputType(InputType.TYPE_NULL)
            maxLines = Integer.MAX_VALUE
            setHorizontallyScrolling(false)
            setTextIsSelectable(false)
        }
    }

    private fun setupHighlightHelpers(context: Context, attrs: AttributeSet) {
        HighlightAttributeReader(context, attrs).let { reader ->
            highlightDrawer = HighlightDrawer(
                horizontalPadding = reader.horizontalPadding,
                verticalPadding = reader.verticalPadding,
                drawable = reader.drawable,
                drawableLeft = reader.drawableLeft,
                drawableMid = reader.drawableMid,
                drawableRight = reader.drawableRight
            )
        }
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

    fun dismissMentionWatchers() {
        watchers.filterIsInstance(MentionTextWatcher::class.java).forEach { it.onDismiss() }
    }

    fun pauseTextWatchers(block: () -> Unit) = synchronized(this) {
        lockTextWatchers()
        block()
        unlockTextWatchers()
    }

    fun pauseSelectionWatcher(block: () -> Unit) = synchronized(this) {
        isSelectionWatcherBlocked = true
        block()
        isSelectionWatcherBlocked = false
    }

    fun pauseFocusChangeListener(block: () -> Unit) = synchronized(this) {
        val listener = onFocusChangeListener
        if (listener is LockableFocusChangeListener) {
            listener.lock()
        }
        block()
        if (listener is LockableFocusChangeListener) {
            listener.unlock()
        }
    }

    private fun lockTextWatchers() {
        watchers.forEach { watcher ->
            if (watcher is DefaultTextWatcher) watcher.lock()
            if (watcher is SlashTextWatcher) watcher.lock()
            if (watcher is MentionTextWatcher) watcher.lock()
        }
    }

    private fun unlockTextWatchers() {
        watchers.forEach { watcher ->
            if (watcher is DefaultTextWatcher) watcher.unlock()
            if (watcher is SlashTextWatcher) watcher.unlock()
            if (watcher is MentionTextWatcher) watcher.unlock()
        }
    }

    /**
     * Send selection event only for blocks in focus state
     */
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (isFocused && !isSelectionWatcherBlocked) {
            Timber.d("New selection: $selStart - $selEnd")
            selectionWatcher?.invoke(selStart..selEnd)
        } else {
            //Timber.d("Ignored selection change: focused: $isFocused, watcherBlocked: $isSelectionWatcherBlocked")
        }
        super.onSelectionChanged(selStart, selEnd)
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        if (clipboardInterceptor == null) {
            return super.onTextContextMenuItem(id)
        }

        var consumed = false

        when (id) {
            paste -> {
                if (clipboardInterceptor != null) {
                    clipboardInterceptor?.onClipboardAction(
                        ClipboardInterceptor.Action.Paste(
                            selection = selectionStart..selectionEnd
                        )
                    )
                    consumed = true
                }
            }
            copy -> {
                if (clipboardInterceptor != null) {
                    clipboardInterceptor?.onClipboardAction(
                        ClipboardInterceptor.Action.Copy(
                            selection = selectionStart..selectionEnd
                        )
                    )
                    consumed = true
                }
            }
        }

        return if (!consumed) {
            super.onTextContextMenuItem(id)
        } else {
            consumed
        }
    }

    override fun onDraw(canvas: Canvas?) {
        // need to draw bg first so that text can be on top during super.onDraw()
        if (text is Spanned && layout != null) {
            canvas?.withTranslation(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat()) {
                highlightDrawer?.draw(canvas, text as Spanned, layout, context.resources)
            }
        }
        super.onDraw(canvas)
    }

    fun setLinksClickable() {
        makeLinksActive()
    }

    fun setDefaultMovementMethod() {
        movementMethod = defaultMovementMethod
    }

    fun setFocus() {
        // Scheduling a runnable that shows the keyboard in the next UI loop.
        post {
            this.apply {
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

    fun enableEnterKeyDetector(
        onEnterClicked: (IntRange) -> Unit
    ) {
        setOnEditorActionListener { v, actionId, _ ->
            if (actionId == TEXT_INPUT_WIDGET_ACTION_GO) {
                onEnterClicked.invoke(v.selectionStart..v.selectionEnd)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (hasFocus()) return super.onTouchEvent(event)
        return editorTouchProcessor.process(this, event)
    }

    override fun onDragEvent(event: DragEvent?): Boolean {
        return if (ignoreDragAndDrop)
            true
        else
            super.onDragEvent(event)
    }

    /**
     *  Makes all links in the TextView object active.
     */
    private fun makeLinksActive() {
        Linkify.addLinks(this, Linkify.ALL)
        movementMethod = CustomBetterLinkMovementMethod
    }

    companion object {
        const val TEXT_INPUT_WIDGET_ACTION_GO = EditorInfo.IME_ACTION_GO
        const val TEXT_INPUT_WIDGET_INPUT_TYPE =
            TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
    }
}