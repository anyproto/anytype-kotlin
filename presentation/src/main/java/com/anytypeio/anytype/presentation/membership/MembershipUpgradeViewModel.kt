package com.anytypeio.anytype.presentation.membership

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.auth.interactor.GetAccount

import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MembershipUpgradeViewModel(
    private val getAccount: GetAccount
) : ViewModel() {

    val commands = MutableSharedFlow<Command>(0)

    fun onContactButtonClicked() {
        viewModelScope.launch {
            val account = getAccount.async(Unit).getOrNull() ?: return@launch
            commands.emit(Command.ShowEmail(account.id))
        }
    }

    sealed class Command {
        data class ShowEmail(val account: Id) : Command()
    }

    class Factory @Inject constructor(
        private val getAccount: GetAccount,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MembershipUpgradeViewModel(
                getAccount = getAccount
            ) as T
        }
    }
}