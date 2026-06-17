package com.anytypeio.anytype.presentation.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

interface SupportNavigation<Navigation> {
    val navigation: MutableLiveData<Navigation>
    fun observeNavigation(): LiveData<Navigation> = navigation

    fun navigate(command: Navigation) {
        navigation.postValue(command)
    }
}

open class NavigationViewModel<Navigation> : BaseViewModel() {
    /**
     * Backed by an unbounded [Channel] (not a `replay = 0` [MutableSharedFlow]) so a
     * destination emitted while the screen is not collecting — e.g. a deeplink
     * resolved during the activity stop -> resume gap on cold start — is buffered and
     * delivered to the next subscriber instead of being silently dropped (DROID-4523).
     * Each screen has a single collector, so [receiveAsFlow]'s single-consumer
     * semantics are correct.
     */
    private val _navigation = Channel<Navigation>(Channel.UNLIMITED)

    /**
     * Single-consumer invariant: collect this from exactly ONE place. The backing
     * channel hands each destination to a single receiver, so a second concurrent
     * collector would silently steal half the destinations. Re-collecting
     * sequentially across lifecycle bounces (`onStart`/`onStop`,
     * `repeatOnLifecycle(STARTED)`) is fine — that is one consumer at a time.
     */
    val navigation: Flow<Navigation> = _navigation.receiveAsFlow()
    fun navigate(destination: Navigation) = viewModelScope.launch {
        _navigation.send(destination)
    }
    suspend fun navigation(destination: Navigation) {
        _navigation.send(destination)
    }
}