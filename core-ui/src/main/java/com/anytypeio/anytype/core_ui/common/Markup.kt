package com.anytypeio.anytype.core_ui.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.widgets.text.MentionSpan
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.VALUE_ROUNDED
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.core_models.ThemeColor
import timber.log.Timber

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
) = SpannableStringBuilder(body).apply {
    marks.forEach { mark ->
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
        }
    }
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
    removeSpans<Span>()
    markup.marks.forEach { mark ->
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
    }
}

fun Editable.setClickableSpan(click: ((String) -> Unit)?, mark: Markup.Mark.Mention) {
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            // TODO consider pausing text watchers. Otherwise, redundant text watcher events will be triggered.
            (widget as? TextInputWidget)?.enableReadMode()
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