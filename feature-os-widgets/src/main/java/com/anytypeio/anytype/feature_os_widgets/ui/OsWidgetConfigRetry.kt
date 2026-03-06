package com.anytypeio.anytype.feature_os_widgets.ui

import kotlinx.coroutines.delay

private const val CONFIG_RETRY_ATTEMPTS = 4
private const val CONFIG_RETRY_INITIAL_DELAY_MS = 100L

internal suspend fun <T> loadWidgetConfigWithRetry(
    load: suspend () -> T?
): T? {
    var delayMs = CONFIG_RETRY_INITIAL_DELAY_MS
    repeat(CONFIG_RETRY_ATTEMPTS) { attempt ->
        load()?.let { return it }
        if (attempt < CONFIG_RETRY_ATTEMPTS - 1) {
            delay(delayMs)
            delayMs *= 2
        }
    }
    return null
}
