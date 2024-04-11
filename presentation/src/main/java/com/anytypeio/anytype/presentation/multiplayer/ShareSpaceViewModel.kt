package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions.OWNER
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.getOrThrow
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ApproveLeaveSpaceRequest
import com.anytypeio.anytype.domain.multiplayer.ChangeSpaceMemberPermissions
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.RemoveSpaceMembers
import com.anytypeio.anytype.domain.multiplayer.RevokeSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.StopSharingSpace
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.objects.SpaceMemberIconView
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.spaceViewKeys
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class ShareSpaceViewModel(
    private val params: Params,
    private val getSpaceInviteLink: GetSpaceInviteLink,
    private val generateSpaceInviteLink: GenerateSpaceInviteLink,
    private val revokeSpaceInviteLink: RevokeSpaceInviteLink,
    private val removeSpaceMembers: RemoveSpaceMembers,
    private val approveLeaveSpaceRequest: ApproveLeaveSpaceRequest,
    private val changeSpaceMemberPermissions: ChangeSpaceMemberPermissions,
    private val stopSharingSpace: StopSharingSpace,
    private val container: StorelessSubscriptionContainer,
    private val getAccount: GetAccount,
    private val urlBuilder: UrlBuilder
) : BaseViewModel() {

    val members = MutableStateFlow<List<ShareSpaceMemberView>>(emptyList())
    val shareLinkViewState = MutableStateFlow<ShareLinkViewState>(ShareLinkViewState.Init)
    val spaceViewCounts = MutableStateFlow<SpaceViewCounts?>(null)
    val commands = MutableSharedFlow<Command>()
    val isCurrentUserOwner = MutableStateFlow(false)

    init {
        Timber.d("Share-space init with params: $params")
        proceedWithSpaceAccessTypeSubscription()
        //proceedWithSpaceMemberSubscription()
    }

    private fun proceedWithSpaceAccessTypeSubscription() {
        viewModelScope.launch {
            container.subscribe(
                StoreSearchByIdsParams(
                    subscription = SHARE_SPACE_SPACE_SUBSCRIPTION,
                    keys = spaceViewKeys,
                    targets = listOf(params.space.id)
                )
            ).mapNotNull { results ->
                val space = results.firstOrNull()
                if (space != null) {
                    val wrapper = ObjectWrapper.SpaceView(space.map)
                    when(wrapper.spaceAccessType) {
                        SpaceAccessType.PRIVATE -> {
                            ShareLinkViewState.NotGenerated
                        }
                        SpaceAccessType.SHARED -> {
                            val link = getSpaceInviteLink.async(params.space)
                            if (link.isSuccess) {
                                ShareLinkViewState.Shared(link.getOrThrow().scheme)
                            } else {
                                null
                            }
                        }
                        else -> {
                            null
                        }
                    }
                } else {
                    null
                }

            }.catch {
                Timber.e("Error while $SHARE_SPACE_SPACE_SUBSCRIPTION subscription")
            }.collect { result ->
                shareLinkViewState.value = result
            }
        }
    }

    private fun proceedWithSpaceMemberSubscription() {
        viewModelScope.launch {
            val account = getAccount.async(Unit).getOrNull()
            container.subscribe(
                StoreSearchParams(
                    subscription = SHARE_SPACE_MEMBER_SUBSCRIPTION,
                    filters = ObjectSearchConstants.filterParticipants(
                        spaces = listOf(params.space.id)
                    ),
                    sorts = listOf(ObjectSearchConstants.sortByName()),
                    keys = ObjectSearchConstants.spaceMemberKeys
                )
            ).map { results ->
                results.mapNotNull { wrapper ->
                    ShareSpaceMemberView.fromObject(
                        obj = ObjectWrapper.SpaceMember(wrapper.map),
                        urlBuilder = urlBuilder
                    )
                }
            }.onEach { results ->
                isCurrentUserOwner.value = results.any { result ->
                    with(result.obj) {
                        identity.isNotEmpty() && identity == account?.id && permissions == OWNER
                    }
                }
            }.catch {
                Timber.e("Error while $SHARE_SPACE_MEMBER_SUBSCRIPTION subscription")
            }.collect {
                members.value = it
            }
        }
    }

    private fun proceedWithGeneratingInviteLink() {
        viewModelScope.launch {
            generateSpaceInviteLink
                .async(params.space)
                .fold(
                    onSuccess = { link ->
                        shareLinkViewState.value = ShareLinkViewState.Shared(link = link.scheme)
                    },
                    onFailure = {
                        Timber.e(it, "Error while generating invite link")
                    }
                )
        }
    }

    fun onRegenerateInviteLinkClicked() {
        proceedWithGeneratingInviteLink()
    }

    fun onShareInviteLinkClicked() {
        viewModelScope.launch {
            when(val value = shareLinkViewState.value) {
                ShareLinkViewState.Init -> {
                    // Do nothing.
                }
                is ShareLinkViewState.Shared -> {
                    commands.emit(Command.ShareInviteLink(value.link))
                }
                is ShareLinkViewState.NotGenerated -> {
                    // Do nothing
                }
            }
        }
    }

    fun onShareQrCodeClicked() {
        viewModelScope.launch {
            when(val value = shareLinkViewState.value) {
                ShareLinkViewState.Init -> {
                    // Do nothing.
                }
                is ShareLinkViewState.Shared -> {
                    commands.emit(Command.ShareQrCode(value.link))
                }
                is ShareLinkViewState.NotGenerated -> {
                    // Do nothing
                }
            }
        }
    }

    fun onViewRequestClicked(view: ShareSpaceMemberView) {
        viewModelScope.launch {
            commands.emit(
                Command.ViewJoinRequest(
                    space = params.space,
                    member = view.obj.id
                )
            )
        }
    }

    fun onApproveLeaveRequestClicked(view: ShareSpaceMemberView) {
        viewModelScope.launch {
            approveLeaveSpaceRequest.async(
                ApproveLeaveSpaceRequest.Params(
                    space = params.space,
                    identities = listOf(view.obj.identity)
                )
            ).fold(
                onFailure = { e ->
                    Timber.e(e, "Error while approving unjoin request").also {
                        sendToast(e.msg())
                    }
                },
                onSuccess = {
                    Timber.d("Successfully removed space member")
                }
            )
        }
    }

    fun onCanEditClicked(
        view: ShareSpaceMemberView
    ) {
        Timber.d("onCanEditClicked")
        viewModelScope.launch {
            if (view.config != ShareSpaceMemberView.Config.Member.Writer) {
                changeSpaceMemberPermissions.async(
                    ChangeSpaceMemberPermissions.Params(
                        space = params.space,
                        identity = view.obj.identity,
                        permission = SpaceMemberPermissions.WRITER
                    )
                ).fold(
                    onFailure = { e ->
                        Timber.e(e, "Error while changing member permissions").also {
                            sendToast(e.msg())
                        }
                    },
                    onSuccess = {
                        Timber.d("Successfully updated space member permissions")
                    }
                )
            }
        }
    }

    fun onCanViewClicked(
        view: ShareSpaceMemberView
    ) {
        Timber.d("onCanViewClicked")
        viewModelScope.launch {
            if (view.config != ShareSpaceMemberView.Config.Member.Reader) {
                changeSpaceMemberPermissions.async(
                    ChangeSpaceMemberPermissions.Params(
                        space = params.space,
                        identity = view.obj.identity,
                        permission = SpaceMemberPermissions.READER
                    )
                ).fold(
                    onFailure = { e ->
                        Timber.e(e, "Error while changing member permissions").also {
                            sendToast(e.msg())
                        }
                    },
                    onSuccess = {
                        Timber.d("Successfully updated space member permissions")
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
            removeSpaceMembers.async(
                RemoveSpaceMembers.Params(
                    space = params.space,
                    identities = listOf(view.obj.identity)
                )
            ).fold(
                onFailure = { e ->
                    Timber.e(e, "Error while removing space member").also {
                        sendToast(e.msg())
                    }
                },
                onSuccess = {
                    Timber.d("Successfully removed space member")
                }
            )
        }
    }

    fun onStopSharingSpaceClicked() {
        Timber.d("onStopSharingClicked")
        viewModelScope.launch {
            if (isCurrentUserOwner.value && shareLinkViewState.value is ShareLinkViewState.Shared) {
                viewModelScope.launch {
                    commands.emit(Command.ShowStopSharingWarning)
                }
            } else {
                Timber.w("Something wrong with permissions.")
            }
        }
    }

    fun onStopSharingAccepted() {
        Timber.d("onStopSharingAccepted")
        viewModelScope.launch {
            if (isCurrentUserOwner.value && shareLinkViewState.value is ShareLinkViewState.Shared) {
                stopSharingSpace.async(
                    params = params.space
                ).fold(
                    onSuccess = {
                        Timber.d("Stopped sharing space")
                    },
                    onFailure = { e ->
                        Timber.e(e, "Error while sharing space").also {
                            sendToast(e.msg())
                        }
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
                viewModelScope.launch {
                    commands.emit(Command.ShowDeleteLinkWarning)
                }
            } else {
                Timber.w("Something wrong with permissions.")
            }
        }
    }

    fun onDeleteLinkAccepted() {
        Timber.d("onDeleteLinkAccepted")
        viewModelScope.launch {
            if (isCurrentUserOwner.value) {
                revokeSpaceInviteLink.async(
                    params = params.space
                ).fold(
                    onSuccess = {
                        Timber.d("Revoked space invite link")
                    },
                    onFailure = { e ->
                        Timber.e(e, "Error while revoking space invite link").also {
                            sendToast(e.msg())
                        }
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
        }
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
        private val params: Params,
        private val generateSpaceInviteLink: GenerateSpaceInviteLink,
        private val revokeSpaceInviteLink: RevokeSpaceInviteLink,
        private val changeSpaceMemberPermissions: ChangeSpaceMemberPermissions,
        private val stopSharingSpace: StopSharingSpace,
        private val getAccount: GetAccount,
        private val removeSpaceMembers: RemoveSpaceMembers,
        private val approveLeaveSpaceRequest: ApproveLeaveSpaceRequest,
        private val container: StorelessSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val getSpaceInviteLink: GetSpaceInviteLink
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShareSpaceViewModel(
            params = params,
            generateSpaceInviteLink = generateSpaceInviteLink,
            revokeSpaceInviteLink = revokeSpaceInviteLink,
            changeSpaceMemberPermissions = changeSpaceMemberPermissions,
            removeSpaceMembers = removeSpaceMembers,
            stopSharingSpace = stopSharingSpace,
            container = container,
            urlBuilder = urlBuilder,
            getAccount = getAccount,
            getSpaceInviteLink = getSpaceInviteLink,
            approveLeaveSpaceRequest = approveLeaveSpaceRequest
        ) as T
    }

    data class Params(
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
        data object ShowHowToShareSpace: Command()
        data object ShowStopSharingWarning: Command()
        data object ShowDeleteLinkWarning: Command()
        data object Dismiss : Command()
    }

    companion object {
        const val SHARE_SPACE_MEMBER_SUBSCRIPTION = "share-space-subscription.member"
        const val SHARE_SPACE_SPACE_SUBSCRIPTION = "share-space-subscription.space"
    }
}

data class ShareSpaceMemberView(
    val obj: ObjectWrapper.SpaceMember,
    val config: Config = Config.Member.Owner,
    val icon: SpaceMemberIconView
) {
    sealed class Config {
        sealed class Request : Config() {
            object Join: Request()
            object Unjoin: Request()
        }
        sealed class Member: Config() {
            object Owner: Member()
            object Writer: Member()
            object Reader: Member()
            object NoPermissions: Member()
            object Unknown: Member()
        }
    }

    companion object {
        fun fromObject(
            obj: ObjectWrapper.SpaceMember,
            urlBuilder: UrlBuilder
        ) : ShareSpaceMemberView? {
            val icon = SpaceMemberIconView.icon(
                obj = obj,
                urlBuilder = urlBuilder
            )
            return when(obj.status) {
                ParticipantStatus.ACTIVE -> {
                    when(obj.permissions) {
                        SpaceMemberPermissions.READER -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.Reader,
                            icon = icon
                        )
                        SpaceMemberPermissions.WRITER -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.Writer,
                            icon = icon
                        )
                        SpaceMemberPermissions.OWNER -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.Owner,
                            icon = icon
                        )
                        SpaceMemberPermissions.NO_PERMISSIONS -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.NoPermissions,
                            icon = icon
                        )
                        null -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.Unknown,
                            icon = icon
                        )
                    }
                }
                ParticipantStatus.JOINING -> ShareSpaceMemberView(
                    obj = obj,
                    config = Config.Request.Join,
                    icon = icon
                )
                ParticipantStatus.REMOVING -> ShareSpaceMemberView(
                    obj = obj,
                    config = Config.Request.Unjoin,
                    icon = icon
                )
                else -> null
            }
        }
    }
}

data class SpaceViewCounts(
    val readers: Int,
    val writers: Int
)