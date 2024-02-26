package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ShareSpaceViewModel(
    private val params: Params,
    private val generateSpaceInviteLink: GenerateSpaceInviteLink,
    private val container: StorelessSubscriptionContainer
) : BaseViewModel() {

    val participants = MutableStateFlow<List<ParticipantView>>(emptyList())

    val viewState = MutableStateFlow<ViewState>(ViewState.Init)
    val commands = MutableSharedFlow<Command>()

    init {
        proceedWithGeneratingInviteLink()

    }

    private fun proceedWithGeneratingInviteLink() {
        viewModelScope.launch {
            generateSpaceInviteLink
                .async(params.space)
                .fold(
                    onSuccess = { link ->
                        viewState.value = ViewState.Share(link = link.scheme)
                    },
                    onFailure = {
                        Timber.e(it, "Error while generating invite link")
                    }
                )
        }
    }

    fun onRegenerateInviteLinkClicked() {
        proceedWithGeneratingInviteLink()
    }

    fun onShareInviteLinkClicked() {
        viewModelScope.launch {
            when(val value = viewState.value) {
                ViewState.Init -> {
                    // Do nothing.
                }
                is ViewState.Share -> {
                    commands.emit(Command.ShareInviteLink(value.link))
                }
            }
        }
    }

    class Factory @Inject constructor(
        private val params: Params,
        private val generateSpaceInviteLink: GenerateSpaceInviteLink,
        private val container: StorelessSubscriptionContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShareSpaceViewModel(
            params = params,
            generateSpaceInviteLink = generateSpaceInviteLink,
            container = container
        ) as T
    }

    data class Params(
        val space: SpaceId
    )

    sealed class ViewState {
        object Init : ViewState()
        data class Share(val link: String): ViewState()
    }

    sealed class Command {
        data class ShareInviteLink(val link: String) : Command()
    }
}

data class ParticipantView(
    val obj: ObjectWrapper.Basic
)