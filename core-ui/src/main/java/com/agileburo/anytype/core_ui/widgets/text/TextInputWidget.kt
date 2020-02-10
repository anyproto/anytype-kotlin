package com.agileburo.anytype.core_ui.widgets.text

import android.content.Context
import android.text.TextWatcher
import android.text.util.Linkify
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.agileburo.anytype.core_ui.extensions.toast
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import timber.log.Timber

class TextInputWidget : AppCompatEditText {

    private val watchers: MutableList<TextWatcher> = mutableListOf()

    var selectionDetector: ((IntRange) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

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

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        Timber.d("New selection: $selStart - $selEnd")
        selectionDetector?.invoke(selStart..selEnd)
        super.onSelectionChanged(selStart, selEnd)
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