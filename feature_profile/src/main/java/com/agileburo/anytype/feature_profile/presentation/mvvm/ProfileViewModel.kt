package com.agileburo.anytype.feature_profile.presentation.mvvm

import androidx.lifecycle.MutableLiveData
import com.agileburo.anytype.core_utils.navigation.SupportNavigation
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.feature_profile.domain.UpdateProfileSettings
import com.agileburo.anytype.feature_profile.navigation.ProfileNavigation
import com.agileburo.anytype.feature_profile.presentation.model.ProfileView

class ProfileViewModel(
    private val updateProfileSettings: UpdateProfileSettings
) : ViewStateViewModel<ViewState<ProfileView>>(), SupportNavigation<ProfileNavigation.Command> {

    override val navigation: MutableLiveData<ProfileNavigation.Command>
        get() = MutableLiveData()

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
    }

    fun onBackButtonClicked() {
        // TODO dispatch navigation command
    }

    fun onLogoutClicked() {
        // TODO dispatch navigation command
    }

    fun onKeyChainPhraseClicked() {
        navigation.value = ProfileNavigation.Command.OpenKeychainScreen
    }

    fun onPinCodeClicked() {
        navigation.value = ProfileNavigation.Command.OpenPinCodeScreen
    }

    fun onUpdateToggled(value: Boolean) {
        // TODO update profile settings
    }

    fun onInviteToggled(value: Boolean) {
        // TODO update profile settings
    }
}