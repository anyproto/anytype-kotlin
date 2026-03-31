package com.anytypeio.anytype.core_ui.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.anytypeio.anytype.core_models.Id

/**
 * Shared span model for rich-text input boxes (chat, discussions, etc.).
 */
sealed class InputSpan {
    abstract val start: Int
    abstract val end: Int
    abstract val style: SpanStyle

    data class Mention(
        override val style: SpanStyle,
        override val start: Int,
        override val end: Int,
        val param: Id
    ) : InputSpan()

    data class Markup(
        override val style: SpanStyle,
        override val start: Int,
        override val end: Int,
        val type: Int
    ) : InputSpan() {
        companion object {
            const val BOLD = 0
            const val ITALIC = 1
            const val STRIKETHROUGH = 2
            const val UNDERLINE = 3
            const val CODE = 4
        }
    }
}

/**
 * [VisualTransformation] that applies [InputSpan] styles to the text field content.
 * Handles multiple spans on the same range by combining text decorations.
 */
class AnnotatedTextTransformation(
    private val spans: List<InputSpan>
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotatedString = AnnotatedString.Builder(text).apply {
            val processedRanges = mutableSetOf<Int>()

            spans.forEachIndexed { index, span ->
                if (index in processedRanges) return@forEachIndexed
                if (span.start !in text.indices || span.end > text.length) return@forEachIndexed

                val sameRangeSpans = mutableListOf(span)
                for (i in (index + 1) until spans.size) {
                    val other = spans[i]
                    if (other.start == span.start && other.end == span.end) {
                        sameRangeSpans.add(other)
                        processedRanges.add(i)
                    }
                }

                val decorations = sameRangeSpans.mapNotNull { it.style.textDecoration }

                if (decorations.size > 1) {
                    val combinedDecoration = TextDecoration.combine(decorations)
                    sameRangeSpans.forEach { s ->
                        if (s.style.textDecoration == null) {
                            addStyle(s.style, span.start, span.end)
                        } else {
                            val nonDecorationStyle = s.style.copy(textDecoration = null)
                            if (nonDecorationStyle != SpanStyle()) {
                                addStyle(nonDecorationStyle, span.start, span.end)
                            }
                        }
                    }
                    addStyle(SpanStyle(textDecoration = combinedDecoration), span.start, span.end)
                } else {
                    sameRangeSpans.forEach { s ->
                        addStyle(s.style, span.start, span.end)
                    }
                }
            }
        }.toAnnotatedString()

        return TransformedText(annotatedString, offsetMapping = OffsetMapping.Identity)
    }
}

/**
 * Toggles a markup span on the current selection: removes it if it already exists, adds it otherwise.
 * Handles partial overlaps by splitting existing spans and merges adjacent spans of the same type.
 */
fun toggleSpan(
    selectionStart: Int,
    selectionEnd: Int,
    spans: List<InputSpan>,
    newSpan: InputSpan.Markup
): List<InputSpan> {
    val start = minOf(selectionStart, selectionEnd)
    val end = maxOf(selectionStart, selectionEnd)
    if (start == end) return spans

    val updatedSpans = spans.toMutableList()
    val finalSpans = mutableListOf<InputSpan>()
    var spanToggled = false

    for (span in updatedSpans) {
        if (span !is InputSpan.Markup || span.type != newSpan.type) {
            finalSpans.add(span)
            continue
        }

        if (span.end <= start || span.start >= end) {
            finalSpans.add(span)
            continue
        }

        spanToggled = true

        if (start <= span.start && end >= span.end) {
            continue
        }

        if (span.start < start) {
            finalSpans.add(span.copy(end = start))
        }
        if (span.end > end) {
            finalSpans.add(span.copy(start = end))
        }
    }

    if (!spanToggled) {
        finalSpans.add(newSpan.copy(start = start, end = end))
    }

    return finalSpans
        .sortedBy { it.start }
        .fold(mutableListOf<InputSpan>()) { acc, span ->
            if (acc.isNotEmpty()) {
                val last = acc.last()
                if (last is InputSpan.Markup && span is InputSpan.Markup &&
                    last.type == span.type && last.end == span.start
                ) {
                    acc[acc.lastIndex] = last.copy(end = span.end)
                } else {
                    acc.add(span)
                }
            } else {
                acc.add(span)
            }
            acc
        }
}

/**
 * Normalizes spans after text changes — shifts positions based on prefix analysis
 * and removes spans that became empty or blank.
 */
fun normalizeSpans(
    oldText: String,
    newText: String,
    spans: List<InputSpan>
): List<InputSpan> {
    val textLengthDifference = newText.length - oldText.length
    val commonPrefixLength = newText.commonPrefixWith(oldText).length

    return spans.mapNotNull { span ->
        val newStart = when {
            textLengthDifference > 0 && commonPrefixLength <= span.start -> span.start + textLengthDifference
            textLengthDifference < 0 && commonPrefixLength <= span.start -> (span.start + textLengthDifference).coerceAtLeast(commonPrefixLength)
            else -> span.start
        }.coerceAtLeast(0)

        val newEnd = when {
            textLengthDifference > 0 && commonPrefixLength < span.end -> span.end + textLengthDifference
            textLengthDifference < 0 && commonPrefixLength < span.end -> span.end + textLengthDifference
            else -> span.end
        }.coerceAtLeast(newStart).coerceAtMost(newText.length)

        if (newStart < newEnd && newText.substring(newStart, newEnd).isNotBlank()) {
            when (span) {
                is InputSpan.Mention -> span.copy(start = newStart, end = newEnd)
                is InputSpan.Markup -> span.copy(start = newStart, end = newEnd)
            }
        } else {
            null
        }
    }
}

/**
 * Markup event types for toolbar interactions.
 */
sealed class MarkupEvent {
    data object Bold : MarkupEvent()
    data object Italic : MarkupEvent()
    data object Strike : MarkupEvent()
    data object Underline : MarkupEvent()
    data object Code : MarkupEvent()
}
