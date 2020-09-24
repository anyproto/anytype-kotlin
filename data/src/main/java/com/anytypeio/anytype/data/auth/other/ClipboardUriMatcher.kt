package com.anytypeio.anytype.data.auth.other

interface ClipboardUriMatcher {
    fun isAnytypeUri(uri: String) : Boolean
}