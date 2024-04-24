package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SpaceListViewModel(
    private val spaces: SpaceViewSubscriptionContainer
) : BaseViewModel() {

    val state : StateFlow<ViewState<List<SpaceListItemView>>> = flow {
        emitAll(
            spaces.observe()
                .map { items ->
                    ViewState.Success(
                        items.map {
                            SpaceListItemView(
                                space = it
                            )
                        }
                    )
                }.catch { e ->
                    emit(ViewState.Error(e.msg()))
                }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ViewState.Init)

    data class SpaceListItemView(
        val space: ObjectWrapper.SpaceView,
        val permissions: SpaceMemberPermissions = SpaceMemberPermissions.NO_PERMISSIONS
    )

    class Factory @Inject constructor(
        private val spaces: SpaceViewSubscriptionContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = SpaceListViewModel(spaces = spaces) as T
    }
}