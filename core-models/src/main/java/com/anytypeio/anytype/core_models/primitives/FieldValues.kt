package com.anytypeio.anytype.core_models.primitives

import com.anytypeio.anytype.core_models.RelativeDate

sealed class Value<T> {
    data class Single<T>(val param: T) : Value<T>()
    data class Multiple<T>(val params: List<T>) : Value<T>()
}

sealed class Field<T>(open val value: Value<T>?) {
    data class Text(override val value: Value<String>?) : Field<String>(value)
    data class Date(override val value: Value.Single<FieldDateValue>) : Field<FieldDateValue>(value) {
        val timestamp: TimestampInSeconds
            get() = value.param.timestamp
        val relativeDate: RelativeDate
            get() = value.param.relativeDate
    }
}

data class FieldDateValue(
    val timestamp: TimestampInSeconds,
    val relativeDate: RelativeDate
)