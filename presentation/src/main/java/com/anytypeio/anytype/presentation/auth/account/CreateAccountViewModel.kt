package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.signupScreenShow
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.presentation.auth.model.Session
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation

class CreateAccountViewModel(
    private val session: Session,
    analytics: Analytics
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val _error = MutableLiveData<String>()
    val error = _error

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    init {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = signupScreenShow
        )
    }

    fun onCreateProfileClicked(input: String, invitationCode: String) {
        if (input.isNotEmpty()) {
            session.name = input
            session.invitationCode = invitationCode
            navigation.postValue(EventWrapper(AppNavigation.Command.SetupNewAccountScreen))
        } else {
            _error.postValue(EMPTY_USERNAME_ERROR_MESSAGE)
        }
    }

    fun onAvatarSet(path: String) {
        session.avatarPath = path
    }

    fun onBackButtonClicked() {
        navigation.postValue(EventWrapper(AppNavigation.Command.Exit))
    }

    companion object {
        const val EMPTY_USERNAME_ERROR_MESSAGE = "Please provide username!"
    }
}