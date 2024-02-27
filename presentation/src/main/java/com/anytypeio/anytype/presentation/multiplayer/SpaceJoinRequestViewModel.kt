package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.ParticipantPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.ApproveJoinSpaceRequest
import com.anytypeio.anytype.domain.multiplayer.DeclineSpaceJoinRequest
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.common.ViewState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceJoinRequestViewModel(
    private val params: Params,
    private val approveJoinSpaceRequest: ApproveJoinSpaceRequest,
    private val declineSpaceJoinRequest: DeclineSpaceJoinRequest,
    private val searchObjects: SearchObjects,
    private val spaceManager: SpaceManager
): BaseViewModel() {

    val isDismissed = MutableStateFlow(false)

    private val state = MutableStateFlow<State>(State.Init)

    val viewState = MutableStateFlow<ViewState>(ViewState.Init)

    init {
        viewModelScope.launch {
            val config = spaceManager.getConfig()
            if (config != null && config.space == params.space.id) {
                searchObjects(
                    SearchObjects.Params(
                        sorts = emptyList(),
                        filters = buildList {
                            add(
                                DVFilter(
                                    relation = Relations.IS_ARCHIVED,
                                    condition = DVFilterCondition.NOT_EQUAL,
                                    value = true
                                )
                            )
                            add(
                                DVFilter(
                                    relation = Relations.IS_DELETED,
                                    condition = DVFilterCondition.NOT_EQUAL,
                                    value = true
                                )
                            )
                            add(
                                DVFilter(
                                    relation = Relations.ID,
                                    condition = DVFilterCondition.IN,
                                    value = listOf(config.spaceView, params.member)
                                )
                            )
                        },
                        limit = 2,
                        keys = listOf(
                            Relations.ID,
                            Relations.SPACE_ID,
                            Relations.TARGET_SPACE_ID,
                            Relations.IDENTITY,
                            Relations.ICON_IMAGE,
                            Relations.NAME
                        )
                    )
                ).process(
                    failure = {
                        Timber.e(it, "Error while fetching space data and member data")
                    },
                    success = { wrappers ->
                        val spaceView = wrappers.firstOrNull { it.id == config.spaceView }
                        val member = wrappers.firstOrNull { it.id == params.member }
                        if (spaceView != null && member != null) {
                            state.value = State.Success(
                                member = ObjectWrapper.Participant(member.map),
                                spaceView = ObjectWrapper.SpaceView(spaceView.map)
                            )
                        } else {
                            state.value = State.Error
                        }
                    }
                )
            } else {
                state.value = State.Error
            }
        }

        viewModelScope.launch {
            state.collect { curr ->
                viewState.value = when (curr) {
                    is State.Error -> ViewState.Error
                    is State.Init -> ViewState.Init
                    is State.Success -> ViewState.Success(
                        memberName = curr.member.name.orEmpty(),
                        spaceName = curr.spaceView.name.orEmpty()
                    )
                }
            }
        }
    }

    fun onRejectRequestClicked() {
        viewModelScope.launch {
            when(val curr = state.value) {
                is State.Error -> {
                    // TODO send toast
                }
                is State.Init -> {
                    // Do nothing
                }
                is State.Success -> {
                    declineSpaceJoinRequest.async(
                        DeclineSpaceJoinRequest.Params(
                            space = params.space,
                            identity = curr.member.identity
                        )
                    ).fold(
                        onSuccess = {
                            isDismissed.value = true
                        },
                        onFailure = {
                            Timber.e(it, "Error while approving join-space request")
                        }
                    )
                }
            }
        }
    }

    fun onJoinAsReaderClicked() {
        viewModelScope.launch {
            when(val curr = state.value) {
                is State.Error -> {
                    // TODO send toast
                }
                is State.Init -> {
                    // Do nothing
                }
                is State.Success -> {
                    approveJoinSpaceRequest.async(
                        ApproveJoinSpaceRequest.Params(
                            space = params.space,
                            identity = curr.member.identity,
                            permissions = ParticipantPermissions.READER
                        )
                    ).fold(
                        onSuccess = {
                            isDismissed.value = true
                        },
                        onFailure = {
                            Timber.e(it, "Error while approving join-space request")
                        }
                    )
                }
            }
        }
    }

    fun onJoinAsEditorClicked() {
        viewModelScope.launch {
            when(val curr = state.value) {
                is State.Error -> {
                    // TODO send toast
                }
                is State.Init -> {
                    // Do nothing
                }
                is State.Success -> {
                    approveJoinSpaceRequest.async(
                        ApproveJoinSpaceRequest.Params(
                            space = params.space,
                            identity = curr.member.identity,
                            permissions = ParticipantPermissions.WRITER
                        )
                    ).fold(
                        onSuccess = {
                            isDismissed.value = true
                        },
                        onFailure = {
                            Timber.e(it, "Error while approving join-space request")
                        }
                    )
                }
            }
        }
    }

    class Factory @Inject constructor(
        private val params: Params,
        private val approveJoinSpaceRequest: ApproveJoinSpaceRequest,
        private val declineSpaceJoinRequest: DeclineSpaceJoinRequest,
        private val searchObjects: SearchObjects,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SpaceJoinRequestViewModel(
            params = params,
            declineSpaceJoinRequest = declineSpaceJoinRequest,
            approveJoinSpaceRequest = approveJoinSpaceRequest,
            searchObjects = searchObjects,
            spaceManager = spaceManager
        ) as T
    }

    data class Params(val space: SpaceId, val member: Id)

    sealed class State {
        object Init : State()
        data class Success(
            val member: ObjectWrapper.Participant,
            val spaceView: ObjectWrapper.SpaceView
        ) : State()
        object Error : State()
    }

    sealed class ViewState {
        object Init: ViewState()
        data class Success(val memberName: String, val spaceName: String): ViewState()
        object Error: ViewState()
    }
}