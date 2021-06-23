package com.anytypeio.anytype.core_ui.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.FileUriExposedException
import android.provider.Browser
import android.text.Annotation
import android.text.TextPaint
import android.text.style.*
import android.view.View
import com.anytypeio.anytype.core_utils.ext.timber
import com.anytypeio.anytype.core_utils.ext.toast
import com.shekhargulati.urlcleaner.UrlCleaner
import com.shekhargulati.urlcleaner.UrlCleanerException

interface Span {
    class Bold : StyleSpan(Typeface.BOLD), Span
    class Italic : StyleSpan(Typeface.ITALIC), Span
    class Strikethrough : StrikethroughSpan(), Span
    class TextColor(color: Int) : ForegroundColorSpan(color), Span
    class Url(url: String, val color: Int) : URLSpan(url), Span {

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = color
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
}