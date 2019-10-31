package com.agileburo.anytype.presentation.auth.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.domain.auth.interactor.StartAccount
import com.agileburo.anytype.domain.auth.repo.PathProvider
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import timber.log.Timber

class SetupSelectedAccountViewModel(
    private val startAccount: StartAccount,
    private val pathProvider: PathProvider
) : ViewModel(), SupportNavigation<Event<AppNavigation.Command>> {

    override val navigation: MutableLiveData<Event<AppNavigation.Command>> = MutableLiveData()

    fun selectAccount(id: String) {
        startAccount.invoke(
            scope = viewModelScope,
            params = StartAccount.Params(
                id = id,
                path = pathProvider.providePath()
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while selecting account with id: $id") },
                fnR = {
                    navigation.postValue(Event(AppNavigation.Command.CongratulationScreen))
                }
            )
        }
    }
}