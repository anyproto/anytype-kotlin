package com.anytypeio.anytype.core_ui.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.FileUriExposedException
import android.provider.Browser
import android.text.Annotation
import android.text.TextPaint
import android.text.style.*
import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.timber
import com.anytypeio.anytype.core_utils.ext.toast
import com.shekhargulati.urlcleaner.UrlCleaner
import com.shekhargulati.urlcleaner.UrlCleanerException
import timber.log.Timber
import kotlin.math.roundToInt

interface Span {
    class Bold : StyleSpan(Typeface.BOLD), Span
    class Italic : StyleSpan(Typeface.ITALIC), Span
    class Strikethrough : StrikethroughSpan(), Span
    class TextColor(color: Int, val value: String) : ForegroundColorSpan(color), Span

    class Url(
        val url: String,
        val color: Int,
        private val underlineHeight: Float
    ) :
        ClickableSpan(), Span {

        override fun updateDrawState(ds: TextPaint) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val alphaText = (Color.alpha(color) * 0.65f).roundToInt()
                val alphaUnderline = (Color.alpha(color) * 0.35f).roundToInt()
                ds.color =
                    Color.argb(alphaText, Color.red(color), Color.green(color), Color.blue(color))
                ds.underlineColor =
                    Color.argb(alphaUnderline, Color.red(color), Color.green(color), Color.blue(color))
                ds.underlineThickness = underlineHeight
            } else {
                ds.color = color
                super.updateDrawState(ds)
            }
        }

        override fun onClick(widget: View) {
            val intent = createIntent(widget.context, url)
            try {
                widget.context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.timber()
                normalizeUrl(widget.context, url)
            } catch (e: FileUriExposedException) {
                e.timber()
            } catch (e: NullPointerException) {
                e.timber()
                widget.context.toast("Url was null or empty")
            }
        }

        private fun normalizeUrl(context: Context, url: String) {
            try {
                val normalizedUrl = UrlCleaner.normalizeUrl(url)
                val intent = createIntent(context, normalizedUrl)
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.timber()
                    context.toast("Couldn't open url:$normalizedUrl")
                }
            } catch (e: UrlCleanerException) {
                e.timber()
                context.toast("Couldn't parse url")
            } catch (e: IllegalArgumentException) {
                e.timber()
                context.toast("Couldn't parse url")
            } catch (e: NullPointerException) {
                e.timber()
                context.toast("Couldn't parse url. String was null")
            }
        }

        private fun createIntent(context: Context, url: String): Intent {
            val uri = Uri.parse(url)
            return Intent(Intent.ACTION_VIEW, uri).apply {
                putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
            }
        }
    }

    class Font(family: String) : TypefaceSpan(family), Span

    class Keyboard(value: String) : Annotation(KEYBOARD_KEY, value), Span {
        companion object {
            const val KEYBOARD_KEY = "keyboard"
        }
    }

    class Highlight(color: String) : Annotation(HIGHLIGHT_KEY, color), Span {
        companion object {
            const val HIGHLIGHT_KEY = "highlight"
        }
    }

    class ObjectLink(
        val context: Context,
        val link: String?,
        val color: Int,
        val click: ((String) -> Unit)?,
        val isArchived: Boolean
    ) :
        ClickableSpan(), Span {

        private val textColorArchive = context.color(R.color.text_tertiary)

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            if (isArchived) {
                ds.color = textColorArchive
            } else {
                ds.color = color
            }
        }

        override fun onClick(widget: View) {
            if (!link.isNullOrBlank() && !isArchived) {
                (widget as? TextInputWidget)?.enableReadMode()
                click?.invoke(link)
            } else {
                Timber.e("Can't proceed with ObjectLinkSpan click, link is null or blank or archived")
            }
        }
    }
}