package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainSettingsViewModel(
    private val analytics: Analytics
) : ViewModel() {

    val events = MutableSharedFlow<Event>(replay = 0)
    val commands = MutableSharedFlow<Command>(replay = 0)

    init {
        events
            .throttleFirst()
            .onEach { event -> dispatchAnalyticEvent(event) }
            .onEach { event -> dispatchCommand(event) }
            .launchIn(viewModelScope)
    }

    fun onOptionClicked(event: Event) {
        viewModelScope.launch { events.emit(event) }
    }

    private suspend fun dispatchCommand(event: Event) {
        when (event) {
            Event.OnAboutClicked -> commands.emit(Command.OpenAboutScreen)
            Event.OnAccountAndDataClicked -> commands.emit(Command.OpenAccountAndDataScreen)
            Event.OnAppearanceClicked -> commands.emit(Command.OpenAppearanceScreen)
            Event.OnPersonalizationClicked -> commands.emit(Command.OpenPersonalizationScreen)
        }
    }

    private fun dispatchAnalyticEvent(event: Event) {
        when (event) {
            Event.OnAboutClicked -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.aboutScreenShow
                )
            }
            Event.OnAccountAndDataClicked -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.accountDataSettingsShow
                )
            }
            Event.OnAppearanceClicked -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.appearanceScreenShow
                )
            }
            Event.OnPersonalizationClicked -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.personalisationSettingsShow
                )
            }
        }
    }

    class Factory(
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = MainSettingsViewModel(
            analytics = analytics
        ) as T
    }

    sealed class Event {
        object OnAboutClicked : Event()
        object OnAppearanceClicked: Event()
        object OnAccountAndDataClicked: Event()
        object OnPersonalizationClicked: Event()
    }

    sealed class Command {
        object OpenAboutScreen : Command()
        object OpenAppearanceScreen: Command()
        object OpenAccountAndDataScreen: Command()
        object OpenPersonalizationScreen: Command()
    }
}