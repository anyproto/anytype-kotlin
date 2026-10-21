package com.anytypeio.anytype.domain.account

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance

interface AwaitAccountStartManager {

    fun state(): Flow<State>
    fun setState(state: State)
    fun awaitStart(): Flow<State.Started>
    fun awaitStopped(): Flow<State.Stopped>

    /** Synchronous snapshot: true once the account has started for this session. */
    fun hasStarted(): Boolean

    object Default: AwaitAccountStartManager {
        private val state = MutableStateFlow<State>(State.Init)
        override fun state(): Flow<State> = state
        override fun setState(state: State) { this.state.value = state }
        override fun awaitStart(): Flow<State.Started> = state.filterIsInstance()
        override fun awaitStopped(): Flow<State.Stopped> = state.filterIsInstance()
        override fun hasStarted(): Boolean = state.value is State.Started
    }

    sealed class State {
        data object Init : State()
        data object Started : State()
        data object Stopped : State()
    }
}