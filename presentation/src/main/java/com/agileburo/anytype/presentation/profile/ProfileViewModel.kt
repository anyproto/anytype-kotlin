package com.agileburo.anytype.presentation.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.auth.interactor.Logout
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import timber.log.Timber

class ProfileViewModel(
    private val logout: Logout
) : ViewStateViewModel<ViewState<ProfileView>>(), SupportNavigation<Event<AppNavigation.Command>> {

    override val navigation: MutableLiveData<Event<AppNavigation.Command>> = MutableLiveData()

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
    }

    fun onBackButtonClicked() {
        // TODO dispatch navigation command
    }

    fun onLogoutClicked() {
        logout.invoke(viewModelScope, BaseUseCase.None) { result ->
            result.either(
                fnL = { e ->
                    Timber.e(e, "Error while logging out")
                },
                fnR = {
                    navigation.postValue(Event(AppNavigation.Command.OpenStartLoginScreen))
                }
            )
        }
    }

    fun onKeyChainPhraseClicked() {
        navigation.postValue(Event(AppNavigation.Command.OpenKeychainScreen))
    }

    fun onPinCodeClicked() {
        navigation.postValue(Event(AppNavigation.Command.OpenPinCodeScreen))
    }

    fun onUpdateToggled(value: Boolean) {
        // TODO update profile settings
    }

    fun onInviteToggled(value: Boolean) {
        // TODO update profile settings
    }
}