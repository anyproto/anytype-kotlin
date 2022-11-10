package com.anytypeio.anytype.core_utils.tools

import android.util.Patterns
import javax.inject.Inject

interface UrlValidator {
    fun isValid(url: String) : Boolean
}

class DefaultUrlValidator @Inject constructor(): UrlValidator {
    override fun isValid(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches()
    }
}