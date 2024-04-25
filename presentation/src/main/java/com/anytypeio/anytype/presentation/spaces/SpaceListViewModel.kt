package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.spaces.DeleteSpace
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceListViewModel(
    private val spaces: SpaceViewSubscriptionContainer,
    private val permissions: UserPermissionProvider,
    private val urlBuilder: UrlBuilder,
    private val deleteSpace: DeleteSpace
) : BaseViewModel() {

    val state : StateFlow<ViewState<List<SpaceListItemView>>> = flow {
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

    fun onCancelJoinRequestAccepted(view: SpaceListItemView) {

    }

    fun onCancelLeaveRequestAccepted(view: SpaceListItemView) {
        // TODO
    }

    fun onDeleteSpaceAccepted(view: SpaceListItemView) {
        viewModelScope.launch {
            val space = view.space.targetSpaceId
            requireNotNull(space)
            deleteSpace.async(SpaceId(space)).fold(
                onSuccess = {
                    Timber.d("Space deleted successfully")
                },
                onFailure = {
                    Timber.e(it, "Error while deleting space")
                }
            )
        }
    }

    data class SpaceListItemView(
        val space: ObjectWrapper.SpaceView,
        val icon: SpaceIconView,
        val permissions: SpaceMemberPermissions,
        val actions: List<Action> = emptyList()
    ) {
        sealed class Action {
            data object CancelJoinRequest: Action()
            data object CancelLeaveRequest: Action()
            data object DeleteSpace: Action()
        }
    }

    class Factory @Inject constructor(
        private val spaces: SpaceViewSubscriptionContainer,
        private val permissions: UserPermissionProvider,
        private val deleteSpace: DeleteSpace,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = SpaceListViewModel(
            spaces = spaces,
            deleteSpace = deleteSpace,
            permissions = permissions,
            urlBuilder = urlBuilder
        ) as T
    }
}