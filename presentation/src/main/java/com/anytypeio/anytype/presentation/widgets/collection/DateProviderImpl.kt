package com.anytypeio.anytype.presentation.widgets.collection

import android.text.format.DateUtils
import com.anytypeio.anytype.domain.misc.DateProvider
import javax.inject.Inject

class DateProviderImpl @Inject constructor() : DateProvider {

    override fun getRelativeTimeSpanString(date: Long): CharSequence = DateUtils.getRelativeTimeSpanString(
        date,
        System.currentTimeMillis(),
        DateUtils.DAY_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    )

    override fun getNowInSeconds(): Long {
        return System.currentTimeMillis() / 1000
    }
}