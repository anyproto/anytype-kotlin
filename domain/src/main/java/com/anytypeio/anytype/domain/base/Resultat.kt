package com.anytypeio.anytype.domain.base

import kotlin.Result

/*
 * Copyright 2022 Nicolas Haan.
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.md file.
 */
/**
 * A sealed class that encapsulates a successful outcome with a value of type [T]
 * or a failure with an arbitrary [Throwable] exception,
 * or a loading state
 * @see: This is a fork of Kotlin [kotlin.Result] class with an additional Loading state,
 * but preserving Result API
 */
sealed class Resultat<out T> {

    /**
     * This type represent a successful outcome.
     * @param value The encapsulated successful value
     */
    data class Success<out T>(val value: T) : Resultat<T>() {
        override fun toString(): String = "Success($value)"
    }

    /**
     * This type represents a failed outcome.
     * @param exception The encapsulated exception value
     */
    data class Failure(val exception: Throwable) : Resultat<Nothing>() {
        override fun toString(): String = "Failure($exception)"
    }

    /**
     * This type represents a loading state.
     */
    class Loading : Resultat<Nothing>() {
        override fun toString(): String = "Loading"
    }

    // discovery

    /**
     * Returns `true` if this instance represents a successful outcome.
     * In this case [isFailure] returns `false`.
     * In this case [isLoading] returns `false`.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns `true` if this instance represents a failed outcome.
     * In this case [isSuccess] returns `false`.
     * In this case [isLoading] returns `false`.
     */
    val isFailure: Boolean get() = this is Failure

    /**
     * Returns `true` if this instance represents a loading outcome.
     * In this case [isSuccess] returns `false`.
     * In this case [isFailure] returns `false`.
     */
    val isLoading: Boolean get() = this is Loading

    // value & exception retrieval

    /**
     * Returns the encapsulated value if this instance represents [success][Resultat.isSuccess] or `null`
     * if it is [failure][Resultat.isFailure] or [Resultat.Loading].
     */
    inline fun getOrNull(): T? =
        when (this) {
            is Failure -> null
            is Loading -> null
            is Success -> value
        }

    /**
     * Returns the encapsulated [Throwable] exception if this instance represents [failure][isFailure] or `null`
     * if it is [success][isSuccess] or [loading][isLoading].
     */
    inline fun exceptionOrNull(): Throwable? =
        when (this) {
            is Failure -> exception
            is Success -> null
            is Loading -> null
        }

    // companion with constructors

    /**
     * Companion object for [Resultat] class that contains its constructor functions
     * [success], [loading] and [failure].
     */
    companion object {
        /**
         * Returns an instance that encapsulates the given [value] as successful value.
         */
        inline fun <T> success(value: T): Resultat<T> = Success(value)

        /**
         * Returns an instance that encapsulates the given [Throwable] [exception] as failure.
         */
        inline fun <T> failure(exception: Throwable): Resultat<T> = Failure(exception)

        /**
         * Returns an instance that represents the loading state.
         */
        inline fun <T> loading(): Resultat<T> = Loading()
    }
}

private val loadingException = Throwable("No value available: Loading")


/**
 * Calls the specified function [block] with `this` value as its receiver and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating it as a failure.
 */
inline fun <T, R> T.runCatchingL(block: T.() -> R): Resultat<R> {
    return try {
        Resultat.success(block())
    } catch (e: Throwable) {
        Resultat.failure(e)
    }
}

// -- extensions ---

/**
 * Returns the encapsulated value if this instance represents [success][Resultat.isSuccess] or throws the encapsulated [Throwable] exception
 * if it is [failure][Resultat.isFailure].
 *
 * This function is a shorthand for `getOrElse { throw it }` (see [getOrElse]).
 */
fun <T> Resultat<T>.getOrThrow(): T {
    when (this) {
        is Resultat.Failure -> throw exception
        is Resultat.Loading -> throw loadingException
        is Resultat.Success -> return value
    }
}

/**
 * Returns the encapsulated value if this instance represents [success][Resultat.isSuccess] or the
 * result of [onFailure] function for the encapsulated [Throwable] exception if it is [failure][Resultat.isFailure]
 * or is [loading][Resultat.Loading].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [onFailure] function.
 *
 */
fun <R, T : R> Resultat<T>.getOrElse(onFailure: (exception: Throwable) -> R): R {
    return when (this) {
        is Resultat.Failure -> onFailure(exception)
        is Resultat.Loading -> onFailure(loadingException)
        is Resultat.Success -> value
    }
}

/**
 * Returns the encapsulated value if this instance represents [success][Resultat.isSuccess] or the
 * [defaultValue] if it is [failure][Resultat.isFailure] or [loading][Resultat.isLoading].
 *
 * This function is a shorthand for `getOrElse { defaultValue }` (see [getOrElse]).
 */
inline fun <R, T : R> Resultat<T>.getOrDefault(defaultValue: R): R {
    return when (this) {
        is Resultat.Failure -> defaultValue
        is Resultat.Loading -> defaultValue
        is Resultat.Success -> value
    }
}

/**
 * Returns the result of [onSuccess] for the encapsulated value if this instance represents [success][Resultat.isSuccess]
 * or the result of [onFailure] function for the encapsulated [Throwable] exception if it is [failure][Resultat.isFailure].
 * or the result of [onLoading] function if it is [loading][Resultat.isLoading].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [onSuccess] or by [onFailure] function.
 */
