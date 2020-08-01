package com.agileburo.anytype.analytics.tracker

import com.agileburo.anytype.analytics.base.Analytics
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AmplitudeTracker(
    private val scope: CoroutineScope,
    private val analytics: Analytics
) : Tracker {

    private val tracker: AmplitudeClient get() = Amplitude.getInstance()

    init {
        scope.launch { startRegisteringEvents() }
    }

    private suspend fun startRegisteringEvents() {
        analytics.observeEvents().collect { event ->
            tracker.logEvent(event.name)
        }
    }
}