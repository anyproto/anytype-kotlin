package com.anytypeio.anytype.core_utils.intents

import android.content.Context
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
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:" + action.email)
            }
            context?.let {
                startActivity(
                    emailIntent.createEmailOnlyChooserIntent(
                        context = it,
                        title = "Send email"
                    )
                )
            }
        } catch (e: Exception) {
            toast("An error occurred. Email may be invalid: ${e.message}")
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

fun Intent.createEmailOnlyChooserIntent(context: Context, title: CharSequence): Intent {
    val intents = mutableListOf<Intent>()
    val placeholderIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "support@anytype.io", null))
    val activities = context.packageManager.queryIntentActivities(placeholderIntent, 0)

    for (resolveInfo in activities) {
        val target = Intent(this)
        target.setPackage(resolveInfo.activityInfo.packageName)
        intents.add(target)
    }

    return if (intents.isNotEmpty()) {
        val chooserIntent = Intent.createChooser(intents.removeAt(0), title)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
        chooserIntent
    } else {
        Intent.createChooser(this, title)
    }
}