package com.anytypeio.anytype.domain.account

interface DateHelper {
    fun isToday(millis: Long) : Boolean
    fun isTomorrow(millis: Long) : Boolean
}