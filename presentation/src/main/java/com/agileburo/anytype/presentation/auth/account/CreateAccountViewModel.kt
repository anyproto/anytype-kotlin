package com.agileburo.anytype.presentation.auth.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.presentation.auth.model.Session
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation

class CreateAccountViewModel(
    val session: Session
) : ViewModel(), SupportNavigation<Event<AppNavigation.Command>> {

    private val _error = MutableLiveData<String>()
    val error = _error

    override val navigation: MutableLiveData<Event<AppNavigation.Command>> = MutableLiveData()

    fun onCreateProfileClicked(input: String) {
        if (input.isNotEmpty()) {
            session.name = input
            navigation.postValue(Event(AppNavigation.Command.SetupNewAccountScreen))
        } else {
            _error.postValue(EMPTY_USERNAME_ERROR_MESSAGE)
        }
    }

    fun onAvatarSet(path: String) {
        session.avatarPath = path
    }

    fun onBackButtonClicked() {
        navigation.postValue(Event(AppNavigation.Command.Exit))
    }

    companion object {
        const val EMPTY_USERNAME_ERROR_MESSAGE = "Please provide username!"
    }
}