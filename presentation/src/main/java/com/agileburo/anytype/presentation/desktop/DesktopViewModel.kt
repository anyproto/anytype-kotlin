package com.agileburo.anytype.presentation.desktop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.desktop.interactor.GetAccount
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import com.agileburo.anytype.presentation.profile.ProfileView
import timber.log.Timber

class DesktopViewModel(
    private val getAccount: GetAccount
) : ViewStateViewModel<ViewState<List<DesktopView>>>(),
    SupportNavigation<Event<AppNavigation.Command>> {

    private val _profile = MutableLiveData<ProfileView>()
    val profile: LiveData<ProfileView>
        get() = _profile

    init {
        proceedWithGettingAccount()
    }

    override val navigation = MutableLiveData<Event<AppNavigation.Command>>()

    private fun proceedWithGettingAccount() {
        getAccount.invoke(viewModelScope, BaseUseCase.None) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while getting account") },
                fnR = { account -> _profile.postValue(ProfileView(name = account.name)) }
            )
        }
    }

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
    }

    fun onAddNewDocumentClicked() {
        stateData.postValue(
            ViewState.Success(
                listOf(
                    DesktopView.Document(
                        id = "1",
                        title = "Document"
                    )
                )
            )
        )
    }

    fun onDocumentClicked() {
        navigation.postValue(Event(AppNavigation.Command.OpenDocument("")))
    }

    fun onProfileClicked() {
        navigation.postValue(Event(AppNavigation.Command.OpenProfile))
    }

}