package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.ObserveAccounts
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.StartLoadingAccounts
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.common.BaseViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.SupportNavigation
import com.jakewharton.rxrelay2.PublishRelay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectAccountViewModel(
    private val startLoadingAccounts: StartLoadingAccounts,
    private val observeAccounts: ObserveAccounts
) : BaseViewModel(), SupportNavigation {

    val state by lazy {
        MutableLiveData<List<ChooseProfileView>>()
    }

    private val navigation by lazy {
        PublishRelay.create<NavigationCommand>().toSerialized()
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

    override fun observeNavigation() = navigation

    fun onProfileClicked(id: String) {
        navigation.accept(NavigationCommand.SetupSelectedAccountScreen(id))
    }

    fun onAddProfileClicked() {}

    fun onLogoutClicked() {}

}