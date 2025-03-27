package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.defaultTypeChanged
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Filepath
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SpaceType
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteView
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.debugging.DebugSpaceShareDownloader
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.launch.SetDefaultObjectType
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.multiplayer.sharedSpaceCount
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.payments.GetMembershipStatus
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.spaces.DeleteSpace
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.wallpaper.ObserveWallpaper
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem.Spacer
import javax.inject.Inject
import kotlin.collections.map
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceSettingsViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val setSpaceDetails: SetSpaceDetails,
    private val spaceManager: SpaceManager,
    private val gradientProvider: SpaceGradientProvider,
    private val urlBuilder: UrlBuilder,
    private val deleteSpace: DeleteSpace,
    private val debugSpaceShareDownloader: DebugSpaceShareDownloader,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val userPermissionProvider: UserPermissionProvider,
    private val spaceViewContainer: SpaceViewSubscriptionContainer,
    private val activeSpaceMemberSubscriptionContainer: ActiveSpaceMemberSubscriptionContainer,
    private val getMembership: GetMembershipStatus,
    private val uploadFile: UploadFile,
    private val profileContainer: ProfileSubscriptionManager,
    private val getDefaultObjectType: GetDefaultObjectType,
    private val setDefaultObjectType: SetDefaultObjectType,
    private val observeWallpaper: ObserveWallpaper,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val appActionManager: AppActionManager,
    private val getSpaceInviteLink: GetSpaceInviteLink
): BaseViewModel() {

    val commands = MutableSharedFlow<Command>()
    val isDismissed = MutableStateFlow(false)

    val uiState = MutableStateFlow<UiSpaceSettingsState>(UiSpaceSettingsState.Initial)

    val permissions = MutableStateFlow(SpaceMemberPermissions.NO_PERMISSIONS)

    init {
        Timber.d("SpaceSettingsViewModel, Init, vmParams: $vmParams")
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenSettingSpacesSpaceIndex
            )
        }
        proceedWithObservingSpaceView()
    }

    private fun proceedWithObservingSpaceView() {

        val restrictions = combine(
            userPermissionProvider.observe(vmParams.space),
            spaceViewContainer.sharedSpaceCount(userPermissionProvider.all()),
            profileContainer
                .observe()
                .map { wrapper ->
                    wrapper.getValue<Double?>(Relations.SHARED_SPACES_LIMIT)?.toInt() ?: 0
                },
        ) { permission, sharedSpaceCount, sharedSpaceLimit ->
            Triple(permission, sharedSpaceCount, sharedSpaceLimit)
        }


        val otherFlows = combine(
            spaceViewContainer.observe(vmParams.space),
            activeSpaceMemberSubscriptionContainer.observe(vmParams.space),
            observeWallpaper.build()
        ) { spaceView, spaceMembers, wallpaper ->
            Triple(spaceView, spaceMembers, wallpaper)
        }

        viewModelScope.launch {

            val defaultObjectTypeResponse = getDefaultObjectType
                .async(params = vmParams.space)
                .getOrNull()

            val defaultObjectTypeSettingItem: UiSpaceSettingsItem.DefaultObjectType

            if (defaultObjectTypeResponse != null) {
                val defaultType = storeOfObjectTypes.get(defaultObjectTypeResponse.id.id)
                defaultObjectTypeSettingItem = UiSpaceSettingsItem.DefaultObjectType(
                    id = defaultType?.id,
                    name = defaultType?.name.orEmpty(),
                    icon = ObjectIcon.TypeIcon.Fallback.DEFAULT
                )
            } else {
                defaultObjectTypeSettingItem = UiSpaceSettingsItem.DefaultObjectType(
                    id = null,
                    name = EMPTY_STRING_VALUE,
                    icon = ObjectIcon.None
                )
            }

            combine(
                restrictions,
                otherFlows
            ) { (permission, sharedSpaceCount, sharedSpaceLimit), (spaceView, spaceMembers, wallpaper) ->

                Timber.d("Got shared space limit: $sharedSpaceLimit, shared space count: $sharedSpaceCount")

                val spaceCreator = if (spaceMembers is ActiveSpaceMemberSubscriptionContainer.Store.Data) {
                    spaceMembers.members.find { it.id == spaceView.getValue<Id>(Relations.CREATOR) }
                } else {
                    null
                }
                val createdByNameOrId = spaceCreator?.globalName?.takeIf { it.isNotEmpty() } ?: spaceCreator?.identity

                val spaceMemberCount = if (spaceMembers is ActiveSpaceMemberSubscriptionContainer.Store.Data) {
                    spaceMembers.members.size
                } else {
                    0
                }

                val requests: Int = if (spaceMembers is ActiveSpaceMemberSubscriptionContainer.Store.Data) {
                    spaceMembers.members.count { it.status == ParticipantStatus.JOINING }
                } else {
                    0
                }

                val spaceTechInfo = SpaceTechInfo(
                    spaceId = vmParams.space,
                    createdBy = createdByNameOrId.orEmpty(),
                    creationDateInMillis = spaceView
                        .getValue<Double?>(Relations.CREATED_DATE)
                        ?.let { timeInSeconds -> (timeInSeconds * 1000L).toLong() }
                    ,
                    networkId = spaceManager.getConfig(vmParams.space)?.network.orEmpty()
                )

                // TODO In the next PR : show different settings for viewer

                val items = buildList<UiSpaceSettingsItem> {
                    add(
                        UiSpaceSettingsItem.Icon(
                            icon = spaceView.spaceIcon(
                                builder = urlBuilder,
                                spaceGradientProvider = gradientProvider
                            )
                        )
                    )
                    add(Spacer(height = 24))
                    add(
                        UiSpaceSettingsItem.Name(
                            name = spaceView.name.orEmpty()
                        )
                    )
                    add(
                        Spacer(height = 8),
                    )
                    add(
                        UiSpaceSettingsItem.Description(
                            description = spaceView.description.orEmpty()
                        )
                    )

                    if (spaceView.spaceAccessType == SpaceAccessType.SHARED) {
                        add(Spacer(height = 8))
                        add(UiSpaceSettingsItem.Multiplayer)
                    }

                    add(UiSpaceSettingsItem.Section.Collaboration)

                    if (spaceView.spaceAccessType == SpaceAccessType.SHARED) {
                        add(UiSpaceSettingsItem.Members(count = spaceMemberCount))
                    } else {
                        add(UiSpaceSettingsItem.InviteMembers)
                    }

                    add(UiSpaceSettingsItem.Section.ContentModel)
                    add(UiSpaceSettingsItem.ObjectTypes)
                    add(Spacer(height = 8))
                    add(UiSpaceSettingsItem.Fields)

                    add(UiSpaceSettingsItem.Section.Preferences)
                    add(defaultObjectTypeSettingItem)
                    add(Spacer(height = 8))
                    add(UiSpaceSettingsItem.Wallpapers(current = wallpaper))

                    add(UiSpaceSettingsItem.Section.DataManagement)
                    add(UiSpaceSettingsItem.RemoteStorage)
                    add(Spacer(height = 8))
                    add(UiSpaceSettingsItem.Bin)

                    add(UiSpaceSettingsItem.Section.Misc)
                    add(UiSpaceSettingsItem.SpaceInfo)
                    add(Spacer(height = 8))
                    add(UiSpaceSettingsItem.DeleteSpace)
                    add(Spacer(height = 32))
                }

                UiSpaceSettingsState.SpaceSettings(
                    spaceTechInfo = spaceTechInfo,
                    items = items,
                    isEditEnabled = permission?.isOwnerOrEditor() == true
                )

            }.collect { update ->
                uiState.value = update
            }
        }
    }

    fun onUiEvent(uiEvent: UiEvent) {
        Timber.d("onUiEvent: $uiEvent")
        when(uiEvent) {
            UiEvent.IconMenu.OnRemoveIconClicked -> {
                proceedWithRemovingSpaceIcon()
            }
            UiEvent.OnBackPressed -> {
                isDismissed.value = true
            }
            UiEvent.OnDeleteSpaceClicked -> {
                viewModelScope.launch { commands.emit(Command.ShowDeleteSpaceWarning) }
            }
            UiEvent.OnLeaveSpaceClicked -> {
                viewModelScope.launch { commands.emit(Command.ShowLeaveSpaceWarning) }
            }
            UiEvent.OnRemoteStorageClick -> {
                viewModelScope.launch {
                    commands.emit(Command.ManageRemoteStorage)
                }
            }
            UiEvent.OnBinClick -> {
                viewModelScope.launch {
                    commands.emit(Command.ManageBin(vmParams.space))
                }
            }
            UiEvent.OnInviteClicked -> {
                viewModelScope.launch {
                    commands.emit(
                        Command.ManageSharedSpace(vmParams.space)
                    )
                }
            }
            UiEvent.OnPersonalizationClicked -> {
                sendToast("Coming soon")
            }
            UiEvent.OnQrCodeClicked -> {
                viewModelScope.launch {
                    getSpaceInviteLink
                        .async(vmParams.space)
                        .onFailure {
                            commands.emit(
                                Command.ManageSharedSpace(vmParams.space)
                            )
                        }
                        .onSuccess { link ->
                            commands.emit(
                                Command.ShowInviteLinkQrCode(link.scheme)
                            )
                        }
                }
            }
            is UiEvent.OnSaveDescriptionClicked -> {
                viewModelScope.launch {
                    setSpaceDetails.async(
                        params = SetSpaceDetails.Params(
                            space = vmParams.space,
                            details = mapOf(
                                Relations.DESCRIPTION to uiEvent.description
                            )
                        )
                    )
                }
            }
            is UiEvent.OnSaveTitleClicked -> {
                viewModelScope.launch {
                    setSpaceDetails.async(
                        params = SetSpaceDetails.Params(
                            space = vmParams.space,
                            details = mapOf(
                                Relations.NAME to uiEvent.title
                            )
                        )
                    )
                }
            }
            is UiEvent.OnSpaceImagePicked -> {
                proceedWithSettingSpaceImage(uiEvent.uri)
            }
            is UiEvent.OnSelectWallpaperClicked -> {
                viewModelScope.launch {
                    commands.emit(Command.OpenWallpaperPicker)
                }
            }
            is UiEvent.OnSpaceMembersClicked -> {
                viewModelScope.launch {
                    commands.emit(Command.ManageSharedSpace(vmParams.space))
                }
            }
            is UiEvent.OnDefaultObjectTypeClicked -> {
                viewModelScope.launch {
                    commands.emit(
                        Command.SelectDefaultObjectType(
                            space = vmParams.space,
                            excludedTypeIds = buildList {
                                val curr = uiEvent.currentDefaultObjectTypeId
                                if (!curr.isNullOrEmpty()) add(curr)
                            }
                        )
                    )
                }
            }
        }
    }

    fun onStop() {
        // TODO unsubscribe
    }

