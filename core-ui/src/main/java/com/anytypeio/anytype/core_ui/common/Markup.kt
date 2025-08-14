package com.anytypeio.anytype.core_ui.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.SpannableStringBuilder
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_utils.ext.VALUE_ROUNDED
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.extensions.EmojiUtils
import com.anytypeio.anytype.core_ui.widgets.text.setMentionSpan

/**
 * Process emoji marks by replacing text at emoji positions with actual emoji characters.
 * This is needed because ReplacementSpan doesn't work properly across newlines.
 * 
 * IMPORTANT LIMITATIONS:
 * - Text length may change when emojis are longer than replaced characters
 * - Positions of other markup spans are NOT remapped after emoji replacement
 * - This may cause misalignment if other spans come after emoji positions
 * - Currently optimized for emoji-only scenarios and multiline emoji rendering
 */
fun Markup.processEmojiMarks(): String {
    val emojiMarks = marks.filterIsInstance<Markup.Mark.Emoji>()
        .sortedByDescending { it.from } // Process from end to start to maintain indices
    
    if (emojiMarks.isEmpty()) return body
    
    val result = StringBuilder(body)
    
    emojiMarks.forEach { mark ->
        if (mark.from >= 0 && mark.to <= result.length && mark.from < mark.to) {
            // Process the emoji through EmojiCompat
            val processedEmoji = EmojiUtils.processSafe(mark.param).toString()
            // Replace the character(s) at the mark position with the emoji
            result.replace(mark.from, mark.to, processedEmoji)
        }
    }
    
    return result.toString()
}

fun Markup.toSpannable(
    textColor: Int,
    context: Context,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0,
    mentionCheckedIcon: Drawable? = null,
    mentionUncheckedIcon: Drawable? = null,
    mentionInitialsSize: Float = 0F,
    onImageReady: (String) -> Unit = {},
    underlineHeight: Float
) = SpannableStringBuilder(processEmojiMarks()).apply {
    applyMarkupSpans(
        markup = this@toSpannable,
        context = context,
        textColor = textColor,
        click = click,
        mentionImageSize = mentionImageSize,
        mentionImagePadding = mentionImagePadding,
        mentionCheckedIcon = mentionCheckedIcon,
        mentionUncheckedIcon = mentionUncheckedIcon,
        mentionInitialsSize = mentionInitialsSize,
        onImageReady = onImageReady,
        underlineHeight = underlineHeight
    )
}

/**
 * Shared logic for applying markup spans to a spannable text.
 * Used by both toSpannable() and Editable.setMarkup().
 */
private fun SpannableStringBuilder.applyMarkupSpans(
    markup: Markup,
    context: Context,
    textColor: Int,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0,
    mentionCheckedIcon: Drawable? = null,
    mentionUncheckedIcon: Drawable? = null,
    mentionInitialsSize: Float = 0F,
    onImageReady: (String) -> Unit = {},
    underlineHeight: Float
) {
    markup.marks.forEach { mark ->
        // Skip emoji marks as they're already processed in the text
        if (mark is Markup.Mark.Emoji) return@forEach
        
        // NOTE: Positions use original indices - may be misaligned if emojis changed text length
        if (!isRangeValid(mark = mark, textLength = length)) return@forEach
        when (mark) {
            is Markup.Mark.Italic -> setSpan(
                Span.Italic(),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            is Markup.Mark.Bold -> setSpan(
                Span.Bold(),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            is Markup.Mark.Strikethrough -> setSpan(
                Span.Strikethrough(),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            is Markup.Mark.TextColor -> {
                val value = mark.color()
                val span = if (value != null && value != ThemeColor.DEFAULT) {
                    Span.TextColor(
                        color = context.resources.dark(color = value, default = textColor),
                        value = mark.color
                    )
                } else {
                    Span.TextColor(
                        color = textColor,
                        value = mark.color
                    )
                }
                setSpan(
                    span,
                    mark.from,
                    mark.to,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
            is Markup.Mark.Underline -> setSpan(
                Underline(underlineHeight = underlineHeight),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            is Markup.Mark.BackgroundColor -> {
                setSpan(
                    Span.Highlight(mark.background),
                    mark.from,
                    mark.to,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
            is Markup.Mark.Link -> setSpan(
                Span.Url(
                    url = mark.param,
                    color = textColor,
                    underlineHeight = underlineHeight
                ),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            is Markup.Mark.Keyboard -> {
                setSpan(
                    Span.Font(Markup.SPAN_MONOSPACE),
                    mark.from,
                    mark.to,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
                setSpan(
                    Span.Keyboard(VALUE_ROUNDED),
                    mark.from,
                    mark.to,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
            is Markup.Mark.Mention -> {
                setMentionSpan(
                    mark = mark,
                    context = context,
                    click = click,
                    mentionImageSize = mentionImageSize,
                    mentionImagePadding = mentionImagePadding,
                    mentionCheckedIcon = mentionCheckedIcon,
                    mentionUncheckedIcon = mentionUncheckedIcon,
                    onImageReady = onImageReady,
                    mentionInitialsSize = mentionInitialsSize,
                    textColor = textColor
                )
            }
            is Markup.Mark.Object -> setSpan(
                Span.ObjectLink(
                    link = mark.param,
                    color = textColor,
                    click = click,
                    isArchived = mark.isArchived,
                    context = context
                ),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            is Markup.Mark.Emoji -> {
                // Already processed in processEmojiMarks() - skip
            }
        }
    }
}

fun isRangeValid(mark: Markup.Mark, textLength: Int): Boolean {
    return mark.from >= 0 && mark.to >= 0 && mark.from < mark.to && mark.to <= textLength
}

fun Editable.setMarkup(
    markup: Markup,
    context: Context,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0,
    mentionCheckedIcon: Drawable?,
    mentionUncheckedIcon: Drawable?,
    mentionInitialsSize: Float,
    onImageReady: (String) -> Unit = {},
    textColor: Int,
    underlineHeight: Float
) {
    // First process emoji marks by replacing text
    val processedText = markup.processEmojiMarks()
    if (processedText != toString()) {
        // Text changed due to emoji processing - update this Editable
        clear()
        append(processedText)
    }
    
    // Clear existing spans and apply new ones using the shared logic
    removeSpans<Span>()
    
    // Convert to SpannableStringBuilder to use shared logic
    val spannableBuilder = SpannableStringBuilder(this)
    spannableBuilder.applyMarkupSpans(
        markup = markup,
        context = context,
        textColor = textColor,
        click = click,
        mentionImageSize = mentionImageSize,
        mentionImagePadding = mentionImagePadding,
        mentionCheckedIcon = mentionCheckedIcon,
        mentionUncheckedIcon = mentionUncheckedIcon,
        mentionInitialsSize = mentionInitialsSize,
        onImageReady = onImageReady,
        underlineHeight = underlineHeight
    )
    
    // Copy the spans back to this Editable
    val spans = spannableBuilder.getSpans(0, spannableBuilder.length, Any::class.java)
    spans.forEach { span ->
        val start = spannableBuilder.getSpanStart(span)
        val end = spannableBuilder.getSpanEnd(span)
        val flags = spannableBuilder.getSpanFlags(span)
        if (start >= 0 && end >= 0 && start <= length && end <= length) {
            setSpan(span, start, end, flags)
        }
    }
}

fun List<Markup.Mark>.isLinksOrMentionsPresent(): Boolean =
    this.any { it is Markup.Mark.Link || it is Markup.Mark.Mention || it is Markup.Mark.Object }