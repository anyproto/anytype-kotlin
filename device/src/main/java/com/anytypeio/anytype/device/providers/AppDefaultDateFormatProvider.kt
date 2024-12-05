package com.anytypeio.anytype.device.providers

import com.anytypeio.anytype.core_models.DEFAULT_DATE_FORMAT_STYLE
import com.anytypeio.anytype.core_models.FALLBACK_DATE_PATTERN
import com.anytypeio.anytype.domain.misc.LocaleProvider
import java.text.DateFormat
import java.text.SimpleDateFormat
import javax.inject.Inject
import timber.log.Timber

/**
 * Interface for providing the default date format pattern as a [String].
 */
interface AppDefaultDateFormatProvider {

    /**
     * Retrieves the default date format pattern based on the current locale.
     *
     * @return A date format pattern [String], e.g., "MM/dd/yyyy".
     */
    fun provide(): String
}

class AppDefaultDateFormatProviderImpl @Inject constructor(
    private val localeProvider: LocaleProvider
) : AppDefaultDateFormatProvider {

    /**
     * Provides the default date format pattern based on the current locale.
     *
     * @return The date format pattern as a [String]. If unable to retrieve the pattern,
     * returns the [FALLBACK_DATE_PATTERN].
     */
    override fun provide(): String {
        return try {
            val locale = localeProvider.locale()
            val dateFormat = DateFormat.getDateInstance(DEFAULT_DATE_FORMAT_STYLE, locale)
            if (dateFormat is SimpleDateFormat) {
                dateFormat.toPattern()
            } else {
                Timber.e("DateFormat instance is not a SimpleDateFormat for locale: %s", locale)
                FALLBACK_DATE_PATTERN
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while getting date format for locale")
            FALLBACK_DATE_PATTERN
        }
    }
}