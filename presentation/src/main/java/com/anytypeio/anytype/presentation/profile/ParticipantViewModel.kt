package com.anytypeio.anytype.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SpaceCreationUseCase
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.suspendFold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SearchOneToOneChatByIdentity
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import timber.log.Timber
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
    private val createSpace: CreateSpace,
    private val searchOneToOneChatByIdentity: SearchOneToOneChatByIdentity,
    private val spaceManager: SpaceManager
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

            // First, check if a 1-1 chat already exists with this identity
            // Using direct middleware search to find space even if it's deleted/left
            val techSpace = configStorage.getOrNull()?.techSpace
            if (techSpace == null) {
                Timber.e("Tech space not available, creating new 1-1 space")
                createNewOneToOneSpace(participantIdentity)
                return@launch
            }
            searchOneToOneChatByIdentity.async(
                SearchOneToOneChatByIdentity.Params(
                    identity = participantIdentity,
                    techSpace = SpaceId(techSpace)
                )
            ).fold(
                onSuccess = { existingChat ->
                    if (existingChat != null) {
                        Timber.d("Found existing 1-1 chat: ${existingChat.spaceId}")
                        // Navigate to existing chat
                        navigateToOneToOneChat(existingChat.spaceId.id, isNewSpace = false)
                    } else {
                        Timber.d("No existing 1-1 chat found, creating new space")
                        createNewOneToOneSpace(participantIdentity)
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Error finding existing 1-1 chat")
                    // Error checking, try to create a new space anyway
                    createNewOneToOneSpace(participantIdentity)
                }
            )
        }
    }

    private suspend fun createNewOneToOneSpace(participantIdentity: String) {
        val state = uiState.value
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
                // Send CreateSpace analytics event
                analytics.sendEvent(
                    eventName = EventsDictionary.createSpace,
                    props = Props(
                        mapOf(
                            EventsPropertiesKey.route to EventsDictionary.Routes.navigation,
                            EventsPropertiesKey.uxType to "OneToOne"
                        )
                    )
                )
                Timber.d("Successfully created 1-1 space: ${response.space.id}")
                navigateToOneToOneChat(response.space.id, isNewSpace = true)
            },
            onFailure = { error ->
                // Reset loading state
                uiState.value = state.copy(isConnecting = false)
                Timber.e(error, "Failed to create 1-1 space")
                // Show error
                commands.emit(
                    Command.Toast.Error(
                        error.message ?: "Failed to create 1-on-1 space"
                    )
                )
            }
        )
    }

    private suspend fun navigateToOneToOneChat(spaceId: Id, isNewSpace: Boolean) {
        val state = uiState.value
        val result = spaceManager.set(spaceId)
        if (result.isSuccess) {
            Timber.d("Successfully set space: $spaceId, navigating to vault")
            // Reset loading state
            uiState.value = state.copy(isConnecting = false)
            // Navigate to vault - it will open the current space
            commands.emit(Command.SwitchToVault(spaceId))
        } else {
            val error = result.exceptionOrNull()
            Timber.e(error, "Failed to set space: $spaceId")
            // Reset loading state
            uiState.value = state.copy(isConnecting = false)
            commands.emit(
                Command.Toast.Error(
                    error?.message ?: "Failed to open chat"
                )
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
        private val createSpace: CreateSpace,
        private val searchOneToOneChatByIdentity: SearchOneToOneChatByIdentity,
        private val spaceManager: SpaceManager
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
                createSpace = createSpace,
                searchOneToOneChatByIdentity = searchOneToOneChatByIdentity,
                spaceManager = spaceManager
            ) as T
        }
    }
}