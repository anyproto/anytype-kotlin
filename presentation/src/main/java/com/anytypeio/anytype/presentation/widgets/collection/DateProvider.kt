package com.anytypeio.anytype.presentation.widgets.collection

import android.text.format.DateUtils
import javax.inject.Inject

class DateProvider @Inject constructor() {

    fun getRelativeTimeSpanString(date: Long): CharSequence = DateUtils.getRelativeTimeSpanString(
        date,
        System.currentTimeMillis(),
        DateUtils.DAY_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    )
}