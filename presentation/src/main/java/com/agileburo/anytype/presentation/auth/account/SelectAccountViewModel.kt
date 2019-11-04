package com.agileburo.anytype.presentation.auth.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.domain.auth.interactor.ObserveAccounts
import com.agileburo.anytype.domain.auth.interactor.StartLoadingAccounts
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.image.LoadAccountImages
import com.agileburo.anytype.presentation.auth.model.ChooseProfileView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectAccountViewModel(
    private val startLoadingAccounts: StartLoadingAccounts,
    private val observeAccounts: ObserveAccounts,
    private val loadAccountImages: LoadAccountImages
) : ViewModel(), SupportNavigation<Event<AppNavigation.Command>> {

    override val navigation: MutableLiveData<Event<AppNavigation.Command>> = MutableLiveData()

    val state by lazy {
        MutableLiveData<List<ChooseProfileView>>()
    }

    init {
        startObservingAccounts()
        startLoadingAccount()
    }

    private fun startLoadingAccount() {
        startLoadingAccounts.invoke(
            viewModelScope, StartLoadingAccounts.Params()
        ) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while starting account loading") },
                fnR = { Timber.d("Account loading started...") }
            )
        }
    }

    private fun startObservingAccounts() {
        viewModelScope.launch {
            observeAccounts.stream { accounts ->
                state.postValue(
                    accounts.map { account ->
                        ChooseProfileView.ProfileView(
                            id = account.id,
                            name = account.name
                        )
                    }
                )

            }
        }
    }

    private fun proceedWithLoadingImagesForAccount(account: Account) {
        // TODO
    }

    fun onProfileClicked(id: String) {
        navigation.postValue(Event(AppNavigation.Command.SetupSelectedAccountScreen(id)))
    }

    fun onAddProfileClicked() {}

    fun onLogoutClicked() {}
}