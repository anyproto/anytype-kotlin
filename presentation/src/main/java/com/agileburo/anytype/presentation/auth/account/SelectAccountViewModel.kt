package com.agileburo.anytype.presentation.auth.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.domain.auth.interactor.ObserveAccounts
import com.agileburo.anytype.domain.auth.interactor.StartLoadingAccounts
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.presentation.auth.model.SelectAccountView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectAccountViewModel(
    private val startLoadingAccounts: StartLoadingAccounts,
    private val observeAccounts: ObserveAccounts
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    val state by lazy { MutableLiveData<List<SelectAccountView>>() }

    private val accountChannel = Channel<Account>()

    private val accounts = accountChannel
        .consumeAsFlow()
        .scan(emptyList<Account>()) { list, value -> list + value }
        .drop(1)

    init {
        startObservingAccounts()
        startLoadingAccount()

        accounts
            .onEach { result ->
                state.postValue(
                    result.map { account ->
                        SelectAccountView.AccountView(
                            id = account.id,
                            name = account.name,
                            image = account.avatar
                        )
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    private fun startLoadingAccount() {
        startLoadingAccounts.invoke(
            viewModelScope, StartLoadingAccounts.Params()
        ) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while account loading") },
                fnR = {
                    Timber.d("Account loading successfully finished")
                }
            )
        }
    }

    private fun startObservingAccounts() {
        viewModelScope.launch {
            observeAccounts.build().collect { account ->
                accountChannel.send(account)
            }
        }
    }

    fun onProfileClicked(id: String) {
        navigation.postValue(EventWrapper(AppNavigation.Command.SetupSelectedAccountScreen(id)))
    }

    fun onAddProfileClicked() {
        // TODO
    }

    override fun onCleared() {
        super.onCleared()
        accountChannel.close()
    }
}