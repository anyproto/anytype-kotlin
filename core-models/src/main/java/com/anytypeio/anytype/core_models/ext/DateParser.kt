package com.anytypeio.anytype.core_models.ext

/**
 * Converts relation {format DATE} value {Any?} to time in millis {Long} or null
 * @tests [RelationValueExtensionTest]
 */
object DateParser {
    fun parse(value: Any?): Long? {
        val result: Long? = when (value) {
            is String -> value.toLongOrNull()
            is Double -> value.toLong()
            is Long -> value
            else -> null
        }
        return result
    }

    fun parseInMillis(value: Any?) : Long? {
        val result: Long? = when (value) {
            is String -> value.toLongOrNull()
            is Double -> value.toLong()
            is Long -> value
            else -> null
        }
        return if (result!= null)
            result * 1000
        else
            null
    }
}