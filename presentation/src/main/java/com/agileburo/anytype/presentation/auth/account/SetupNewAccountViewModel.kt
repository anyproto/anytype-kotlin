package com.agileburo.anytype.presentation.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.domain.auth.interactor.CreateAccount
import com.agileburo.anytype.presentation.auth.model.Session
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import timber.log.Timber

class SetupNewAccountViewModel(
    private val session: Session,
    private val createAccount: CreateAccount
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    private val _state = MutableLiveData<ViewState<Any>>()
    val state: LiveData<ViewState<Any>>
        get() = _state

    init {
        _state.postValue(ViewState.Loading)
        proceedWithCreatingAccount()
    }

    private fun proceedWithCreatingAccount() {

        Timber.d("Starting setting up new account")

        createAccount.invoke(
            scope = viewModelScope,
            params = CreateAccount.Params(
                name = session.name ?: throw IllegalStateException("Name not set"),
                avatarPath = session.avatarPath
            )
        ) { result ->
            result.either(
                fnL = {
                    _state.postValue(ViewState.Error("Error while creating account"))
                    Timber.e(it, "Error while creating account")
                },
                fnR = {
                    _state.postValue(ViewState.Success(Any()))
                    navigation.postValue(EventWrapper(AppNavigation.Command.CongratulationScreen))
                }
            )
        }
    }
}