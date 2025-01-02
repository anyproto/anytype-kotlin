package com.anytypeio.anytype.core_ui.widgets.text

import android.text.style.ClickableSpan
import android.view.View
import com.anytypeio.anytype.core_ui.common.Span

class MentionTextWithIconSpan(
    private val click: ((String) -> Unit)?,
    val param: String
) : ClickableSpan(), Span {
    override fun onClick(widget: View) {
        widget.cancelPendingInputEvents()
        widget.isEnabled = false
        (widget as? TextInputWidget)?.apply {
            pauseTextWatchers {
                enableReadMode()
            }
        }
        click?.invoke(param)
    }
}

class MentionTextWithoutIconSpan(
    private val click: ((String) -> Unit)?,
    val param: String
) : ClickableSpan(), Span {
    override fun onClick(widget: View) {
        widget.cancelPendingInputEvents()
        widget.isEnabled = false
        (widget as? TextInputWidget)?.apply {
            pauseTextWatchers {
                enableReadMode()
            }
        }
        click?.invoke(param)
    }
}