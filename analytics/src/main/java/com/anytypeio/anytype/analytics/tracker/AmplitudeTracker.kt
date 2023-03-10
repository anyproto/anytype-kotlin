package com.anytypeio.anytype.analytics.tracker

import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.anytypeio.anytype.analytics.BuildConfig
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.analytics.tracker.AmplitudeTracker.Companion.PROP_MIDDLE
import com.anytypeio.anytype.analytics.tracker.AmplitudeTracker.Companion.PROP_RENDER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class AmplitudeTracker(
    scope: CoroutineScope,
    private val analytics: Analytics
) : Tracker {

    companion object {
        const val PROP_MIDDLE = "middleTime"
        const val PROP_RENDER = "renderTime"
    }

    private val tracker: AmplitudeClient get() = Amplitude.getInstance()

    init {
        scope.launch { startRegisteringEvents() }
        scope.launch { startRegisteringUserProps() }
    }

    private suspend fun startRegisteringEvents() {
        analytics.observeEvents().collect { event ->
            if (BuildConfig.SEND_EVENTS && event is EventAnalytics.Anytype) {
                val props = event.props.getEventProperties(
                    startTime = event.duration?.start,
                    middleTime = event.duration?.middleware,
                    renderTime = event.duration?.render
                )
                tracker.logEvent(event.name, props)
                Timber.d("Analytics Amplitude(event = $event)")
            }
        }
    }

    private suspend fun startRegisteringUserProps() {
        analytics.observeUserProperties().collect { prop: UserProperty ->
            if (BuildConfig.SEND_EVENTS && prop is UserProperty.AccountId) {
                tracker.setUserId(prop.id, true)
            }
        }
    }
}

fun Props.getEventProperties(startTime: Long?, middleTime: Long?, renderTime: Long?): JSONObject {
    val map = map
    return JSONObject().apply {
        if (map.isNotEmpty()) {
            map.forEach { (key, value) ->
                if (key != null) {
                    try {
                        this.put(key, value)
                    } catch (e: JSONException) {
                        Timber.e("Analytics props exception:${e.message}")
                    }
                }
            }
        }
        if (startTime != null && middleTime != null) {
            try {
                this.put(PROP_MIDDLE, middleTime - startTime)
            } catch (e: JSONException) {
                Timber.e("Analytics props exception:${e.message}")
            }
        }
        if (middleTime != null && renderTime != null) {
            try {
                this.put(PROP_RENDER, renderTime - middleTime)
            } catch (e: JSONException) {
                Timber.e("Analytics props exception:${e.message}")
            }
        }
    }
}