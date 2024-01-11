package com.anytypeio.anytype.other

import android.content.Context
import com.anytypeio.anytype.localization.R
import com.anytypeio.anytype.domain.misc.DateType
import com.anytypeio.anytype.domain.misc.DateTypeNameProvider
import javax.inject.Inject

class DefaultDateTypeNameProvider @Inject constructor(
    private val context: Context
) : DateTypeNameProvider {
    override fun name(type: DateType): String = when(type) {
        DateType.TOMORROW -> context.resources.getString(R.string.date_type_tomorrow)
        DateType.TODAY -> context.resources.getString(R.string.date_type_today)
        DateType.YESTERDAY -> context.resources.getString(R.string.date_type_yesterday)
        DateType.PREVIOUS_SEVEN_DAYS -> context.resources.getString(R.string.date_type_previous_seven_days)
        DateType.PREVIOUS_THIRTY_DAYS -> context.resources.getString(R.string.date_type_previous_thirty_days)
        DateType.OLDER -> context.resources.getString(R.string.date_type_older)
        DateType.UNDEFINED -> context.resources.getString(R.string.undefined)
    }
}