package com.anytypeio.anytype.device

import android.content.Context
import androidx.core.os.ConfigurationCompat
import com.anytypeio.anytype.domain.misc.LocaleProvider
import java.util.Locale

class DefaultLocalProvider(
    private val context: Context
) : LocaleProvider {

    private val defaultLocale by lazy {
        ConfigurationCompat.getLocales(context.resources.configuration).get(0)
            ?: Locale.getDefault()
    }

    override fun language(): String = defaultLocale.language
    override fun locale(): Locale = defaultLocale
}