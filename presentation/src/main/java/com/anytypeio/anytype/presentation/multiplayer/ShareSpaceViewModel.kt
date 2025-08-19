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
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
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
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ChangeSpaceInvitePermissions
import com.anytypeio.anytype.domain.multiplayer.ChangeSpaceMemberPermissions
import com.anytypeio.anytype.domain.multiplayer.CopyInviteLinkToClipboard
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.domain.invite.GetCurrentInviteAccessLevel
import com.anytypeio.anytype.domain.invite.SpaceInviteLinkStore
import com.anytypeio.anytype.domain.multiplayer.MakeSpaceShareable
import com.anytypeio.anytype.domain.multiplayer.RemoveSpaceMembers
import com.anytypeio.anytype.domain.multiplayer.RevokeSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel.Command.ShareInviteLink
import com.anytypeio.anytype.presentation.objects.SpaceMemberIconView
import com.anytypeio.anytype.presentation.objects.toSpaceMembers
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.getSpaceMembersSearchParams
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
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
    private val spaceInviteLinkStore: SpaceInviteLinkStore
) : BaseViewModel() {

    private val _activeTier = MutableStateFlow<ActiveTierState>(ActiveTierState.Init)

    val members = MutableStateFlow<List<ShareSpaceMemberView>>(emptyList())
    val commands = MutableSharedFlow<Command>()
    val isCurrentUserOwner = MutableStateFlow(false)
    val showIncentive = MutableStateFlow<ShareSpaceIncentiveState>(ShareSpaceIncentiveState.Hidden)
    val isLoadingInProgress = MutableStateFlow(false)
    val shareSpaceErrors = MutableStateFlow<ShareSpaceErrors>(ShareSpaceErrors.Hidden)
    private var _spaceViews: ObjectWrapper.SpaceView? = null

    // New state for invite link access levels (Task #24)
    val inviteLinkAccessLevel = MutableStateFlow<SpaceInviteLinkAccessLevel>(SpaceInviteLinkAccessLevel.LinkDisabled)
    val inviteLinkAccessLoading = MutableStateFlow(false)
    val inviteLinkConfirmationDialog = MutableStateFlow<SpaceInviteLinkAccessLevel?>(null)

    init {
        Timber.i("Share-space init with params: $vmParams")
        proceedWithUserPermissions(space = vmParams.space)
        proceedWithSubscriptions()
        proceedWithGettingActiveTier()
        viewModelScope.launch {
            spaceInviteLinkStore.observe(vmParams.space)
                .onStart {
                    Timber.d("Observing space invite link store for space: ${vmParams.space}")
                    proceedWithRequestCurrentInviteLink()
                }
                .catch {
                    Timber.e(it, "Error while observing space invite link store")
                    inviteLinkAccessLevel.value = SpaceInviteLinkAccessLevel.LinkDisabled
                }
                .collect { inviteLink ->
                    inviteLinkAccessLevel.value = inviteLink
                }
        }
    }

    private fun proceedWithUserPermissions(space: SpaceId) {
        viewModelScope.launch {
            permissions
                .observe(space = space)
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
                spaceViewFlow,
                container.subscribe(spaceMembersSearchParams),
                isCurrentUserOwner,
                _activeTier.filterIsInstance<ActiveTierState.Success>()
            ) { spaceView, membersResponse, isCurrentUserOwner, activeTier ->
                CombineResult(
                    isCurrentUserOwner = isCurrentUserOwner,
                    spaceView = spaceView,
                    tierId = activeTier.tierId,
                    spaceMembers = membersResponse.toSpaceMembers()
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
                val spaceView = result.spaceView
                val spaceMembers = result.spaceMembers
                    .sortedByDescending { it.status == ParticipantStatus.JOINING }
                members.value = spaceMembers.toView(
                    spaceView = spaceView,
                    urlBuilder = urlBuilder,
                    isCurrentUserOwner = result.isCurrentUserOwner,
                    account = account
                )
                showIncentive.value = spaceView?.getIncentiveState(
                    spaceMembers = spaceMembers,
                    isCurrentUserOwner = result.isCurrentUserOwner
                ) ?: ShareSpaceIncentiveState.Hidden
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
            analytics.sendEvent(
                eventName = clickSettingsSpaceShare,
                props = Props(
                    mapOf(EventsPropertiesKey.type to shareTypeShareLink)
                )
            )
        }
    }

    fun onShareQrCodeClicked(link: String) {
        Timber.d("onShareQrCodeClicked, link: $link")
        viewModelScope.launch {
            commands.emit(Command.ShareQrCode(link))
            analytics.sendEvent(
                eventName = clickSettingsSpaceShare,
                props = Props(
                    mapOf(EventsPropertiesKey.type to shareTypeQR)
                )
            )
        }
    }

    fun onViewRequestClicked(view: ShareSpaceMemberView) {
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

    fun onCanEditClicked(
        view: ShareSpaceMemberView
    ) {
        Timber.d("onCanEditClicked, view: [$view]")
        if (!view.canEditEnabled) {
            Timber.w("Can't change permissions")
            viewModelScope.launch {
                commands.emit(Command.ToastPermission)
            }
            return
        }
        viewModelScope.launch {
            if (view.config != ShareSpaceMemberView.Config.Member.Writer) {
                changeSpaceMemberPermissions.async(
                    ChangeSpaceMemberPermissions.Params(
                        space = vmParams.space,
                        identity = view.obj.identity,
                        permission = SpaceMemberPermissions.WRITER
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
                                mapOf(EventsPropertiesKey.type to "Write")
                            )
                        )
                    }
                )
            }
        }
    }

    fun onCanViewClicked(
        view: ShareSpaceMemberView
    ) {
        Timber.d("onCanViewClicked, view: [$view]")
        if (!view.canReadEnabled) {
            Timber.w("Can't change permissions")
            viewModelScope.launch {
                commands.emit(Command.ToastPermission)
            }
            return
        }
        viewModelScope.launch {
            if (view.config != ShareSpaceMemberView.Config.Member.Reader) {
                changeSpaceMemberPermissions.async(
                    ChangeSpaceMemberPermissions.Params(
                        space = vmParams.space,
                        identity = view.obj.identity,
                        permission = SpaceMemberPermissions.READER
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
                                mapOf(EventsPropertiesKey.type to "Read")
                            )
                        )
                    }
                )
            }
        }
    }

    fun onRemoveMemberClicked(
        view: ShareSpaceMemberView
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

    fun onCopyInviteLinkClicked(link: String) {
        Timber.d("onCopyInviteLinkClicked, link: $link")
        viewModelScope.launch {
            try {
                copyInviteLinkToClipboard.run(
                    CopyInviteLinkToClipboard.Params(link)
                )
                sendToast("Invite link copied to clipboard")
            } catch (error: Exception) {
                Timber.e(error, "Failed to copy invite link to clipboard")
            }
        }
    }

    private fun updateInviteLinkAccessLevel(newLevel: SpaceInviteLinkAccessLevel) {
        if (!inSpaceSharable()) {
            viewModelScope.launch {
                makeSpaceShareable(
                    space = vmParams.space,
                    actionSuccess = { proceedWithUpdatingInviteLink(newLevel) },
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

            SpaceInviteLinkAccessLevel.LinkDisabled -> {
                revokeSpaceInviteLink.async(space).fold(
                    onSuccess = {
                        Timber.d("Successfully disabled invite link")
                        inviteLinkAccessLoading.value = false
                        inviteLinkConfirmationDialog.value = null
                        inviteLinkAccessLevel.value = newLevel
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
                    SpaceInviteLinkAccessLevel.LinkDisabled -> {
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
                                inviteLinkAccessLevel.value = newLevel
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
                    SpaceInviteLinkAccessLevel.LinkDisabled -> {
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
                                inviteLinkAccessLevel.value = newLevel
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
                    SpaceInviteLinkAccessLevel.LinkDisabled -> {
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
                SpaceInviteLinkAccessLevel.LinkDisabled ->
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
        private val container: StorelessSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val permissions: UserPermissionProvider,
        private val analytics: Analytics,
        private val membershipProvider: MembershipProvider,
        private val spaceViews: SpaceViewSubscriptionContainer,
        private val getCurrentInviteAccessLevel: GetCurrentInviteAccessLevel,
        private val copyInviteLinkToClipboard: CopyInviteLinkToClipboard,
        private val changeSpaceInvitePermissions: ChangeSpaceInvitePermissions,
        private val spaceInviteLinkStore: SpaceInviteLinkStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShareSpaceViewModel(
            vmParams = params,
            generateSpaceInviteLink = generateSpaceInviteLink,
            revokeSpaceInviteLink = revokeSpaceInviteLink,
            changeSpaceMemberPermissions = changeSpaceMemberPermissions,
            removeSpaceMembers = removeSpaceMembers,
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
            spaceInviteLinkStore = spaceInviteLinkStore
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
        data class ShareQrCode(val link: String) : Command()
        data class ViewJoinRequest(val space: SpaceId, val member: Id) : Command()
        data class ShowRemoveMemberWarning(val identity: Id, val name: String) : Command()
        data class ShowMultiplayerError(val error: MultiplayerError.Generic) : Command()
        data object ShowHowToShareSpace : Command()
        data object ToastPermission : Command()
        data object Dismiss : Command()
        data object ShowMembershipScreen : Command()
        data object ShowMembershipUpgradeScreen : Command()
        data class OpenParticipantObject(val objectId: Id, val space: SpaceId) : Command()
    }

    sealed class ShareSpaceIncentiveState {
        data object Hidden : ShareSpaceIncentiveState()
        data object VisibleSpaceReaders : ShareSpaceIncentiveState()
        data object VisibleSpaceEditors : ShareSpaceIncentiveState()
    }

    companion object {
        const val SHARE_SPACE_MEMBER_SUBSCRIPTION = "share-space-subscription.member"
        const val SHARE_SPACE_SPACE_SUBSCRIPTION = "share-space-subscription.space"
    }

    sealed class UiEvent{
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
        val spaceView: ObjectWrapper.SpaceView?,
        val tierId: TierId,
        val spaceMembers: List<ObjectWrapper.SpaceMember>,
    )
}

data class ShareSpaceMemberView(
    val obj: ObjectWrapper.SpaceMember,
    val config: Config = Config.Member.Owner,
    val icon: SpaceMemberIconView,
    val canReadEnabled: Boolean = false,
    val canEditEnabled: Boolean = false,
    val isUser: Boolean = false
) {
    sealed class Config {
        sealed class Request : Config() {
            data object Join : Request()
            data object Leave : Request()
        }

        sealed class Member : Config() {
            data object Owner : Member()
            data object Writer : Member()
            data object Reader : Member()
            data object NoPermissions : Member()
            data object Unknown : Member()
        }
    }

    companion object {
        fun fromObject(
            account: Id?,
            obj: ObjectWrapper.SpaceMember,
            urlBuilder: UrlBuilder,
            canChangeWriterToReader: Boolean,
            canChangeReaderToWriter: Boolean,
            includeRequests: Boolean
        ): ShareSpaceMemberView? {
            val icon = SpaceMemberIconView.icon(
                obj = obj,
                urlBuilder = urlBuilder
            )
            val isUser = obj.identity == account
            return when (obj.status) {
                ParticipantStatus.ACTIVE -> {
                    when (obj.permissions) {
                        SpaceMemberPermissions.READER -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.Reader,
                            icon = icon,
                            canReadEnabled = canChangeWriterToReader,
                            canEditEnabled = canChangeReaderToWriter,
                            isUser = isUser
                        )

                        SpaceMemberPermissions.WRITER -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.Writer,
                            icon = icon,
                            canReadEnabled = canChangeWriterToReader,
                            canEditEnabled = canChangeReaderToWriter,
                            isUser = isUser
                        )

                        SpaceMemberPermissions.OWNER -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.Owner,
                            icon = icon,
                            canReadEnabled = canChangeWriterToReader,
                            canEditEnabled = canChangeReaderToWriter,
                            isUser = isUser
                        )

                        SpaceMemberPermissions.NO_PERMISSIONS -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.NoPermissions,
                            icon = icon,
                            isUser = isUser
                        )

                        null -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.Unknown,
                            icon = icon,
                            isUser = isUser
                        )
                    }
                }

                ParticipantStatus.JOINING -> {
                    if (includeRequests)
                        ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Request.Join,
                            icon = icon,
                            isUser = isUser
                        )
                    else
                        null
                }

                ParticipantStatus.REMOVING -> {
                    // Always filter out participants with REMOVING status
                    null
                }

                else -> null
            }
        }
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
    data class Error(val msg: String) : ShareSpaceErrors()
}