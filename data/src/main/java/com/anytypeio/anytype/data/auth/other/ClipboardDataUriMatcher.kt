package com.anytypeio.anytype.data.auth.other

import com.anytypeio.anytype.domain.clipboard.Clipboard

class ClipboardDataUriMatcher(
    private val matcher: ClipboardUriMatcher
) : Clipboard.UriMatcher {

    override fun isAnytypeUri(
        uri: String
    ): Boolean = matcher.isAnytypeUri(uri)
}