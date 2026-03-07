package com.anytypeio.anytype.feature_os_widgets.ui

import kotlinx.coroutines.delay
import timber.log.Timber

private const val TAG = "OsWidgetConfigRetry"

/**
 * Initial fast retries to pick up config that was saved just before provideGlance.
 */
private const val FAST_RETRY_ATTEMPTS = 4
private const val FAST_RETRY_INITIAL_DELAY_MS = 100L

/**
 * Slow poll that keeps provideGlance alive so that a subsequent
 * Glance update() cancels this coroutine and triggers a fresh provideGlance
 * (which will then find the saved config).
 */
private const val SLOW_POLL_INTERVAL_MS = 2_000L
private const val SLOW_POLL_MAX_DURATION_MS = 8_000L

internal suspend fun <T> loadWidgetConfigWithRetry(
    load: suspend () -> T?
): T? {
    // Phase 1: fast exponential-backoff retries
    var delayMs = FAST_RETRY_INITIAL_DELAY_MS
    repeat(FAST_RETRY_ATTEMPTS) { attempt ->
        load()?.let { return it }
        if (attempt < FAST_RETRY_ATTEMPTS - 1) {
            delay(delayMs)
            delayMs *= 2
        }
    }

    // Phase 2: slow poll — keeps provideGlance coroutine alive so that
    // the next Glance update() call cancels it and starts a fresh one.
    Timber.tag(TAG).d("Fast retries exhausted, entering slow poll")
    var elapsed = 0L
    while (elapsed < SLOW_POLL_MAX_DURATION_MS) {
        delay(SLOW_POLL_INTERVAL_MS)
        elapsed += SLOW_POLL_INTERVAL_MS
        load()?.let {
            Timber.tag(TAG).d("Config found during slow poll after ${elapsed}ms")
            return it
        }
    }
    Timber.tag(TAG).d("Slow poll timed out after ${SLOW_POLL_MAX_DURATION_MS}ms")
    return null
}
