package com.agileburo.anytype.model

import java.time.Month

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 06.03.2019.
 */
class Day(val month: Month, val day: String, val content: String, val size: Float = 1.5f) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Day

        if (month != other.month) return false
        if (day != other.day) return false
        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        var result = month.hashCode()
        result = 31 * result + day.hashCode()
        result = 31 * result + content.hashCode()
        return result
    }
}