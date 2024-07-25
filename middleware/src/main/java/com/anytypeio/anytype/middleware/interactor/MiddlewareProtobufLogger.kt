package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_utils.tools.FeatureToggles
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
                Timber.d("event -> ${any.toLogMessage()}")
            }
        }

        private fun Any.toLogMessage(): String {
            return if (featureToggles.isConciseLogging) {
                this::class.java.canonicalName
            } else {
                "${this::class.java.canonicalName}:\n${
                    protobufConverter.provideConverter().toJson(this)
                }"
            }
        }
    }
}
