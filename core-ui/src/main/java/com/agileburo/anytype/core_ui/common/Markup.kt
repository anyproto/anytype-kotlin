package com.agileburo.anytype.core_ui.common

import android.content.Context
import android.os.Parcelable
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.extensions.drawable
import com.agileburo.anytype.core_ui.widgets.text.MentionSpan
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.VALUE_ROUNDED
import com.agileburo.anytype.core_utils.ext.removeSpans
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

/**
 * Classes implementing this interface should support markup rendering.
 */
interface Markup {

    /**
     * A text body that this markup should be applied to.
     */
    val body: String

    /**
     * List of marks associated with the text body.
     */
    var marks: List<Mark>

    /**
     * @property from caracter index where this markup starts (inclusive)
     * @property to caracter index where this markup ends (inclusive)
     * @property type markup's type
     */
    @Parcelize
    data class Mark(
        val from: Int,
        val to: Int,
        val type: Type,
        val param: String? = null,
        val extras: Map<String, String?> = emptyMap()
    ) : Parcelable {

        val image: String? by extras
        val emoji: String? by extras

        fun color(): Int = ThemeColor.values().first { color -> color.title == param }.text
        fun background(): Int = ThemeColor.values().first { color -> color.title == param }.background

    }

    /**
     * Markup types.
     */
    enum class Type {
        ITALIC,
        BOLD,
        STRIKETHROUGH,
        TEXT_COLOR,
        BACKGROUND_COLOR,
        LINK,
        KEYBOARD,
        MENTION
    }

    companion object {
        const val DEFAULT_SPANNABLE_FLAG = Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        const val MENTION_SPANNABLE_FLAG = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        const val SPAN_MONOSPACE = "monospace"
    }
}

fun Markup.toSpannable(
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
    onImageReady: (String) -> Unit = {}
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
        setSpan(
            MentionSpan(
                onImageResourceReady = onImageReady,
                context = context,
                placeholder = context.drawable(R.drawable.ic_block_page_without_emoji),
                imageSize = mentionImageSize,
                imagePadding = mentionImagePadding,
                param = mark.param,
                emoji = if (mark.extras.isNotEmpty()) mark.emoji else null,
                image = if (mark.extras.isNotEmpty()) mark.image else null
            ),
            mark.from,
            mark.to,
            Markup.MENTION_SPANNABLE_FLAG
        )
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                (widget as? TextInputWidget)?.enableReadMode()
                click?.invoke( mark.param)
            }
        }
        setSpan(
            clickableSpan,
            mark.from,
            mark.to,
            Markup.MENTION_SPANNABLE_FLAG
        )
    } else {
        Timber.e("Get MentionSpan without param!")
    }
}

fun List<Markup.Mark>.isLinksPresent(): Boolean =
    this.any { it.type == Markup.Type.LINK || it.type == Markup.Type.MENTION }