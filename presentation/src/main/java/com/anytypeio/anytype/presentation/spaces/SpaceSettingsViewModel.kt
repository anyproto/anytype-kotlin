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
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.launch.SetDefaultObjectType
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.CopyInviteLinkToClipboard
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.multiplayer.sharedSpaceCount
import com.anytypeio.anytype.domain.`object`.FetchObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.spaces.DeleteSpace
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails.*
import com.anytypeio.anytype.domain.wallpaper.ObserveWallpaper
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.anytypeio.anytype.domain.invite.GetCurrentInviteAccessLevel
import com.anytypeio.anytype.domain.invite.SpaceInviteLinkStore
import com.anytypeio.anytype.domain.notifications.SetSpaceNotificationMode
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel.Command.*
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem.*
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem.Spacer
import javax.inject.Inject
import kotlin.collections.map
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
    private val spaceGradientProvider: SpaceGradientProvider,
    private val userPermissionProvider: UserPermissionProvider,
    private val spaceViewContainer: SpaceViewSubscriptionContainer,
    private val activeSpaceMemberSubscriptionContainer: ActiveSpaceMemberSubscriptionContainer,
    private val uploadFile: UploadFile,
    private val profileContainer: ProfileSubscriptionManager,
    private val getDefaultObjectType: GetDefaultObjectType,
    private val setDefaultObjectType: SetDefaultObjectType,
    private val observeWallpaper: ObserveWallpaper,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val appActionManager: AppActionManager,
    private val copyInviteLinkToClipboard: CopyInviteLinkToClipboard,
    private val fetchObject: FetchObject,
    private val setObjectDetails: SetObjectDetails,
    private val getAccount: GetAccount,
    private val notificationPermissionManager: NotificationPermissionManager,
    private val setSpaceNotificationMode: SetSpaceNotificationMode,
    private val deviceTokenStoringService: DeviceTokenStoringService,
    private val getCurrentInviteAccessLevel: GetCurrentInviteAccessLevel,
    private val spaceInviteLinkStore: SpaceInviteLinkStore
): BaseViewModel() {

    val commands = MutableSharedFlow<Command>()
    val isDismissed = MutableStateFlow(false)

    val uiState = MutableStateFlow<UiSpaceSettingsState>(UiSpaceSettingsState.Initial)

    val permissions = MutableStateFlow(SpaceMemberPermissions.NO_PERMISSIONS)

    val _notificationState = MutableStateFlow(NotificationState.ALL)

    val uiQrCodeState = MutableStateFlow<UiSpaceQrCodeState>(UiSpaceQrCodeState.Hidden)
    
    private val spaceInfoTitleClickCount = MutableStateFlow(0)
    val inviteLinkAccessLevel = MutableStateFlow<SpaceInviteLinkAccessLevel>(SpaceInviteLinkAccessLevel.LinkDisabled)

    init {
        Timber.d("SpaceSettingsViewModel, Init, vmParams: $vmParams")
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenSettingSpacesSpaceIndex
            )
        }
        proceedWithObservingSpaceView()
        subscribeToInviteLinkState()
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

            var widgetAutoCreationPreference: UiSpaceSettingsItem.AutoCreateWidgets? = null

            val config = spaceManager.getConfig(vmParams.space)

            if (config != null) {
                val widget = fetchObject.async(
                    FetchObject.Params(
                        space = vmParams.space,
                        obj = config.widgets,
                        keys = listOf(
                            Relations.ID,
                            Relations.AUTO_WIDGET_DISABLED
                        )
                    )
                ).getOrNull()

                if (widget != null) {
                    widgetAutoCreationPreference = UiSpaceSettingsItem.AutoCreateWidgets(
                        widget = widget.id,
                        isAutoCreateEnabled = widget.getValue<Boolean>(Relations.AUTO_WIDGET_DISABLED) != true
                    )
                }
            }

            val defaultObjectTypeResponse = getDefaultObjectType
                .async(params = vmParams.space)
                .getOrNull()

            val defaultObjectTypeSettingItem: UiSpaceSettingsItem.DefaultObjectType

            if (defaultObjectTypeResponse != null) {
                val defaultType = storeOfObjectTypes.get(defaultObjectTypeResponse.id.id)
                defaultObjectTypeSettingItem = UiSpaceSettingsItem.DefaultObjectType(
                    id = defaultType?.id,
                    name = defaultType?.name.orEmpty(),
                    icon = defaultType?.objectIcon()
                        ?: ObjectIcon.TypeIcon.Fallback.DEFAULT
                )
            } else {
                defaultObjectTypeSettingItem = UiSpaceSettingsItem.DefaultObjectType(
                    id = null,
                    name = EMPTY_STRING_VALUE,
                    icon = ObjectIcon.None
                )
            }

            // Get account for toView function
            val account = getAccount.async(Unit).getOrNull()?.id

            combine(
                restrictions,
                otherFlows,
                spaceInfoTitleClickCount,
                inviteLinkAccessLevel
            ) { (permission, sharedSpaceCount, sharedSpaceLimit), (spaceView, spaceMembers, wallpaper), clickCount, inviteLink ->

                Timber.d("Got shared space limit: $sharedSpaceLimit, shared space count: $sharedSpaceCount")

                val targetSpaceId = spaceView.targetSpaceId

                val spaceCreator = if (spaceMembers is ActiveSpaceMemberSubscriptionContainer.Store.Data) {
                    spaceMembers.members.find { it.id == spaceView.getValue<Id>(Relations.CREATOR) }
                } else {
                    null
                }
                val createdByNameOrId =
                    spaceCreator?.globalName?.takeIf { it.isNotEmpty() } ?: spaceCreator?.identity

                val spaceMemberCount = if (spaceMembers is ActiveSpaceMemberSubscriptionContainer.Store.Data) {
                    spaceMembers.members.toView(
                        spaceView = spaceView,
                        urlBuilder = urlBuilder,
                        isCurrentUserOwner = permission?.isOwner() == true,
                        account = account
                    ).size
                } else {
                    0
                }

                val requests: Int = if (spaceMembers is ActiveSpaceMemberSubscriptionContainer.Store.Data) {
                    spaceMembers.members.count { it.status == ParticipantStatus.JOINING }
                } else {
                    0
                }

                val deviceToken = if (BuildConfig.DEBUG || clickCount >= 5) {
                    try {
                        deviceTokenStoringService.getToken()
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }

                val spaceTechInfo = SpaceTechInfo(
                    spaceId = vmParams.space,
                    createdBy = createdByNameOrId.orEmpty(),
                    creationDateInMillis = spaceView
                        .getValue<Double?>(Relations.CREATED_DATE)
                        ?.let { timeInSeconds -> (timeInSeconds * 1000L).toLong() }
                    ,
                    networkId = spaceManager.getConfig(vmParams.space)?.network.orEmpty(),
                    isDebugVisible = BuildConfig.DEBUG || clickCount >= 5,
                    deviceToken = deviceToken
                )

                val items = buildList {
                    add(
                        UiSpaceSettingsItem.Icon(
                            icon = spaceView.spaceIcon(urlBuilder)
                        )
                    )
                    add(Spacer(height = 24))
                    add(
                        UiSpaceSettingsItem.Name(
                            name = spaceView.name.orEmpty()
                        )
                    )
                    when (spaceView.spaceAccessType) {
                        SpaceAccessType.PRIVATE, SpaceAccessType.SHARED -> {
                            add(Spacer(height = 4))
                            add(MembersSmall(count = spaceMemberCount))
                        }
                        SpaceAccessType.DEFAULT, null -> {
                            add(Spacer(height = 4))
                            add(EntrySpace)
                        }
                    }

                    if (spaceView.isPossibleToShare) {
                        when (inviteLink) {
                            is SpaceInviteLinkAccessLevel.EditorAccess -> {
                                add(Spacer(height = 24))
                                add(InviteLink(inviteLink.link))
                                add(UiSpaceSettingsItem.Section.Collaboration)
                                add(Members(count = spaceMemberCount, withColor = true))
                            }
                            is SpaceInviteLinkAccessLevel.RequestAccess -> {
                                add(Spacer(height = 24))
                                add(InviteLink(inviteLink.link))
                                add(UiSpaceSettingsItem.Section.Collaboration)
                                add(Members(count = spaceMemberCount, withColor = true))
                            }
                            is SpaceInviteLinkAccessLevel.ViewerAccess -> {
                                add(Spacer(height = 24))
                                add(InviteLink(inviteLink.link))
                                add(UiSpaceSettingsItem.Section.Collaboration)
                                add(Members(count = spaceMemberCount, withColor = true))
                            }
                            SpaceInviteLinkAccessLevel.LinkDisabled -> {
                                add(UiSpaceSettingsItem.Section.Collaboration)
                                add(Members(count = spaceMemberCount))
                            }
                        }
                    }

                    if (!targetSpaceId.isNullOrEmpty()) {
                        // Target space is set, show change Notification mode option
                        add(Spacer(height = 8))
                        add(Notifications)
                    }

                    add(UiSpaceSettingsItem.Section.ContentModel)
                    add(UiSpaceSettingsItem.ObjectTypes)
                    add(Spacer(height = 8))
                    add(UiSpaceSettingsItem.Fields)

                    add(UiSpaceSettingsItem.Section.Preferences)
                    add(defaultObjectTypeSettingItem)
                    add(Spacer(height = 8))
                    add(UiSpaceSettingsItem.Wallpapers(current = wallpaper))
                    if (widgetAutoCreationPreference != null) {
                        add(Spacer(height = 8))
                        add(widgetAutoCreationPreference)
                    }

                    if (permission?.isOwnerOrEditor() == true) {
                        add(UiSpaceSettingsItem.Section.DataManagement)
                        add(UiSpaceSettingsItem.RemoteStorage)
                        add(Spacer(height = 8))
                        add(UiSpaceSettingsItem.Bin)
                    }

                    add(UiSpaceSettingsItem.Section.Misc)
                    add(UiSpaceSettingsItem.SpaceInfo)
                    add(Spacer(height = 8))
                    add(UiSpaceSettingsItem.DeleteSpace)
                    add(Spacer(height = 32))
                }

                UiSpaceSettingsState.SpaceSettings(
                    spaceTechInfo = spaceTechInfo,
                    items = items,
                    isEditEnabled = permission?.isOwnerOrEditor() == true,
                    notificationState = spaceView.spacePushNotificationMode,
                    targetSpaceId = targetSpaceId
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
                viewModelScope.launch { commands.emit(ShowDeleteSpaceWarning) }
            }
            UiEvent.OnLeaveSpaceClicked -> {
                viewModelScope.launch { commands.emit(ShowLeaveSpaceWarning) }
            }
            UiEvent.OnRemoteStorageClick -> {
                viewModelScope.launch {
                    commands.emit(ManageRemoteStorage)
                }
            }
            UiEvent.OnBinClick -> {
                viewModelScope.launch {
                    commands.emit(ManageBin(vmParams.space))
                }
            }
            UiEvent.OnInviteClicked -> {
                viewModelScope.launch {
                    commands.emit(
                        ManageSharedSpace(vmParams.space)
                    )
                }
            }
            UiEvent.OnPersonalizationClicked -> {
                sendToast("Coming soon")
            }
            is UiEvent.OnQrCodeClicked -> {
                viewModelScope.launch {
                    val (spaceName, spaceIcon) = when (val state = uiState.value) {
                        is UiSpaceSettingsState.SpaceSettings -> {
                            val name = state.items.filterIsInstance<Name>()
                                .firstOrNull()?.name ?: ""
                            val icon = state.items.filterIsInstance<Icon>()
                                .firstOrNull()?.icon
                            name to icon
                        }
                        else -> "" to null
                    }
                    uiQrCodeState.value = UiSpaceQrCodeState.SpaceInvite(
                        link = uiEvent.link,
                        spaceName = spaceName,
                        icon = spaceIcon
                    )
                }
            }
            is UiEvent.OnSaveDescriptionClicked -> {
                viewModelScope.launch {
                    setSpaceDetails.async(
                        params = Params(
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
                        params = Params(
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
                    commands.emit(OpenWallpaperPicker)
                }
            }
            is UiEvent.OnSpaceMembersClicked -> {
                viewModelScope.launch {
                    commands.emit(ManageSharedSpace(vmParams.space))
                }
            }
            is UiEvent.OnDefaultObjectTypeClicked -> {
                viewModelScope.launch {
                    commands.emit(
                        SelectDefaultObjectType(
                            space = vmParams.space,
                            excludedTypeIds = buildList {
                                val curr = uiEvent.currentDefaultObjectTypeId
                                if (!curr.isNullOrEmpty()) add(curr)
                            }
                        )
                    )
                }
            }
            is UiEvent.OnAutoCreateWidgetSwitchChanged -> {
                viewModelScope.launch {
                    setObjectDetails.async(
                        SetObjectDetails.Params(
                            ctx = uiEvent.widget,
                            details = mapOf(
                                Relations.AUTO_WIDGET_DISABLED to !uiEvent.isAutoCreateEnabled
                            )
                        )
                    )
                }
            }

            UiEvent.OnObjectTypesClicked -> {
                viewModelScope.launch {
                    commands.emit(OpenTypesScreen(vmParams.space))
                }
            }
            UiEvent.OnPropertiesClicked -> {
                viewModelScope.launch {
                    commands.emit(OpenPropertiesScreen(vmParams.space))
                }
            }

            is UiEvent.OnNotificationsSetting -> {
                setNotificationState(
                    targetSpaceId = uiEvent.targetSpaceId,
                    newState = when (uiEvent) {
                        is UiEvent.OnNotificationsSetting.All -> NotificationState.ALL
                        is UiEvent.OnNotificationsSetting.Mentions -> NotificationState.MENTIONS
                        is UiEvent.OnNotificationsSetting.None -> NotificationState.DISABLE
                    }
                )
            }
            UiEvent.OnDebugClicked -> {
                viewModelScope.launch {
                    commands.emit(OpenDebugScreen(vmParams.space.id))
                }
            }
            UiEvent.OnSpaceInfoTitleClicked -> {
                val currentCount = spaceInfoTitleClickCount.value
                spaceInfoTitleClickCount.value = currentCount + 1
            }

            is UiEvent.OnCopyLinkClicked -> {
                viewModelScope.launch {
                    val params = CopyInviteLinkToClipboard.Params(uiEvent.link)
                    copyInviteLinkToClipboard.invoke(params)
                        .proceed(
                            failure = {
                                Timber.e(it, "Failed to copy invite link to clipboard")
                                sendToast("Failed to copy invite link")
                            },
                            success = {
                                Timber.d("Invite link copied to clipboard: ${uiEvent.link}")
                                sendToast("Invite link copied to clipboard")
                            }
                        )
                }
            }
            is UiEvent.OnShareLinkClicked -> {
                viewModelScope.launch {
                    commands.emit(
                        ShareInviteLink(uiEvent.link)
                    )
                }
            }
        }
    }

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

    fun updateNotificationState() {
        viewModelScope.launch {
            // Check if notifications are enabled system-wide
            if (!notificationPermissionManager.shouldShowPermissionDialog()) {
                // Permissions are granted
                //todo update UI accordingly
            } else {
                // Permissions not granted, show disabled state
                //todo update UI accordingly
                Timber.d("Notification permissions not granted")
            }
        }
    }

    fun setNotificationState(targetSpaceId: Id?, newState: NotificationState) {
        if (targetSpaceId == null) {
            Timber.e("Cannot set notification state: space ID is null")
            return
        }
        viewModelScope.launch {
            // Check if trying to enable notifications without system permission
            if (newState != NotificationState.DISABLE && notificationPermissionManager.shouldShowPermissionDialog()) {
                // Need to request permission first
                commands.emit(Command.RequestNotificationPermission)
                return@launch
            }
            
            // Call backend to set notification state
            setSpaceNotificationMode.async(
                SetSpaceNotificationMode.Params(
                    spaceViewId = targetSpaceId,
                    mode = newState
                )
            ).fold(
                onSuccess = {
                    _notificationState.value = newState
                    Timber.d("Successfully set notification state to: $newState for space: $targetSpaceId")
                },
                onFailure = { error ->
                    Timber.e("Failed to set notification state: $error")
                    sendToast("Failed to update notification settings")
                    // Don't update the state on failure to show the user the actual current state
                }
            )
        }
    }

    // Notification permission handling methods
    fun onNotificationPermissionGranted() {
        viewModelScope.launch {
            notificationPermissionManager.onPermissionGranted()
            updateNotificationState() // Refresh notification state
            Timber.d("Notification permission granted")
        }
    }

    fun onNotificationPermissionDenied() {
        viewModelScope.launch {
            notificationPermissionManager.onPermissionDenied()
            _notificationState.value = NotificationState.DISABLE
            Timber.d("Notification permission denied")
        }
    }

    fun onNotificationPermissionRequested() {
        notificationPermissionManager.onPermissionRequested()
        Timber.d("Notification permission requested")
    }

    fun onNotificationPermissionDismissed() {
        notificationPermissionManager.onPermissionDismissed()
        Timber.d("Notification permission dialog dismissed")
    }

    fun onHideQrCodeScreen() {
        uiQrCodeState.value = UiSpaceQrCodeState.Hidden
    }

    private fun subscribeToInviteLinkState() {
        viewModelScope.launch {
            spaceInviteLinkStore
                .observe(vmParams.space)
                .onStart {
                    val params = GetCurrentInviteAccessLevel.Params(space = vmParams.space)
                    getCurrentInviteAccessLevel.async(params).getOrNull()
                }
                .catch {
                    Timber.e(it, "Error observing invite link access level")
                    // Emit default value on error
                    inviteLinkAccessLevel.value = SpaceInviteLinkAccessLevel.LinkDisabled
                }
                .collect { accessLevel ->
                    Timber.d("Invite link access level updated: $accessLevel")
                    inviteLinkAccessLevel.value = accessLevel
                }
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
        data class ShareInviteLink(val link: String) : Command()
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
        data class OpenPropertiesScreen(val spaceId: SpaceId) : Command()
        data class OpenTypesScreen(val spaceId: SpaceId) : Command()
        data class OpenDebugScreen(val spaceId: String) : Command()
        data object RequestNotificationPermission : Command()
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
        private val spaceGradientProvider: SpaceGradientProvider,
        private val userPermissionProvider: UserPermissionProvider,
        private val activeSpaceMemberSubscriptionContainer: ActiveSpaceMemberSubscriptionContainer,
        private val uploadFile: UploadFile,
        private val profileContainer: ProfileSubscriptionManager,
        private val getDefaultObjectType: GetDefaultObjectType,
        private val setDefaultObjectType: SetDefaultObjectType,
        private val observeWallpaper: ObserveWallpaper,
        private val appActionManager: AppActionManager,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val copyInviteLinkToClipboard: CopyInviteLinkToClipboard,
        private val fetchObject: FetchObject,
        private val setObjectDetails: SetObjectDetails,
        private val getAccount: GetAccount,
        private val notificationPermissionManager: NotificationPermissionManager,
        private val setSpaceNotificationMode: SetSpaceNotificationMode,
        private val deviceTokenStoreService: DeviceTokenStoringService,
        private val getCurrentInviteAccessLevel: GetCurrentInviteAccessLevel,
        private val spaceInviteLinkStore: SpaceInviteLinkStore
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
            spaceGradientProvider = spaceGradientProvider,
            vmParams = params,
            userPermissionProvider = userPermissionProvider,
            activeSpaceMemberSubscriptionContainer = activeSpaceMemberSubscriptionContainer,
            uploadFile = uploadFile,
            profileContainer = profileContainer,
            getDefaultObjectType = getDefaultObjectType,
            setDefaultObjectType = setDefaultObjectType,
            observeWallpaper = observeWallpaper,
            appActionManager = appActionManager,
            storeOfObjectTypes = storeOfObjectTypes,
            copyInviteLinkToClipboard = copyInviteLinkToClipboard,
            fetchObject = fetchObject,
            setObjectDetails = setObjectDetails,
            getAccount = getAccount,
            notificationPermissionManager = notificationPermissionManager,
            setSpaceNotificationMode = setSpaceNotificationMode,
            deviceTokenStoringService = deviceTokenStoreService,
            getCurrentInviteAccessLevel = getCurrentInviteAccessLevel,
            spaceInviteLinkStore = spaceInviteLinkStore
        ) as T
    }

    data class VmParams(val space: SpaceId)
}