package com.anytypeio.anytype.core_utils.tools

import android.util.Patterns

interface UrlValidator {
    fun isValid(url: String) : Boolean
}

class DefaultUrlValidator: UrlValidator {
    override fun isValid(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches()
    }
}