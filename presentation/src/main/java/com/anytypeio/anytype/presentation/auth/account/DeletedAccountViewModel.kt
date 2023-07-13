package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.domain.account.DateHelper
import com.anytypeio.anytype.domain.account.RestoreAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.time.DurationUnit.DAYS
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DeletedAccountViewModel(
    private val restoreAccount: RestoreAccount,
    private val logout: Logout,
    private val dateHelper: DateHelper,
    private val analytics: Analytics,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val appActionManager: AppActionManager
) : BaseViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)
    val progress = MutableStateFlow(0f)
    val isLoggingOut = MutableStateFlow(false)
    val date = MutableStateFlow<DeletionDate>(DeletionDate.Unknown)

    fun onStart(deadlineInMillis: Long, nowInMillis: Long) {
        val remainingInMillis = (deadlineInMillis - nowInMillis)
        if (remainingInMillis >= 0) {
            val days = remainingInMillis.toDuration(MILLISECONDS).toDouble(DAYS)
            when {
                dateHelper.isToday(deadlineInMillis) -> {
                    date.value = DeletionDate.Today
                }
                dateHelper.isTomorrow(deadlineInMillis) -> {
                    date.value = DeletionDate.Tomorrow
                }
                else -> {
                    date.value = DeletionDate.Later(
                        days = days.toBigDecimal().setScale(0, RoundingMode.HALF_EVEN).toInt()
                    )
                }
            }
            val delta = 1f - (days.toFloat() / DEADLINE_DURATION_IN_DAYS)
            this.progress.value = delta
        } else {
            sendToast("Your account has been deleted")
            progress.value = 1.0f
            date.value = DeletionDate.Deleted
            proceedWithLoggingOut()
        }
    }

    fun cancelDeletionClicked() {
        if (!isLoggingOut.value) {
            viewModelScope.launch {
                restoreAccount(BaseUseCase.None).process(
                    success = { status ->
                        when (status) {
                            is AccountStatus.Active -> {
                                commands.emit(Command.Resume)
                            }
                            is AccountStatus.Deleted -> {
                                sendToast("Sorry, your account has been deleted")
                                proceedWithLoggingOut()
                            }
                            is AccountStatus.PendingDeletion -> {
                                // TODO
                            }
                            else -> {}
                        }
                        sendEvent(
                            analytics = analytics,
                            eventName = EventsDictionary.cancelDeletion
                        )
                    },
                    failure = {
                        Timber.e(it, "Error while cancelling account deletion")
                        sendToast("Error while cancelling account deletion. Please, try again later.")
                    }
                )
            }
        } else {
            sendToast(LOG_OUT_MSG)
        }
    }

    fun onLogoutAndClearDataClicked() {
        proceedWithLoggingOut()
    }

    private fun clearShortcuts() {
        appActionManager.setup(AppActionManager.Action.ClearAll)
    }

    private fun proceedWithLoggingOut() {
        if (!isLoggingOut.value) {
            viewModelScope.launch {
                logout(Logout.Params(clearLocalRepositoryData = false)).collect { status ->
                    when (status) {
                        is Interactor.Status.Error -> {
                            isLoggingOut.value = false
                        }
                        is Interactor.Status.Started -> {
                            isLoggingOut.value = true
                        }
                        is Interactor.Status.Success -> {
                            clearShortcuts()
                            isLoggingOut.value = false
                            relationsSubscriptionManager.onStop()
                            commands.emit(Command.Logout)
                        }
                    }
                }
                sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.logout,
                    props = Props(
                        mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.screenDeletion)
                    )
                )
            }
        } else {
            sendToast(LOG_OUT_MSG)
        }
    }

    class Factory @Inject constructor(
        private val restoreAccount: RestoreAccount,
        private val logout: Logout,
        private val helper: DateHelper,
        private val analytics: Analytics,
        private val relationsSubscriptionManager: RelationsSubscriptionManager,
        private val appActionManager: AppActionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeletedAccountViewModel(
                restoreAccount = restoreAccount,
                logout = logout,
                dateHelper = helper,
                analytics = analytics,
                relationsSubscriptionManager = relationsSubscriptionManager,
                appActionManager = appActionManager
            ) as T
        }
    }

    sealed class Command {
        object Resume : Command()
        object Logout : Command()
    }

    sealed class DeletionDate {
        object Unknown : DeletionDate()
        object Deleted : DeletionDate()
        object Today : DeletionDate()
        object Tomorrow : DeletionDate()
        data class Later(val days: Int) : DeletionDate()
    }

    companion object {
        const val DEADLINE_DURATION_IN_DAYS = 30f
        const val LOG_OUT_MSG = "Logging out... We're almost there!"
    }
}