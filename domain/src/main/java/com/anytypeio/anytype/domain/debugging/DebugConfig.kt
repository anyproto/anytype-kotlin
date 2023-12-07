package com.anytypeio.anytype.domain.debugging

interface DebugConfig {
    val traceSubscriptions: Boolean
    val setTimeouts: Boolean

    companion object {
        const val SELECT_ACCOUNT_TIMEOUT = 0L
        const val CREATE_ACCOUNT_TIMEOUT = 5000L
    }
}