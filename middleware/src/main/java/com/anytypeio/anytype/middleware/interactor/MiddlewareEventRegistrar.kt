package com.anytypeio.anytype.middleware.interactor

import service.Service.setEventHandlerMobile

/**
 * Seam over the gomobile event-handler registration. Keeping the native
 * [setEventHandlerMobile] call behind this interface lets [EventHandler] be unit-tested without
 * loading the gomobile native library (which is unavailable in JVM unit tests).
 */
fun interface MiddlewareEventRegistrar {
    /** Register [onEvent] as the single sink for raw middleware event bytes. Called once. */
    fun register(onEvent: (ByteArray?) -> Unit)
}

class DefaultMiddlewareEventRegistrar : MiddlewareEventRegistrar {
    override fun register(onEvent: (ByteArray?) -> Unit) {
        setEventHandlerMobile { bytes -> onEvent(bytes) }
    }
}
