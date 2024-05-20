package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenInviteRequest
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenRequestSent
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteError
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteView
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.getOrDefault
import com.anytypeio.anytype.domain.multiplayer.CheckIsUserSpaceMember
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteView
import com.anytypeio.anytype.domain.multiplayer.SendJoinSpaceRequest
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.notifications.SystemNotificationService
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.common.TypedViewState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class RequestJoinSpaceViewModel(
    private val params: Params,
    private val getSpaceInviteView: GetSpaceInviteView,
    private val sendJoinSpaceRequest: SendJoinSpaceRequest,
    private val spaceInviteResolver: SpaceInviteResolver,
    private val checkIsUserSpaceMember: CheckIsUserSpaceMember,
    private val spaceManager: SpaceManager,
    private val saveCurrentSpace: SaveCurrentSpace,
    private val analytics: Analytics,
    private val notificator: SystemNotificationService
) : BaseViewModel() {

    val state = MutableStateFlow<TypedViewState<SpaceInviteView, ErrorView>>(TypedViewState.Loading)
    val isRequestInProgress = MutableStateFlow(false)
    val showEnableNotificationDialog = MutableStateFlow(false)
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
                        val isAlreadyMember = checkIsUserSpaceMember
                            .async(view.space)
                            .getOrDefault(false)
                        if (isAlreadyMember) {
                            state.value = TypedViewState.Error(
                                ErrorView.AlreadySpaceMember(view.space)
                            )
                        } else {
                            state.value = TypedViewState.Success(view)
                        }
                    },
                    onFailure = { e ->
                        if (e is SpaceInviteError) {
                            when(e) {
                                is SpaceInviteError.InvalidInvite -> {
                                    state.value = TypedViewState.Error(
                                        ErrorView.InvalidLink
                                    )
                                }
                                is SpaceInviteError.SpaceDeleted -> {
                                    commands.emit(Command.Toast.SpaceDeleted)
                                    commands.emit(Command.Dismiss)
                                }
                                is SpaceInviteError.SpaceNotFound -> {
                                    commands.emit(Command.Toast.SpaceNotFound)
                                    commands.emit(Command.Dismiss)
                                }
                            }
                        }
                        Timber.e(e, "Error while getting space invite view")
                    }
                )
            }
        } else {
            Timber.w("Could not parse invite link: ${params.link}")
            state.value = TypedViewState.Error(ErrorView.InvalidLink)
        }
        viewModelScope.launch {
            analytics.sendEvent(eventName = screenInviteRequest)
        }
    }

    fun onRequestToJoinClicked() {
        when(val curr = state.value) {
            is TypedViewState.Success -> {
                viewModelScope.launch {
                    val fileKey = spaceInviteResolver.parseFileKey(params.link)
                    val contentId = spaceInviteResolver.parseContentId(params.link)
                    if (contentId != null && fileKey != null) {
                        isRequestInProgress.value = true
                        sendJoinSpaceRequest.async(
                            SendJoinSpaceRequest.Params(
                                space = curr.data.space,
                                network = spaceManager.getConfig()?.network,
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
                                analytics.sendEvent(eventName = screenRequestSent)
                                if (notificator.areNotificationsEnabled) {
                                    commands.emit(Command.Toast.RequestSent)
                                    commands.emit(Command.Dismiss)
                                } else {
                                    commands.emit(Command.Toast.RequestSent)
                                    showEnableNotificationDialog.value = true
                                }
                            }
                        )
                        isRequestInProgress.value = false
                    }
                }
            } else -> {
                // Do nothing.
            }
        }
    }

    fun onOpenSpaceClicked(space: SpaceId) {
        viewModelScope.launch {
            val curr = spaceManager.get()
            if (curr == space.id) {
                commands.emit(Command.Dismiss)
            } else {
                spaceManager.set(space.id)
                saveCurrentSpace.async(params = SaveCurrentSpace.Params(space))
                // TODO navigate to the target space instead of dismissing
                commands.emit(Command.Dismiss)
            }
        }
    }

    fun onNotificationPromptDismissed() {
        viewModelScope.launch {
            commands.emit(Command.Dismiss)
        }
    }

    class Factory @Inject constructor(
        private val params: Params,
        private val getSpaceInviteView: GetSpaceInviteView,
        private val sendJoinSpaceRequest: SendJoinSpaceRequest,
        private val spaceInviteResolver: SpaceInviteResolver,
        private val checkIsUserSpaceMember: CheckIsUserSpaceMember,
        private val saveCurrentSpace: SaveCurrentSpace,
        private val spaceManager: SpaceManager,
        private val analytics: Analytics,
        private val notificator: SystemNotificationService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RequestJoinSpaceViewModel(
            params = params,
            getSpaceInviteView = getSpaceInviteView,
            sendJoinSpaceRequest = sendJoinSpaceRequest,
            spaceInviteResolver = spaceInviteResolver,
            checkIsUserSpaceMember = checkIsUserSpaceMember,
            saveCurrentSpace = saveCurrentSpace,
            spaceManager = spaceManager,
            analytics = analytics,
            notificator = notificator
        ) as T
    }

    data class Params(val link: String)

    sealed class Command {
        sealed class Toast : Command() {
            data object RequestSent : Toast()
            data object SpaceNotFound : Toast()
            data object SpaceDeleted : Toast()
        }
        data object Dismiss: Command()
    }

    sealed class ErrorView {
        data object InvalidLink : ErrorView()
        data class AlreadySpaceMember(val space: SpaceId) : ErrorView()
     }
}