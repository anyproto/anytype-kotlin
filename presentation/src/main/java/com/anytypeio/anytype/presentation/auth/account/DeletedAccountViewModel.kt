package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.domain.account.DateHelper
import com.anytypeio.anytype.domain.account.RestoreAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.RoundingMode
import kotlin.time.DurationUnit.DAYS
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

class DeletedAccountViewModel(
    private val restoreAccount: RestoreAccount,
    private val logout: Logout,
    private val dateHelper: DateHelper
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
                        days = days.toBigDecimal().setScale(0, RoundingMode.UP).toInt()
                    )
                }
            }
            val delta = 1f - (days.toFloat() / DEADLINE_DURATION_IN_DAYS)
            this.progress.value = delta
        } else {
            sendToast("Your account has been deleted")
            proceedWithLogginOut()
        }
    }

    fun cancelDeletionClicked() {
        viewModelScope.launch {
            restoreAccount(BaseUseCase.None).process(
                success = { status ->
                    when (status) {
                        is AccountStatus.Active -> {
                            commands.emit(Command.Resume)
                        }
                        is AccountStatus.Deleted -> {
                            sendToast("Sorry, your account has been deleted")
                            proceedWithLogginOut()
                        }
                        is AccountStatus.PendingDeletion -> {
                            // TODO
                        }
                    }
                },
                failure = {
                    Timber.e(it, "Error while cancelling account deletion")
                    sendToast("Error while cancelling account deletion. Please, try again later.")
                }
            )
        }
    }

    fun onLogoutAndClearDataClicked() {
        proceedWithLogginOut()
    }

    private fun proceedWithLogginOut() {
        viewModelScope.launch {
            logout(Logout.Params(clearLocalRepositoryData = true)).collect { status ->
                when (status) {
                    is Interactor.Status.Error -> {
                        isLoggingOut.value = false
                    }
                    is Interactor.Status.Started -> {
                        isLoggingOut.value = true
                    }
                    is Interactor.Status.Success -> {
                        isLoggingOut.value = false
                        commands.emit(Command.Logout)
                    }
                }
            }
        }
    }

    class Factory(
        private val restoreAccount: RestoreAccount,
        private val logout: Logout,
        private val helper: DateHelper
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeletedAccountViewModel(
                restoreAccount = restoreAccount,
                logout = logout,
                dateHelper = helper
            ) as T
        }
    }

    sealed class Command {
        object Resume : Command()
        object Logout : Command()
    }

    sealed class DeletionDate {
        object Unknown : DeletionDate()
        object Today : DeletionDate()
        object Tomorrow : DeletionDate()
        data class Later(val days: Int) : DeletionDate()
    }

    companion object {
        const val DEADLINE_DURATION_IN_DAYS = 30f
    }
}