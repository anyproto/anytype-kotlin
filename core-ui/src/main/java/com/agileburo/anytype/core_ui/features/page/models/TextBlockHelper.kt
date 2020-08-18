package com.agileburo.anytype.core_ui.features.page.models

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.text.toSpannable
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.Span
import com.agileburo.anytype.core_ui.widgets.text.MentionSpan
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.VALUE_ROUNDED
import com.agileburo.anytype.core_utils.ext.removeSpans
import timber.log.Timber

fun Editable.setMarkup(
    markup: Markup?,
    context: Context? = null,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0
) {
    apply {
        removeSpans<Span>()
        toSpannable().setMarkup(markup, context, click, mentionImageSize, mentionImagePadding)
    }
}

fun Spannable.setMarkup(
    markup: Markup?,
    context: Context? = null,
    click: ((String) -> Unit)? = null,
    mentionImageSize: Int = 0,
    mentionImagePadding: Int = 0
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
        Markup.Type.TEXT_COLOR -> setSpan(
            Span.TextColor(mark.color()),
            mark.from,
            mark.to,
            Markup.DEFAULT_SPANNABLE_FLAG
        )
        Markup.Type.BACKGROUND_COLOR -> setSpan(
            Span.Highlight(mark.background().toString()),
            mark.from,
            mark.to,
            Markup.DEFAULT_SPANNABLE_FLAG
        )
        Markup.Type.LINK -> setSpan(
            Span.Url(mark.param as String),
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
): Spannable = this.apply {
    if (!mark.param.isNullOrBlank()) {
        setSpan(
            MentionSpan(
                context = context,
                imageSize = mentionImageSize,
                imagePadding = mentionImagePadding,
                mResourceId = R.drawable.ic_block_page_without_emoji,
                param = mark.param
            ),
            mark.from,
            mark.to,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
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