package com.anytypeio.anytype.core_utils.intents

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.anytypeio.anytype.core_utils.clipboard.copyPlainTextToClipboard
import com.anytypeio.anytype.core_utils.ext.normalizeUrl
import com.anytypeio.anytype.core_utils.ext.toast

sealed class SystemAction {
    data class OpenUrl(val url: String) : SystemAction()
    data class CopyToClipboard(val plain: String, val label: String) : SystemAction()
    data class MailTo(val email: String) : SystemAction()
    data class Dial(val phone: String) : SystemAction()

    companion object {
        const val LABEL_EMAIL = "Email"
        const val LABEL_PHONE = "Phone"
        const val LABEL_URL = "Phone"
    }
}

fun Fragment.proceedWithAction(action: SystemAction) = when(action) {
    is SystemAction.CopyToClipboard -> {
        requireContext().copyPlainTextToClipboard(
            plainText = action.plain,
            label = action.label,
            successToast = "Copied to clipboard!",
            failureToast = "Failed to copy to clipboard. Please, try again later."
        )
    }
    is SystemAction.MailTo -> {
        try {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:" + action.email)
            }.let {
                startActivity(it)
            }
        } catch (e: Exception) {
            toast("An error occurred. Email address may be invalid: ${e.message}")
        }
    }
    is SystemAction.OpenUrl -> {
        try {
            Intent(Intent.ACTION_VIEW).apply {
                val url = action.url.normalizeUrl()
                data = Uri.parse(url)
            }.let {
                startActivity(it)
            }
        } catch (e: Exception) {
            toast("An error occurred. Url may be invalid: ${e.message}")
        }
    }
    is SystemAction.Dial -> {
        try {
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${action.phone}")
            }.let {
                startActivity(it)
            }
        } catch (e: Exception) {
            toast("An error occurred. Phone number may be invalid: ${e.message}")
        }
    }
}