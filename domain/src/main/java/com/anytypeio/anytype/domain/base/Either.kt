package com.anytypeio.anytype.domain.base

sealed class Either<out L, out R> {
    /** * Represents the left side of [Either] class which by convention is a "Failure". */
    data class Left<out L>(val a: L) : Either<L, Nothing>()

    /** * Represents the right side of [Either] class which by convention is a "Success". */
    data class Right<out R>(val b: R) : Either<Nothing, R>()

    val isRight get() = this is Right<R>
    val isLeft get() = this is Left<L>

    fun <L> left(a: L) = Left(a)
    fun <R> right(b: R) = Right(b)

    fun either(fnL: (L) -> Any, fnR: (R) -> Any): Any =
        when (this) {
            is Left -> fnL(a)
            is Right -> fnR(b)
        }

    suspend fun proceed(
        failure: suspend (L) -> Any,
        success: suspend (R) -> Any
    ): Any = when (this) {
        is Left -> failure(a)
        is Right -> success(b)
    }

    suspend fun process(
        failure: suspend (L) -> Unit,
        success: suspend (R) -> Unit
    ): Unit = when (this) {
        is Left -> failure(a)
        is Right -> success(b)
    }
}