package com.anytypeio.anytype.core_models.primitives


//sealed class Either<out R> {
//    data object None : Either<Nothing>()
//    data class Value<out R>(val v: R) : Either<R>()
//
//    fun get(none: () -> Any, value: (R) -> Any): Any =
//        when (this) {
//            is None -> none()
//            is Value -> value(v)
//        }
//}
//
//sealed class Field {
//
//    abstract val value: Either<Value>
//
//    data class Text(
//        override val value: Either<Value.Text>
//    ) : Field()
//
//    data class Date(
//        override val value: Either<Value.Date>
//    ) : Field() {
//    }
//
//    sealed class Value {
//        data class Text(val value: String) : Value()
//        data class Date(val value: Long) : Value()
//    }
//}

sealed class Value<T> {
    data class Single<T>(val value: T) : Value<T>()
    data class Multiple<T>(val values: List<T>) : Value<T>()
}

sealed class Field<T>(open val value: Value<T>?) {
    data class Text(override val value: Value<String>?) : Field<String>(value)
    data class Date(override val value: Value<Long>?) : Field<Long>(value)
}

object DateParser {
    fun parse(value: Any?): Field.Date {
        val result: Value<Long>? = when (value) {
            is String -> value.toLongOrNull()?.let { Value.Single(it) }
            is Number -> Value.Single(value.toLong())
            is List<*> -> {
                val longs = value.mapNotNull {
                    when (it) {
                        is String -> it.toLongOrNull()
                        is Number -> it.toLong()
                        else -> null
                    }
                }
                if (longs.isNotEmpty()) Value.Multiple(longs) else null
            }
            else -> null
        }
        return Field.Date(value = result)
    }
}