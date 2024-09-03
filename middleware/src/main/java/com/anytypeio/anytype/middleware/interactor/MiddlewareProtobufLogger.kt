package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit

interface MiddlewareProtobufLogger {

    fun logRequest(any: Any)

    fun logResponse(any: Any)

    fun logResponse(any: Any, time: Duration?)

    fun logEvent(any: Any)

    class Impl @Inject constructor(
        private val protobufConverter: ProtobufConverterProvider,
        private val featureToggles: FeatureToggles
    ) : MiddlewareProtobufLogger {

        override fun logRequest(any: Any) {
            //if (featureToggles.isLogMiddlewareInteraction) {
                Timber.d("request -> ${any.toLogMessage(false)}")
            //}
        }

        override fun logResponse(any: Any) {
            if (featureToggles.isLogMiddlewareInteraction) {
                Timber.d("response -> ${any.toLogMessage(true)}")
            }
        }

        override fun logResponse(any: Any, time: Duration?) {
            Timber.d("response -> ${any.toLogMessage(true)} [${time.format()}ms] ")
        }

        private fun Duration?.format(): Long? = this?.toLong(DurationUnit.MILLISECONDS)

        override fun logEvent(any: Any) {
            if (featureToggles.isLogMiddlewareInteraction) {
                //Timber.d("event -> ${any.toLogMessage(false)}")
            }
        }

        private fun Any.toLogMessage(isConciseLogging: Boolean): String {
            return if (isConciseLogging) {
                this::class.java.canonicalName
            } else {
                "${this::class.java.canonicalName}:\n${
                    protobufConverter.provideConverter().toJson(this)
                }"
            }
        }
    }
}
