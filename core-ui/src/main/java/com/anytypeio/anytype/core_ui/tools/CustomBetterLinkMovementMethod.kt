package com.anytypeio.anytype.core_ui.tools

import android.text.Spannable
import android.text.style.ClickableSpan
import android.widget.TextView
import me.saket.bettermovementmethod.BetterLinkMovementMethod

object CustomBetterLinkMovementMethod : BetterLinkMovementMethod() {
    override fun highlightUrl(textView: TextView?, clickableSpan: ClickableSpan?, text: Spannable?) {}
}