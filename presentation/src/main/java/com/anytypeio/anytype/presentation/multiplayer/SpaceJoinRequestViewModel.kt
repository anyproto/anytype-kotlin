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
import com.anytypeio.anytype.core_models.membership.MembershipConstants.EXPLORER_ID
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
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
import com.anytypeio.anytype.core_models.membership.TierId
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.objects.SpaceMemberIconView
import com.anytypeio.anytype.presentation.objects.toSpaceMembers
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.filterParticipants
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceJoinRequestViewModel(
    private val params: Params,
    private val approveJoinSpaceRequest: ApproveJoinSpaceRequest,
    private val declineSpaceJoinRequest: DeclineSpaceJoinRequest,
    private val searchObjects: SearchObjects,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    private val userPermissionProvider: UserPermissionProvider,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val membershipProvider: MembershipProvider
): BaseViewModel() {

    val isDismissed = MutableStateFlow(false)
    private val _isCurrentUserOwner = MutableStateFlow(false)
    private val _spaceMembers = MutableStateFlow<List<ObjectWrapper.SpaceMember>>(mutableListOf())
    private val _newMember = MutableStateFlow<ObjectWrapper.SpaceMember?>(null)
    private val _viewState = MutableStateFlow<ViewState>(ViewState.Init)
    private val _activeTier = MutableStateFlow<TierId?>(null)
    val viewState: StateFlow<ViewState> = _viewState

    private val _commands = MutableStateFlow<Command?>(null)
    val commands: StateFlow<Command?> = _commands

    init {
        viewModelScope.launch {
            combine(
                _activeTier.filterNotNull(),
                spaceViewSubscriptionContainer.observe(params.space),
                _isCurrentUserOwner,
                _spaceMembers,
                _newMember
            ) { tierId, spaceView, isCurrentUserOwner, spaceMembers, newMember ->
                Resultat(tierId, spaceView, isCurrentUserOwner, spaceMembers, newMember)
            }.collect { (tierId, spaceView, isCurrentUserOwner, spaceMembers, newMember) ->
                proceedWithState(
                    tierId = tierId,
                    spaceView = spaceView,
                    spaceMembers = spaceMembers,
                    newMember = newMember,
                    isCurrentUserOwner = isCurrentUserOwner
                )
            }
        }
        sendAnalyticsInviteScreen()
        proceedWithUserPermissions(space = params.space)
        proceedWithSpaceMembers(space = params.space)
        proceedGettingNewMember()
        proceedWithGettingActiveTier()
    }

    private fun proceedWithGettingActiveTier() {
        viewModelScope.launch {
            membershipProvider.activeTier().collect { tierId ->
                _activeTier.value = tierId
            }
        }
    }

    private fun sendAnalyticsInviteScreen() {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenInviteConfirm,
                props = Props(
                    mapOf(EventsPropertiesKey.route to params.route)
                )
            )
        }
    }

    private fun proceedWithState(
        tierId: TierId,
        spaceView: ObjectWrapper.SpaceView,
        spaceMembers: List<ObjectWrapper.SpaceMember>,
        newMember: ObjectWrapper.SpaceMember?,
        isCurrentUserOwner: Boolean
    ) {
        Timber.d("proceedWithState, tierId: $tierId, spaceView: $spaceView, spaceMembers: $spaceMembers, newMember: $newMember, isCurrentUserOwner: $isCurrentUserOwner")
        val state = when (tierId.value) {
            EXPLORER_ID -> createExplorerState(spaceView, spaceMembers, newMember, isCurrentUserOwner)
            BUILDER_ID -> createBuilderState(spaceView, spaceMembers, newMember, isCurrentUserOwner)
            else -> createOtherState(spaceView, spaceMembers, newMember, isCurrentUserOwner)
        }
        _viewState.value = state
    }

    private fun createExplorerState(
        spaceView: ObjectWrapper.SpaceView,
        spaceMembers: List<ObjectWrapper.SpaceMember>,
        newMember: ObjectWrapper.SpaceMember?,
        isCurrentUserOwner: Boolean
    ): ViewState {
        val canAddReaders = spaceView.canAddReaders(isCurrentUserOwner, spaceMembers)
        val canAddWriters = spaceView.canAddWriters(isCurrentUserOwner, spaceMembers)

        return when {
            newMember == null -> ViewState.Error.EmptyMember
            !canAddReaders && !canAddWriters -> createViewStateWithButtons(spaceView, newMember, listOf(InviteButton.UPGRADE, InviteButton.REJECT))
            else -> createViewStateWithButtons(
                spaceView,
                newMember,
                buildList {
                    if (!canAddReaders) add(InviteButton.JOIN_AS_VIEWER_DISABLED) else add(InviteButton.JOIN_AS_VIEWER)
                    if (!canAddWriters) add(InviteButton.JOIN_AS_EDITOR_DISABLED) else add(InviteButton.JOIN_AS_EDITOR)
                    add(InviteButton.REJECT)
                }
            )
        }
    }

    private fun createBuilderState(
        spaceView: ObjectWrapper.SpaceView,
        spaceMembers: List<ObjectWrapper.SpaceMember>,
        newMember: ObjectWrapper.SpaceMember?,
        isCurrentUserOwner: Boolean
    ): ViewState {
        val canAddReaders = spaceView.canAddReaders(isCurrentUserOwner, spaceMembers)
        val canAddWriters = spaceView.canAddWriters(isCurrentUserOwner, spaceMembers)
        return when {
            newMember == null -> ViewState.Error.EmptyMember
            !canAddReaders && !canAddWriters -> createViewStateWithButtons(spaceView, newMember, listOf(InviteButton.ADD_MORE_VIEWERS, InviteButton.ADD_MORE_EDITORS, InviteButton.REJECT))
            else -> createViewStateWithButtons(
                spaceView,
                newMember,
                buildList {
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
        newMember: ObjectWrapper.SpaceMember?,
        isCurrentUserOwner: Boolean
    ): ViewState {
        val canAddReaders = spaceView.canAddReaders(isCurrentUserOwner, spaceMembers)
        val canAddWriters = spaceView.canAddWriters(isCurrentUserOwner, spaceMembers)
        return when {
            newMember == null -> ViewState.Error.EmptyMember
            !canAddReaders && !canAddWriters -> createViewStateWithButtons(spaceView, newMember, listOf(InviteButton.ADD_MORE_VIEWERS, InviteButton.ADD_MORE_EDITORS, InviteButton.REJECT))
            else -> createViewStateWithButtons(
                spaceView,
                newMember,
                buildList {
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
                obj = ObjectWrapper.SpaceMember(mapOf()),
                urlBuilder = urlBuilder
            ),
            buttons = buttons
        )
    }

    private fun proceedWithUserPermissions(space: SpaceId) {
        viewModelScope.launch {
            userPermissionProvider
                .observe(space = space)
                .collect { permission ->
                    _isCurrentUserOwner.value = permission == SpaceMemberPermissions.OWNER
                }
        }
    }

    private fun proceedWithSpaceMembers(space: SpaceId) {
        val searchMembersParams = SearchObjects.Params(
            filters = filterParticipants(
                spaces = listOf(space.id)
            ),
            keys = ObjectSearchConstants.spaceMemberKeys
        )
        viewModelScope.launch {
            searchObjects(searchMembersParams).proceed(
                failure = { Timber.e(it, "Error while fetching participants") },
                success = { result ->
                    val spaceMembers = result.toSpaceMembers()
                    _spaceMembers.value = spaceMembers
                    true
                }
            )
        }
    }

    private fun proceedGettingNewMember() {
        viewModelScope.launch {
            val filters = ObjectSearchConstants.filterNewMember(params.member)
            searchObjects(
                SearchObjects.Params(
                    filters = filters,
                    keys = ObjectSearchConstants.spaceMemberKeys,
                    limit = 1
                )
            ).proceed(
                failure = { Timber.e(it, "Error while fetching new member") },
                success = { result ->
                    val memberMap = result.firstOrNull()?.map
                    _newMember.value = if (memberMap.isNullOrEmpty()) {
                        null
                    } else {
                        ObjectWrapper.SpaceMember(memberMap)
                    }
                    true
                }
            )
        }
    }

    fun onRejectRequestClicked(newMember: Id) {
        Timber.d("onRejectRequestClicked, newMember: $newMember")
        viewModelScope.launch {
            declineSpaceJoinRequest.async(
                DeclineSpaceJoinRequest.Params(
                    space = params.space,
                    identity = newMember
                )
            ).fold(
                onSuccess = {
                    analytics.sendEvent(eventName = rejectInviteRequest)
                    isDismissed.value = true
                },
                onFailure = { e ->
                    Timber.e(e, "Error while rejecting join-space request").also {
                        sendToast(e.msg())
                    }
                }
            )
        }
    }

    private fun onJoinClicked(newMember: Id, permissions: SpaceMemberPermissions) {
        Timber.d("onJoinClicked, newMember: $newMember, permissions: $permissions")
        viewModelScope.launch {
            approveJoinSpaceRequest.async(
                ApproveJoinSpaceRequest.Params(
                    space = params.space,
                    identity = newMember,
                    permissions = permissions
                )
            ).fold(
                onSuccess = {
                    analytics.sendAnalyticsApproveInvite(permissions)
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

    fun onJoinAsReaderClicked(newMember: Id) {
        onJoinClicked(newMember, SpaceMemberPermissions.READER)
    }

    fun onJoinAsEditorClicked(newMember: Id) {
        onJoinClicked(newMember, SpaceMemberPermissions.WRITER)
    }

    fun onUpgradeClicked() {
        val activeTier = _activeTier.value
        val isPossibleToUpgrade = activeTier?.isPossibleToUpgradeNumberOfSpaceMembers()
        viewModelScope.launch {
            if (isPossibleToUpgrade == true) {
                _commands.value = Command.NavigateToMembership
            } else {
                //todo navigate to membership email screen
            }
        }
    }

    class Factory @Inject constructor(
        private val params: Params,
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
            params = params,
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

    data class Params(val space: SpaceId, val member: Id, val route: String)

    data class Resultat(
        val tierId: TierId,
        val spaceView: ObjectWrapper.SpaceView,
        val isCurrentUserOwner: Boolean,
        val spaceMembers: List<ObjectWrapper.SpaceMember>,
        val newMember: ObjectWrapper.SpaceMember?
    )

    sealed class ViewState {
        data object Init: ViewState()
        data class Success(
            val newMember: Id,
            val newMemberName: String,
            val spaceName: String,
            val icon: SpaceMemberIconView,
            val buttons: List<InviteButton>
        ): ViewState()
        sealed class Error: ViewState() {
            data object EmptyMember: Error()
        }
    }

    sealed class Command {
        data object NavigateToMembership: Command()
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