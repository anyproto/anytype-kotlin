package com.anytypeio.anytype.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.openAccount
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.base.updateUserProperties
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.domain.account.InterceptAccountStatus
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.wallpaper.ObserveWallpaper
import com.anytypeio.anytype.domain.wallpaper.RestoreWallpaper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val launchAccount: LaunchAccount,
    private val observeWallpaper: ObserveWallpaper,
    private val restoreWallpaper: RestoreWallpaper,
    private val analytics: Analytics,
    private val interceptAccountStatus: InterceptAccountStatus,
    private val logout: Logout
) : ViewModel() {

    val wallpaper = MutableStateFlow<Wallpaper>(Wallpaper.Default)
    val commands = MutableSharedFlow<Command>(replay = 0)
    val toasts = MutableSharedFlow<String>(replay = 0)

    init {
        viewModelScope.launch { restoreWallpaper(BaseUseCase.None) }
        viewModelScope.launch {
            observeWallpaper.build(BaseUseCase.None).collect {
                wallpaper.value = it
            }
        }
        viewModelScope.launch {
            interceptAccountStatus.build().collect { status ->
                when (status) {
                    is AccountStatus.PendingDeletion -> {
                        commands.emit(
                            Command.ShowDeletedAccountScreen(
                                deadline = status.deadline
                            )
                        )
                    }
                    is AccountStatus.Deleted -> {
                        proceedWithLogoutDueToAccountDeletion()
                    }
                    else -> {
                        // Do nothing
                    }
                }
            }
        }
    }

    private fun proceedWithLogoutDueToAccountDeletion() {
        viewModelScope.launch {
            logout(Logout.Params(false)).collect { status ->
                when (status) {
                    is Interactor.Status.Error -> {
                        toasts.emit("Error while logging out due to account deletion")
                    }
                    is Interactor.Status.Started -> {
                        toasts.emit("Your account is deleted. Logging out...")
                    }
                    is Interactor.Status.Success -> {
                        commands.emit(Command.LogoutDueToAccountDeletion)
                    }
                }
            }
        }
    }

    fun onRestore() {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            launchAccount(BaseUseCase.None).either(
                fnR = { id ->
                    updateUserProperties(
                        analytics = analytics,
                        userProperty = UserProperty.AccountId(id)
                    )
                    Timber.d("Restored account after activity recreation")
                    sendAuthEvent(startTime, id)
                },
                fnL = { error ->
                    Timber.e(error, "Error while launching account after activity recreation")
                }
            )
        }
    }

    private fun sendAuthEvent(start: Long, id: String) {
        val middle = System.currentTimeMillis()
        viewModelScope.sendEvent(
            analytics = analytics,
            startTime = start,
            middleTime = middle,
            eventName = openAccount
        )
    }

    sealed class Command {
        data class ShowDeletedAccountScreen(val deadline: Long) : Command()
        object LogoutDueToAccountDeletion : Command()
    }
}