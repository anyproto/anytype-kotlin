package com.anytypeio.anytype.core_utils.date

import java.text.SimpleDateFormat
import javax.inject.Inject

interface DateFormatter {
    fun format(millis: Long) : String

    class Basic @Inject constructor() : DateFormatter {
        override fun format(millis: Long): String {
            return defaultFormat.format(millis)
        }
        private val defaultFormat = SimpleDateFormat("d MMMM yyyy")
    }
}