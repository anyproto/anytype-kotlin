package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.ParticipantPermissions
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class ShareSpaceViewModel(
    private val params: Params,
    private val generateSpaceInviteLink: GenerateSpaceInviteLink,
    private val container: StorelessSubscriptionContainer
) : BaseViewModel() {

    val participants = MutableStateFlow<List<ShareSpaceMemberView>>(emptyList())

    val viewState = MutableStateFlow<ViewState>(ViewState.Init)
    val commands = MutableSharedFlow<Command>()

    init {
        proceedWithGeneratingInviteLink()
        viewModelScope.launch {
            container.subscribe(
                StoreSearchParams(
                    subscription = this::class.java.toGenericString(),
                    filters = ObjectSearchConstants.filterParticipants(
                        spaces = listOf(params.space.id)
                    ),
                    sorts = listOf(ObjectSearchConstants.sortByName()),
                    keys = ObjectSearchConstants.participantKeys
                )
            ).map { results ->
                results.mapNotNull { wrapper ->
                    ShareSpaceMemberView.fromObject(ObjectWrapper.Participant(wrapper.map))
                }
            }.collect {
                participants.value = it
            }
        }
    }

    private fun proceedWithGeneratingInviteLink() {
        viewModelScope.launch {
            generateSpaceInviteLink
                .async(params.space)
                .fold(
                    onSuccess = { link ->
                        viewState.value = ViewState.Share(link = link.scheme)
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
            when(val value = viewState.value) {
                ViewState.Init -> {
                    // Do nothing.
                }
                is ViewState.Share -> {
                    commands.emit(Command.ShareInviteLink(value.link))
                }
            }
        }
    }

    class Factory @Inject constructor(
        private val params: Params,
        private val generateSpaceInviteLink: GenerateSpaceInviteLink,
        private val container: StorelessSubscriptionContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShareSpaceViewModel(
            params = params,
            generateSpaceInviteLink = generateSpaceInviteLink,
            container = container
        ) as T
    }

    data class Params(
        val space: SpaceId
    )

    sealed class ViewState {
        object Init : ViewState()
        data class Share(val link: String): ViewState()
    }

    sealed class Command {
        data class ShareInviteLink(val link: String) : Command()
    }
}

data class ShareSpaceMemberView(
    val obj: ObjectWrapper.Participant,
    val config: Config = Config.Member.Owner
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
        fun fromObject(obj: ObjectWrapper.Participant) : ShareSpaceMemberView? {
            return when(obj.status) {
                ParticipantStatus.ACTIVE -> {
                    when(obj.permissions) {
                        ParticipantPermissions.READER -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.Reader
                        )
                        ParticipantPermissions.WRITER -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.Writer
                        )
                        ParticipantPermissions.OWNER -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.Owner
                        )
                        ParticipantPermissions.NO_PERMISSIONS -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.NoPermissions
                        )
                        null -> ShareSpaceMemberView(
                            obj = obj,
                            config = Config.Member.Unknown
                        )
                    }
                }
                ParticipantStatus.JOINING -> ShareSpaceMemberView(
                    obj = obj,
                    config = Config.Request.Join
                )
                ParticipantStatus.REMOVING -> ShareSpaceMemberView(
                    obj = obj,
                    config = Config.Request.Unjoin
                )
                else -> null
            }
        }
    }
}