package com.agileburo.anytype.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.auth.interactor.Logout
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.desktop.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import timber.log.Timber

class ProfileViewModel(
    private val getCurrentAccount: GetCurrentAccount,
    private val loadImage: LoadImage,
    private val logout: Logout
) : ViewStateViewModel<ViewState<ProfileView>>(), SupportNavigation<Event<AppNavigation.Command>> {

    private val _image = MutableLiveData<ByteArray>()
    val image: LiveData<ByteArray>
        get() = _image

    override val navigation: MutableLiveData<Event<AppNavigation.Command>> = MutableLiveData()

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
        proceedWithGettingAccount()
    }

    fun onBackButtonClicked() {
        // TODO dispatch navigation command
    }

    fun onAddProfileClicked() {
        navigation.postValue(Event(AppNavigation.Command.OpenCreateAccount))
    }

    private fun proceedWithGettingAccount() {
        getCurrentAccount.invoke(viewModelScope, BaseUseCase.None) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while getting account") },
                fnR = { account ->
                    stateData.postValue(ViewState.Success(ProfileView(name = account.name)))
                    loadAvatarImage(account)
                }
            )
        }
    }

    private fun loadAvatarImage(account: Account) {
        account.avatar?.let { image ->
            loadImage.invoke(
                scope = viewModelScope,
                params = LoadImage.Param(
                    id = image.id
                )
            ) { result ->
                result.either(
                    fnL = { e -> Timber.e(e, "Error while loading image") },
                    fnR = { blob -> _image.postValue(blob) }
                )
            }
        } ?: Timber.d("Avatar not loaded: null value")
    }

    fun onLogoutClicked() {
        logout.invoke(viewModelScope, BaseUseCase.None) { result ->
            result.either(
                fnL = { e ->
                    Timber.e(e, "Error while logging out")
                },
                fnR = {
                    navigation.postValue(Event(AppNavigation.Command.StartSplashFromDesktop))
                }
            )
        }
    }

    fun onKeyChainPhraseClicked() {
        navigation.postValue(Event(AppNavigation.Command.OpenKeychainScreen))
    }

    fun onPinCodeClicked() {
        navigation.postValue(Event(AppNavigation.Command.OpenPinCodeScreen))
    }

    fun onUpdateToggled(value: Boolean) {
        // TODO update profile settings
    }

    fun onInviteToggled(value: Boolean) {
        // TODO update profile settings
    }
}