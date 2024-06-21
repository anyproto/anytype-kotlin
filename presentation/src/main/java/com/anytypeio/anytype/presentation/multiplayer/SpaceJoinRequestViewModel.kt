package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.rejectInviteRequest
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ApproveJoinSpaceRequest
import com.anytypeio.anytype.domain.multiplayer.DeclineSpaceJoinRequest
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.objects.SpaceMemberIconView
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.filterParticipants
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceJoinRequestViewModel(
    private val params: Params,
    private val approveJoinSpaceRequest: ApproveJoinSpaceRequest,
    private val declineSpaceJoinRequest: DeclineSpaceJoinRequest,
    private val searchObjects: SearchObjects,
    private val spaceManager: SpaceManager,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    private val userPermissionProvider: UserPermissionProvider
): BaseViewModel() {

    val isDismissed = MutableStateFlow(false)
    private val _isCurrentUserOwner = MutableStateFlow(false)

    private val state = MutableStateFlow<State>(State.Init)

    val viewState = MutableStateFlow<ViewState>(ViewState.Init)

    init {
        proceedWithUserPermissions()
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
                    failure = { e ->
                        Timber.e(e, "Error while fetching space data and member data").also {
                            sendToast(e.msg())
                        }
                    },
                    success = { wrappers ->
                        val spaceView = wrappers.firstOrNull { it.id == config.spaceView }
                        val member = wrappers.firstOrNull { it.id == params.member }
                        if (spaceView != null && member != null) {
                            state.value = State.Success(
                                member = ObjectWrapper.SpaceMember(member.map),
                                spaceView = ObjectWrapper.SpaceView(spaceView.map),
                                participants = emptyList()
                            )
                            getMembers(config)
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
            state.combine(_isCurrentUserOwner) { curr, isOwner ->
                curr to isOwner
            }.collect { (curr, isCurrentUserOwner) ->
                viewState.value = when (curr) {
                    is State.Error -> ViewState.Error
                    is State.Init -> ViewState.Init
                    is State.Success -> {
                        val incentiveState = curr.spaceView.getIncentiveState(
                            spaceMembers = curr.participants,
                            isCurrentUserOwner = isCurrentUserOwner
                        )
                        when (incentiveState) {
                            ShareSpaceViewModel.ShareSpaceIncentiveState.Hidden -> {
                                ViewState.Success(
                                    memberName = curr.member.name.orEmpty(),
                                    spaceName = curr.spaceView.name.orEmpty(),
                                    icon = SpaceMemberIconView.icon(
                                        obj = curr.member,
                                        urlBuilder = urlBuilder
                                    )
                                )
                            }
                            else -> {
                                ViewState.Upgrade(
                                    memberName = curr.member.name.orEmpty(),
                                    spaceName = curr.spaceView.name.orEmpty(),
                                    icon = SpaceMemberIconView.icon(
                                        obj = curr.member,
                                        urlBuilder = urlBuilder
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenInviteConfirm,
                props = Props(
                    mapOf(EventsPropertiesKey.route to params.route)
                )
            )
        }
    }

    private fun proceedWithUserPermissions() {
        viewModelScope.launch {
            userPermissionProvider
                .observe(space = params.space)
                .collect { permission ->
                    _isCurrentUserOwner.value = permission == SpaceMemberPermissions.OWNER
                }
        }
    }

    private suspend fun getMembers(config: Config) {
        val searchMembersParams = SearchObjects.Params(
            filters = filterParticipants(
                spaces = listOf(config.spaceView)
            ),
            keys = ObjectSearchConstants.spaceMemberKeys
        )
        searchObjects(searchMembersParams).proceed(
            failure = { Timber.e(it, "Error while fetching participants") },
            success = { result ->
                val currentState = state.value
                if (currentState is State.Success) {
                    state.value = currentState.copy(
                        participants = result
                            .map { ObjectWrapper.SpaceMember(it.map) }
                            .filter { it.status == ParticipantStatus.ACTIVE
                                    && it.permissions != SpaceMemberPermissions.NO_PERMISSIONS
                            }
                    )
                }
            }
        )
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
                            analytics.sendEvent(eventName = rejectInviteRequest)
                            isDismissed.value = true
                        },
                        onFailure = { e ->
                            Timber.e(e, "Error while approving join-space request").also {
                                sendToast(e.msg())
                            }
                        }
                    )
                }
            }
        }
    }

    fun onJoinAsReaderClicked() {
        Timber.d("onJoinAsReaderClicked, state: ${state.value}")
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
                            permissions = SpaceMemberPermissions.READER
                        )
                    ).fold(
                        onSuccess = {
                            analytics.sendEvent(
                                eventName = EventsDictionary.approveInviteRequest,
                                props = Props(
                                    mapOf(
                                        EventsPropertiesKey.type to "Read"
                                    )
                                )
                            )
                            isDismissed.value = true
                        },
                        onFailure = { e ->
                            Timber.e(e, "Error while approving join-space request").also {
                                sendToast(e.msg())
                            }
                        }
                    )
                }
            }
        }
    }

    fun onJoinAsEditorClicked() {
        Timber.d("onJoinAsEditorClicked, state: ${state.value}")
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
                            permissions = SpaceMemberPermissions.WRITER
                        )
                    ).fold(
                        onSuccess = {
                            analytics.sendEvent(
                                eventName = EventsDictionary.approveInviteRequest,
                                props = Props(
                                    mapOf(
                                        EventsPropertiesKey.type to "Write"
                                    )
                                )
                            )
                            isDismissed.value = true
                        },
                        onFailure = { e ->
                            Timber.e(e, "Error while approving join-space request").also {
                                sendToast(e.msg())
                            }
                        }
                    )
                }
            }
        }
    }

    fun onUpgradeClicked() {

    }

    class Factory @Inject constructor(
        private val params: Params,
        private val approveJoinSpaceRequest: ApproveJoinSpaceRequest,
        private val declineSpaceJoinRequest: DeclineSpaceJoinRequest,
        private val searchObjects: SearchObjects,
        private val spaceManager: SpaceManager,
        private val urlBuilder: UrlBuilder,
        private val analytics: Analytics,
        private val userPermissionProvider: UserPermissionProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SpaceJoinRequestViewModel(
            params = params,
            declineSpaceJoinRequest = declineSpaceJoinRequest,
            approveJoinSpaceRequest = approveJoinSpaceRequest,
            searchObjects = searchObjects,
            spaceManager = spaceManager,
            urlBuilder = urlBuilder,
            analytics = analytics,
            userPermissionProvider = userPermissionProvider
        ) as T
    }

    data class Params(val space: SpaceId, val member: Id, val route: String)

    sealed class State {
        data object Init : State()
        data class Success(
            val member: ObjectWrapper.SpaceMember,
            val spaceView: ObjectWrapper.SpaceView,
            val participants: List<ObjectWrapper.SpaceMember>
        ) : State()
        data object Error : State()
    }

    sealed class ViewState {
        data object Init: ViewState()
        data class Success(
            val memberName: String,
            val spaceName: String,
            val icon: SpaceMemberIconView
        ): ViewState()
        data class Upgrade(
            val memberName: String,
            val spaceName: String,
            val icon: SpaceMemberIconView
        ) : ViewState()
        data object Error: ViewState()
    }
}