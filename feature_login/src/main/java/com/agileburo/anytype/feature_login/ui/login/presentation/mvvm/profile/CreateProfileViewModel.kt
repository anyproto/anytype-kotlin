package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.agileburo.anytype.feature_login.ui.login.domain.common.Session
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.congratulation.ViewState
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.SupportNavigation
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class CreateProfileViewModel(
    val session: Session
) : ViewModel(), SupportNavigation {

    private val navigation by lazy {
        PublishRelay.create<NavigationCommand>().toSerialized()
    }

    val state = MutableLiveData<ViewState<Any>>()

    fun onCreateProfileClicked(input: String) {
        session.name = input
        navigation.accept(NavigationCommand.SetupNewAccountScreen)
    }

    fun onAddPictureClicked() {
        // TODO
    }

    override fun observeNavigation(): Observable<NavigationCommand> = navigation
}