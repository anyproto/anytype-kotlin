package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.ObserveAccounts
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.common.BaseViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.SupportNavigation
import com.jakewharton.rxrelay2.PublishRelay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ChooseProfileViewModel : BaseViewModel(), SupportNavigation {

    val state by lazy {
        MutableLiveData<List<ChooseProfileView>>()
    }

    private val navigation by lazy {
        PublishRelay.create<NavigationCommand>().toSerialized()
    }

    private val observeAccounts = ObserveAccounts()

    init {
        viewModelScope.launch {
            observeAccounts
                .observe()
                .map { profiles ->
                    profiles.map { profile ->
                        ChooseProfileView.ProfileView(
                            id = profile.id,
                            name = profile.name
                        )
                    }
                }
                .collect { state.postValue(it) }
        }
    }

    override fun observeNavigation() = navigation

    fun onProfileClicked(id: String) {
        navigation.accept(NavigationCommand.SetupSelectedAccountScreen(id))
    }

    fun onAddProfileClicked() {}

    fun onLogoutClicked() {}

}