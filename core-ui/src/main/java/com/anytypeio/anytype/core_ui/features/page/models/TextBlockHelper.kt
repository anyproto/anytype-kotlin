package com.anytypeio.anytype.core_ui.features.page.models

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.common.ThemeColor
import com.anytypeio.anytype.core_ui.common.setMarkup
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.widgets.text.MentionSpan
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.VALUE_ROUNDED
import com.anytypeio.anytype.core_utils.ext.removeSpans
import timber.log.Timber

fun Editable.setMarkup(
    markup: Markup,
    context: Context? = null,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0
) {
    apply {
        removeSpans<Span>()
        setMarkup(markup, context, click, mentionImageSize, mentionImagePadding)
    }
}

fun Spannable.setMarkup(
    markup: Markup?,
    context: Context? = null,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0,
    textColor: Int
) = markup?.marks?.forEach { mark ->
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
                    mentionImagePadding = mentionImagePadding
                )
            } ?: run { Timber.d("Mention Span context is null") }
        }
    }
}

fun Spannable.setMentionSpan(
    mark: Markup.Mark,
    context: Context,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0
): Spannable = apply {
    if (!mark.param.isNullOrBlank()) {
        setSpan(
            MentionSpan(
                context = context,
                image = mark.image,
                emoji = mark.emoji,
                imageSize = mentionImageSize,
                imagePadding = mentionImagePadding,
                param = mark.param,
                placeholder = context.drawable(R.drawable.ic_block_page_without_emoji),
            ),
            mark.from,
            mark.to,
            Markup.MENTION_SPANNABLE_FLAG
        )
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                (widget as? TextInputWidget)?.enableReadMode()
                click?.invoke(mark.param)
            }
        }
        setSpan(
            clickableSpan,
            mark.from,
            mark.to,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    } else {
        Timber.e("Get MentionSpan without param!")
    }
}