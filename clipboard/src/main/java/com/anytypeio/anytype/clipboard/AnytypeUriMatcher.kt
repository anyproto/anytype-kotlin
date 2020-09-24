package com.anytypeio.anytype.clipboard

import com.anytypeio.anytype.data.auth.other.ClipboardUriMatcher

class AnytypeUriMatcher : ClipboardUriMatcher {
    override fun isAnytypeUri(uri: String): Boolean {
        return uri == BuildConfig.ANYTYPE_CLIPBOARD_URI
    }
}