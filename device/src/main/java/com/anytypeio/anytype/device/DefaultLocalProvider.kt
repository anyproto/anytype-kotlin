package com.anytypeio.anytype.device

import android.content.Context
import androidx.core.os.ConfigurationCompat
import com.anytypeio.anytype.domain.misc.LocaleProvider

class DefaultLocalProvider(
    private val context: Context
): LocaleProvider {
    override fun language(): String? {
        return ConfigurationCompat.getLocales(context.resources.configuration)[0]?.language
    }
}