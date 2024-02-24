package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ShareSpaceViewModel(
    private val params: Params,
    private val generateSpaceInviteLink: GenerateSpaceInviteLink
) : BaseViewModel() {

    val viewState = MutableStateFlow<ViewState>(ViewState.Init)
    val commands = MutableSharedFlow<Command>()

    init {
        proceedWithGeneratingInviteLink()
    }

    private fun proceedWithGeneratingInviteLink() {
        viewModelScope.launch {
            val link = generateSpaceInviteLink.async(params.space)
            Timber.d("Generated link result: $link")
            viewState.value = ViewState.Share(
                link = link.getOrNull()?.cid.orEmpty()
            )
        }
    }

    fun onRegenerateInviteLinkClicked() {
        proceedWithGeneratingInviteLink()
    }

    fun onShareInviteLinkClicked(link: String) {
        viewModelScope.launch {
            commands.emit(Command.ShareInviteLink(link))
        }
    }

    class Factory @Inject constructor(
        private val params: Params,
        private val generateSpaceInviteLink: GenerateSpaceInviteLink
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShareSpaceViewModel(
            params = params,
            generateSpaceInviteLink = generateSpaceInviteLink
        ) as T
    }

    data class Params(
        val space: SpaceId
    )

    sealed class ViewState {
        object Init : ViewState()
        data class Share(
            val link: String
        ): ViewState()
    }

    sealed class Command {
        data class ShareInviteLink(val link: String) : Command()
    }
}