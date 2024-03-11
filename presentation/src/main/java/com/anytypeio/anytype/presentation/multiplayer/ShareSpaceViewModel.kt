package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions.OWNER
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ChangeSpaceMemberPermissions
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.RemoveSpaceMembers
import com.anytypeio.anytype.domain.multiplayer.StopSharingSpace
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.objects.SpaceMemberIconView
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class ShareSpaceViewModel(
    private val params: Params,
    private val generateSpaceInviteLink: GenerateSpaceInviteLink,
    private val removeSpaceMembers: RemoveSpaceMembers,
    private val changeSpaceMemberPermissions: ChangeSpaceMemberPermissions,
    private val stopSharingSpace: StopSharingSpace,
    private val container: StorelessSubscriptionContainer,
    private val getAccount: GetAccount,
    private val urlBuilder: UrlBuilder
) : BaseViewModel() {

    val members = MutableStateFlow<List<ShareSpaceMemberView>>(emptyList())
    val shareLinkViewState = MutableStateFlow<ShareLinkViewState>(ShareLinkViewState.Init)
    val commands = MutableSharedFlow<Command>()
    val isCurrentUserOwner = MutableStateFlow(false)

    init {
        proceedWithGeneratingInviteLink()
        proceedWithSpaceMemberSubscription()
    }

    private fun proceedWithSpaceMemberSubscription() {
        viewModelScope.launch {
            val account = getAccount.async(Unit).getOrNull()
            container.subscribe(
                StoreSearchParams(
                    subscription = SHARE_SPACE_SUBSCRIPTION,
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
                        shareLinkViewState.value = ShareLinkViewState.Share(link = link.scheme)
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
                is ShareLinkViewState.Share -> {
                    commands.emit(Command.ShareInviteLink(value.link))
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

    fun onApproveUnjoinRequestClicked(view: ShareSpaceMemberView) {
        viewModelScope.launch {
            removeSpaceMembers.async(
                RemoveSpaceMembers.Params(
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
            if (isCurrentUserOwner.value) {
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

    override fun onCleared() {
        viewModelScope.launch {
            container.unsubscribe(subscriptions = listOf(SHARE_SPACE_SUBSCRIPTION))
        }
        super.onCleared()
    }

    class Factory @Inject constructor(
        private val params: Params,
        private val generateSpaceInviteLink: GenerateSpaceInviteLink,
        private val changeSpaceMemberPermissions: ChangeSpaceMemberPermissions,
        private val stopSharingSpace: StopSharingSpace,
        private val getAccount: GetAccount,
        private val removeSpaceMembers: RemoveSpaceMembers,
        private val configStorage: ConfigStorage,
        private val container: StorelessSubscriptionContainer,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShareSpaceViewModel(
            params = params,
            generateSpaceInviteLink = generateSpaceInviteLink,
            changeSpaceMemberPermissions = changeSpaceMemberPermissions,
            removeSpaceMembers = removeSpaceMembers,
            stopSharingSpace = stopSharingSpace,
            container = container,
            urlBuilder = urlBuilder,
            getAccount = getAccount
        ) as T
    }

    data class Params(
        val space: SpaceId
    )

    sealed class ShareLinkViewState {
        object Init : ShareLinkViewState()
        data class Share(val link: String): ShareLinkViewState()
    }

    sealed class Command {
        data class ShareInviteLink(val link: String) : Command()
        data class ViewJoinRequest(val space: SpaceId, val member: Id) : Command()
        object Dismiss : Command()
    }

    companion object {
        const val SHARE_SPACE_SUBSCRIPTION = "share-space-subscription"
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