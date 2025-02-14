package com.anytypeio.anytype.core_models.primitives

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelativeDate

sealed class Value<T> {
    data class Single<T>(val single: T) : Value<T>()
    data class Multiple<T>(val multiple: List<T>) : Value<T>()
}

sealed class Field<T>(open val value: Value<T>) {
    data class Text(override val value: Value<String>) : Field<String>(value)
    data class Date(override val value: Value.Single<FieldDateValue>) : Field<FieldDateValue>(value) {
        val timestamp: TimestampInSeconds
            get() = value.single.timestamp
        val relativeDate: RelativeDate
            get() = value.single.relativeDate
    }
}

data class FieldDateValue(
    val timestamp: TimestampInSeconds,
    val relativeDate: RelativeDate
)

data class ParsedFields(
    val featured: List<ObjectWrapper.Relation> = emptyList(),
    val sidebar: List<ObjectWrapper.Relation> = emptyList(),
    val hidden: List<ObjectWrapper.Relation> = emptyList(),
    val conflictedWithoutSystem: List<ObjectWrapper.Relation> = emptyList(),
    val conflictedSystem: List<ObjectWrapper.Relation> = emptyList()
)