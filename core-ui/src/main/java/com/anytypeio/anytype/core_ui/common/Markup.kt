package com.anytypeio.anytype.core_ui.common

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.widgets.text.MentionSpan
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.VALUE_ROUNDED
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import timber.log.Timber

fun Markup.toSpannable(
    textColor: Int,
    context: Context? = null,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0,
    onImageReady: (String) -> Unit = {}
) = SpannableStringBuilder(body).apply {
    marks.forEach { mark ->
        when (mark.type) {
            Markup.Type.ITALIC -> setSpan(
                Span.Italic(),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.BOLD -> setSpan(
                Span.Bold(),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.STRIKETHROUGH -> setSpan(
                Span.Strikethrough(),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.TEXT_COLOR -> {
                val color = mark.color() ?: ThemeColor.DEFAULT.text
                setSpan(
                    Span.TextColor(color),
                    mark.from,
                    mark.to,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
            Markup.Type.BACKGROUND_COLOR -> {
                val background = mark.background() ?: ThemeColor.DEFAULT.background
                setSpan(
                    Span.Highlight(background.toString()),
                    mark.from,
                    mark.to,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
            Markup.Type.LINK -> setSpan(
                Span.Url(mark.param as String, textColor),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.KEYBOARD -> {
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
            Markup.Type.MENTION -> {
                context?.let {
                    setMentionSpan(
                        mark = mark,
                        context = it,
                        click = click,
                        mentionImageSize = mentionImageSize,
                        mentionImagePadding = mentionImagePadding,
                        onImageReady = onImageReady
                    )
                } ?: run { Timber.d("Mention Span context is null") }
            }
        }
    }
}

fun Editable.setMarkup(
    markup: Markup,
    context: Context? = null,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0,
    onImageReady: (String) -> Unit = {},
    textColor: Int
) {
    removeSpans<Span>()
    markup.marks.forEach { mark ->
        when (mark.type) {
            Markup.Type.ITALIC -> setSpan(
                Span.Italic(),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.BOLD -> setSpan(
                Span.Bold(),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.STRIKETHROUGH -> setSpan(
                Span.Strikethrough(),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.TEXT_COLOR -> {
                val color = mark.color() ?: ThemeColor.DEFAULT.text
                setSpan(
                    Span.TextColor(color),
                    mark.from,
                    mark.to,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
            Markup.Type.BACKGROUND_COLOR -> {
                val background = mark.background() ?: ThemeColor.DEFAULT.background
                setSpan(
                    Span.Highlight(background.toString()),
                    mark.from,
                    mark.to,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
            Markup.Type.LINK -> setSpan(
                Span.Url(mark.param as String, textColor),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.KEYBOARD -> {
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
            Markup.Type.MENTION -> {
                context?.let {
                    setMentionSpan(
                        onImageReady = onImageReady,
                        mark = mark,
                        context = it,
                        click = click,
                        mentionImageSize = mentionImageSize,
                        mentionImagePadding = mentionImagePadding
                    )
                } ?: run { Timber.d("Mention Span context is null") }
            }
        }
    }
}

fun Editable.setMentionSpan(
    onImageReady: (String) -> Unit = {},
    mark: Markup.Mark,
    context: Context,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0
) : Editable = this.apply {
    if (!mark.param.isNullOrBlank()) {

        val isLoading = mark.isLoading == Markup.Mark.IS_LOADING_VALUE

        val placeholder = if (isLoading)
            context.drawable(R.drawable.ic_mention_loading_state)
        else
            context.drawable(R.drawable.ic_block_page_without_emoji)

        setSpan(
            MentionSpan(
                onImageResourceReady = onImageReady,
                context = context,
                placeholder = placeholder,
                imageSize = mentionImageSize,
                imagePadding = mentionImagePadding,
                param = mark.param!!,
                emoji = if (mark.extras.isNotEmpty()) mark.emoji else null,
                image = if (mark.extras.isNotEmpty()) mark.image else null
            ),
            mark.from,
            mark.to,
            Markup.MENTION_SPANNABLE_FLAG
        )

        if (!isLoading) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // TODO consider pausing text watchers. Otherwise, redundant text watcher events will be triggered.
                    (widget as? TextInputWidget)?.enableReadMode()
                    click?.invoke(mark.param!!)
                }
            }
            setSpan(
                clickableSpan,
                mark.from,
                mark.to,
                Markup.MENTION_SPANNABLE_FLAG
            )
        }
    } else {
        Timber.e("Get MentionSpan without param!")
    }
}

fun List<Markup.Mark>.isLinksOrMentionsPresent(): Boolean =
    this.any { it.type == Markup.Type.LINK || it.type == Markup.Type.MENTION }