//    fun onSpaceDebugClicked() {
//        proceedWithSpaceDebug()
//    }
//
    private fun proceedWithRemovingSpaceIcon() {
        viewModelScope.launch {
            setSpaceDetails.async(
                SetSpaceDetails.Params(
                    space = vmParams.space,
                    details = mapOf(
                        Relations.ICON_OPTION to spaceGradientProvider.randomId().toDouble(),
                        Relations.ICON_IMAGE to "",
                    )
                )
            )
        }
    }

//    fun onDeleteSpaceClicked() {
//        viewModelScope.launch {
//            val state = spaceViewState.value as? SpaceData.Success ?: return@launch
//            if (state.isUserOwner) {
//                commands.emit(Command.ShowDeleteSpaceWarning)
//                analytics.sendEvent(
//                    eventName = EventsDictionary.clickDeleteSpace,
//                    props = Props(mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.settings))
//                )
//            } else {
//                commands.emit(Command.ShowLeaveSpaceWarning)
//                analytics.sendEvent(eventName = screenLeaveSpace)
//            }
//        }
//    }

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
        viewModelScope.launch {
                deleteSpace.async(params = vmParams.space).fold(
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
    }

    // What is below is candidate to legacy. Might be deleted soon.

//    private fun proceedWithSpaceDebug() {
//        viewModelScope.launch {
//            debugSpaceShareDownloader
//                .stream(Unit)
//                .collect { result ->
//                    result.fold(
//                        onLoading = { sendToast(SPACE_DEBUG_MSG) },
//                        onSuccess = { path -> commands.emit(Command.ShareSpaceDebug(path)) }
//                    )
//                }
//        }
//    }

//    fun onSharePrivateSpaceClicked() {
//        viewModelScope.launch {
//            val data = spaceViewState.value as? SpaceData.Success ?: return@launch
//            when(data.spaceType) {
//                PRIVATE_SPACE_TYPE -> {
//                    analytics.sendEvent(
//                        eventName = EventsDictionary.screenSettingsSpaceShare,
//                        props = Props(
//                            mapOf(
//                                EventsPropertiesKey.route to EventsDictionary.Routes.settings
//                            )
//                        )
//                    )
//                }
//                SHARED_SPACE_TYPE -> {
//                    analytics.sendEvent(
//                        eventName = EventsDictionary.screenSettingsSpaceMembers,
//                        props = Props(
//                            mapOf(
//                                EventsPropertiesKey.route to EventsDictionary.Routes.settings
//                            )
//                        )
//                    )
//                }
//            }
//        }
//        viewModelScope.launch {
//            val data = spaceViewState.value as? SpaceData.Success ?: return@launch
//            val shareLimits = data.shareLimitReached
//            if (!shareLimits.shareLimitReached) {
//                commands.emit(Command.SharePrivateSpace(params.space))
//            } else {
//                commands.emit(Command.ShowShareLimitReachedError)
//            }
//        }
//    }

//    private fun resolveIsSpaceDeletable(spaceView: ObjectWrapper.SpaceView) : Boolean {
//        return spaceView.spaceAccessType != null
//    }

//    fun onAddMoreSpacesClicked() {
//        viewModelScope.launch {
//            getMembership.async(GetMembershipStatus.Params(noCache = false)).fold(
//                onSuccess = { membership ->
//                    if (membership != null) {
//                        val activeTier = TierId(membership.tier)
//                        if (activeTier.isPossibleToUpgrade(reason = MembershipUpgradeReason.NumberOfSharedSpaces)) {
//                            commands.emit(Command.NavigateToMembership)
//                        } else {
//                            commands.emit(Command.NavigateToMembershipUpdate)
//                        }
//                    }
//                },
//                onFailure = {
//                    Timber.e(it, "Error while getting membership status")
//                    commands.emit(Command.NavigateToMembershipUpdate)
//                }
//            )
//        }
//    }

    fun proceedWithSettingSpaceImage(path: String) {
        Timber.d("onSpaceImageClicked: $path")
        viewModelScope.launch {
            uploadFile.async(
                params = UploadFile.Params(
                    path = path,
                    space = vmParams.space,
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
                space = vmParams.space,
                details = mapOf(
                    Relations.ICON_IMAGE to file.id,
                    Relations.ICON_OPTION to null,
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

    fun onSelectObjectType(type: ObjectWrapper.Type) {
        // Setting space default object type
        viewModelScope.launch {
            val params = SetDefaultObjectType.Params(
                space = vmParams.space,
                type = TypeId(type.id)
            )
            setDefaultObjectType.async(params).fold(
                onFailure = {
                    Timber.e(it, "Error while setting default object type")
                },
                onSuccess = {
                    when(val state = uiState.value) {
                        is UiSpaceSettingsState.SpaceSettings -> {
                            uiState.value = state.copy(
                                items = state.items.map { item ->
                                    if (item is UiSpaceSettingsItem.DefaultObjectType) {
                                        UiSpaceSettingsItem.DefaultObjectType(
                                            id = type.id,
                                            name = type.name.orEmpty(),
                                            icon = ObjectIcon.TypeIcon.Fallback.DEFAULT
                                        )
                                    } else {
                                        item
                                    }
                                }
                            )
                        }
                        else -> {
                            Timber.w("Unexpected ui state when updating object type: $state")
                        }
                    }
                    analytics.registerEvent(
                        EventAnalytics.Anytype(
                            name = defaultTypeChanged,
                            props = Props(
                                mapOf(
                                    EventsPropertiesKey.objectType to type.uniqueKey,
                                    EventsPropertiesKey.route to "Settings"
                                )
                            ),
                            duration = null
                        )
                    )
                }
            )
        }
        // Updating app actions (app shortcuts):
        viewModelScope.launch {
            val types = buildList<ObjectWrapper.Type> {
                add(type)
                val note = storeOfObjectTypes.getByKey(
                    ObjectTypeUniqueKeys.NOTE
                )
                val page = storeOfObjectTypes.getByKey(
                    ObjectTypeUniqueKeys.PAGE
                )
                val task = storeOfObjectTypes.getByKey(
                    ObjectTypeUniqueKeys.TASK
                )
                if (note != null) add(note)
                if (page != null) add(page)
                if (task != null) add(task)
            }
            val actions = types.map { type ->
                AppActionManager.Action.CreateNew(
                    type = TypeKey(type.uniqueKey),
                    name = type.name.orEmpty()
                )
            }
            appActionManager.setup(actions = actions)
        }
    }

    data class SpaceData(
        val spaceId: Id?,
        val createdDateInMillis: Long?,
        val createdBy: Id?,
        val network: Id?,
        val name: String,
        val description: String,
        val icon: SpaceIconView,
        val isDeletable: Boolean = false,
        val spaceType: SpaceType,
        val shareLimitReached: ShareLimitsState,
        val requests: Int = 0,
        val isEditEnabled: Boolean,
        val isUserOwner: Boolean,
        val permissions: SpaceMemberPermissions,
    )

    data class ShareLimitsState(
        val shareLimitReached: Boolean = false,
        val sharedSpacesLimit: Int = 0
    )

    sealed class Command {
        data class ShareSpaceDebug(val filepath: Filepath) : Command()
        data class SharePrivateSpace(val space: SpaceId) : Command()
        data class ManageSharedSpace(val space: SpaceId) : Command()
        data class ShowInviteLinkQrCode(val link: String) : Command()
        data class ManageBin(val space: SpaceId) : Command()
        data class SelectDefaultObjectType(val space: SpaceId, val excludedTypeIds: List<Id>) : Command()
        data object ExitToVault : Command()
        data object ShowDeleteSpaceWarning : Command()
        data object ShowLeaveSpaceWarning : Command()
        data object ShowShareLimitReachedError : Command()
        data object NavigateToMembership : Command()
        data object NavigateToMembershipUpdate : Command()
        data object OpenWallpaperPicker : Command()
        data object ManageRemoteStorage : Command()
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
        private val debugFileShareDownloader: DebugSpaceShareDownloader,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val userPermissionProvider: UserPermissionProvider,
        private val activeSpaceMemberSubscriptionContainer: ActiveSpaceMemberSubscriptionContainer,
        private val getMembership: GetMembershipStatus,
        private val uploadFile: UploadFile,
        private val profileContainer: ProfileSubscriptionManager,
        private val getDefaultObjectType: GetDefaultObjectType,
        private val setDefaultObjectType: SetDefaultObjectType,
        private val observeWallpaper: ObserveWallpaper,
        private val appActionManager: AppActionManager,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val getSpaceInviteLink: GetSpaceInviteLink
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
            debugSpaceShareDownloader = debugFileShareDownloader,
            spaceGradientProvider = spaceGradientProvider,
            vmParams = params,
            userPermissionProvider = userPermissionProvider,
            activeSpaceMemberSubscriptionContainer = activeSpaceMemberSubscriptionContainer,
            getMembership = getMembership,
            uploadFile = uploadFile,
            profileContainer = profileContainer,
            getDefaultObjectType = getDefaultObjectType,
            setDefaultObjectType = setDefaultObjectType,
            observeWallpaper = observeWallpaper,
            appActionManager = appActionManager,
            storeOfObjectTypes = storeOfObjectTypes,
            getSpaceInviteLink = getSpaceInviteLink
        ) as T
    }

    data class VmParams(val space: SpaceId)

    companion object {
        const val SPACE_DEBUG_MSG = "Kindly share this debug logs with Anytype developers."
    }
}