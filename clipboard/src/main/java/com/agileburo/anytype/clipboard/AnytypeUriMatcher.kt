package com.agileburo.anytype.clipboard

import com.agileburo.anytype.data.auth.other.ClipboardUriMatcher

class AnytypeUriMatcher : ClipboardUriMatcher {
    override fun isAnytypeUri(uri: String): Boolean {
        return uri == BuildConfig.ANYTYPE_CLIPBOARD_URI
    }
}