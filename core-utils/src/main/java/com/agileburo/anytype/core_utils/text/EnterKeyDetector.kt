package com.agileburo.anytype.core_utils.text

import android.text.InputFilter
import android.text.Spanned

class DefaultEnterKeyDetector(
    private val onEndLineEnterClicked: () -> Unit,
    private val onSplitLineEnterClicked: (Int) -> Unit
) : EnterKeyDetector() {

    override fun onEndEnterPress(textBeforeEnter: Spanned): CharSequence? {
        onEndLineEnterClicked()
        return EMPTY_REPLACEMENT
    }

    override fun onSplitEnterPress(textBeforeEnter: Spanned, index: Int): CharSequence? {
        onSplitLineEnterClicked(index)
        return EMPTY_REPLACEMENT
    }
}

abstract class EnterKeyDetector : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val enterPressed = source == "\n" && end - start == 1
        return when {
            enterPressed -> {
                if (dend - 1 == dest.lastIndex)
                    onEndEnterPress(textBeforeEnter = dest)
                else
                    onSplitEnterPress(textBeforeEnter = dest, index = dend)
            }
            else -> null
        }
    }

    abstract fun onEndEnterPress(textBeforeEnter: Spanned): CharSequence?
    abstract fun onSplitEnterPress(textBeforeEnter: Spanned, index: Int): CharSequence?

    companion object {
        const val EMPTY_REPLACEMENT = ""
    }
}