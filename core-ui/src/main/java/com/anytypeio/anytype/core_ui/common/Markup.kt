package com.anytypeio.anytype.core_ui.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.disable
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.widgets.getDrawableAndTintColor
import com.anytypeio.anytype.core_ui.widgets.text.MentionSpan
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.VALUE_ROUNDED
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.extensions.EmojiUtils
import timber.log.Timber

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
            Timber.d("Replaced text at ${mark.from}-${mark.to} with emoji: ${mark.param}")
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
    marks.forEach { mark ->
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

private fun isRangeValid(mark: Markup.Mark, textLength: Int): Boolean {
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
    
    removeSpans<Span>()
    markup.marks.forEach { mark ->
        if (!isRangeValid(mark, length)) return@forEach
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
                context?.let {
                    setMentionSpan(
                        onImageReady = onImageReady,
                        mark = mark,
                        context = it,
                        click = click,
                        mentionImageSize = mentionImageSize,
                        mentionImagePadding = mentionImagePadding,
                        mentionCheckedIcon = mentionCheckedIcon,
                        mentionUncheckedIcon = mentionUncheckedIcon,
                        mentionInitialsSize = mentionInitialsSize,
                        textColor = textColor
                    )
                } ?: run { Timber.d("Mention Span context is null") }
            }
            is Markup.Mark.Object -> setSpan(
                Span.ObjectLink(
                    context = context,
                    link = mark.param,
                    color = textColor,
                    click = click,
                    isArchived = mark.isArchived
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

fun Editable.setMentionSpan(
    onImageReady: (String) -> Unit = {},
    mark: Markup.Mark.Mention,
    context: Context,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0,
    mentionCheckedIcon: Drawable?,
    mentionUncheckedIcon: Drawable?,
    mentionInitialsSize: Float,
    textColor: Int
): Editable = this.apply {
    if (isRangeValid(mark, length)) {
        proceedWithSettingMentionSpan(
            onImageReady = onImageReady,
            mark = mark,
            context = context,
            click = click,
            mentionImageSize = mentionImageSize,
            mentionImagePadding = mentionImagePadding,
            mentionCheckedIcon = mentionCheckedIcon,
            mentionUncheckedIcon = mentionUncheckedIcon,
            mentionInitialsSize = mentionInitialsSize,
            textColor = textColor
        )
    }
}

fun Editable.proceedWithSettingMentionSpan(
    onImageReady: (String) -> Unit = {},
    mark: Markup.Mark.Mention,
    context: Context,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0,
    mentionCheckedIcon: Drawable?,
    mentionUncheckedIcon: Drawable?,
    mentionInitialsSize: Float,
    textColor: Int
) {
    if (!isRangeValid(mark, length)) return
    when (mark) {
        is Markup.Mark.Mention.Deleted -> {
            val placeholder = context.drawable(R.drawable.ic_non_existent_object)
            setSpan(
                MentionSpan(
                    context = context,
                    placeholder = placeholder,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    isDeleted = true,
                    isArchived = false
                ),
                mark.from,
                mark.to,
                Markup.MENTION_SPANNABLE_FLAG
            )
        }
        is Markup.Mark.Mention.Loading -> {
            val placeholder = context.drawable(R.drawable.ic_mention_loading_state)
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    placeholder = placeholder,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    isArchived = false
                ),
                mark.from,
                mark.to,
                Markup.MENTION_SPANNABLE_FLAG
            )
        }
        is Markup.Mark.Mention.Base -> {
            setSpan(
                MentionSpan(
                    onImageResourceReady = {},
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.WithEmoji -> {
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    emoji = mark.emoji,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.WithImage -> {
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    image = mark.image,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.Task.Checked -> {
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    placeholder = mentionCheckedIcon,
                    param = mark.param,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.Task.Unchecked -> {
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    placeholder = mentionUncheckedIcon,
                    param = mark.param,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.Profile.WithImage -> {
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    profile = mark.imageUrl,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.Profile.WithInitials -> {
            val placeholder =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.object_in_list_background_profile_initial
                )
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    placeholder = placeholder,
                    param = mark.param,
                    initials = mark.initials.toString(),
                    initialsTextSize = mentionInitialsSize,
                    isArchived = mark.isArchived
                ),
                mark.from,
                mark.to,
                Markup.MENTION_SPANNABLE_FLAG
            )
            if (!mark.isArchived) setClickableSpan(click, mark)
        }

        is Markup.Mark.Mention.Date -> {
            val placeholder =
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_obj_date_20
                )
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    placeholder = placeholder,
                    isArchived = false
                ),
                mark.from,
                mark.to,
                Markup.MENTION_SPANNABLE_FLAG
            )
            setClickableSpan(click, mark)
        }
        is Markup.Mark.Mention.ObjectType -> {
            val (drawableRes, tint) = mark.icon.getDrawableAndTintColor(context)

            val baseDrawable = when {
                drawableRes == 0 -> null
                else -> try {
                    context.drawable(drawableRes).mutate().apply {
                        DrawableCompat.setTint(this, tint)
                    }
                } catch (e: Exception) {
                    null
                }
            }

            val finalDrawable = baseDrawable ?: context.drawable(R.drawable.ic_empty_state_type)
            setSpan(
                MentionSpan(
                    onImageResourceReady = onImageReady,
                    context = context,
                    imageSize = mentionImageSize,
                    imagePadding = mentionImagePadding,
                    param = mark.param,
                    emoji = null,
                    placeholder = finalDrawable,
                    isArchived = false
                ),
                mark.from,
                mark.to,
                Markup.MENTION_SPANNABLE_FLAG
            )
            setClickableSpan(click, mark)
        }
    }
}

fun Editable.setClickableSpan(click: ((String) -> Unit)?, mark: Markup.Mark.Mention) {
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            (widget as? TextInputWidget)?.disable()
            click?.invoke(mark.param)
        }
    }
    setSpan(
        clickableSpan,
        mark.from,
        mark.to,
        Markup.MENTION_SPANNABLE_FLAG
    )
}

fun List<Markup.Mark>.isLinksOrMentionsPresent(): Boolean =
    this.any { it is Markup.Mark.Link || it is Markup.Mark.Mention || it is Markup.Mark.Object }