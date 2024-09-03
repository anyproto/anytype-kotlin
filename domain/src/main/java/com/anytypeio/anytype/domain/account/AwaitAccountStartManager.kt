package com.anytypeio.anytype.domain.account

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance

interface AwaitAccountStartManager {

    fun state(): Flow<State>
    fun setState(state: State)
    fun awaitStart(): Flow<State.Started>
    fun awaitStopped(): Flow<State.Stopped>

    object Default: AwaitAccountStartManager {
        private val state = MutableStateFlow<State>(State.Init)
        override fun state(): Flow<State> = state
        override fun setState(state: State) { this.state.value = state }
        override fun awaitStart(): Flow<State.Started> = state.filterIsInstance()
        override fun awaitStopped(): Flow<State.Stopped> = state.filterIsInstance()
    }

    sealed class State {
        data object Init : State()
        data object Started : State()
        data object Stopped : State()
    }
}