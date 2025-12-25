package com.anytypeio.anytype.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SpaceCreationUseCase
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.suspendFold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ParticipantViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val membershipProvider: MembershipProvider,
    private val subscriptionContainer: StorelessSubscriptionContainer,
    private val fieldsParser: FieldParser,
    private val userPermissionProvider: UserPermissionProvider,
    private val configStorage: ConfigStorage,
    private val createSpace: CreateSpace
) : ViewModel() {

    val uiState = MutableStateFlow<UiParticipantScreenState>(UiParticipantScreenState.EMPTY)

    val membershipStatusState = MutableStateFlow<MembershipStatus?>(null)
    val commands = MutableSharedFlow<Command>(0)

    private val permission = MutableStateFlow(userPermissionProvider.get(vmParams.space))

    init {
        proceedWithObservingPermissions()
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenSettingsAccount
            )
        }
        viewModelScope.launch {
            membershipProvider.status().collect { status ->
                membershipStatusState.value = status
            }
        }
    }

    fun onStart() {
        viewModelScope.launch {
            val params = StoreSearchByIdsParams(
                space = vmParams.space,
                subscription = "$SUB_ID-${vmParams.objectId}",
                targets = listOf(vmParams.objectId),
                keys = ObjectSearchConstants.spaceMemberKeys
            )
            subscriptionContainer.subscribe(params)
                .collect { participant ->
                    if (participant.isNotEmpty()) {
                        val obj = participant.first()
                        val identityProfileLink = obj.getSingleValue<String>(Relations.IDENTITY_PROFILE_LINK)
                        uiState.value = UiParticipantScreenState(
                            name = fieldsParser.getObjectName(obj),
                            icon = obj.profileIcon(urlBuilder),
                            isOwner = configStorage.getOrNull()?.profile == identityProfileLink,
                            identity = obj.getSingleValue<String>(Relations.IDENTITY),
                            description = if (obj.description?.isBlank() == true) {
                                null
                            } else {
                                obj.description
                            }
                        )
                    }
                }
        }
    }

    private fun proceedWithObservingPermissions() {
        viewModelScope.launch {
            userPermissionProvider
                .observe(space = vmParams.space)
                .collect { result ->
                    permission.value = result
                }
        }
    }

    fun onStop() {
        viewModelScope.launch{
            subscriptionContainer.unsubscribe(listOf("$SUB_ID-${vmParams.objectId}"))
        }
    }

    fun onEvent(event: ParticipantEvent) {
        when (event) {
            ParticipantEvent.OnDismiss -> {
                viewModelScope.launch {
                    commands.emit(Command.Dismiss)
                }
            }

            ParticipantEvent.OnCardClicked -> {
                val uiState = uiState.value
                if (uiState.isOwner) {
                    viewModelScope.launch {
                        commands.emit(Command.OpenSettingsProfile)
                    }
                }
            }

            ParticipantEvent.OnConnectClicked -> {
                viewModelScope.launch {
                    analytics.sendEvent(eventName = EventsDictionary.clickConnectOneToOne)
                }
                proceedWithCreatingOneToOneSpace()
            }
        }
    }

    private fun proceedWithCreatingOneToOneSpace() {
        val state = uiState.value
        val participantIdentity = state.identity
        if (participantIdentity.isNullOrBlank()) {
            viewModelScope.launch {
                commands.emit(Command.Toast.Error("Unable to connect: participant identity not found"))
            }
            return
        }

        viewModelScope.launch {
            // Set loading state
            uiState.value = state.copy(isConnecting = true)

            // Create the 1-on-1 space
            val params = CreateSpace.Params(
                details = mapOf(
                    Relations.ONE_TO_ONE_IDENTITY to participantIdentity,
                    Relations.SPACE_UX_TYPE to SpaceUxType.ONE_TO_ONE.code.toDouble(),
                    Relations.SPACE_ACCESS_TYPE to SpaceAccessType.SHARED.code.toDouble(),
                ),
                useCase = SpaceCreationUseCase.ONE_TO_ONE_SPACE
            )

            createSpace.async(params).suspendFold(
                onSuccess = { response ->
                    // Reset loading state
                    uiState.value = state.copy(isConnecting = false)
                    // Switch to the new space
                    commands.emit(Command.SwitchToVault(response.space.id))
                },
                onFailure = { error ->
                    // Reset loading state
                    uiState.value = state.copy(isConnecting = false)
                    // Show error
                    commands.emit(
                        Command.Toast.Error(
                            error.message ?: "Failed to create 1-on-1 space"
                        )
                    )
                }
            )
        }
    }

    data class UiParticipantScreenState(
        val name: String,
        val icon: ProfileIconView,
        val description: String? = null,
        val identity: String? = null,
        val isOwner: Boolean,
        val isConnecting: Boolean = false
    ) {
       companion object {
           val EMPTY = UiParticipantScreenState(
               name = "",
               icon = ProfileIconView.Loading,
               isOwner = false,
               isConnecting = false
           )
       }
    }

    data class VmParams(
        val objectId: Id,
        val space: SpaceId
    )

    sealed class Command {
        sealed class Toast : Command() {
            data class Error(val msg: String) : Toast()
        }

        data object Dismiss : Command()
        data object OpenSettingsProfile : Command()
        data class SwitchToVault(val spaceId: Id) : Command()
    }

    companion object {
        const val SUB_ID = "Participant-subscription"
    }

    class Factory @Inject constructor(
        private val vmParams: VmParams,
        private val analytics: Analytics,
        private val urlBuilder: UrlBuilder,
        private val membershipProvider: MembershipProvider,
        private val subscriptionContainer: StorelessSubscriptionContainer,
        private val fieldsParser: FieldParser,
        private val userPermissionProvider: UserPermissionProvider,
        private val configStorage: ConfigStorage,
        private val createSpace: CreateSpace
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ParticipantViewModel(
                vmParams = vmParams,
                analytics = analytics,
                urlBuilder = urlBuilder,
                membershipProvider = membershipProvider,
                subscriptionContainer = subscriptionContainer,
                fieldsParser = fieldsParser,
                userPermissionProvider = userPermissionProvider,
                configStorage = configStorage,
                createSpace = createSpace
            ) as T
        }
    }
}