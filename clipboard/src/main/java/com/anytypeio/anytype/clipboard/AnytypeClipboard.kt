package com.anytypeio.anytype.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import com.anytypeio.anytype.clipboard.BuildConfig.ANYTYPE_CLIPBOARD_LABEL
import com.anytypeio.anytype.clipboard.BuildConfig.ANYTYPE_CLIPBOARD_URI

import com.anytypeio.anytype.data.auth.model.ClipEntity
import com.anytypeio.anytype.data.auth.repo.clipboard.ClipboardDataStore

class AnytypeClipboard(
    private val cm: ClipboardManager
) : ClipboardDataStore.System {

    override suspend fun put(text: String, html: String?, ignoreHtml: Boolean) {
        val uri = Uri.parse(ANYTYPE_CLIPBOARD_URI)
        if (!ignoreHtml && html != null)
            cm.setPrimaryClip(
                ClipData.newHtmlText(ANYTYPE_CLIPBOARD_LABEL, text, html).apply {
                    addItem(ClipData.Item(uri))
                }
            )
        else
            cm.setPrimaryClip(
                ClipData.newPlainText(ANYTYPE_CLIPBOARD_LABEL, text).apply {
                    addItem(ClipData.Item(uri))
                }
            )
    }

    override suspend fun clip(): ClipEntity? {
        return cm.primaryClip?.let { clip ->
            when {
                clip.itemCount > 1 -> {
                    ClipEntity(
                        text = clip.getItemAt(0).text.toString(),
                        html = clip.getItemAt(0).htmlText,
                        uri = clip.getItemAt(1).uri.toString()
                    )
                }
                clip.itemCount == 1 -> {
                    ClipEntity(
                        text = clip.getItemAt(0).text.toString(),
                        html = clip.getItemAt(0).htmlText,
                        uri = null
                    )
                }
                else -> {
                    null
                }
            }
        }
    }
}