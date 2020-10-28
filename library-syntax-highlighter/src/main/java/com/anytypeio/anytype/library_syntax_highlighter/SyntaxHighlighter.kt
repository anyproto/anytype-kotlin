package com.anytypeio.anytype.library_syntax_highlighter

import android.text.Editable
import android.text.Spannable

/**
 * @property [source] text containing source code, whose syntax we need to highlight
 * @property [rules] set of syntax rules for a programming language
 */
interface SyntaxHighlighter {

    val source: Editable
    val rules: MutableList<Syntax>

    fun addRules(new: List<Syntax>) {
        rules.apply {
            clear()
            addAll(new)
        }
    }

    fun highlight() {
        clear()
        rules.forEach { syntax ->
            val matcher = syntax.matcher(source.toString())
            while (matcher.find()) {
                source.setSpan(
                    SyntaxColorSpan(syntax.color),
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    fun clear() {
        val current = source.getSpans(0, source.length, SyntaxColorSpan::class.java)
        current.forEach { span -> source.removeSpan(span) }
    }
}