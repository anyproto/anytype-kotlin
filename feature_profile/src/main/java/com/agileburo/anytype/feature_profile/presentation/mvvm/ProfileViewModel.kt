package com.agileburo.anytype.feature_profile.presentation.mvvm

import androidx.lifecycle.MutableLiveData
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.core_utils.navigation.SupportNavigation
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.feature_profile.domain.UpdateProfileSettings
import com.agileburo.anytype.feature_profile.navigation.ProfileNavigation
import com.agileburo.anytype.feature_profile.presentation.model.ProfileView

class ProfileViewModel(
    private val updateProfileSettings: UpdateProfileSettings
) : ViewStateViewModel<ViewState<ProfileView>>(), SupportNavigation<Event<ProfileNavigation.Command>> {

    override val navigation: MutableLiveData<Event<ProfileNavigation.Command>> = MutableLiveData()

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
        navigation.postValue(Event(ProfileNavigation.Command.OpenKeychainScreen))
    }

    fun onPinCodeClicked() {
        navigation.postValue(Event(ProfileNavigation.Command.OpenPinCodeScreen))
    }

    fun onUpdateToggled(value: Boolean) {
        // TODO update profile settings
    }

    fun onInviteToggled(value: Boolean) {
        // TODO update profile settings
    }
}