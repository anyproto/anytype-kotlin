package com.anytypeio.anytype.util

import android.webkit.URLUtil
import com.anytypeio.anytype.presentation.util.UrlHelper

class UrlHelperImpl : UrlHelper {
    override fun isValidUrl(url: String): Boolean {
        return URLUtil.isValidUrl(url)
    }
}
