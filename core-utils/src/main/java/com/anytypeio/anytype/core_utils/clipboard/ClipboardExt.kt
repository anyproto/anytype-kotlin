package com.anytypeio.anytype.core_utils.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Patterns
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

fun Context.parseUrlFromClipboard(): String? {
    return try {
        val mng = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = mng.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val txt = clip.getItemAt(0).text
            if (txt.isNullOrEmpty())
                null
            else {
                val matcher = Patterns.WEB_URL.matcher(txt)
                if (matcher.matches()) {
                    txt.toString()
                } else {
                    null
                }
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}