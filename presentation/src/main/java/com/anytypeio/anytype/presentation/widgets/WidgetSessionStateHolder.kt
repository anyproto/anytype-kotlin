package com.anytypeio.anytype.presentation.widgets

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

interface WidgetSessionStateHolder {
    val session: StateFlow<Session>
    val isSessionActive: Flow<Boolean>

    fun onSessionStarted()
    fun onSessionStopped()
    fun onSessionFailed()

    class Impl @Inject constructor() : WidgetSessionStateHolder {

        private val state = MutableStateFlow<Session>(Session.Init)

        override val session: StateFlow<Session> = state

        override val isSessionActive: Flow<Boolean> = session.map { update ->
            update is Session.Started || update is Session.Resumed
        }

        override fun onSessionStarted() {
            if (state.value is Session.Init)
                state.value = Session.Started
            else
                state.value = Session.Resumed
        }

        override fun onSessionStopped() {
            state.value = Session.Stopped
        }

        override fun onSessionFailed() {
            state.value = Session.Failed
        }
    }
}


sealed class Session {
    object Init : Session()
    object Started : Session()
    object Stopped : Session()
    object Resumed : Session()
    object Failed : Session()
}