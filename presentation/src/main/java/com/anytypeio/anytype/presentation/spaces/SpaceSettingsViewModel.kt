package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Filepath
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SpaceType
import com.anytypeio.anytype.core_models.UNKNOWN_SPACE_TYPE
import com.anytypeio.anytype.core_models.asSpaceType
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.debugging.DebugSpaceShareDownloader
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.spaces.DeleteSpace
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceSettingsViewModel(
    private val params: Params,
    private val analytics: Analytics,
    private val setSpaceDetails: SetSpaceDetails,
    private val spaceManager: SpaceManager,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val gradientProvider: SpaceGradientProvider,
    private val urlBuilder: UrlBuilder,
    private val deleteSpace: DeleteSpace,
    private val configStorage: ConfigStorage,
    private val debugSpaceShareDownloader: DebugSpaceShareDownloader,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val getAccount: GetAccount
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
        proceedWithUserAsSpaceMemberPermissions()
        proceedWithFetchingSpaceMetaData()
    }

    private fun proceedWithFetchingSpaceMetaData() {
        viewModelScope.launch {
            val config = spaceManager.getConfig(params.space)
            storelessSubscriptionContainer.subscribe(
                StoreSearchParams(
                    subscription = SPACE_SETTINGS_SUBSCRIPTION,
                    filters = buildList {
                        add(
                            DVFilter(
                                relation = Relations.TARGET_SPACE_ID,
                                value = params.space.id,
                                condition = DVFilterCondition.EQUAL
                            )
                        )
                        add(
                            DVFilter(
                                relation = Relations.LAYOUT,
                                value = ObjectType.Layout.SPACE_VIEW.code.toDouble(),
                                condition = DVFilterCondition.EQUAL
                            )
                        )
                    },
                    keys = listOf(
                        Relations.ID,
                        Relations.SPACE_ID,
                        Relations.NAME,
                        Relations.ICON_EMOJI,
                        Relations.ICON_IMAGE,
                        Relations.ICON_OPTION,
                        Relations.CREATED_DATE,
                        Relations.CREATOR,
                        Relations.TARGET_SPACE_ID,
                        Relations.SPACE_ACCESS_TYPE,
                        Relations.SPACE_LOCAL_STATUS,
                        Relations.SPACE_ACCOUNT_STATUS
                    ),
                    limit = 1
                )
            ).mapNotNull { results ->
                results.firstOrNull()
            }.combine(permissions) { wrapper, permission ->
                val spaceView = ObjectWrapper.SpaceView(wrapper.map)
                SpaceData(
                    name = wrapper.name.orEmpty(),
                    icon = wrapper.spaceIcon(
                        builder = urlBuilder,
                        spaceGradientProvider = gradientProvider
                    ),
                    createdDateInMillis = wrapper
                        .getValue<Double?>(Relations.CREATED_DATE)
                        ?.let { timeInSeconds -> (timeInSeconds * 1000L).toLong() },
                    createdBy = wrapper
                        .getValue<Id?>(Relations.CREATOR)
                        .toString(),
                    spaceId = params.space.id,
                    network = config?.network.orEmpty(),
                    isDeletable = resolveIsSpaceDeletable(spaceView),
                    spaceType = spaceView.spaceAccessType?.asSpaceType() ?: UNKNOWN_SPACE_TYPE,
                    permissions = permission
                )
            }.collect { spaceData ->
                spaceViewState.value = ViewState.Success(spaceData)
            }
        }
    }

    private fun resolveIsSpaceDeletable(spaceView: ObjectWrapper.SpaceView) =
        spaceView.spaceAccessType != null && spaceView.spaceAccessType != SpaceAccessType.PERSONAL

    private fun proceedWithUserAsSpaceMemberPermissions() {
        viewModelScope.launch {
            getAccount.async(Unit).fold(
                onSuccess = { account ->
                    storelessSubscriptionContainer.subscribe(
                        searchParams = StoreSearchParams(
                            subscription = SPACE_SETTINGS_PARTICIPANT_SUBSCRIPTION,
                            filters = buildList {
                                addAll(
                                    ObjectSearchConstants.filterParticipants(
                                        spaces = listOf(params.space.id)
                                    )
                                )
                                add(
                                    DVFilter(
                                        relation = Relations.IDENTITY,
                                        value = account.id,
                                        condition = DVFilterCondition.EQUAL
                                    )
                                )
                            },
                            sorts = emptyList(),
                            keys = ObjectSearchConstants.spaceMemberKeys,
                            limit = 1
                        )
                    ).map { results ->
                        if (results.isNotEmpty())
                            ObjectWrapper.SpaceMember(results.first().map)
                        else
                            null
                    }.collect { user ->
                        if (user != null)
                            permissions.value =
                                user.permissions ?: SpaceMemberPermissions.NO_PERMISSIONS
                        else
                            Timber.w("User-as-space-member permission is not found.")
                    }
                },
                onFailure = {
                    Timber.e(it, "Could not get account")
                }
            )
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
                            Relations.ICON_OPTION to spaceGradientProvider.randomId().toDouble()
                        )
                    )
                )
            }
        }
    }

    fun onDeleteSpaceClicked() {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.clickDeleteSpace,
                props = Props(mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.settings))
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

    private fun proceedWithSpaceDeletion() {
        val state = spaceViewState.value
        if (state is ViewState.Success) {
            val space = state.data.spaceId
            val accountConfig = configStorage.getOrNull()
            if (accountConfig == null) {
                sendToast("Account config not found")
                return
            }
            val personalSpaceId = accountConfig.space
            if (space != null && space != personalSpaceId) {
                viewModelScope.launch {
                    deleteSpace.async(params = SpaceId(space)).fold(
                        onSuccess = {
                            analytics.sendEvent(
                                eventName = EventsDictionary.deleteSpace,
                                props = Props(mapOf(EventsPropertiesKey.type to "Private"))
                            )
                            fallbackToPersonalSpaceAfterDeletion(personalSpaceId)
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

    private suspend fun fallbackToPersonalSpaceAfterDeletion(personalSpaceId: Id) {
        spaceManager.set(personalSpaceId)
        isDismissed.value = true
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
            commands.emit(
                Command.SharePrivateSpace(params.space)
            )
        }
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
        val permissions: SpaceMemberPermissions
    )

    sealed class Command {
        data class ShareSpaceDebug(val filepath: Filepath) : Command()
        data class SharePrivateSpace(val space: SpaceId) : Command()
        data class ManageSharedSpace(val space: SpaceId) : Command()
    }

    class Factory @Inject constructor(
        private val params: Params,
        private val getAccount: GetAccount,
        private val analytics: Analytics,
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val setSpaceDetails: SetSpaceDetails,
        private val gradientProvider: SpaceGradientProvider,
        private val spaceManager: SpaceManager,
        private val deleteSpace: DeleteSpace,
        private val configStorage: ConfigStorage,
        private val debugFileShareDownloader: DebugSpaceShareDownloader,
        private val spaceGradientProvider: SpaceGradientProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = SpaceSettingsViewModel(
            storelessSubscriptionContainer = storelessSubscriptionContainer,
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
            getAccount = getAccount
        ) as T
    }

    class Params(val space: SpaceId)

    companion object {
        const val SPACE_DEBUG_MSG = "Kindly share this debug logs with Anytype developers."
        const val SPACE_SETTINGS_SUBSCRIPTION = "subscription.space-settings.space-views"
        const val SPACE_SETTINGS_PARTICIPANT_SUBSCRIPTION = "subscription.space-settings.participant"
    }
}