package com.anytypeio.anytype.core_models.primitives

@JvmInline
value class FieldDateValue(val value: Long) {
    companion object {
        // Represents an unchosen or "no date" state
        val None = FieldDateValue(Long.MIN_VALUE)
    }

    val inMillis: Long
        get() = if (this == None) 0L else value * 1000

    fun isNone(): Boolean = this == None
}

sealed class FieldDate {
    data class Chosen(val value: FieldDateValue) : FieldDate()
    object None : FieldDate()
}

// Update DateParser to return FieldDate instead of FieldDateValue
object DateParser {
    fun parse(value: Any?): FieldDate {
        val result: Long? = when (value) {
            is String -> value.toLongOrNull()
            is Double -> value.toLong()
            is Long -> value
            else -> null
        }
        return if (result != null) FieldDate.Chosen(FieldDateValue(result)) else FieldDate.None
    }
}