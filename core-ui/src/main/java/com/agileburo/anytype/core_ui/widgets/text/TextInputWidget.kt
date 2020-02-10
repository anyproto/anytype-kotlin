package com.agileburo.anytype.core_ui.widgets.text

import android.content.Context
import android.graphics.Canvas
import android.text.Spanned
import android.text.TextWatcher
import android.text.util.Linkify
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.graphics.withTranslation
import com.agileburo.anytype.core_ui.extensions.toast
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import timber.log.Timber

class TextInputWidget : AppCompatEditText {

    private val watchers: MutableList<TextWatcher> = mutableListOf()
    private var textRoundedBgHelper: TextRoundedBgHelper? = null

    var selectionDetector: ((IntRange) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setupKeyboardMarkupHelpers(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        setupKeyboardMarkupHelpers(context, attrs)
    }

    private fun setupKeyboardMarkupHelpers(
        context: Context,
        attrs: AttributeSet
    ) {
        val attributeReader = TextRoundedBgAttributeReader(context, attrs)
        textRoundedBgHelper = TextRoundedBgHelper(
            horizontalPadding = attributeReader.horizontalPadding,
            verticalPadding = attributeReader.verticalPadding,
            drawable = attributeReader.drawable,
            drawableLeft = attributeReader.drawableLeft,
            drawableMid = attributeReader.drawableMid,
            drawableRight = attributeReader.drawableRight
        )
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

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        Timber.d("New selection: $selStart - $selEnd")
        selectionDetector?.invoke(selStart..selEnd)
        super.onSelectionChanged(selStart, selEnd)
    }

    override fun onDraw(canvas: Canvas?) {
        // need to draw bg first so that text can be on top during super.onDraw()
        if (text is Spanned && layout != null) {
            canvas?.withTranslation(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat()) {
                textRoundedBgHelper?.draw(canvas, text as Spanned, layout)
            }
        }
        super.onDraw(canvas)
    }

    fun setLinksClickable() {
        //makeLinksActive()
    }

    /**
     *  Makes all links in the TextView object active.
     */
    private fun makeLinksActive() {
        BetterLinkMovementMethod.linkify(Linkify.ALL, this)
            .setOnLinkClickListener { textView, url ->
                textView.context.toast("On link click $url")
                false
            }
    }
}