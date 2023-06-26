package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject

interface MiddlewareProtobufLogger {

    fun logRequest(any: Any)

    fun logResponse(any: Any)

    fun logEvent(any: Any)

    class Impl @Inject constructor(
        private val protobufConverter: ProtobufConverterProvider,
        private val featureToggles: FeatureToggles
    ) : MiddlewareProtobufLogger {

        override fun logRequest(any: Any) {
            if (featureToggles.isLogMiddlewareInteraction) {
                Timber.d("request -> ${any.toLogMessage()}")
            }
        }

        override fun logResponse(any: Any) {
            if (featureToggles.isLogMiddlewareInteraction) {
                Timber.d("response -> ${any.toLogMessage()}")
            }
        }

        override fun logEvent(any: Any) {
            if (featureToggles.isLogMiddlewareInteraction) {
                if (featureToggles.excludeThreadStatusLogging) {
                    if (any is Event && containsOnlyThreadStatusEvents(any)) {
                        // Do nothing.
                    } else {
                        Timber.d("event -> ${any.toLogMessage()}")
                    }
                } else {
                    Timber.d("event -> ${any.toLogMessage()}")
                }

            }
        }

        private fun Any.toLogMessage(): String {
            return "${this::class.java.canonicalName}:\n${
                protobufConverter.provideConverter().toJson(this)
            }"
        }

        private fun containsOnlyThreadStatusEvents(event: Event) : Boolean {
            return event.messages.all { msg ->
                msg.threadStatus != null
            }
        }
    }
}
