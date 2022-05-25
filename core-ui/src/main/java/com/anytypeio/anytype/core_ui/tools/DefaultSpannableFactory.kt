package com.anytypeio.anytype.core_ui.tools

import android.text.Spannable

class DefaultSpannableFactory : Spannable.Factory() {
    override fun newSpannable(source: CharSequence): Spannable {
        return source as? Spannable ?: super.newSpannable(source)
    }
}