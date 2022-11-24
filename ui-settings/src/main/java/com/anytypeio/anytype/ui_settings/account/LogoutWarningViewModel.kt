package com.anytypeio.anytype.ui_settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class LogoutWarningViewModel(
    private val logout: Logout,
    private val analytics: Analytics,
    private val relationsSubscriptionManager: RelationsSubscriptionManager
) : ViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)
    val isLoggingOut = MutableStateFlow(false)

    fun onLogoutClicked() {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            logout(params = Logout.Params()).collect { status ->
                when (status) {
                    is Interactor.Status.Started -> {
                        isLoggingOut.value = true
                    }
                    is Interactor.Status.Success -> {
                        viewModelScope.sendEvent(
                            analytics = analytics,
                            startTime = startTime,
                            middleTime = System.currentTimeMillis(),
                            eventName = EventsDictionary.logout,
                            props = Props(
                                mapOf(
                                    EventsPropertiesKey.route to EventsDictionary.Routes.screenSettings
                                )
                            )
                        )
                        relationsSubscriptionManager.onStop()
                        isLoggingOut.value = false
                        commands.emit(Command.Logout)
                    }
                    is Interactor.Status.Error -> {
                        isLoggingOut.value = true
                        Timber.e(status.throwable, "Error while logging out")
                    }
                }
            }
        }
    }

    fun onBackupClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.keychainPhraseScreenShow,
            props = Props(
                mapOf(EventsPropertiesKey.type to EventsDictionary.Type.beforeLogout)
            )
        )
    }

    class Factory(
        private val logout: Logout,
        private val analytics: Analytics,
        private val relationsSubscriptionManager: RelationsSubscriptionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LogoutWarningViewModel(
                logout = logout,
                analytics = analytics,
                relationsSubscriptionManager = relationsSubscriptionManager
            ) as T
        }
    }

    sealed class Command {
        object Logout : Command()
    }
}