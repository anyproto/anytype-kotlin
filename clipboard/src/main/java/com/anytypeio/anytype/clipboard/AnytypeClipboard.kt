package com.anytypeio.anytype.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.os.PersistableBundle
import com.anytypeio.anytype.clipboard.BuildConfig.ANYTYPE_CLIPBOARD_LABEL
import com.anytypeio.anytype.clipboard.BuildConfig.ANYTYPE_CLIPBOARD_URI
import com.anytypeio.anytype.data.auth.model.ClipEntity
import com.anytypeio.anytype.data.auth.repo.clipboard.ClipboardDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnytypeClipboard(
    private val cm: ClipboardManager
) : ClipboardDataStore.System {

    override suspend fun put(text: String, html: String?, ignoreHtml: Boolean) {
        withContext(Dispatchers.Main) {
            val clip = if (!ignoreHtml && html != null)
                ClipData.newHtmlText(ANYTYPE_CLIPBOARD_LABEL, text, html)
            else
                ClipData.newPlainText(ANYTYPE_CLIPBOARD_LABEL, text)
            clip.description.extras = PersistableBundle().apply {
                putString(EXTRAS_KEY_SOURCE, ANYTYPE_CLIPBOARD_URI)
            }
            cm.setPrimaryClip(clip)
        }
    }

    override suspend fun clip(): ClipEntity? {
        return cm.primaryClip?.let { clip ->
            if (clip.itemCount < 1) return@let null
            val text = clip.getItemAt(0).text?.toString() ?: ""
            val html = clip.getItemAt(0).htmlText
            // Check extras first (new), then fall back to URI item (backward compat)
            val uri = clip.description?.extras?.getString(EXTRAS_KEY_SOURCE)
                ?: if (clip.itemCount > 1) clip.getItemAt(1)?.uri?.toString() else null
            ClipEntity(text = text, html = html, uri = uri)
        }
    }

    companion object {
        const val EXTRAS_KEY_SOURCE = "anytype_source"
    }
}