inline fun <T> Resultat<T>.fold(
    onSuccess: (value: T) -> Unit = {},
    onFailure: (exception: Throwable) -> Unit = {},
    onLoading: () -> Unit = {},
) {
    return when (this) {
        is Resultat.Failure -> onFailure(exception)
        is Resultat.Loading -> onLoading()
        is Resultat.Success -> onSuccess(value)
    }
}

suspend fun <T> Resultat<T>.suspendFold(
    onSuccess: suspend (value: T) -> Unit,
    onFailure: suspend (Throwable) -> Unit = {},
    onLoading: suspend () -> Unit = {},
): Unit {
    return when (this) {
        is Resultat.Failure -> onFailure(exception)
        is Resultat.Loading -> onLoading()
        is Resultat.Success -> onSuccess(value)
    }
}

// transformation

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
 * if this instance represents [success][Resultat.isSuccess] or the
 * original encapsulated [Throwable] exception if it is [failure][Resultat.isFailure] or [loading][Resultat.Loading].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [transform] function.
 * See [mapCatching] for an alternative that encapsulates exceptions.
 */
inline fun <R, T> Resultat<T>.map(transform: (value: T) -> R): Resultat<R> {
    return when (this) {
        is Resultat.Failure -> this
        is Resultat.Success -> Resultat.success(transform(value))
        is Resultat.Loading -> this
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
 * if this instance represents [success][Resultat.isSuccess] or the
 * original encapsulated [Throwable] exception if it is [failure][Resultat.isFailure]
 * or the loading state if it [loading][Resultat.Loading].
 *
 * This function catches any [Throwable] exception thrown by [transform] function and encapsulates it as a failure.
 * See [map] for an alternative that rethrows exceptions from `transform` function.
 */
inline fun <R, T> Resultat<T>.mapCatching(transform: (value: T) -> R): Resultat<R> {
    return when (this) {
        is Resultat.Failure -> Resultat.Failure(exception)
        is Resultat.Success -> runCatchingL { transform(value) }
        is Resultat.Loading -> this
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated [Throwable] exception
 * if this instance represents [failure][Resultat.isFailure] or the
 * original encapsulated value if it is [success][Resultat.isSuccess].
 *
 * @param recoverLoading Whether loading state calls transform or exposes untouched loading state
 * Note, that this function rethrows any [Throwable] exception thrown by [transform] function.
 * See [recoverCatching] for an alternative that encapsulates exceptions.
 */
inline fun <R, T : R> Resultat<T>.recover(
    recoverLoading: Boolean = false,
    transform: (exception: Throwable) -> R,
): Resultat<R> {

    return when (this) {
        is Resultat.Success -> this
        is Resultat.Failure -> Resultat.success(transform(exception))
        is Resultat.Loading -> if (!recoverLoading) {
            this
        } else {
            Resultat.success(transform(Throwable("No value available: Loading")))
        }
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated [Throwable] exception
 * if this instance represents [failure][Resultat.isFailure] or the
 * original encapsulated value if it is [success][Resultat.isSuccess].
 *
 * @param recoverLoading Whether loading state calls transform or exposes untouched loading state
 * This function catches any [Throwable] exception thrown by [transform] function and encapsulates it as a failure.
 * See [recover] for an alternative that rethrows exceptions.
 */
inline fun <R, T : R> Resultat<T>.recoverCatching(
    recoverLoading: Boolean = false,
    transform: (exception: Throwable) -> R,
): Resultat<R> {
    return when (this) {
        is Resultat.Success -> this
        is Resultat.Failure -> runCatchingL { transform(exception) }
        is Resultat.Loading -> if (!recoverLoading) {
            this
        } else {
            runCatchingL { transform(Throwable("No value available: Loading")) }
        }
    }
}

// "peek" onto value/exception and pipe

/**
 * Performs the given [action] on the encapsulated [Throwable] exception if this instance represents [failure][Resultat.isFailure].
 * Returns the original `Resultat` unchanged.
 */
inline fun <T> Resultat<T>.onFailure(action: (exception: Throwable) -> Unit): Resultat<T> {
    exceptionOrNull()?.let { action(it) }
    return this
}

/**
 * Performs the given [action] on the encapsulated value if this instance represents [success][Resultat.isSuccess].
 * Returns the original `Resultat` unchanged.
 */
inline fun <T> Resultat<T>.onSuccess(action: (value: T) -> Unit): Resultat<T> {
    if (this is Resultat.Success) action(value)
    return this
}

/**
 * Performs the given [action] on the encapsulated value if this instance represents [success][Resultat.isLoading].
 * Returns the original `Resultat` unchanged.
 */
inline fun <T> Resultat<T>.onLoading(action: () -> Unit): Resultat<T> {
    if (this is Resultat.Loading) action()
    return this
}

/**
 * Convert Kotlin [Result] to Resultat type
 */
fun <T> Result<T>.toResultat(): Resultat<T> = fold(
    onFailure = {
        Resultat.failure(it)
    }, onSuccess = {
        Resultat.success(it)
    }
)

/**
 * Convert [Resultat] to Kotlin [Result]
 * if [Resultat] is [Resultat.Loading], null is returned
 */
fun <T> Resultat<T>.toResult(): Result<T>? {
    return when (this) {
        is Resultat.Loading -> null
        is Resultat.Success -> Result.success(this.value)
        is Resultat.Failure -> Result.failure(this.exception)
    }
}