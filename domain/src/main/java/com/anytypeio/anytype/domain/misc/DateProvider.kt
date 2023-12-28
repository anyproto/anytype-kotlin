package com.anytypeio.anytype.domain.misc

interface DateProvider {

    fun getRelativeTimeSpanString(date: Long): CharSequence
    fun getNowInSeconds(): Long
}