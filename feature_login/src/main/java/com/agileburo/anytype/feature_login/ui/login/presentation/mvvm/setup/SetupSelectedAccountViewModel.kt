package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.feature_login.ui.login.domain.common.PathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.SelectAccount
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.SupportNavigation
import com.jakewharton.rxrelay2.PublishRelay
import timber.log.Timber

class SetupSelectedAccountViewModel(
    private val selectAccount: SelectAccount,
    private val pathProvider: PathProvider
) : ViewModel(), SupportNavigation {

    private val navigation by lazy {
        PublishRelay.create<NavigationCommand>().toSerialized()
    }

    override fun observeNavigation() = navigation

    fun selectAccount(id: String) {
        selectAccount.invoke(
            scope = viewModelScope,
            params = SelectAccount.Params(
                id = id,
                path = pathProvider.providePath()
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while selecting account with id: $id") },
                fnR = { navigation.accept(NavigationCommand.CongratulationScreen) }
            )
        }
    }
}