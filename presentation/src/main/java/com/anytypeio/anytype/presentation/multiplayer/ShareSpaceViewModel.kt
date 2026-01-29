package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.SharingSpacesTypes.shareTypeMoreInfo
import com.anytypeio.anytype.analytics.base.EventsDictionary.SharingSpacesTypes.shareTypeQR
import com.anytypeio.anytype.analytics.base.EventsDictionary.SharingSpacesTypes.shareTypeShareLink
import com.anytypeio.anytype.analytics.base.EventsDictionary.clickSettingsSpaceShare
import com.anytypeio.anytype.analytics.base.EventsDictionary.removeSpaceMember
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenSettingsSpaceMembers
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenSettingsSpaceShare
import com.anytypeio.anytype.analytics.base.EventsDictionary.shareSpace
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.ext.isPossibleToUpgradeNumberOfSpaceMembers
import com.anytypeio.anytype.core_models.membership.TierId
import com.anytypeio.anytype.core_models.multiplayer.InviteType
import com.anytypeio.anytype.core_models.multiplayer.MultiplayerError
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions.OWNER
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.SpaceMemberIconView
import com.anytypeio.anytype.core_models.ui.spaceIcon
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.invite.GetCurrentInviteAccessLevel
import com.anytypeio.anytype.domain.invite.SpaceInviteLinkStore
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ChangeSpaceInvitePermissions
import com.anytypeio.anytype.domain.multiplayer.ChangeSpaceMemberPermissions
import com.anytypeio.anytype.domain.multiplayer.CopyInviteLinkToClipboard
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.MakeSpaceShareable
import com.anytypeio.anytype.domain.multiplayer.RemoveSpaceMembers
import com.anytypeio.anytype.domain.multiplayer.RevokeSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.StopSharingSpace
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.multiplayer.sharedSpaceCount
import com.anytypeio.anytype.domain.`object`.canChangeReaderToWriter
import com.anytypeio.anytype.domain.`object`.canChangeWriterToReader
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsShareSpaceNewLink
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel.Command.ShareInviteLink
import com.anytypeio.anytype.presentation.multiplayer.SpaceMemberView.ActionType
import com.anytypeio.anytype.presentation.multiplayer.SpaceMemberView.ContextAction
import com.anytypeio.anytype.presentation.objects.toSpaceMembers
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.getSpaceMembersSearchParams
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.UiSpaceQrCodeState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class ShareSpaceViewModel(
    private val vmParams: VmParams,
    private val makeSpaceShareable: MakeSpaceShareable,
    private val generateSpaceInviteLink: GenerateSpaceInviteLink,
    private val revokeSpaceInviteLink: RevokeSpaceInviteLink,
    private val removeSpaceMembers: RemoveSpaceMembers,
    private val changeSpaceMemberPermissions: ChangeSpaceMemberPermissions,
    private val stopSharingSpace: StopSharingSpace,
    private val container: StorelessSubscriptionContainer,
    private val permissions: UserPermissionProvider,
    private val getAccount: GetAccount,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    private val membershipProvider: MembershipProvider,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val getCurrentInviteAccessLevel: GetCurrentInviteAccessLevel,
    private val copyInviteLinkToClipboard: CopyInviteLinkToClipboard,
    private val changeSpaceInvitePermissions: ChangeSpaceInvitePermissions,
    private val spaceInviteLinkStore: SpaceInviteLinkStore,
    private val gradientProvider: SpaceGradientProvider,
    private val stringResourceProvider: StringResourceProvider,
    private val profileContainer: ProfileSubscriptionManager
) : BaseViewModel() {

    private val _activeTier = MutableStateFlow<ActiveTierState>(ActiveTierState.Init)

    val members = MutableStateFlow<List<SpaceMemberView>>(emptyList())
    val commands = MutableSharedFlow<Command>()
    val isCurrentUserOwner = MutableStateFlow(false)
    val spaceLimitsState = MutableStateFlow<SpaceLimitsState>(SpaceLimitsState.Init)
    val isLoadingInProgress = MutableStateFlow(false)
    val shareSpaceErrors = MutableStateFlow<ShareSpaceErrors>(ShareSpaceErrors.Hidden)
    private var _spaceViews: ObjectWrapper.SpaceView? = null
    val uiQrCodeState = MutableStateFlow<UiSpaceQrCodeState>(UiSpaceQrCodeState.Hidden)

    // New state for invite link access levels (Task #24)
    val inviteLinkAccessLevel = MutableStateFlow<SpaceInviteLinkAccessLevel>(SpaceInviteLinkAccessLevel.LinkDisabled())
    val inviteLinkAccessLoading = MutableStateFlow(false)
    val inviteLinkConfirmationDialog = MutableStateFlow<SpaceInviteLinkAccessLevel?>(null)
    val spaceAccessType = MutableStateFlow<SpaceAccessType?>(null)
    val isMakePrivateEnabled = MutableStateFlow(false)

    init {
        Timber.i("Share-space init with params: $vmParams")
        proceedWithUserPermissions()
        proceedWithSubscriptions()
        proceedWithGettingActiveTier()
        proceedWithInviteLinkState()
        proceedWithMakePrivateEnabledState()
    }

    private fun proceedWithInviteLinkState() {
        viewModelScope.launch {
            combine(
                spaceInviteLinkStore.observe(vmParams.space)
                    .onStart {
                        Timber.d("Observing space invite link store for space: ${vmParams.space}")
                        proceedWithRequestCurrentInviteLink()
                    },
                spaceLimitsState
            ) { inviteLink, incentive ->
                inviteLink to incentive
            }
                .catch {
                    Timber.e(it, "Error while observing space invite link store")
                    inviteLinkAccessLevel.value =
                        SpaceInviteLinkAccessLevel.LinkDisabled(possibleToUpdate = false)
                }.collect { (inviteLink, incentiveState) ->
                    inviteLinkAccessLevel.value =
                        if (incentiveState is SpaceLimitsState.SharableLimit) {
                            SpaceInviteLinkAccessLevel.LinkDisabled(possibleToUpdate = false)
                        } else {
                            inviteLink
                        }
                }
        }
    }

    /**
     * Observes member list, ownership status, and space access type to determine
     * if the "Make Private" button should be enabled.
     * Enabled when: single member (owner) and space is currently SHARED.
     */
    private fun proceedWithMakePrivateEnabledState() {
        viewModelScope.launch {
            combine(
                flow = members,
                flow2 = isCurrentUserOwner,
                flow3 = spaceAccessType
            ) { membersList, isOwner, accessType ->
                membersList.size == 1 && isOwner && accessType == SpaceAccessType.SHARED
            }.collect { enabled ->
                isMakePrivateEnabled.value = enabled
            }
        }
    }

    private fun proceedWithUserPermissions() {
        viewModelScope.launch {
            permissions
                .observe(space = vmParams.space)
                .collect { permission ->
                    isCurrentUserOwner.value = permission == OWNER
                    if (permission == OWNER) {
                        analytics.sendEvent(eventName = screenSettingsSpaceShare)
                    } else {
                        analytics.sendEvent(eventName = screenSettingsSpaceMembers)
                    }
                }
        }
    }

    private fun proceedWithGettingActiveTier() {
        viewModelScope.launch {
            membershipProvider.activeTier()
                .catch { e ->
                    Timber.e(e, "Error while fetching active tier")
                }
                .collect { tierId ->
                    _activeTier.value = ActiveTierState.Success(tierId)
                }
        }
    }

    private fun proceedWithSubscriptions() {
        viewModelScope.launch {
            val spaceLimits = combine(
                spaceViews.sharedSpaceCount(permissions.all()),
                profileContainer
                    .observe()
                    .map { wrapper ->
                        wrapper.getValue<Double?>(Relations.SHARED_SPACES_LIMIT)?.toInt() ?: 0
                    },
            ) { sharedSpaceCount, sharedSpaceLimit ->
                sharedSpaceCount to sharedSpaceLimit
            }

            val account = getAccount.async(Unit).getOrNull()?.id
            val spaceViewFlow = spaceViews
                .observe()
                .mapNotNull { spaces ->
                    spaces.firstOrNull { it.targetSpaceId == vmParams.space.id }
                }
            val spaceMembersSearchParams = getSpaceMembersSearchParams(
                space = vmParams.space,
                subscription = SHARE_SPACE_MEMBER_SUBSCRIPTION
            )
            combine(
                spaceLimits,
                spaceViewFlow,
                container.subscribe(spaceMembersSearchParams),
                isCurrentUserOwner,
                _activeTier.filterIsInstance<ActiveTierState.Success>()
            ) { (sharedSpacesCount, sharedSpacesLimit), spaceView, membersResponse, isCurrentUserOwner, activeTier ->
                CombineResult(
                    isCurrentUserOwner = isCurrentUserOwner,
                    spaceView = spaceView,
                    tierId = activeTier.tierId,
                    spaceMembers = membersResponse.toSpaceMembers(),
                    sharedSpacesCount = sharedSpacesCount,
                    sharedSpacesLimit = sharedSpacesLimit
                )
            }.catch {
                Timber.e(
                    it, "Error while $SHARE_SPACE_MEMBER_SUBSCRIPTION " +
                            "and $SHARE_SPACE_SPACE_SUBSCRIPTION subscription"
                )
            }.onStart {
                isLoadingInProgress.value = true
            }.collect { result ->
                isLoadingInProgress.value = false
                _spaceViews = result.spaceView
                spaceAccessType.value = result.spaceView?.spaceAccessType
                val spaceView = result.spaceView
                val spaceMembers = result.spaceMembers
                    .sortedByDescending { it.status == ParticipantStatus.JOINING }

                members.value = spaceMembers.toSpaceMemberView(
                    spaceView = spaceView,
                    urlBuilder = urlBuilder,
                    isCurrentUserOwner = result.isCurrentUserOwner,
                    account = account,
                    stringResourceProvider = stringResourceProvider
                ).sortedByDescending { it.isUser }

                spaceLimitsState.value = spaceView.spaceLimitsState(
                    spaceMembers = spaceMembers,
                    isCurrentUserOwner = result.isCurrentUserOwner,
                    sharedSpaceCount = result.sharedSpacesCount,
                    sharedSpaceLimit = result.sharedSpacesLimit
                )
            }
        }
    }

    private suspend fun generateInviteLink(
        inviteType: InviteType? = null,
        permissions: SpaceMemberPermissions? = null
    ) {
        generateSpaceInviteLink.async(
            params = GenerateSpaceInviteLink.Params(
                space = vmParams.space,
                inviteType = inviteType,
                permissions = permissions
            )
        ).fold(
            onSuccess = { inviteLink ->
                Timber.d("Successfully generated invite link, link: ${inviteLink.scheme}")

                // Analytics Event #1: ClickShareSpaceNewLink with type property
                analytics.sendAnalyticsShareSpaceNewLink(
                    inviteType = inviteType,
                    permissions = permissions
                )

                proceedWithRequestCurrentInviteLink()
                // Reset loading state and close confirmation dialog after successful generation
                inviteLinkAccessLoading.value = false
                inviteLinkConfirmationDialog.value = null
            },
            onFailure = {
                Timber.e(it, "Error while generating invite link")
                inviteLinkAccessLoading.value = false
                proceedWithMultiplayerError(it)
            }
        )
    }

    fun onShareInviteLinkClicked(link: String) {
        Timber.d("onShareInviteLinkClicked, link: $link")
        viewModelScope.launch {
            commands.emit(ShareInviteLink(link))

            // Analytics Event #6: ClickShareSpaceShareLink
            analytics.sendEvent(eventName = EventsDictionary.clickShareSpaceShareLink)

            analytics.sendEvent(
                eventName = clickSettingsSpaceShare,
                props = Props(
                    mapOf(EventsPropertiesKey.type to shareTypeShareLink)
                )
            )
        }
    }

    fun onShareQrCodeClicked(link: String, route: String = EventsDictionary.ScreenQrRoutes.INVITE_LINK) {
        Timber.d("onShareQrCodeClicked, link: $link, route: $route")
        viewModelScope.launch {
            val spaceView = _spaceViews ?: return@launch
            uiQrCodeState.value = UiSpaceQrCodeState.SpaceInvite(
                link = link,
                spaceName = spaceView.name.orEmpty(),
                icon = spaceView.spaceIcon(urlBuilder)
            )

            // Analytics Event #3: ScreenQr with route property
            analytics.sendEvent(
                eventName = EventsDictionary.screenQr,
                props = Props(
                    mapOf(EventsPropertiesKey.route to route)
                )
            )

            analytics.sendEvent(
                eventName = clickSettingsSpaceShare,
                props = Props(
                    mapOf(EventsPropertiesKey.type to shareTypeQR)
                )
            )
        }
    }

    fun onViewRequestClicked(view: SpaceMemberView) {
        Timber.d("onViewRequestClicked, view: [$view]")
        viewModelScope.launch {
            commands.emit(
                Command.ViewJoinRequest(
                    space = vmParams.space,
                    member = view.obj.id
                )
            )
        }
    }

    /**
     * Changes space member permissions to the specified permission level.
     * Consolidates the logic for both viewer and editor permission changes.
     */
    private fun changeParticipantPermissions(
        spaceMemberView: SpaceMemberView,
        targetPermission: SpaceMemberPermissions,
        analyticsType: String
    ) {
        Timber.d("changeParticipantPermissions, view: [$spaceMemberView], targetPermission: $targetPermission")

        if (!spaceMemberView.canReadEnabled) {
            Timber.w("Can't change permissions")
            viewModelScope.launch {
                commands.emit(Command.ToastPermission)
            }
            return
        }

        viewModelScope.launch {
            // Only change if the current permission is different from target
            if (spaceMemberView.obj.permissions != targetPermission) {
                changeSpaceMemberPermissions.async(
                    ChangeSpaceMemberPermissions.Params(
                        space = vmParams.space,
                        identity = spaceMemberView.obj.identity,
                        permission = targetPermission
                    )
                ).fold(
                    onFailure = { e ->
                        Timber.e(e, "Error while changing member permissions")
                        proceedWithMultiplayerError(e)
                    },
                    onSuccess = {
                        Timber.d("Successfully updated space member permissions")
                        analytics.sendEvent(
                            eventName = EventsDictionary.changeSpaceMemberPermissions,
                            props = Props(
                                mapOf(EventsPropertiesKey.type to analyticsType)
                            )
                        )
                    }
                )
            }
        }
    }

    fun onProceedWithChangingParticipantToEditor(view: SpaceMemberView) {
        changeParticipantPermissions(
            spaceMemberView = view,
            targetPermission = SpaceMemberPermissions.WRITER,
            analyticsType = "Write"
        )
    }

    fun onProceedWithChangingParticipantToViewer(view: SpaceMemberView) {
        changeParticipantPermissions(
            spaceMemberView = view,
            targetPermission = SpaceMemberPermissions.READER,
            analyticsType = "Read"
        )
    }

    fun onRemoveMemberClicked(
        view: SpaceMemberView
    ) {
        Timber.d("onRemoveMemberClicked")
        viewModelScope.launch {
            viewModelScope.launch {
                commands.emit(
                    Command.ShowRemoveMemberWarning(
                        identity = view.obj.identity,
                        name = view.obj.name.orEmpty()
                    )
                )
            }
        }
    }

    fun onRemoveMemberAccepted(identity: Id) {
        Timber.d("onRemoveMemberAccepted: Starting member removal process for identity: $identity")
        viewModelScope.launch {
            try {
                removeSpaceMembers.async(
                    RemoveSpaceMembers.Params(
                        space = vmParams.space,
                        identities = listOf(identity)
                    )
                ).fold(
                    onFailure = { e ->
                        Timber.e(
                            e,
                            "Error while removing space member (identity: $identity, space: ${vmParams.space})"
                        )
                        when (e) {
                            is java.net.SocketTimeoutException,
                            is java.net.UnknownHostException,
                            is java.io.IOException -> {
                                sendToast("Network error occurred. Please check your connection and try again.")
                            }

                            else -> proceedWithMultiplayerError(e)
                        }
                    },
                    onSuccess = {
                        Timber.d("Successfully removed space member (identity: $identity, space: ${vmParams.space})")
                        analytics.sendEvent(eventName = removeSpaceMember)
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error while removing space member")
                sendToast("An unexpected error occurred. Please try again.")
            }
        }
    }

    fun onMemberClicked(member: ObjectWrapper.SpaceMember) {
        Timber.d("onMemberClicked, member: $member")
        viewModelScope.launch {
            commands.emit(
                Command.OpenParticipantObject(
                    objectId = member.id,
                    space = vmParams.space
                )
            )
        }
    }

    /**
     * Handles context action clicks using the unified ActionType approach
     */
    fun onContextActionClicked(view: SpaceMemberView, actionType: ActionType) {
        Timber.d("onContextActionClicked, view: [$view], actionType: $actionType")
        when (actionType) {
            ActionType.CAN_VIEW -> onProceedWithChangingParticipantToViewer(view)
            ActionType.CAN_EDIT -> onProceedWithChangingParticipantToEditor(view)
            ActionType.REMOVE_MEMBER -> onRemoveMemberClicked(view)
            ActionType.VIEW_REQUEST -> onViewRequestClicked(view)
        }
    }

    fun onIncentiveClicked() {
        Timber.d("onIncentiveClicked")
        val activeTier = (_activeTier.value as? ActiveTierState.Success) ?: return
        val isPossibleToUpgrade = activeTier.tierId.isPossibleToUpgradeNumberOfSpaceMembers()
        viewModelScope.launch {
            if (isPossibleToUpgrade) {
                commands.emit(Command.ShowMembershipScreen)
            } else {
                commands.emit(Command.ShowMembershipUpgradeScreen)
            }
        }
    }

    fun onManageSpacesClicked() {
        Timber.d("onManageSpacesClicked")
        viewModelScope.launch {
            commands.emit(Command.ShowManageSpacesScreen)
        }
    }

    //region Invite Link Access Level
    /**
     * Called when user selects a new invite link access level
     */
    fun onInviteLinkAccessLevelSelected(newLevel: SpaceInviteLinkAccessLevel) {
        val currentLevel = inviteLinkAccessLevel.value
        Timber.d("onInviteLinkAccessLevelSelected, new level: $newLevel, current level: $currentLevel")
        if (currentLevel.needsConfirmationToChangeTo(newLevel)) {
            inviteLinkConfirmationDialog.value = newLevel
        } else {
            updateInviteLinkAccessLevel(newLevel)
        }
    }

    /**
     * Called when user confirms changing the invite link access level
     */
    fun onInviteLinkAccessChangeConfirmed() {
        Timber.d("onInviteLinkAccessChangeConfirmed")
        val newLevel = inviteLinkConfirmationDialog.value ?: return
        updateInviteLinkAccessLevel(newLevel)
    }

    /**
     * Called when user cancels the confirmation dialog
     */
    fun onInviteLinkAccessChangeCancel() {
        Timber.d("onInviteLinkAccessChangeCancel")
        inviteLinkConfirmationDialog.value = null
    }

    fun onCopyInviteLinkClicked(link: String, route: String = EventsDictionary.CopyLinkRoutes.BUTTON) {
        Timber.d("onCopyInviteLinkClicked, link: $link, route: $route")
        viewModelScope.launch {
            try {
                copyInviteLinkToClipboard.run(
                    CopyInviteLinkToClipboard.Params(link)
                )

                // Analytics Event #4: ClickShareSpaceCopyLink with route property
                analytics.sendEvent(
                    eventName = EventsDictionary.clickShareSpaceCopyLink,
                    props = Props(
                        mapOf(EventsPropertiesKey.route to route)
                    )
                )

                sendToast("Invite link copied to clipboard")
            } catch (error: Exception) {
                Timber.e(error, "Failed to copy invite link to clipboard")
            }
        }
    }

    fun onMakePrivateClicked() {
        Timber.d("onMakePrivateClicked: Making space private")
        viewModelScope.launch {
            isLoadingInProgress.value = true

            // First, revoke invite link if it exists and is not disabled
            val currentInviteLink = inviteLinkAccessLevel.value
            if (currentInviteLink !is SpaceInviteLinkAccessLevel.LinkDisabled) {
                Timber.d("Revoking active invite link before making space private")
                revokeSpaceInviteLink.async(vmParams.space).fold(
                    onSuccess = {
                        Timber.d("Successfully revoked invite link")
                        proceedWithMakingSpacePrivate()
                    },
                    onFailure = { error ->
                        Timber.e(error, "Failed to revoke invite link, proceeding with making space private anyway")
                        // Proceed anyway - the main goal is to make space private
                        proceedWithMakingSpacePrivate()
                    }
                )
            } else {
                // No active invite link, proceed directly
                proceedWithMakingSpacePrivate()
            }
        }
    }

    private suspend fun proceedWithMakingSpacePrivate() {
        stopSharingSpace.async(vmParams.space).fold(
            onSuccess = {
                Timber.d("Successfully made space private")
                isLoadingInProgress.value = false
                viewModelScope.launch {
                    commands.emit(Command.Dismiss)
                }
            },
            onFailure = { error ->
                Timber.e(error, "Failed to make space private")
                isLoadingInProgress.value = false
                shareSpaceErrors.value = ShareSpaceErrors.MakePrivateFailed
            }
        )
    }

    private fun updateInviteLinkAccessLevel(newLevel: SpaceInviteLinkAccessLevel) {
        if (!inSpaceSharable()) {
            viewModelScope.launch {
                makeSpaceShareable(
                    space = vmParams.space,
                    actionSuccess = {
                        analytics.sendEvent(eventName = shareSpace)
                        proceedWithUpdatingInviteLink(newLevel)
                    },
                    actionFailure = { proceedWithMultiplayerError(it) }
                )
            }
        } else {
            viewModelScope.launch {
                proceedWithUpdatingInviteLink(newLevel)
            }
        }
    }

    private suspend fun proceedWithUpdatingInviteLink(newLevel: SpaceInviteLinkAccessLevel) {
        inviteLinkAccessLoading.value = true
        val currentLevel = inviteLinkAccessLevel.value
        val space = vmParams.space

        when (newLevel) {

            is SpaceInviteLinkAccessLevel.LinkDisabled -> {
                revokeSpaceInviteLink.async(space).fold(
                    onSuccess = {
                        Timber.d("Successfully disabled invite link")
                        proceedWithRequestCurrentInviteLink()
                        inviteLinkAccessLoading.value = false
                        inviteLinkConfirmationDialog.value = null
                    },
                    onFailure = {
                        Timber.e(it, "Failed to disable invite link")
                        inviteLinkAccessLoading.value = false
                        proceedWithMultiplayerError(it)
                    }
                )
            }

            is SpaceInviteLinkAccessLevel.EditorAccess -> {
                when (currentLevel) {
                    is SpaceInviteLinkAccessLevel.LinkDisabled -> {
                        generateInviteLink(
                            inviteType = InviteType.WITHOUT_APPROVE,
                            permissions = SpaceMemberPermissions.WRITER
                        )
                    }

                    is SpaceInviteLinkAccessLevel.EditorAccess -> {
                        Timber.d("Invite link already has EDITOR access")
                        inviteLinkAccessLoading.value = false
                        inviteLinkConfirmationDialog.value = null
                        return
                    }

                    is SpaceInviteLinkAccessLevel.ViewerAccess -> {
                        proceedWithUpdateLink(
                            space = space,
                            newLevel = newLevel,
                            onSuccess = {
                                proceedWithRequestCurrentInviteLink()
                                inviteLinkAccessLoading.value = false
                                inviteLinkConfirmationDialog.value = null
                            },
                            onFailure = { error ->
                                inviteLinkAccessLoading.value = false
                                proceedWithMultiplayerError(error)
                            }
                        )
                    }

                    is SpaceInviteLinkAccessLevel.RequestAccess -> {
                        revokeSpaceInviteLink.async(space).fold(
                            onSuccess = {
                                Timber.d("Successfully revoked invite link for REQUEST_ACCESS")
                                generateInviteLink(
                                    inviteType = InviteType.WITHOUT_APPROVE,
                                    permissions = SpaceMemberPermissions.WRITER
                                )
                            },
                            onFailure = {
                                Timber.e(it, "Failed to revoke invite link for REQUEST_ACCESS")
                                inviteLinkAccessLoading.value = false
                                proceedWithMultiplayerError(it)
                            }
                        )
                    }
                }
            }

            is SpaceInviteLinkAccessLevel.ViewerAccess -> {
                when (currentLevel) {
                    is SpaceInviteLinkAccessLevel.LinkDisabled -> {
                        generateInviteLink(
                            inviteType = InviteType.WITHOUT_APPROVE,
                            permissions = SpaceMemberPermissions.READER
                        )
                    }

                    is SpaceInviteLinkAccessLevel.EditorAccess -> {
                        proceedWithUpdateLink(
                            space = space,
                            newLevel = newLevel,
                            onSuccess = {
                                proceedWithRequestCurrentInviteLink()
                                inviteLinkAccessLoading.value = false
                                inviteLinkConfirmationDialog.value = null
                            },
                            onFailure = { error ->
                                inviteLinkAccessLoading.value = false
                                proceedWithMultiplayerError(error)
                            }
                        )
                    }

                    is SpaceInviteLinkAccessLevel.ViewerAccess -> {
                        Timber.d("Invite link already has VIEWER access")
                        inviteLinkAccessLoading.value = false
                        inviteLinkConfirmationDialog.value = null
                        return
                    }

                    is SpaceInviteLinkAccessLevel.RequestAccess -> {
                        revokeSpaceInviteLink.async(space).fold(
                            onSuccess = {
                                Timber.d("Successfully revoked invite link for REQUEST_ACCESS")
                                generateInviteLink(
                                    inviteType = InviteType.WITHOUT_APPROVE,
                                    permissions = SpaceMemberPermissions.READER
                                )
                            },
                            onFailure = {
                                Timber.e(it, "Failed to revoke invite link for REQUEST_ACCESS")
                                inviteLinkAccessLoading.value = false
                                proceedWithMultiplayerError(it)
                            }
                        )
                    }
                }
            }

            is SpaceInviteLinkAccessLevel.RequestAccess -> {
                when (currentLevel) {
                    is SpaceInviteLinkAccessLevel.LinkDisabled -> {
                        generateInviteLink()
                    }

                    is SpaceInviteLinkAccessLevel.EditorAccess -> {
                        generateInviteLink()
                    }

                    is SpaceInviteLinkAccessLevel.ViewerAccess -> {
                        generateInviteLink()
                    }

                    is SpaceInviteLinkAccessLevel.RequestAccess -> {
                        Timber.d("Invite link already has REQUEST_ACCESS")
                        inviteLinkAccessLoading.value = false
                        inviteLinkConfirmationDialog.value = null
                        return
                    }
                }
            }
        }
    }

    private fun inSpaceSharable(): Boolean {
        return _spaceViews?.spaceAccessType == SpaceAccessType.SHARED
    }

    private suspend fun proceedWithUpdateLink(
        space: SpaceId,
        newLevel: SpaceInviteLinkAccessLevel,
        onSuccess: suspend () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val params = ChangeSpaceInvitePermissions.Params(
            space = space,
            permissions = when (newLevel) {
                is SpaceInviteLinkAccessLevel.EditorAccess ->
                    SpaceMemberPermissions.WRITER

                is SpaceInviteLinkAccessLevel.LinkDisabled ->
                    SpaceMemberPermissions.NO_PERMISSIONS

                is SpaceInviteLinkAccessLevel.RequestAccess ->
                    SpaceMemberPermissions.NO_PERMISSIONS

                is SpaceInviteLinkAccessLevel.ViewerAccess ->
                    SpaceMemberPermissions.READER
            }
        )
        changeSpaceInvitePermissions.async(params).fold(
            onSuccess = {
                Timber.d("Successfully updated invite link permissions to: $newLevel")
                onSuccess()
            },
            onFailure = {
                Timber.e(it, "Failed to update invite link permissions")
                onFailure(it)
            }
        )
    }

    private suspend fun makeSpaceShareable(
        space: SpaceId,
        actionSuccess: suspend () -> Unit,
        actionFailure: suspend (Throwable) -> Unit
    ) {
        makeSpaceShareable.async(space).fold(
            onSuccess = {
                Timber.d("Successfully made space shareable")
                actionSuccess()
            },
            onFailure = {
                Timber.e(it, "Error while making space shareable")
                actionFailure(it)
            }
        )
    }

    private suspend fun proceedWithRequestCurrentInviteLink() {
        val params = GetCurrentInviteAccessLevel.Params(space = vmParams.space)
        getCurrentInviteAccessLevel.async(params).getOrNull()
    }

    //endregion

    fun onMoreInfoClicked() {
        viewModelScope.launch {
            commands.emit(Command.ShowHowToShareSpace)
            analytics.sendEvent(
                eventName = clickSettingsSpaceShare,
                props = Props(
                    mapOf(EventsPropertiesKey.type to shareTypeMoreInfo)
                )
            )
        }
    }

    private fun proceedWithMultiplayerError(error: Throwable) {
        if (error is MultiplayerError) {
            when (error) {
                is MultiplayerError.Generic.LimitReached -> {
                    shareSpaceErrors.value = ShareSpaceErrors.LimitReached
                }

                is MultiplayerError.Generic.NotShareable -> {
                    shareSpaceErrors.value = ShareSpaceErrors.NotShareable
                }

                is MultiplayerError.Generic.RequestFailed -> {
                    shareSpaceErrors.value = ShareSpaceErrors.RequestFailed
                }

                is MultiplayerError.Generic.SpaceIsDeleted -> {
                    shareSpaceErrors.value = ShareSpaceErrors.SpaceIsDeleted
                }

                is MultiplayerError.Generic.IncorrectPermissions -> {
                    shareSpaceErrors.value = ShareSpaceErrors.IncorrectPermissions
                }

                is MultiplayerError.Generic.NoSuchSpace -> {
                    shareSpaceErrors.value = ShareSpaceErrors.NoSuchSpace
                }
            }
        } else {
            shareSpaceErrors.value = ShareSpaceErrors.Error(error.message ?: "Unknown error")
        }
    }

    fun dismissShareSpaceErrors() {
        shareSpaceErrors.value = ShareSpaceErrors.Hidden
    }

    fun onHideQrCodeScreen() {
        uiQrCodeState.value = UiSpaceQrCodeState.Hidden
    }

    override fun onCleared() {
        viewModelScope.launch {
            container.unsubscribe(
                subscriptions = listOf(
                    SHARE_SPACE_MEMBER_SUBSCRIPTION,
                    SHARE_SPACE_SPACE_SUBSCRIPTION
                )
            )
        }
        super.onCleared()
    }

    class Factory @Inject constructor(
        private val params: VmParams,
        private val makeSpaceShareable: MakeSpaceShareable,
        private val generateSpaceInviteLink: GenerateSpaceInviteLink,
        private val revokeSpaceInviteLink: RevokeSpaceInviteLink,
        private val changeSpaceMemberPermissions: ChangeSpaceMemberPermissions,
        private val getAccount: GetAccount,
        private val removeSpaceMembers: RemoveSpaceMembers,
        private val stopSharingSpace: StopSharingSpace,
        private val container: StorelessSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val permissions: UserPermissionProvider,
        private val analytics: Analytics,
        private val membershipProvider: MembershipProvider,
        private val spaceViews: SpaceViewSubscriptionContainer,
        private val getCurrentInviteAccessLevel: GetCurrentInviteAccessLevel,
        private val copyInviteLinkToClipboard: CopyInviteLinkToClipboard,
        private val changeSpaceInvitePermissions: ChangeSpaceInvitePermissions,
        private val spaceInviteLinkStore: SpaceInviteLinkStore,
        private val gradientProvider: SpaceGradientProvider,
        private val stringResourceProvider: StringResourceProvider,
        private val profileContainer: ProfileSubscriptionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShareSpaceViewModel(
            vmParams = params,
            generateSpaceInviteLink = generateSpaceInviteLink,
            revokeSpaceInviteLink = revokeSpaceInviteLink,
            changeSpaceMemberPermissions = changeSpaceMemberPermissions,
            removeSpaceMembers = removeSpaceMembers,
            stopSharingSpace = stopSharingSpace,
            container = container,
            urlBuilder = urlBuilder,
            getAccount = getAccount,
            permissions = permissions,
            makeSpaceShareable = makeSpaceShareable,
            analytics = analytics,
            membershipProvider = membershipProvider,
            spaceViews = spaceViews,
            getCurrentInviteAccessLevel = getCurrentInviteAccessLevel,
            copyInviteLinkToClipboard = copyInviteLinkToClipboard,
            changeSpaceInvitePermissions = changeSpaceInvitePermissions,
            spaceInviteLinkStore = spaceInviteLinkStore,
            gradientProvider = gradientProvider,
            stringResourceProvider = stringResourceProvider,
            profileContainer = profileContainer
        ) as T
    }

    data class VmParams(
        val space: SpaceId
    )

    sealed class ShareLinkViewState {
        data object Init : ShareLinkViewState()
        data object NotGenerated : ShareLinkViewState()
        data class Shared(val link: String) : ShareLinkViewState()
    }

    sealed class Command {
        data class ShareInviteLink(val link: String) : Command()
        data class ViewJoinRequest(val space: SpaceId, val member: Id) : Command()
        data class ShowRemoveMemberWarning(val identity: Id, val name: String) : Command()
        data class ShowMultiplayerError(val error: MultiplayerError.Generic) : Command()
        data object ShowHowToShareSpace : Command()
        data object ToastPermission : Command()
        data object Dismiss : Command()
        data object ShowMembershipScreen : Command()
        data object ShowMembershipUpgradeScreen : Command()
        data class OpenParticipantObject(val objectId: Id, val space: SpaceId) : Command()
        data object ShowManageSpacesScreen : Command()
    }

    companion object {
        const val SHARE_SPACE_MEMBER_SUBSCRIPTION = "share-space-subscription.member"
        const val SHARE_SPACE_SPACE_SUBSCRIPTION = "share-space-subscription.space"
    }

    sealed class UiEvent {
        sealed class AccessChange : UiEvent() {
            data object Editor : AccessChange()
            data object Viewer : AccessChange()
            data object Request : AccessChange()
            data object Disabled : AccessChange()
        }
    }

    //Active membership status of the current user
    sealed class ActiveTierState {
        data object Init : ActiveTierState()
        data class Success(val tierId: TierId) : ActiveTierState()
    }

    data class CombineResult(
        val isCurrentUserOwner: Boolean,
        val spaceView: ObjectWrapper.SpaceView,
        val tierId: TierId,
        val spaceMembers: List<ObjectWrapper.SpaceMember>,
        val sharedSpacesCount: Int,
        val sharedSpacesLimit: Int
    )
}

data class SpaceMemberView(
    val obj: ObjectWrapper.SpaceMember,
    val icon: SpaceMemberIconView,
    val canReadEnabled: Boolean = false,
    val canEditEnabled: Boolean = false,
    val isUser: Boolean = false,
    val statusText: String? = null,
    val contextActions: List<ContextAction> = emptyList()
) {

    data class ContextAction(
        val title: String,
        val isSelected: Boolean = false,
        val isDestructive: Boolean = false,
        val isEnabled: Boolean = true,
        val actionType: ActionType
    )

    enum class ActionType {
        CAN_VIEW,
        CAN_EDIT,
        REMOVE_MEMBER,
        VIEW_REQUEST
    }
}

/**
 * Data class to hold both participant info (status text and context actions).
 */
data class ParticipantInfo(
    val statusText: String? = null,
    val contextActions: List<ContextAction> = emptyList()
)

/**
 * Get participant info including status text and context actions based on their status and permissions.
 * This method combines the logic for generating both status text and context actions.
 */
private fun ObjectWrapper.SpaceMember.getParticipantInfo(
    canChangeWriterToReader: Boolean,
    canChangeReaderToWriter: Boolean,
    isCurrentUserOwner: Boolean,
    stringResourceProvider: StringResourceProvider
): ParticipantInfo {
    return when (status) {
        ParticipantStatus.ACTIVE -> {
            // Status text shows permission level for active participants
            val statusText = when (permissions) {
                SpaceMemberPermissions.READER -> stringResourceProvider.getMultiplayerViewer()
                SpaceMemberPermissions.WRITER -> stringResourceProvider.getMultiplayerEditor()
                SpaceMemberPermissions.OWNER -> stringResourceProvider.getMultiplayerOwner()
                SpaceMemberPermissions.NO_PERMISSIONS -> stringResourceProvider.getMultiplayerNoPermissions()
                null -> null
            }

            // Context actions are only available for non-owners when current user is owner
            val contextActions = if (isCurrentUserOwner && permissions != SpaceMemberPermissions.OWNER) {
                buildList {
                    // Change to Viewer action
                    add(
                        ContextAction(
                            title = stringResourceProvider.getMultiplayerViewer(),
                            isSelected = permissions == SpaceMemberPermissions.READER,
                            isDestructive = false,
                            isEnabled = canChangeWriterToReader || permissions == SpaceMemberPermissions.READER,
                            actionType = ActionType.CAN_VIEW
                        )
                    )

                    // Change to Editor action
                    add(
                        ContextAction(
                            title = stringResourceProvider.getMultiplayerEditor(),
                            isSelected = permissions == SpaceMemberPermissions.WRITER,
                            isDestructive = false,
                            isEnabled = canChangeReaderToWriter || permissions == SpaceMemberPermissions.WRITER,
                            actionType = ActionType.CAN_EDIT
                        )
                    )

                    // Remove Member action
                    add(
                        ContextAction(
                            title = stringResourceProvider.getMultiplayerRemoveMember(),
                            isSelected = false,
                            isDestructive = true,
                            isEnabled = true,
                            actionType = ActionType.REMOVE_MEMBER
                        )
                    )
                }
            } else {
                emptyList()
            }

            ParticipantInfo(statusText = statusText, contextActions = contextActions)
        }

        ParticipantStatus.JOINING -> {
            // Status text varies based on viewer's permissions
            val statusText = if (isCurrentUserOwner) {
                stringResourceProvider.getMultiplayerApproveRequest()
            } else {
                stringResourceProvider.getMultiplayerPending()
            }

            // Context actions only for those who can approve
            val contextActions = if (isCurrentUserOwner) {
                listOf(
                    ContextAction(
                        title = stringResourceProvider.getMultiplayerViewRequest(),
                        isSelected = false,
                        isDestructive = false,
                        isEnabled = true,
                        actionType = ActionType.VIEW_REQUEST
                    )
                )
            } else {
                emptyList()
            }

            ParticipantInfo(statusText = statusText, contextActions = contextActions)
        }

        ParticipantStatus.REMOVING -> {
            val statusText = stringResourceProvider.getMultiplayerLeaveRequest()

            val contextActions = if (isCurrentUserOwner) {
                listOf(
                    ContextAction(
                        title = stringResourceProvider.getMultiplayerApprove(),
                        isSelected = false,
                        isDestructive = false,
                        isEnabled = true,
                        actionType = ActionType.VIEW_REQUEST
                    )
                )
            } else {
                emptyList()
            }

            ParticipantInfo(statusText = statusText, contextActions = contextActions)
        }

        ParticipantStatus.DECLINED,
        ParticipantStatus.CANCELLED,
        ParticipantStatus.REMOVED,
        null -> {
            // These statuses should not be shown
            ParticipantInfo(statusText = null, contextActions = emptyList())
        }
    }
}

fun List<ObjectWrapper.SpaceMember>.toSpaceMemberView(
    spaceView: ObjectWrapper.SpaceView,
    urlBuilder: UrlBuilder,
    isCurrentUserOwner: Boolean,
    account: Id? = null,
    stringResourceProvider: StringResourceProvider
): List<SpaceMemberView> = mapNotNull { spaceMember ->

    val canChangeReaderToWriter = spaceView.canChangeReaderToWriter(participants = this)
    val canChangeWriterToReader = spaceView.canChangeWriterToReader(participants = this)

    val isUser = spaceMember.identity == account

    val icon = SpaceMemberIconView.icon(
        obj = spaceMember,
        urlBuilder = urlBuilder
    )

    // Generate participant info (status text and context actions)
    val participantInfo = spaceMember.getParticipantInfo(
        canChangeWriterToReader = canChangeWriterToReader,
        canChangeReaderToWriter = canChangeReaderToWriter,
        isCurrentUserOwner = isCurrentUserOwner,
        stringResourceProvider = stringResourceProvider
    )

    val statusText = participantInfo.statusText
    val contextActions = participantInfo.contextActions
    when (spaceMember.status) {
        ParticipantStatus.ACTIVE -> {
            SpaceMemberView(
                obj = spaceMember,
                icon = icon,
                canReadEnabled = canChangeWriterToReader,
                canEditEnabled = canChangeReaderToWriter,
                isUser = isUser,
                statusText = statusText,
                contextActions = contextActions
            )
        }

        ParticipantStatus.JOINING -> {
            // Only show join requests to space owners
            if (isCurrentUserOwner) {
                SpaceMemberView(
                    obj = spaceMember,
                    icon = icon,
                    isUser = isUser,
                    statusText = statusText,
                    contextActions = contextActions
                )
            } else {
                // Hide join requests from non-owners (Editors/Viewers)
                null
            }
        }

        ParticipantStatus.REMOVING -> {
            if (isCurrentUserOwner) {
                SpaceMemberView(
                    obj = spaceMember,
                    icon = icon,
                    isUser = isUser,
                    statusText = statusText,
                    contextActions = contextActions
                )
            } else {
                // Hide leave requests from non-owners (Editors/Viewers)
                null
            }
        }

        ParticipantStatus.REMOVED -> null
        ParticipantStatus.DECLINED -> null
        ParticipantStatus.CANCELLED -> null
        null -> null
    }
}

sealed class ShareSpaceErrors {
    data object Hidden : ShareSpaceErrors()
    data object LimitReached : ShareSpaceErrors()
    data object NotShareable : ShareSpaceErrors()
    data object RequestFailed : ShareSpaceErrors()
    data object SpaceIsDeleted : ShareSpaceErrors()
    data object IncorrectPermissions : ShareSpaceErrors()
    data object NoSuchSpace : ShareSpaceErrors()
    data object MakePrivateFailed : ShareSpaceErrors()
    data class Error(val msg: String) : ShareSpaceErrors()
}

sealed class SpaceLimitsState {
    data object Init : SpaceLimitsState()
    data class ViewersLimit(val count: Int) : SpaceLimitsState()
    data class EditorsLimit(val count: Int) : SpaceLimitsState()
    data class SharableLimit(val count: Int) : SpaceLimitsState()
}