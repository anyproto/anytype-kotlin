package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.congratulation

import androidx.lifecycle.ViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.SupportNavigation
import com.jakewharton.rxrelay2.PublishRelay

class CongratulationViewModel : ViewModel(), SupportNavigation {

    private val navigation by lazy {
        PublishRelay.create<NavigationCommand>()
    }

    fun onStartClicked() {
        navigation.accept(NavigationCommand.WorkspaceScreen)
    }

    override fun observeNavigation() = navigation
}