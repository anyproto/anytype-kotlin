package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.feature_login.ui.login.domain.common.Session
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.CreateAccount
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.SupportNavigation
import com.jakewharton.rxrelay2.PublishRelay
import timber.log.Timber

class SetupNewAccountViewModel(
    session: Session,
    createAccount: CreateAccount
) : ViewModel(), SupportNavigation {

    private val navigation by lazy {
        PublishRelay.create<NavigationCommand>().toSerialized()
    }

    override fun observeNavigation() = navigation

    init {
        createAccount.invoke(
            scope = viewModelScope,
            params = CreateAccount.Params(
                name = session.name ?: throw IllegalStateException("Name not set")
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating account") },
                fnR = { navigation.accept(NavigationCommand.CongratulationScreen) }
            )
        }
    }
}