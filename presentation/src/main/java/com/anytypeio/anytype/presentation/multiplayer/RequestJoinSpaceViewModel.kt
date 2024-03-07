package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteView
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteView
import com.anytypeio.anytype.domain.multiplayer.SendJoinSpaceRequest
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.common.ViewState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class RequestJoinSpaceViewModel(
    private val params: Params,
    private val getSpaceInviteView: GetSpaceInviteView,
    private val sendJoinSpaceRequest: SendJoinSpaceRequest,
    private val spaceInviteResolver: SpaceInviteResolver
) : BaseViewModel() {

    val state = MutableStateFlow<ViewState<SpaceInviteView>>(ViewState.Loading)
    val commands = MutableSharedFlow<Command>(0)

    init {
        proceedWithGettingSpaceInviteView()
    }

    private fun proceedWithGettingSpaceInviteView() {
        val fileKey = spaceInviteResolver.parseFileKey(params.link)
        val contentId = spaceInviteResolver.parseContentId(params.link)
        if (fileKey != null && contentId != null) {
            viewModelScope.launch {
                getSpaceInviteView.async(
                    GetSpaceInviteView.Params(
                        inviteContentId = contentId,
                        inviteFileKey = fileKey
                    )
                ).fold(
                    onSuccess = { view ->
                        state.value = ViewState.Success(view)
                    },
                    onFailure = { e ->
                        Timber.e(e, "Error while getting space invite view")
                    }
                )
            }
        } else {
            Timber.e("Could not parse invite link: ${params.link}")
            state.value = ViewState.Error("Could not parse invite link: ${params.link}")
        }
    }

    fun onRequestToJoinClicked() {
        when(val curr = state.value) {
            is ViewState.Error -> {
                // Do nothing.
            }
            is ViewState.Loading -> {
                // Do nothing.
            }
            is ViewState.Success -> {
                viewModelScope.launch {
                    val fileKey = spaceInviteResolver.parseFileKey(params.link)
                    val contentId = spaceInviteResolver.parseContentId(params.link)
                    if (contentId != null && fileKey != null) {
                        sendJoinSpaceRequest.async(
                            SendJoinSpaceRequest.Params(
                                space = curr.data.space,
                                network = null,
                                inviteFileKey = fileKey,
                                inviteContentId = contentId
                            )
                        ).fold(
                            onFailure = { e ->
                                Timber.e(e, "Error while sending space join request").also {
                                    sendToast(e.msg())
                                }
                            },
                            onSuccess = {
                                commands.emit(Command.Toast.RequestSent)
                                commands.emit(Command.Dismiss)
                            }
                        )
                    }
                }
            }
        }
    }

    class Factory @Inject constructor(
        private val params: Params,
        private val getSpaceInviteView: GetSpaceInviteView,
        private val sendJoinSpaceRequest: SendJoinSpaceRequest,
        private val spaceInviteResolver: SpaceInviteResolver
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RequestJoinSpaceViewModel(
            params = params,
            getSpaceInviteView = getSpaceInviteView,
            sendJoinSpaceRequest = sendJoinSpaceRequest,
            spaceInviteResolver = spaceInviteResolver
        ) as T
    }

    data class Params(val link: String)

    sealed class Command {
        sealed class Toast : Command() {
            object RequestSent : Toast()
        }
        object Dismiss: Command()
    }
}