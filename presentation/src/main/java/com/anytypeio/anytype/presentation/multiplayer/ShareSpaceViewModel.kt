package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.SharingSpacesTypes.shareTypeMoreInfo
import com.anytypeio.anytype.analytics.base.EventsDictionary.SharingSpacesTypes.shareTypeQR
import com.anytypeio.anytype.analytics.base.EventsDictionary.SharingSpacesTypes.shareTypeRevoke
import com.anytypeio.anytype.analytics.base.EventsDictionary.SharingSpacesTypes.shareTypeShareLink
import com.anytypeio.anytype.analytics.base.EventsDictionary.clickSettingsSpaceShare
import com.anytypeio.anytype.analytics.base.EventsDictionary.removeSpaceMember
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenRevokeShareLink
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenSettingsSpaceMembers
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenSettingsSpaceShare
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenStopShare
import com.anytypeio.anytype.analytics.base.EventsDictionary.stopSpaceShare
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
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions.OWNER
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.getOrThrow
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ChangeSpaceMemberPermissions
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.MakeSpaceShareable
import com.anytypeio.anytype.domain.multiplayer.RemoveSpaceMembers
import com.anytypeio.anytype.domain.multiplayer.RevokeSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.StopSharingSpace
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
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
    private val getSpaceInviteLink: GetSpaceInviteLink,
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
    private val spaceViews: SpaceViewSubscriptionContainer
) : BaseViewModel() {

    private val _activeTier = MutableStateFlow<ActiveTierState>(ActiveTierState.Init)

    val members = MutableStateFlow<List<ShareSpaceMemberView>>(emptyList())
    val shareLinkViewState = MutableStateFlow<ShareLinkViewState>(ShareLinkViewState.Init)
    val commands = MutableSharedFlow<Command>()
    val isCurrentUserOwner = MutableStateFlow(false)
    val spaceAccessType = MutableStateFlow<SpaceAccessType?>(null)
    val showIncentive = MutableStateFlow<ShareSpaceIncentiveState>(ShareSpaceIncentiveState.Hidden)
    val isLoadingInProgress = MutableStateFlow(false)
    val shareSpaceErrors = MutableStateFlow<ShareSpaceErrors>(ShareSpaceErrors.Hidden)

    init {
        Timber.i("Share-space init with params: $vmParams")
        proceedWithUserPermissions(space = vmParams.space)
        proceedWithSubscriptions()
        proceedWithGettingActiveTier()
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
                val spaceView = result.spaceView
                val spaceMembers = result.spaceMembers
                    .sortedByDescending { it.status == ParticipantStatus.JOINING }
                spaceAccessType.value = spaceView?.spaceAccessType
                setShareLinkViewState(spaceView, result.isCurrentUserOwner)
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

    private suspend fun setShareLinkViewState(
        space: ObjectWrapper.SpaceView?,
        isCurrentUserOwner: Boolean
    ) {
        shareLinkViewState.value = when (space?.spaceAccessType) {
            SpaceAccessType.PRIVATE -> {
                if (isCurrentUserOwner)
                    ShareLinkViewState.NotGenerated
                else
                    ShareLinkViewState.Init
            }
            SpaceAccessType.SHARED -> {
                val link = getSpaceInviteLink.async(vmParams.space)
                if (link.isSuccess) {
                    ShareLinkViewState.Shared(link.getOrThrow().scheme)
                } else {
                    if (isCurrentUserOwner)
                        ShareLinkViewState.NotGenerated
                    else
                        ShareLinkViewState.Init
                }
            }
            else -> ShareLinkViewState.Init
        }
    }

    private fun proceedWithGeneratingInviteLink(
        inviteType: InviteType = InviteType.MEMBER,
        permissions: SpaceMemberPermissions = SpaceMemberPermissions.READER
    ) {
        viewModelScope.launch {
            if (spaceAccessType.value == SpaceAccessType.PRIVATE) {
                makeSpaceShareable.async(
                    params = vmParams.space
                ).fold(
                    onSuccess = {
                        analytics.sendEvent(eventName = EventsDictionary.shareSpace)
                        Timber.d("Successfully made space shareable")
                        generateInviteLink(
                            inviteType = inviteType,
                            permissions = permissions
                        )
                    },
                    onFailure = {
                        Timber.e(it, "Error while making space shareable")
                        proceedWithMultiplayerError(it)
                    }
                )
            } else {
                generateInviteLink(
                    inviteType = inviteType,
                    permissions = permissions
                )
            }
        }
    }

    private suspend fun generateInviteLink(inviteType: InviteType, permissions: SpaceMemberPermissions) {
        generateSpaceInviteLink.async(
            params = GenerateSpaceInviteLink.Params(
                space = vmParams.space,
                inviteType = inviteType,
                permissions = permissions
            )
        ).fold(
            onSuccess = { inviteLink ->
                shareLinkViewState.value = ShareLinkViewState.Shared(inviteLink.scheme)
                Timber.d("Successfully generated invite link")
            },
            onFailure = {
                Timber.e(it, "Error while generating invite link")
                proceedWithMultiplayerError(it)
            }
        )
    }

    fun onShareInviteLinkClicked() {
        viewModelScope.launch {
            when (val value = shareLinkViewState.value) {
                is ShareLinkViewState.Shared -> {
                    commands.emit(Command.ShareInviteLink(value.link))
                    analytics.sendEvent(
                        eventName = clickSettingsSpaceShare,
                        props = Props(
                            mapOf(EventsPropertiesKey.type to shareTypeShareLink)
                        )
                    )
                }
                else -> {
                    Timber.w("Ignoring share-invite click while in state: $value")
                }
            }
        }
    }

    fun onShareQrCodeClicked() {
        viewModelScope.launch {
            when(val value = shareLinkViewState.value) {
                is ShareLinkViewState.Shared -> {
                    commands.emit(Command.ShareQrCode(value.link))
                    analytics.sendEvent(
                        eventName = clickSettingsSpaceShare,
                        props = Props(
                            mapOf(EventsPropertiesKey.type to shareTypeQR)
                        )
                    )
                }
                else -> {
                    Timber.w("Ignoring QR-code click while in state: $value")
                }
            }
        }
    }

    fun onViewRequestClicked(view: ShareSpaceMemberView) {
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
        if (!view.canEditEnabled)  {
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
        if (!view.canReadEnabled)  {
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

    fun onStopSharingSpaceClicked() {
        Timber.d("onStopSharingClicked")
        viewModelScope.launch {
            if (isCurrentUserOwner.value && spaceAccessType.value == SpaceAccessType.SHARED) {
                commands.emit(Command.ShowStopSharingWarning)
                analytics.sendEvent(eventName = screenStopShare)
            } else {
                Timber.w("Something wrong with permissions.")
            }
        }
    }

    fun onStopSharingAccepted() {
        Timber.d("onStopSharingAccepted")
        viewModelScope.launch {
            if (isCurrentUserOwner.value && spaceAccessType.value == SpaceAccessType.SHARED) {
                stopSharingSpace.async(
                    params = vmParams.space
                ).fold(
                    onSuccess = {
                        Timber.d("Stopped sharing space")
                        analytics.sendEvent(eventName = stopSpaceShare)
                    },
                    onFailure = { e ->
                        Timber.e(e, "Error while sharing space")
                        proceedWithMultiplayerError(e)
                    }
                )
            } else {
                Timber.w("Something wrong with permissions.")
            }
        }
    }

    fun onDeleteLinkClicked() {
        Timber.d("onDeleteLinkClicked")
        viewModelScope.launch {
            if (isCurrentUserOwner.value) {
                commands.emit(Command.ShowDeleteLinkWarning)
                analytics.sendEvent(
                    eventName = clickSettingsSpaceShare,
                    props = Props(
                        mapOf(EventsPropertiesKey.type to shareTypeRevoke)
                    )
                )
                analytics.sendEvent(eventName = screenRevokeShareLink)
            } else {
                Timber.w("Something wrong with permissions.")
            }
        }
    }

    fun onMemberClicked(member: ObjectWrapper.SpaceMember) {
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

    fun onDeleteLinkAccepted() {
        Timber.d("onDeleteLinkAccepted")
        viewModelScope.launch {
            if (isCurrentUserOwner.value) {
                revokeSpaceInviteLink.async(
                    params = vmParams.space
                ).fold(
                    onSuccess = {
                        Timber.d("Revoked space invite link").also {
                            shareLinkViewState.value = ShareLinkViewState.NotGenerated
                        }
                        analytics.sendEvent(eventName = EventsDictionary.revokeShareLink)
                    },
                    onFailure = { e ->
                        Timber.e(e, "Error while revoking space invite link")
                        proceedWithMultiplayerError(e)
                    }
                )
            } else {
                Timber.w("Something wrong with permissions.")
            }
        }
    }

    fun onGenerateSpaceInviteLink() {
        proceedWithGeneratingInviteLink()
    }

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
            }
        } else {
            shareSpaceErrors.value = ShareSpaceErrors.Error(error)
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
        private val stopSharingSpace: StopSharingSpace,
        private val getAccount: GetAccount,
        private val removeSpaceMembers: RemoveSpaceMembers,
        private val container: StorelessSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val getSpaceInviteLink: GetSpaceInviteLink,
        private val permissions: UserPermissionProvider,
        private val analytics: Analytics,
        private val membershipProvider: MembershipProvider,
        private val spaceViews: SpaceViewSubscriptionContainer
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
            getSpaceInviteLink = getSpaceInviteLink,
            permissions = permissions,
            makeSpaceShareable = makeSpaceShareable,
            analytics = analytics,
            membershipProvider = membershipProvider,
            spaceViews = spaceViews
        ) as T
    }

    data class VmParams(
        val space: SpaceId
    )

    sealed class ShareLinkViewState {
        data object Init: ShareLinkViewState()
        data object NotGenerated: ShareLinkViewState()
        data class Shared(val link: String): ShareLinkViewState()
    }

    sealed class Command {
        data class ShareInviteLink(val link: String) : Command()
        data class ShareQrCode(val link: String) : Command()
        data class ViewJoinRequest(val space: SpaceId, val member: Id) : Command()
        data class ShowRemoveMemberWarning(val identity: Id, val name: String): Command()
        data class ShowMultiplayerError(val error: MultiplayerError.Generic) : Command()
        data object ShowHowToShareSpace: Command()
        data object ShowStopSharingWarning: Command()
        data object ShowDeleteLinkWarning: Command()
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
            data object Join: Request()
            data object Leave: Request()
        }
        sealed class Member: Config() {
            data object Owner: Member()
            data object Writer: Member()
            data object Reader: Member()
            data object NoPermissions: Member()
            data object Unknown: Member()
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
        ) : ShareSpaceMemberView? {
            val icon = SpaceMemberIconView.icon(
                obj = obj,
                urlBuilder = urlBuilder
            )
            val isUser = obj.identity == account
            return when(obj.status) {
                ParticipantStatus.ACTIVE -> {
                    when(obj.permissions) {
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
    data class Error(val error: Throwable) : ShareSpaceErrors()
}