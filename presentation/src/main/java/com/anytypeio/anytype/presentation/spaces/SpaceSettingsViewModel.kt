package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.screenLeaveSpace
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Filepath
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SpaceType
import com.anytypeio.anytype.core_models.UNKNOWN_SPACE_TYPE
import com.anytypeio.anytype.core_models.asSpaceType
import com.anytypeio.anytype.core_models.ext.isPossibleToUpgrade
import com.anytypeio.anytype.core_models.membership.MembershipUpgradeReason
import com.anytypeio.anytype.core_models.membership.TierId
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.debugging.DebugSpaceShareDownloader
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.multiplayer.sharedSpaceCount
import com.anytypeio.anytype.domain.payments.GetMembershipStatus
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.spaces.DeleteSpace
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceSettingsViewModel(
    private val params: VmParams,
    private val analytics: Analytics,
    private val setSpaceDetails: SetSpaceDetails,
    private val spaceManager: SpaceManager,
    private val gradientProvider: SpaceGradientProvider,
    private val urlBuilder: UrlBuilder,
    private val deleteSpace: DeleteSpace,
    private val configStorage: ConfigStorage,
    private val debugSpaceShareDownloader: DebugSpaceShareDownloader,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val userPermissionProvider: UserPermissionProvider,
    private val spaceViewContainer: SpaceViewSubscriptionContainer,
    private val activeSpaceMemberSubscriptionContainer: ActiveSpaceMemberSubscriptionContainer,
    private val getMembership: GetMembershipStatus,
    private val uploadFile: UploadFile,
    private val profileContainer: ProfileSubscriptionManager
): BaseViewModel() {

    val commands = MutableSharedFlow<Command>()
    val isDismissed = MutableStateFlow(false)
    val spaceViewState = MutableStateFlow<ViewState<SpaceData>>(ViewState.Init)

    val permissions = MutableStateFlow(SpaceMemberPermissions.NO_PERMISSIONS)

    private val spaceConfig = spaceManager.getConfig()

    init {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenSettingSpacesSpaceIndex
            )
        }
        proceedWithObservingSpaceView()
    }

    private fun proceedWithObservingSpaceView() {
        viewModelScope.launch {
            val config = spaceManager.getConfig(params.space)
            combine(
                spaceViewContainer.observe(params.space),
                userPermissionProvider.observe(params.space),
                profileContainer
                    .observe()
                    .map { wrapper ->
                        wrapper.getValue<Double?>(Relations.SHARED_SPACES_LIMIT)?.toInt() ?: 0
                         },
                spaceViewContainer.sharedSpaceCount(userPermissionProvider.all()),
                activeSpaceMemberSubscriptionContainer.observe(params.space),
            ) { spaceView, permission, sharedSpaceLimit: Int, sharedSpaceCount: Int, store ->
                Timber.d("Got shared space limit: $sharedSpaceLimit, shared space count: $sharedSpaceCount")
                val requests: Int = if (store is ActiveSpaceMemberSubscriptionContainer.Store.Data) {
                    store.members.count { it.status == ParticipantStatus.JOINING }
                } else {
                    0
                }
                val spaceMember = if (store is ActiveSpaceMemberSubscriptionContainer.Store.Data) {
                    store.members.find { it.id == spaceView.getValue<Id>(Relations.CREATOR) }
                } else {
                    null
                }
                val createdBy = spaceMember?.globalName?.takeIf { it.isNotEmpty() } ?: spaceMember?.identity
                SpaceData(
                    name = spaceView.name.orEmpty(),
                    icon = spaceView.spaceIcon(
                        builder = urlBuilder,
                        spaceGradientProvider = gradientProvider
                    ),
                    createdDateInMillis = spaceView
                        .getValue<Double?>(Relations.CREATED_DATE)
                        ?.let { timeInSeconds -> (timeInSeconds * 1000L).toLong() },
                    createdBy = createdBy,
                    spaceId = params.space.id,
                    network = config?.network.orEmpty(),
                    isDeletable = resolveIsSpaceDeletable(spaceView),
                    spaceType = spaceView.spaceAccessType?.asSpaceType() ?: UNKNOWN_SPACE_TYPE,
                    permissions = permission ?: SpaceMemberPermissions.NO_PERMISSIONS,
                    shareLimitReached = ShareLimitsState(
                        shareLimitReached = sharedSpaceCount >= sharedSpaceLimit,
                        sharedSpacesLimit = sharedSpaceLimit
                    ),
                    requests = requests
                )
            }.collect { spaceData ->
                Timber.d("Space data: ${spaceData}")
                spaceViewState.value = ViewState.Success(spaceData)
            }
        }
    }

    fun onNameSet(name: String) {
        Timber.d("onNameSet")
        if (name.isEmpty()) return
        if (isDismissed.value) return
        viewModelScope.launch {
            if (spaceConfig != null) {
                setSpaceDetails.async(
                    SetSpaceDetails.Params(
                        space = SpaceId(spaceConfig.space),
                        details = mapOf(Relations.NAME to name)
                    )
                ).fold(
                    onFailure = {
                        Timber.e(it, "Error while updating object details")
                        sendToast("Something went wrong. Please try again")
                    },
                    onSuccess = {
                        Timber.d("Name successfully set for current space: ${spaceConfig.space}")
                    }
                )
            } else {
                Timber.w("Something went wrong: config is empty")
            }
        }
    }

    fun onStop() {
        // TODO unsubscribe
    }

    fun onSpaceDebugClicked() {
        proceedWithSpaceDebug()
    }

    fun onRandomSpaceGradientClicked() {
        viewModelScope.launch {
            val config = spaceConfig
            if (config != null) {
                setSpaceDetails.async(
                    SetSpaceDetails.Params(
                        space = SpaceId(config.space),
                        details = mapOf(
                            Relations.ICON_OPTION to spaceGradientProvider.randomId().toDouble(),
                            Relations.ICON_IMAGE to null,
                            Relations.ICON_EMOJI to null
                        )
                    )
                )
            }
        }
    }

    fun onDeleteSpaceClicked() {
        viewModelScope.launch {
            val state = spaceViewState.value
            if (state is ViewState.Success) {
                if (state.data.permissions.isOwner()) {
                    commands.emit(Command.ShowDeleteSpaceWarning)
                    analytics.sendEvent(
                        eventName = EventsDictionary.clickDeleteSpace,
                        props = Props(mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.settings))
                    )
                } else {
                    commands.emit(Command.ShowLeaveSpaceWarning)
                    analytics.sendEvent(eventName = screenLeaveSpace)
                }
            }
        }
    }

    fun onDeleteSpaceWarningCancelled() {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.clickDeleteSpaceWarning,
                props = Props(
                    mapOf(
                        EventsPropertiesKey.type to "Cancel"
                    )
                )
            )
        }
    }

    fun onDeleteSpaceAcceptedClicked() {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.clickDeleteSpaceWarning,
                props = Props(
                    mapOf(
                        EventsPropertiesKey.type to "Delete"
                    )
                )
            )
        }
        proceedWithSpaceDeletion()
    }

    fun onLeaveSpaceAcceptedClicked() {
        viewModelScope.launch {
            analytics.sendEvent(eventName = EventsDictionary.leaveSpace)
        }
        proceedWithSpaceDeletion()
    }

    private fun proceedWithSpaceDeletion() {
        val state = spaceViewState.value
        if (state is ViewState.Success) {
            val space = state.data.spaceId
            if (space != null) {
                viewModelScope.launch {
                    deleteSpace.async(params = SpaceId(space)).fold(
                        onSuccess = {
                            analytics.sendEvent(
                                eventName = EventsDictionary.deleteSpace,
                                props = Props(mapOf(EventsPropertiesKey.type to "Private"))
                            )
                            spaceManager.clear()
                            commands.emit(Command.ExitToVault)
                        },
                        onFailure = {
                            Timber.e(it, "Error while deleting space")
                        }
                    )
                }
            } else {
                sendToast("Space not found. Please, try again later")
            }
        }
    }

    private fun proceedWithSpaceDebug() {
        viewModelScope.launch {
            debugSpaceShareDownloader
                .stream(Unit)
                .collect { result ->
                    result.fold(
                        onLoading = { sendToast(SPACE_DEBUG_MSG) },
                        onSuccess = { path -> commands.emit(Command.ShareSpaceDebug(path)) }
                    )
                }
        }
    }

    fun onManageSharedSpaceClicked() {
        viewModelScope.launch {
            commands.emit(
                Command.ManageSharedSpace(params.space)
            )
        }
    }

    fun onSharePrivateSpaceClicked() {
        viewModelScope.launch {
            val data = spaceViewState.value
            if (data is ViewState.Success) {
                val shareLimits = data.data.shareLimitReached
                if (!shareLimits.shareLimitReached) {
                    commands.emit(Command.SharePrivateSpace(params.space))
                } else {
                    commands.emit(Command.ShowShareLimitReachedError)
                }
            }
        }
    }

    private fun resolveIsSpaceDeletable(spaceView: ObjectWrapper.SpaceView) : Boolean {
        return spaceView.spaceAccessType != null
    }

    fun onAddMoreSpacesClicked() {
        viewModelScope.launch {
            getMembership.async(GetMembershipStatus.Params(noCache = false)).fold(
                onSuccess = { membership ->
                    if (membership != null) {
                        val activeTier = TierId(membership.tier)
                        if (activeTier.isPossibleToUpgrade(reason = MembershipUpgradeReason.NumberOfSharedSpaces)) {
                            commands.emit(Command.NavigateToMembership)
                        } else {
                            commands.emit(Command.NavigateToMembershipUpdate)
                        }
                    }
                },
                onFailure = {
                    Timber.e(it, "Error while getting membership status")
                    commands.emit(Command.NavigateToMembershipUpdate)
                }
            )
        }
    }

    fun onSpaceImagePicked(path: String) {
        Timber.d("onSpaceImageClicked: $path")
        viewModelScope.launch {
            uploadFile.async(
                params = UploadFile.Params(
                    path = path,
                    space = params.space,
                    type = Block.Content.File.Type.IMAGE
                )
            ).fold(
                onSuccess = { file ->
                    proceedWithSettingSpaceIconImage(file)
                },
                onFailure = {
                    Timber.e(it, "Error while uploading image as space icon")
                }
            )
        }
    }

    private suspend fun proceedWithSettingSpaceIconImage(file: ObjectWrapper.File) {
        setSpaceDetails.async(
            SetSpaceDetails.Params(
                space = params.space,
                details = mapOf(
                    Relations.ICON_OPTION to null,
                    Relations.ICON_IMAGE to file.id,
                    Relations.ICON_EMOJI to null
                )
            )
        ).fold(
            onSuccess = {
                Timber.d("Successfully set image as space icon.")
            },
            onFailure = { e ->
                Timber.e(e, "Error while setting image as space icon")
            }
        )
    }

    data class SpaceData(
        val spaceId: Id?,
        val createdDateInMillis: Long?,
        val createdBy: Id?,
        val network: Id?,
        val name: String,
        val icon: SpaceIconView,
        val isDeletable: Boolean = false,
        val spaceType: SpaceType,
        val permissions: SpaceMemberPermissions,
        val shareLimitReached: ShareLimitsState,
        val requests: Int = 0
    )

    data class ShareLimitsState(
        val shareLimitReached: Boolean = false,
        val sharedSpacesLimit: Int = 0
    )

    sealed class Command {
        data class ShareSpaceDebug(val filepath: Filepath) : Command()
        data class SharePrivateSpace(val space: SpaceId) : Command()
        data class ManageSharedSpace(val space: SpaceId) : Command()
        data object ExitToVault : Command()
        data object ShowDeleteSpaceWarning : Command()
        data object ShowLeaveSpaceWarning : Command()
        data object ShowShareLimitReachedError : Command()
        data object NavigateToMembership : Command()
        data object NavigateToMembershipUpdate : Command()
    }

    class Factory @Inject constructor(
        private val params: VmParams,
        private val analytics: Analytics,
        private val container: SpaceViewSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val setSpaceDetails: SetSpaceDetails,
        private val gradientProvider: SpaceGradientProvider,
        private val spaceManager: SpaceManager,
        private val deleteSpace: DeleteSpace,
        private val configStorage: ConfigStorage,
        private val debugFileShareDownloader: DebugSpaceShareDownloader,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val userPermissionProvider: UserPermissionProvider,
        private val activeSpaceMemberSubscriptionContainer: ActiveSpaceMemberSubscriptionContainer,
        private val getMembership: GetMembershipStatus,
        private val uploadFile: UploadFile,
        private val profileContainer: ProfileSubscriptionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = SpaceSettingsViewModel(
            spaceViewContainer = container,
            urlBuilder = urlBuilder,
            spaceManager = spaceManager,
            setSpaceDetails = setSpaceDetails,
            gradientProvider = gradientProvider,
            analytics = analytics,
            deleteSpace = deleteSpace,
            configStorage = configStorage,
            debugSpaceShareDownloader = debugFileShareDownloader,
            spaceGradientProvider = spaceGradientProvider,
            params = params,
            userPermissionProvider = userPermissionProvider,
            activeSpaceMemberSubscriptionContainer = activeSpaceMemberSubscriptionContainer,
            getMembership = getMembership,
            uploadFile = uploadFile,
            profileContainer = profileContainer
        ) as T
    }

    data class VmParams(val space: SpaceId)

    companion object {
        const val SPACE_DEBUG_MSG = "Kindly share this debug logs with Anytype developers."
    }
}