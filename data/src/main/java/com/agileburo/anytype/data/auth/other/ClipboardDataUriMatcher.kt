package com.agileburo.anytype.data.auth.other

import com.agileburo.anytype.domain.clipboard.Clipboard

class ClipboardDataUriMatcher(
    private val matcher: ClipboardUriMatcher
) : Clipboard.UriMatcher {

    override fun isAnytypeUri(
        uri: String
    ): Boolean = matcher.isAnytypeUri(uri)
}