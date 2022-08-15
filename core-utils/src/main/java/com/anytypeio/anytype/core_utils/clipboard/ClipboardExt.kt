package com.anytypeio.anytype.core_utils.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.fragment.app.Fragment
import com.anytypeio.anytype.core_utils.ext.toast

fun Context.copyPlainTextToClipboard(
    plainText: String,
    label: String,
    successToast: String? = null,
    failureToast: String? = null
) {
    try {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, plainText)
        clipboard.setPrimaryClip(clip)
        successToast?.let { toast(it) }
    } catch (e: Exception) {
        failureToast?.let { toast(it) }
    }
}