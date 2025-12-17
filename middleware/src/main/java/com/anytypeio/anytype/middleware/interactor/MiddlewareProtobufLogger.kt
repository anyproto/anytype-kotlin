package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import timber.log.Timber

interface MiddlewareProtobufLogger {

    fun logRequest(any: Any)

    fun logResponse(any: Any)

    fun logResponse(any: Any, time: Duration?)

    fun logEvent(any: Any)

    class Impl @Inject constructor(
        private val protobufConverter: ProtobufConverterProvider,
        private val featureToggles: FeatureToggles
    ) : MiddlewareProtobufLogger {

        private val isConciseLogging: Boolean
            get() = false//featureToggles.isConciseLogging

        override fun logRequest(any: Any) {
            if (featureToggles.isLogMiddlewareInteraction) {
                Timber.d("request -> ${any.toLogMessage(isConciseLogging)}")
            }
        }

        override fun logResponse(any: Any) {
            if (featureToggles.isLogMiddlewareInteraction) {
                Timber.d("response -> ${any.toLogMessage(isConciseLogging)}")
            }
        }

        override fun logResponse(any: Any, time: Duration?) {
            Timber.d("response -> ${any.toLogMessage(isConciseLogging)} [${time.format()}ms] ")
        }

        private fun Duration?.format(): Long? = this?.toLong(DurationUnit.MILLISECONDS)

        override fun logEvent(any: Any) {
            if (featureToggles.isLogMiddlewareInteraction) {
                val event = any as? Event
                if (event?.messages[0]?.p2pStatusUpdate != null || event?.messages[0]?.spaceSyncStatusUpdate != null) return
                Timber.d("event -> ${any.toLogMessage(isConciseLogging)}")
            }
        }

        private fun Any.toLogMessage(isConcise: Boolean): String {
            return if (isConcise) {
                this::class.java.canonicalName
            } else {
                "${this::class.java.canonicalName}:\n${
                    protobufConverter.provideConverter().toJson(this)
                }"
            }
        }
    }
}
