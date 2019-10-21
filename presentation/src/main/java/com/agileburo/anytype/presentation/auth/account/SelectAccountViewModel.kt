package com.agileburo.anytype.presentation.auth.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.domain.auth.interactor.ObserveAccounts
import com.agileburo.anytype.domain.auth.interactor.StartLoadingAccounts
import com.agileburo.anytype.presentation.auth.model.ChooseProfileView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectAccountViewModel(
    private val startLoadingAccounts: StartLoadingAccounts,
    private val observeAccounts: ObserveAccounts
) : ViewModel(), SupportNavigation<Event<AppNavigation.Command>> {

    override val navigation: MutableLiveData<Event<AppNavigation.Command>> = MutableLiveData()

    val state by lazy {
        MutableLiveData<List<ChooseProfileView>>()
    }

    init {
        startObservingAccounts()
        startRecoveringAccounts()
    }

    private fun startRecoveringAccounts() {
        startLoadingAccounts.invoke(viewModelScope, StartLoadingAccounts.Params()) {
            Timber.d(it.toString())
        }
    }

    private fun startObservingAccounts() {
        viewModelScope.launch {
            observeAccounts
                .stream(Unit)
                .collect { account ->
                    state.postValue(
                        listOf(
                            ChooseProfileView.ProfileView(
                                id = account.id,
                                name = account.name
                            )
                        )
                    )
                }
        }
    }

    fun onProfileClicked(id: String) {
        navigation.postValue(Event(AppNavigation.Command.SetupSelectedAccountScreen(id)))
    }

    fun onAddProfileClicked() {}

    fun onLogoutClicked() {}
}