package com.anytypeio.anytype.core_utils.ext

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

fun <T> Flow<T>.throttleFirst(windowDuration: Long = DEFAULT_THROTTLE_DURATION): Flow<T> = flow {
    var windowStartTime = System.currentTimeMillis()
    var emitted = false
    collect { value ->
        val currentTime = System.currentTimeMillis()
        val delta = currentTime - windowStartTime
        if (delta >= windowDuration) {
            windowStartTime += delta / windowDuration * windowDuration
            emitted = false
        }
        if (!emitted) {
            emit(value)
            emitted = true
        }
    }
}

fun <A, B : Any?, R> Flow<A>.withLatestFrom(
    other: Flow<B>,
    transform: suspend (A, B) -> R
): Flow<R> = flow {
    coroutineScope {
        val latestB = AtomicReference<B?>()
        val outerScope = this
        launch {
            try {
                other.collect { latestB.set(it) }
            } catch (e: CancellationException) {
                outerScope.cancel(e) // cancel outer scope on cancellation exception, too
            }
        }
        collect { a: A ->
            latestB.get()?.let { b -> emit(transform(a, b)) }
        }
    }
}

/**
 * Every time a new value is emitted from A, it will emit instead the latest value from B.
 */
fun <A, B : Any> Flow<A>.switchToLatestFrom(other: Flow<B>): Flow<B> = flow {
    coroutineScope {
        val latestB = AtomicReference<B?>()
        val outerScope = this
        launch {
            try {
                other.collect { latestB.set(it) }
            } catch (e: CancellationException) {
                outerScope.cancel(e)
            }
        }
        collect {
            latestB.get()?.let { b -> emit(b) }
        }
    }
}

fun <A, B, C : Any?, R> Flow<A>.withLatestFrom(
    other: Flow<B>,
    another: Flow<C>,
    transform: suspend (A, B, C) -> R
): Flow<R> = flow {
    coroutineScope {
        val latestB = AtomicReference<B?>()
        val latestC = AtomicReference<C?>()
        val outerScope = this
        launch {
            try {
                other.collect { latestB.set(it) }
            } catch (e: CancellationException) {
                outerScope.cancel(e) // cancel outer scope on cancellation exception, too
            }
        }
        launch {
            try {
                another.collect { latestC.set(it) }
            } catch (e: CancellationException) {
                outerScope.cancel(e) // cancel outer scope on cancellation exception, too
            }
        }
        collect { a: A ->
            val b = latestB.get()
            val c = latestC.get()
            if (b != null && c != null) {
                emit(transform(a, b, c))
            }
        }
    }
}

fun MutableList<Job>.cancel() {
    forEach { it.cancel() }
    clear()
}

const val DEFAULT_THROTTLE_DURATION = 1000L
const val LONG_THROTTLE_DURATION = 2000L