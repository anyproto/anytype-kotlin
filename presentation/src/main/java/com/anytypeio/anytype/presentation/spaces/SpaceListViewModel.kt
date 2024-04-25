package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.CancelJoinSpaceRequest
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.spaces.DeleteSpace
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceListViewModel(
    private val spaces: SpaceViewSubscriptionContainer,
    private val permissions: UserPermissionProvider,
    private val urlBuilder: UrlBuilder,
    private val deleteSpace: DeleteSpace,
    private val cancelJoinSpaceRequest: CancelJoinSpaceRequest
) : BaseViewModel() {

    val warning: MutableStateFlow<Warning> = MutableStateFlow(Warning.None)

    val state: StateFlow<ViewState<List<SpaceListItemView>>> = flow {
        emit(ViewState.Loading)
        emitAll(
            combine(
                spaces.observe(),
                permissions.all()
            ) { s, p ->
                ViewState.Success(
                    data = s.map { space ->
                        SpaceListItemView(
                            space = space,
                            icon = space.spaceIcon(
                                builder = urlBuilder,
                                spaceGradientProvider = SpaceGradientProvider.Default
                            ),
                            permissions = p.getOrDefault(
                                key = requireNotNull(space.targetSpaceId),
                                defaultValue = SpaceMemberPermissions.NO_PERMISSIONS
                            )
                        )
                    }
                )
            }.catch { e ->
                emit(ViewState.Error(e.msg()))
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = ViewState.Init
    )

    fun onDeleteSpaceClicked(view: SpaceListItemView) {
        warning.value = Warning.DeleteSpace(SpaceId(view.space.targetSpaceId!!))
    }

    fun onLeaveSpaceClicked(view: SpaceListItemView) {
        warning.value = Warning.LeaveSpace(SpaceId(view.space.targetSpaceId!!))
    }

    fun onCancelJoinSpaceClicked(view: SpaceListItemView) {
        warning.value = Warning.CancelSpaceJoinRequest(SpaceId(view.space.targetSpaceId!!))
    }

    fun onCancelJoinRequestAccepted(space: SpaceId) {
        viewModelScope.launch {
            Timber.d("Cancelling")
//            cancelJoinSpaceRequest.async(space).fold(
//                onSuccess = {
//                    Timber.d("Space deleted successfully")
//                },
//                onFailure = {
//                    Timber.e(it, "Error while deleting space")
//                }
//            )
        }
    }

    fun onDeleteSpaceAccepted(space: SpaceId) {
        viewModelScope.launch {
            warning.update { Warning.None }
            Timber.d("Deleting")
//            deleteSpace.async(space).fold(
//                onSuccess = {
//                    Timber.d("Space deleted successfully")
//                },
//                onFailure = {
//                    Timber.e(it, "Error while deleting space")
//                }
//            )
        }
    }

    fun onLeaveSpaceAccepted(space: SpaceId) {
        viewModelScope.launch {
            warning.update { Warning.None }
            Timber.d("Leaving")
//            deleteSpace.async(space).fold(
//                onSuccess = {
//                    Timber.d("Space deleted successfully")
//                },
//                onFailure = {
//                    Timber.e(it, "Error while deleting space")
//                }
//            )
        }
    }

    fun onWarningDismissed() {
        warning.value = Warning.None
    }

    data class SpaceListItemView(
        val space: ObjectWrapper.SpaceView,
        val icon: SpaceIconView,
        val permissions: SpaceMemberPermissions,
        val actions: List<Action> = listOf(
            Action.LeaveSpace,
            Action.DeleteSpace,
            Action.CancelJoinRequest
        )
    ) {
        sealed class Action {
            data object CancelJoinRequest : Action()
            data object DeleteSpace : Action()
            data object LeaveSpace: Action()
        }
    }

    sealed class Warning {
        data object None: Warning()
        data class DeleteSpace(val space: SpaceId): Warning()
        data class LeaveSpace(val space: SpaceId): Warning()
        data class CancelSpaceJoinRequest(val space: SpaceId): Warning()
    }

    class Factory @Inject constructor(
        private val spaces: SpaceViewSubscriptionContainer,
        private val permissions: UserPermissionProvider,
        private val deleteSpace: DeleteSpace,
        private val cancelJoinSpaceRequest: CancelJoinSpaceRequest,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = SpaceListViewModel(
            spaces = spaces,
            deleteSpace = deleteSpace,
            permissions = permissions,
            urlBuilder = urlBuilder,
            cancelJoinSpaceRequest = cancelJoinSpaceRequest
        ) as T
    }
}