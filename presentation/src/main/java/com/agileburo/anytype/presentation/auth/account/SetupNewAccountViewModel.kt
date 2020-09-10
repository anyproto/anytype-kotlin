package com.agileburo.anytype.presentation.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.domain.auth.interactor.CreateAccount
import com.agileburo.anytype.presentation.auth.model.Session
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class SetupNewAccountViewState{
    object Loading: SetupNewAccountViewState()
    object Success: SetupNewAccountViewState()
    data class Error(val message: String) : SetupNewAccountViewState()
    data class InvalidCodeError(val message: String) : SetupNewAccountViewState()
}

class SetupNewAccountViewModel(
    private val session: Session,
    private val createAccount: CreateAccount
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    private val _state = MutableLiveData<SetupNewAccountViewState>()
    val state: LiveData<SetupNewAccountViewState>
        get() = _state

    init {
        _state.postValue(SetupNewAccountViewState.Loading)
        proceedWithCreatingAccount()
    }

    private fun proceedWithCreatingAccount() {

        Timber.d("Starting setting up new account")

        createAccount.invoke(
            scope = viewModelScope,
            params = CreateAccount.Params(
                name = session.name ?: throw IllegalStateException("Name not set"),
                avatarPath = session.avatarPath,
                invitationCode = session.invitationCode
            )
        ) { result ->
            result.either(
                fnL = {
                    //todo Remove this, after adding proper error handling logic
                    if (it.message?.contains("BAD_INVITE_CODE") == true) {
                        _state.postValue(SetupNewAccountViewState.InvalidCodeError("Invalid invite code!"))
                        viewModelScope.launch {
                            delay(300)
                            navigation.postValue(EventWrapper(AppNavigation.Command.ExitToInvitationCodeScreen))
                        }
                    } else {
                        _state.postValue(SetupNewAccountViewState.Error("Error while creating account"))
                    }
                    Timber.e(it, "Error while creating account")
                },
                fnR = {
                    _state.postValue(SetupNewAccountViewState.Success)
                    navigation.postValue(EventWrapper(AppNavigation.Command.CongratulationScreen))
                }
            )
        }
    }
}