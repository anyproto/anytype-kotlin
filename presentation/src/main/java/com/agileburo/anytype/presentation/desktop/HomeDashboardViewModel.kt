package com.agileburo.anytype.presentation.desktop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.ObserveDashboardBlocks
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.domain.page.CreatePage
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import com.agileburo.anytype.presentation.profile.ProfileView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeDashboardViewModel(
    private val loadImage: LoadImage,
    private val getCurrentAccount: GetCurrentAccount,
    private val openDashboard: OpenDashboard,
    private val closeDashboard: CloseDashboard,
    private val createPage: CreatePage,
    private val observeDashboardBlocks: ObserveDashboardBlocks
) : ViewStateViewModel<HomeDashboardViewModel.ViewState>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val _profile = MutableLiveData<ProfileView>()
    val profile: LiveData<ProfileView> = _profile

    private val _image = MutableLiveData<ByteArray>()
    val image: LiveData<ByteArray> = _image

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    init {
        proceedWithObservingDashboardBlocks()
    }

    private fun proceedWithObservingDashboardBlocks() {
        viewModelScope.launch {
            observeDashboardBlocks
                .build()
                .map { blocks -> blocks.map { mapToView(it) } }
                .collect { blocks -> dispatchViewState(blocks) }
        }
    }

    private fun addBlock(block: Block) {
        if (state.value is ViewState.Success) {
            val result = (state.value as ViewState.Success).data + listOf(mapToView(block))
            dispatchViewState(result)
        }
    }

    private fun dispatchViewState(blocks: List<DashboardView>) {
        stateData.postValue(ViewState.Success(data = blocks))
    }

    private fun mapToView(block: Block): DashboardView.Document {
        return DashboardView.Document(
            id = block.id,
            title = block.fields.name
        )
    }

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

    private fun proceedWithOpeningHomeDashboard() {
        stateData.postValue(ViewState.Loading)
        openDashboard.invoke(viewModelScope, null) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while opening home dashboard") },
                fnR = { Timber.d("Home dashboard opened") }
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
        proceedWithGettingAccount()
        proceedWithOpeningHomeDashboard()
    }

    fun onAddNewDocumentClicked() {
        createPage.invoke(viewModelScope, CreatePage.Params.insideDashboard()) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while creating a new page") },
                fnR = { id -> navigateToPage(id) }
            )
        }
    }

    private fun navigateToPage(id: String) {

        Timber.d("Starting navigation to page: $id")

        closeDashboard.invoke(viewModelScope, CloseDashboard.Param.home()) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while closing a dashobard") },
                fnR = { navigation.postValue(EventWrapper(AppNavigation.Command.OpenPage(id))) }
            )
        }
    }

    fun onDocumentClicked(id: String) {
        navigateToPage(id)
    }

    fun onProfileClicked() {
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenProfile))
    }

    sealed class ViewState {
        object Loading : ViewState()
        data class Success(val data: List<DashboardView>) : ViewState()
        data class Error(val error: String) : ViewState()
    }
}