package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenInviteRequest
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenRequestSent
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Notification
import com.anytypeio.anytype.core_models.NotificationPayload
import com.anytypeio.anytype.core_models.NotificationStatus
import com.anytypeio.anytype.core_models.multiplayer.MultiplayerError
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteError
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteView
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.getOrDefault
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.multiplayer.CheckIsUserSpaceMember
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteView
import com.anytypeio.anytype.domain.multiplayer.SendJoinSpaceRequest
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.notifications.SystemNotificationService
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.common.TypedViewState
import com.anytypeio.anytype.presentation.common.TypedViewState.*
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.Job
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
    private val notificator: SystemNotificationService,
    private val configStorage: ConfigStorage,
    private val spaceViewContainer: SpaceViewSubscriptionContainer
) : BaseViewModel() {

    val state = MutableStateFlow<TypedViewState<SpaceInviteView, ErrorView>>(TypedViewState.Loading)
    val isRequestInProgress = MutableStateFlow(false)
    val showEnableNotificationDialog = MutableStateFlow(false)
    val commands = MutableSharedFlow<Command>(0)
    val showLoadingInviteProgress = MutableStateFlow(false)
    private var getSpaceInviteViewJob: Job? = null
    private var joinSpaceRequestJob: Job? = null

    init {
        Timber.i("RequestJoinSpaceViewModel, init")
        proceedWithGettingSpaceInviteView()
    }

    private fun proceedWithGettingSpaceInviteView() {
        val fileKey = spaceInviteResolver.parseFileKey(params.link)
        val contentId = spaceInviteResolver.parseContentId(params.link)
        if (fileKey != null && contentId != null) {
            showLoadingInviteProgress.value = true
            getSpaceInviteViewJob = viewModelScope.launch {
                getSpaceInviteView.async(
                    GetSpaceInviteView.Params(
                        inviteContentId = contentId,
                        inviteFileKey = fileKey
                    )
                ).fold(
                    onSuccess = { view ->
                        showLoadingInviteProgress.value = false
                        val isAlreadyMember = checkIsUserSpaceMember
                            .async(view.space)
                            .getOrDefault(false)
                        if (isAlreadyMember) {
                            state.value = TypedViewState.Error(
                                ErrorView.AlreadySpaceMember(view.space)
                            )
                        } else {
                            val spaceView = spaceViewContainer.get(view.space)
                            if (spaceView != null && spaceView.spaceAccountStatus == SpaceStatus.SPACE_JOINING) {
                                state.value = TypedViewState.Error(ErrorView.RequestAlreadySent)
                            } else {
                                state.value = TypedViewState.Success(view)
                            }
                        }
                    },
                    onFailure = { e ->
                        showLoadingInviteProgress.value = false
                        if (e is SpaceInviteError) {
                            when(e) {
                                is SpaceInviteError.InvalidInvite -> {
                                    state.value = Error(
                                        ErrorView.InvalidLink
                                    )
                                }
                                is SpaceInviteError.InvalidNotFound -> {
                                    state.value = Error(
                                        ErrorView.InviteNotFound
                                    )
                                }
                                is SpaceInviteError.SpaceDeleted -> {
                                    state.value = Error(
                                        ErrorView.SpaceDeleted
                                    )
                                }
                                SpaceInviteError.InviteNotActive -> {
                                    state.value = Error(
                                        ErrorView.InvalidLink
                                    )
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

    fun onCancelLoadingInviteClicked() {
        getSpaceInviteViewJob?.cancel()
        showLoadingInviteProgress.value = false
    }

    fun onRequestToJoinClicked() {
        val currentState = state.value
        if (currentState !is TypedViewState.Success) return

        joinSpaceRequestJob?.cancel()
        joinSpaceRequestJob = viewModelScope.launch {
            val fileKey = spaceInviteResolver.parseFileKey(params.link)
            val contentId = spaceInviteResolver.parseContentId(params.link)

            if (fileKey == null || contentId == null) {
                Timber.w("Could not parse invite link in onRequestToJoinClicked: ${params.link}")
                return@launch
            }

            isRequestInProgress.value = true

            val params = SendJoinSpaceRequest.Params(
                space = currentState.data.space,
                network = configStorage.getOrNull()?.network,
                inviteFileKey = fileKey,
                inviteContentId = contentId
            )

            sendJoinSpaceRequest.async(params).fold(
                onFailure = { handleJoinRequestFailure(it) },
                onSuccess = { handleJoinRequestSuccess(currentState.data) }
            )

            isRequestInProgress.value = false
        }
    }

    private suspend fun handleJoinRequestFailure(error: Throwable) {
        Timber.e(error, "Error while sending space join request")
        when (error) {
            is MultiplayerError.Generic -> commands.emit(Command.ShowGenericMultiplayerError(error))
            else -> sendToast(error.msg())
        }
    }

    private suspend fun handleJoinRequestSuccess(data: SpaceInviteView) {
        analytics.sendEvent(eventName = screenRequestSent)

        val shouldNotify = data.withoutApprove
        val notificationsEnabled = notificator.areNotificationsEnabled

        if (shouldNotify) {
            sendApprovalNotification(data)
        }

        if (notificationsEnabled) {
            if (!shouldNotify) {
                commands.emit(Command.Toast.RequestSent)
            }
            commands.emit(Command.Dismiss)
        } else {
            if (!shouldNotify) {
                commands.emit(Command.Toast.RequestSent)
            }
            showEnableNotificationDialog.value = true
        }
    }

    private fun createApprovalNotification(data: SpaceInviteView): Notification {
        return Notification(
            id = Random.nextInt().toString(),
            createTime = System.currentTimeMillis(),
            status = NotificationStatus.CREATED,
            isLocal = true,
            payload = NotificationPayload.ParticipantWithoutApprovalRequestApproved(
                spaceId = data.space,
                spaceName = data.spaceName
            ),
            space = data.space
        )
    }

    private fun sendApprovalNotification(data: SpaceInviteView) {
        notificator.notify(createApprovalNotification(data))
    }

    fun onCancelJoinSpaceRequestClicked() {
        joinSpaceRequestJob?.cancel()
        isRequestInProgress.value = false
    }

    fun onOpenSpaceClicked(space: SpaceId) {
        viewModelScope.launch {
            val curr = spaceManager.get()
            if (curr == space.id) {
                commands.emit(Command.Dismiss)
            } else {
                spaceManager.set(space.id)
                saveCurrentSpace.async(params = SaveCurrentSpace.Params(space))
                commands.emit(Command.SwitchToSpace(space))
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
        private val notificator: SystemNotificationService,
        private val configStorage: ConfigStorage,
        private val spaceViewContainer: SpaceViewSubscriptionContainer
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
            notificator = notificator,
            configStorage = configStorage,
            spaceViewContainer = spaceViewContainer
        ) as T
    }

    data class Params(val link: String)

    sealed class Command {
        sealed class Toast : Command() {
            data object RequestSent : Toast()
            data object SpaceNotFound : Toast()
            data object SpaceDeleted : Toast()
        }
        data class ShowGenericMultiplayerError(val error: MultiplayerError.Generic) : Command()
        data object Dismiss: Command()
        data class SwitchToSpace(val space: SpaceId): Command()
    }

    sealed class ErrorView {
        data object InvalidLink : ErrorView()
        data object InviteNotFound : ErrorView()
        data object SpaceDeleted : ErrorView()
        data class AlreadySpaceMember(val space: SpaceId) : ErrorView()
        data object RequestAlreadySent: ErrorView()
     }
}