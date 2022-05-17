package com.anytypeio.anytype.core_ui.widgets.text

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ReplacementSpan
import kotlin.math.max

class MonospaceTabTextWatcher(
    private val oneLetterWidth: Float
) : TextWatcher {

    private var start = 0
    private var end = 0

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        this.start = start
        end = start + max(count, before)
    }

    override fun afterTextChanged(s: Editable) {
        applyTabWidth(s, start, end)
    }

    /**
     * Function gets the substring and extracts ALL lines that intersects from
     * [start] to [end] in this way:
     * 1. Find a line that starts before [start],
     * or include -1 (fake start index if we inside first line)
     * 2. Find all "\n" characters indexes in the substring of [text] from [start] to [end]
     * 3. Find a line that starts after [end],
     * or include text.length (fake end index if we inside last line)
     */
    private fun getAllNewLineCharacterIndexes(text: Editable, start: Int, end: Int): List<Int> {

        val newLineCharacterIndices = mutableListOf<Int>()
        val firstNewLineCharacterIndex = text.lastIndexOf('\n', start)

        when {
            firstNewLineCharacterIndex == 0 -> {
                newLineCharacterIndices.add(-1)
                newLineCharacterIndices.add(firstNewLineCharacterIndex)
            }
            firstNewLineCharacterIndex > 0 -> {
                newLineCharacterIndices.add(firstNewLineCharacterIndex)
            }
            else -> {
                newLineCharacterIndices.add(-1)
            }
        }
        var searchFromIndex: Int = if (firstNewLineCharacterIndex == start) start + 1 else start
        if (searchFromIndex == end) {
            return newLineCharacterIndices
        }
        do {
            val nextNewLineCharacterIndex = text.indexOf('\n', searchFromIndex)
            if (nextNewLineCharacterIndex > 0) {
                newLineCharacterIndices.add(nextNewLineCharacterIndex)
                searchFromIndex = nextNewLineCharacterIndex + 1
            } else {
                newLineCharacterIndices.add(text.length)
                break
            }
        } while (searchFromIndex in 0..end)
        return newLineCharacterIndices
    }

    private fun fixTabsInSingleLine(text: Editable, start: Int, end: Int) {
        var tabsBeforeCurrentAmount = 0
        var tabsBeforeCurrentWidthSum = 0
        var start = start
        val offset = start
        while (start <= end) {
            val tabIndex = text.indexOf("\t", start)
            if (tabIndex < 0 || tabIndex > end) break

            val tabWidth = if (tabsBeforeCurrentAmount == 0) {
                TAB_SIZE - (tabIndex - offset) % TAB_SIZE
            } else {
                TAB_SIZE - (tabIndex - offset - tabsBeforeCurrentAmount +
                        tabsBeforeCurrentWidthSum) % TAB_SIZE
            }

            tabsBeforeCurrentWidthSum += tabWidth
            tabsBeforeCurrentAmount++

            text.getSpans(tabIndex, tabIndex + 1, CustomTabWidthSpan::class.java)
                .forEach { text.removeSpan(it) }

            text.setSpan(
                CustomTabWidthSpan((tabWidth * oneLetterWidth).toInt()),
                tabIndex,
                tabIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            start = tabIndex + 1
        }
    }

    private fun applyTabWidth(text: Editable, start: Int, end: Int) {
        if (text.isEmpty()) return
        val allNewLineCharacterIndexes = getAllNewLineCharacterIndexes(text, start, end)
        for (i in 1 until allNewLineCharacterIndexes.size) {
            val start = allNewLineCharacterIndexes[i - 1] + 1
            val end = allNewLineCharacterIndexes[i] - 1
            fixTabsInSingleLine(text, start, end)
        }
    }
}

internal class CustomTabWidthSpan(val tabWidth: Int) : ReplacementSpan() {
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return tabWidth
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
    }
}

private const val TAB_SIZE = 4