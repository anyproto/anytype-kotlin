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
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.ext.isPossibleToUpgradeNumberOfSpaceMembers
import com.anytypeio.anytype.core_models.membership.MembershipConstants.BUILDER_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.OLD_EXPLORER_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.PIONEER_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.STARTER_ID
import com.anytypeio.anytype.core_models.membership.TierId
import com.anytypeio.anytype.core_models.multiplayer.MultiplayerError
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ApproveJoinSpaceRequest
import com.anytypeio.anytype.domain.multiplayer.DeclineSpaceJoinRequest
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.canAddReaders
import com.anytypeio.anytype.domain.`object`.canAddWriters
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsApproveInvite
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.objects.SpaceMemberIconView
import com.anytypeio.anytype.presentation.objects.toSpaceMembers
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.filterParticipants
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceJoinRequestViewModel(
    private val vmParams: VmParams,
    private val approveJoinSpaceRequest: ApproveJoinSpaceRequest,
    private val declineSpaceJoinRequest: DeclineSpaceJoinRequest,
    private val searchObjects: SearchObjects,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    private val userPermissionProvider: UserPermissionProvider,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val membershipProvider: MembershipProvider
) : BaseViewModel() {

    val isDismissed = MutableStateFlow(false)
    private val _isCurrentUserOwner = MutableStateFlow(false)
    private val _spaceMembers = MutableStateFlow<SpaceMembersState>(SpaceMembersState.Init)
    private val _newMember = MutableStateFlow<NewMemberState>(NewMemberState.Init)
    private val _viewState = MutableStateFlow<ViewState>(ViewState.Init)
    private val _activeTier = MutableStateFlow<ActiveTierState>(ActiveTierState.Init)
    val viewState: StateFlow<ViewState> = _viewState

    private val _commands = MutableSharedFlow<Command>(replay = 0)
    val commands: SharedFlow<Command> = _commands

    init {
        Timber.i("SpaceJoinRequestViewModel, init")
        viewModelScope.launch {
            combine(
                _isCurrentUserOwner,
                spaceViewSubscriptionContainer.observe(vmParams.space),
                _activeTier.filterIsInstance<ActiveTierState.Success>(),
                _spaceMembers.filterIsInstance<SpaceMembersState.Success>(),
                _newMember.filterIsInstance<NewMemberState.Success>()
            ) { isCurrentUserOwner, spaceView, tierState, spaceMembersState, newMemberState ->
                Result(
                    isCurrentUserOwner = isCurrentUserOwner,
                    spaceView = spaceView,
                    tierId = tierState.tierId,
                    spaceMembers = spaceMembersState.spaceMembers,
                    newMember = newMemberState.newMember
                )
            }.collect { result ->
                proceedWithState(
                    tierId = result.tierId,
                    spaceView = result.spaceView,
                    spaceMembers = result.spaceMembers,
                    newMember = result.newMember,
                    isCurrentUserOwner = result.isCurrentUserOwner
                )
            }
        }
        sendAnalyticsInviteScreen()
        proceedWithUserPermissions(space = vmParams.space)
        proceedWithSpaceMembers(space = vmParams.space)
        proceedGettingNewMember()
        proceedWithGettingActiveTier()
    }

    private fun proceedWithGettingActiveTier() {
        viewModelScope.launch {
            membershipProvider.activeTier()
                .catch { e ->
                    Timber.e(e, "Error while fetching active tier")
                    _viewState.value = ViewState.Error.ActiveTierError(e.msg())
                }
                .collect { tierId ->
                    _activeTier.value = ActiveTierState.Success(tierId)
                }
        }
    }

    private fun sendAnalyticsInviteScreen() {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenInviteConfirm,
                props = Props(
                    mapOf(EventsPropertiesKey.route to vmParams.route)
                )
            )
        }
    }

    private fun proceedWithState(
        tierId: TierId,
        spaceView: ObjectWrapper.SpaceView,
        spaceMembers: List<ObjectWrapper.SpaceMember>,
        newMember: ObjectWrapper.SpaceMember,
        isCurrentUserOwner: Boolean
    ) {
        Timber.d("proceedWithState, tierId: $tierId, spaceView: $spaceView, spaceMembers: $spaceMembers, newMember: $newMember, isCurrentUserOwner: $isCurrentUserOwner")

        val state = when (tierId.value) {
            STARTER_ID, OLD_EXPLORER_ID, PIONEER_ID -> createExplorerState(
                spaceView = spaceView,
                spaceMembers = spaceMembers,
                newMember = newMember,
                isCurrentUserOwner = isCurrentUserOwner
            )

            BUILDER_ID -> createBuilderState(
                spaceView = spaceView,
                spaceMembers = spaceMembers,
                newMember = newMember,
                isCurrentUserOwner = isCurrentUserOwner
            )

            else -> createOtherState(
                spaceView = spaceView,
                spaceMembers = spaceMembers,
                newMember = newMember,
                isCurrentUserOwner = isCurrentUserOwner
            )
        }
        _viewState.value = state
    }

    private fun createExplorerState(
        spaceView: ObjectWrapper.SpaceView,
        spaceMembers: List<ObjectWrapper.SpaceMember>,
        newMember: ObjectWrapper.SpaceMember,
        isCurrentUserOwner: Boolean
    ): ViewState {
        val canAddReaders = spaceView.canAddReaders(
            isCurrentUserOwner = isCurrentUserOwner,
            participants = spaceMembers,
            newMember = newMember
        )
        val canAddWriters = spaceView.canAddWriters(
            isCurrentUserOwner = isCurrentUserOwner,
            participants = spaceMembers,
            newMember = newMember
        )
        return when {
            !canAddReaders && !canAddWriters ->
                createViewStateWithButtons(
                    spaceView = spaceView,
                    newMember = newMember,
                    buttons = listOf(InviteButton.UPGRADE, InviteButton.REJECT)
                )

            else -> createViewStateWithButtons(
                spaceView = spaceView,
                newMember = newMember,
                buttons = buildList {
                    if (!canAddReaders) add(InviteButton.JOIN_AS_VIEWER_DISABLED)
                    else add(InviteButton.JOIN_AS_VIEWER)
                    if (!canAddWriters) add(InviteButton.JOIN_AS_EDITOR_DISABLED)
                    else add(InviteButton.JOIN_AS_EDITOR)
                    add(InviteButton.REJECT)
                }
            )
        }
    }

    private fun createBuilderState(
        spaceView: ObjectWrapper.SpaceView,
        spaceMembers: List<ObjectWrapper.SpaceMember>,
        newMember: ObjectWrapper.SpaceMember,
        isCurrentUserOwner: Boolean
    ): ViewState {
        val canAddReaders = spaceView.canAddReaders(
            isCurrentUserOwner = isCurrentUserOwner,
            participants = spaceMembers,
            newMember = newMember
        )
        val canAddWriters = spaceView.canAddWriters(
            isCurrentUserOwner = isCurrentUserOwner,
            participants = spaceMembers,
            newMember = newMember
        )
        return when {
            !canAddReaders && !canAddWriters ->
                createViewStateWithButtons(
                    spaceView = spaceView,
                    newMember = newMember,
                    buttons = listOf(
                        InviteButton.ADD_MORE_VIEWERS,
                        InviteButton.ADD_MORE_EDITORS,
                        InviteButton.REJECT
                    )
                )

            else -> createViewStateWithButtons(
                spaceView = spaceView,
                newMember = newMember,
                buttons = buildList {
                    if (!canAddReaders) add(InviteButton.ADD_MORE_VIEWERS) else add(InviteButton.JOIN_AS_VIEWER)
                    if (!canAddWriters) add(InviteButton.ADD_MORE_EDITORS) else add(InviteButton.JOIN_AS_EDITOR)
                    add(InviteButton.REJECT)
                }
            )
        }
    }

    private fun createOtherState(
        spaceView: ObjectWrapper.SpaceView,
        spaceMembers: List<ObjectWrapper.SpaceMember>,
        newMember: ObjectWrapper.SpaceMember,
        isCurrentUserOwner: Boolean
    ): ViewState {
        val canAddReaders = spaceView.canAddReaders(
            isCurrentUserOwner = isCurrentUserOwner,
            participants = spaceMembers,
            newMember = newMember
        )
        val canAddWriters = spaceView.canAddWriters(
            isCurrentUserOwner = isCurrentUserOwner,
            participants = spaceMembers,
            newMember = newMember
        )
        return when {
            !canAddReaders && !canAddWriters -> createViewStateWithButtons(
                spaceView = spaceView,
                newMember = newMember,
                buttons = listOf(
                    InviteButton.ADD_MORE_VIEWERS,
                    InviteButton.ADD_MORE_EDITORS,
                    InviteButton.REJECT
                )
            )

            else -> createViewStateWithButtons(
                spaceView = spaceView,
                newMember = newMember,
                buttons = buildList {
                    if (!canAddReaders) add(InviteButton.ADD_MORE_VIEWERS) else add(InviteButton.JOIN_AS_VIEWER)
                    if (!canAddWriters) add(InviteButton.ADD_MORE_EDITORS) else add(InviteButton.JOIN_AS_EDITOR)
                    add(InviteButton.REJECT)
                }
            )
        }
    }

    private fun createViewStateWithButtons(
        spaceView: ObjectWrapper.SpaceView,
        newMember: ObjectWrapper.SpaceMember,
        buttons: List<InviteButton>
    ): ViewState.Success {
        return ViewState.Success(
            spaceName = spaceView.name.orEmpty(),
            newMember = newMember.identity,
            newMemberName = newMember.name.orEmpty(),
            icon = SpaceMemberIconView.icon(
                obj = newMember,
                urlBuilder = urlBuilder
            ),
            buttons = buttons
        )
    }

    private fun proceedWithUserPermissions(space: SpaceId) {
        viewModelScope.launch {
            userPermissionProvider
                .observe(space = space)
                .catch {
                    Timber.e(it, "Error while fetching user permissions")
                    _viewState.value = ViewState.Error.CurrentUserStatusError(it.msg())
                }
                .collect { permission ->
                    _isCurrentUserOwner.value = permission == SpaceMemberPermissions.OWNER
                }
        }
    }

    private fun proceedWithSpaceMembers(space: SpaceId) {
        val searchMembersParams = SearchObjects.Params(
            space = space,
            filters = filterParticipants(space = space),
            keys = ObjectSearchConstants.spaceMemberKeys
        )
        viewModelScope.launch {
            searchObjects(searchMembersParams).process(
                failure = {
                    Timber.e(it, "Error while fetching participants")
                    _viewState.value = ViewState.Error.SpaceParticipantsError(it.msg())
                },
                success = { result ->
                    Timber.d("proceedWithSpaceMembers, success: $result")
                    val spaceMembers = result.toSpaceMembers()
                    _spaceMembers.value = SpaceMembersState.Success(spaceMembers)
                }
            )
        }
    }

    private fun proceedGettingNewMember() {
        viewModelScope.launch {
            val filters = ObjectSearchConstants.filterNewMember(vmParams.member)
            searchObjects(
                SearchObjects.Params(
                    space = vmParams.space,
                    filters = filters,
                    keys = ObjectSearchConstants.spaceMemberKeys,
                    limit = 1
                )
            ).process(
                failure = {
                    Timber.e(it, "Error while fetching new member")
                    _viewState.value = ViewState.Error.NewMemberError(it.msg())
                },
                success = { result ->
                    val memberMap = result.firstOrNull()?.map
                    if (memberMap.isNullOrEmpty()) {
                        _viewState.value = ViewState.Error.NewMemberError("New member not found")
                    } else {
                        _newMember.value =
                            NewMemberState.Success(ObjectWrapper.SpaceMember(memberMap))
                    }
                }
            )
        }
    }

    fun onRejectRequestClicked(newMember: Id) {
        Timber.d("onRejectRequestClicked, newMember: $newMember")
        viewModelScope.launch {
            declineSpaceJoinRequest.async(
                DeclineSpaceJoinRequest.Params(
                    space = vmParams.space,
                    identity = newMember
                )
            ).fold(
                onSuccess = {
                    analytics.sendEvent(eventName = rejectInviteRequest)
                    isDismissed.value = true
                },
                onFailure = { e ->
                    Timber.e(e, "Error while rejecting join-space request")
                    if (e is MultiplayerError.Generic) {
                        _commands.emit(Command.ShowGenericMultiplayerError(e))
                    } else {
                        sendToast(e.msg())
                    }
                }
            )
        }
    }

    private fun onJoinClicked(newMember: Id, permissions: SpaceMemberPermissions) {
        Timber.d("onJoinClicked, newMember: $newMember, permissions: $permissions")
        viewModelScope.launch {
            val spaceView = spaceViewSubscriptionContainer.get(vmParams.space)
            val spaceUxType = spaceView?.spaceUxType
            approveJoinSpaceRequest.async(
                ApproveJoinSpaceRequest.Params(
                    space = vmParams.space,
                    identity = newMember,
                    permissions = permissions
                )
            ).fold(
                onSuccess = {
                    analytics.sendAnalyticsApproveInvite(
                        permissions = permissions,
                        spaceUxType = spaceUxType
                    )
                    isDismissed.value = true
                },
                onFailure = { e ->
                    Timber.e(e, "Error while approving join-space request")
                    if (e is MultiplayerError.Generic) {
                        _commands.emit(Command.ShowGenericMultiplayerError(e))
                    } else {
                        sendToast(e.msg())
                    }
                }
            )
        }
    }

    fun onJoinAsReaderClicked(newMember: Id) {
        onJoinClicked(newMember, SpaceMemberPermissions.READER)
    }

    fun onJoinAsEditorClicked(newMember: Id) {
        onJoinClicked(newMember, SpaceMemberPermissions.WRITER)
    }

    fun onUpgradeClicked() {
        val activeTier = (_activeTier.value as? ActiveTierState.Success) ?: return
        val isPossibleToUpgrade = activeTier.tierId.isPossibleToUpgradeNumberOfSpaceMembers()
        viewModelScope.launch {
            if (isPossibleToUpgrade) {
                _commands.emit(Command.NavigateToMembership)
            } else {
                _commands.emit(Command.NavigateToMembershipUpdate)
            }
        }
    }

    class Factory @Inject constructor(
        private val params: VmParams,
        private val approveJoinSpaceRequest: ApproveJoinSpaceRequest,
        private val declineSpaceJoinRequest: DeclineSpaceJoinRequest,
        private val searchObjects: SearchObjects,
        private val urlBuilder: UrlBuilder,
        private val analytics: Analytics,
        private val userPermissionProvider: UserPermissionProvider,
        private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        private val membershipProvider: MembershipProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SpaceJoinRequestViewModel(
            vmParams = params,
            declineSpaceJoinRequest = declineSpaceJoinRequest,
            approveJoinSpaceRequest = approveJoinSpaceRequest,
            searchObjects = searchObjects,
            urlBuilder = urlBuilder,
            analytics = analytics,
            userPermissionProvider = userPermissionProvider,
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            membershipProvider = membershipProvider
        ) as T
    }

    data class VmParams(val space: SpaceId, val member: Id, val route: String)

    data class Result(
        val isCurrentUserOwner: Boolean,
        val spaceView: ObjectWrapper.SpaceView,
        val tierId: TierId,
        val spaceMembers: List<ObjectWrapper.SpaceMember>,
        val newMember: ObjectWrapper.SpaceMember
    )

    //We're trying to add or remove new member to this space
    sealed class SpaceViewState {
        data object Init : SpaceViewState()
        data class Success(val spaceView: ObjectWrapper.SpaceView) : SpaceViewState()
    }

    //All participants of this space
    sealed class SpaceMembersState {
        data object Init : SpaceMembersState()
        data class Success(val spaceMembers: List<ObjectWrapper.SpaceMember>) : SpaceMembersState()
    }

    //New member that we're trying to add or remove
    sealed class NewMemberState {
        data object Init : NewMemberState()
        data class Success(val newMember: ObjectWrapper.SpaceMember) : NewMemberState()
    }

    //Active membership status of the current user
    sealed class ActiveTierState {
        data object Init : ActiveTierState()
        data class Success(val tierId: TierId) : ActiveTierState()
    }

    sealed class ViewState {
        data object Init : ViewState()
        data class Success(
            val newMember: Id,
            val newMemberName: String,
            val spaceName: String,
            val icon: SpaceMemberIconView,
            val buttons: List<InviteButton>
        ) : ViewState()

        sealed class Error : ViewState() {
            data class SpaceParticipantsError(val message: String) : Error()
            data class ActiveTierError(val message: String) : Error()
            data class CurrentUserStatusError(val message: String) : Error()
            data class NewMemberError(val message: String) : Error()
        }
    }

    sealed class Command {
        data object NavigateToMembership : Command()
        data object NavigateToMembershipUpdate : Command()
        data class ShowGenericMultiplayerError(val error: MultiplayerError.Generic) : Command()
    }
}

enum class InviteButton {
    REJECT,
    JOIN_AS_VIEWER,
    JOIN_AS_VIEWER_DISABLED,
    ADD_MORE_VIEWERS,
    JOIN_AS_EDITOR,
    JOIN_AS_EDITOR_DISABLED,
    ADD_MORE_EDITORS,
    UPGRADE,
}