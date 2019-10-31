package com.agileburo.anytype.presentation.desktop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.desktop.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import com.agileburo.anytype.presentation.profile.ProfileView
import timber.log.Timber

class DesktopViewModel(
    private val loadImage: LoadImage,
    private val getCurrentAccount: GetCurrentAccount
) : ViewStateViewModel<ViewState<List<DesktopView>>>(),
    SupportNavigation<Event<AppNavigation.Command>> {

    private val _profile = MutableLiveData<ProfileView>()
    val profile: LiveData<ProfileView>
        get() = _profile


    private val _image = MutableLiveData<ByteArray>()
    val image: LiveData<ByteArray>
        get() = _image

    override val navigation = MutableLiveData<Event<AppNavigation.Command>>()

    private fun proceedWithGettingAccount() {
        getCurrentAccount.invoke(viewModelScope, BaseUseCase.None) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while getting account") },
                fnR = { account ->
                    _profile.postValue(ProfileView(name = account.name))
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

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
        proceedWithGettingAccount()
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