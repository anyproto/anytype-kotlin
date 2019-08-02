package com.agileburo.anytype.feature_editor.ui

import android.graphics.Typeface
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.agileburo.anytype.feature_editor.R

class TextStyleCallback(
    private val editText: ClearFocusEditText,
    private val linkClick: (EditText, Int, Int) -> Unit
) : ActionMode.Callback {

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        val ssb = SpannableStringBuilder(editText.text)
        when (item?.itemId) {
            R.id.boldInactive -> {
                ssb.setSpan(StyleSpan(Typeface.BOLD), start, end, 1)
                editText.text = ssb
                return true
            }
            R.id.boldActive -> {
                removeStyleSpanFromSubstring(
                    style = Typeface.BOLD, start = start,
                    end = end, spannable = ssb
                )?.let {
                    editText.text = it
                }
                return true
            }
            R.id.italicInactive -> {
                ssb.setSpan(StyleSpan(Typeface.ITALIC), start, end, 1)
                editText.text = ssb
                return true
            }
            R.id.italicActive -> {
                removeStyleSpanFromSubstring(
                    style = Typeface.ITALIC, start = start,
                    end = end, spannable = ssb
                )?.let {
                    editText.text = it
                }
                return true
            }
            R.id.strikeInactive -> {
                ssb.setSpan(StrikethroughSpan(), start, end, 1)
                editText.text = ssb
                return true
            }
            R.id.strikeActive -> {
                removeStrikeSpanFromSubstring(
                    start = start, end = end, spannable = ssb
                )?.let {
                    editText.text = it
                }
                return true
            }
            R.id.linkInactive -> {
                linkClick.invoke(editText, start, end)
                return true
            }
            R.id.linkActive -> {
                removeUrlSpanFromSubstring(
                    start = start, end = end, spannable = ssb
                )?.let {
                    editText.text = it
                }
                return true
            }
        }
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.let {
            it.inflate(R.menu.style_toolbar, menu)
            editText.text?.getSpans(
                editText.selectionStart,
                editText.selectionEnd,
                Object::class.java
            )?.forEach { span ->
                if (span is StyleSpan) {
                    if (span.style == Typeface.BOLD) {
                        changeMenuItemsVisibility(
                            menu = menu,
                            inactiveId = R.id.boldInactive,
                            activeId = R.id.boldActive
                        )
                    }
                    if (span.style == Typeface.ITALIC) {
                        changeMenuItemsVisibility(
                            menu = menu,
                            inactiveId = R.id.italicInactive,
                            activeId = R.id.italicActive
                        )
                    }
                }
                if (span is StrikethroughSpan) {
                    changeMenuItemsVisibility(
                        menu = menu,
                        inactiveId = R.id.strikeInactive,
                        activeId = R.id.strikeActive
                    )
                }

                if (span is URLSpan) {
                    changeMenuItemsVisibility(
                        menu = menu,
                        inactiveId = R.id.linkInactive,
                        activeId = R.id.linkActive
                    )
                }
            }
            return true
        }
        return false
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
    }

    private fun changeMenuItemsVisibility(menu: Menu?, inactiveId: Int, activeId: Int) {
        menu?.findItem(inactiveId)?.isVisible = false
        menu?.findItem(activeId)?.isVisible = true
    }

    private fun removeStyleSpanFromSubstring(
        style: Int,
        start: Int,
        end: Int,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder? {
        spannable.getSpans(start, end, StyleSpan::class.java)?.forEach { span ->
            if (span.style == style) {
                return SpannableStringBuilder().apply {
                    if (start > 0) append(spannable.subSequence(0, start))

                    append(
                        SpannableStringBuilder(spannable, start, end)
                            .apply { removeSpan(span) }
                    )

                    if (end < spannable.length) {
                        append(spannable.subSequence(end, spannable.length))
                    }
                }
            }
        }
        return null
    }

    private fun removeStrikeSpanFromSubstring(
        start: Int,
        end: Int,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder? {
        spannable.getSpans(start, end, StrikethroughSpan::class.java)?.forEach { span ->
            return SpannableStringBuilder().apply {
                if (start > 0) append(spannable.subSequence(0, start))

                append(
                    SpannableStringBuilder(spannable, start, end)
                        .apply { removeSpan(span) }
                )

                if (end < spannable.length) {
                    append(spannable.subSequence(end, spannable.length))
                }
            }
        }
        return null
    }

    private fun removeUrlSpanFromSubstring(
        start: Int,
        end: Int, spannable: SpannableStringBuilder
    ): SpannableStringBuilder? {
        spannable.getSpans(start, end, URLSpan::class.java)?.forEach { span ->
            return SpannableStringBuilder().apply {
                if (start > 0) append(spannable.subSequence(0, start))

                append(
                    SpannableStringBuilder(spannable, start, end)
                        .apply { removeSpan(span) }
                )

                if (end < spannable.length) {
                    append(spannable.subSequence(end, spannable.length))
                }
            }
        }
        return null
    }
}