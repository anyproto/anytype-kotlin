package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.FileLimitsEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NodeUsageInfo
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.isPossibleToUpgrade
import com.anytypeio.anytype.core_models.membership.MembershipUpgradeReason
import com.anytypeio.anytype.core_models.membership.TierId
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.readableFileSize
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.search.PROFILE_SUBSCRIPTION_ID
import com.anytypeio.anytype.domain.workspace.InterceptFileLimitEvents
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.SpacesUsageInfo
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendGetMoreSpaceEvent
import com.anytypeio.anytype.presentation.extension.sendSettingsSpaceStorageManageEvent
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class SpacesStorageViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val spacesUsageInfo: SpacesUsageInfo,
    private val interceptFileLimitEvents: InterceptFileLimitEvents,
    private val getAccount: GetAccount,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val membershipProvider: MembershipProvider,
    private val userPermissionProvider: UserPermissionProvider
) : BaseViewModel() {

    private val _activeTier = MutableStateFlow<ActiveTierState>(ActiveTierState.Init)
    private val _nodeUsageInfo = MutableStateFlow(NodeUsageInfo())
    private val _viewState: MutableStateFlow<SpacesStorageScreenState?> = MutableStateFlow(null)
    val viewState: StateFlow<SpacesStorageScreenState?> = _viewState
    val events = MutableSharedFlow<Event>(replay = 0)
    val commands = MutableSharedFlow<Command>(replay = 0)
    private val jobs = mutableListOf<Job>()

    init {
        subscribeToViewEvents()
        subscribeToMiddlewareEvents()
        proceedWithGettingNodeUsageInfo()
        proceedWithGettingActiveTier()
    }

    private fun subscribeToViewEvents() {
        events
            .throttleFirst()
            .onEach { event ->
                dispatchCommand(event)
            }
            .launchIn(viewModelScope)
    }

    private suspend fun dispatchCommand(event: Event) {
        when (event) {
            Event.OnManageFilesClicked -> {
                commands.emit(
                    Command.OpenRemoteFilesManageScreen(
                        subscription = Subscription.Files.id,
                        space = vmParams.spaceId
                    )
                )
                analytics.sendSettingsSpaceStorageManageEvent()
            }
            Event.OnGetMoreSpaceClicked -> {
                onGetMoreSpaceClicked()
                analytics.sendGetMoreSpaceEvent()
            }
        }
    }

    fun onStart() {
        subscribeToSpaces()
    }

    fun onStop() {
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(listOf(SPACES_STORAGE_SUBSCRIPTION_ID))
        }
        jobs.cancel()
    }

    private fun subscribeToSpaces() {
        jobs += viewModelScope.launch {
            val subscribeParams = createStoreSearchParams()
            combine(
                _nodeUsageInfo,
                storelessSubscriptionContainer.subscribe(subscribeParams)
            ) { nodeUsageInfo, spaces ->
                createSpacesStorageScreenState(nodeUsageInfo, spaces)
            }
                .flowOn(appCoroutineDispatchers.io)
                .catch { Timber.e(it, "Error while getting spaces") }
                .collect { _viewState.value = it }
        }
    }

    private fun createStoreSearchParams(): StoreSearchParams {
        return StoreSearchParams(
            subscription = SPACES_STORAGE_SUBSCRIPTION_ID,
            keys = listOf(
                Relations.ID,
                Relations.TARGET_SPACE_ID,
                Relations.NAME
            ),
            filters = createFilters()
        )
    }

    private fun createFilters(): List<DVFilter> {
        return listOf(
            DVFilter(
                relation = Relations.LAYOUT,
                value = ObjectType.Layout.SPACE_VIEW.code.toDouble(),
                condition = DVFilterCondition.EQUAL
            ),
            DVFilter(
                relation = Relations.SPACE_ACCOUNT_STATUS,
                value = buildList {
                    add(SpaceStatus.SPACE_DELETED.code.toDouble())
                    add(SpaceStatus.SPACE_REMOVING.code.toDouble())
                },
                condition = DVFilterCondition.NOT_IN
            ),
            DVFilter(
                relation = Relations.SPACE_LOCAL_STATUS,
                value = SpaceStatus.OK.code.toDouble(),
                condition = DVFilterCondition.EQUAL
            )
        )
    }

    private suspend fun createSpacesStorageScreenState(
        nodeUsageInfo: NodeUsageInfo,
        spaces: List<ObjectWrapper.Basic>
    ): SpacesStorageScreenState {
        val bytesUsage = nodeUsageInfo.nodeUsage.bytesUsage
        val bytesLimit = nodeUsageInfo.nodeUsage.bytesLimit
        val localUsage = nodeUsageInfo.nodeUsage.localBytesUsage
        val percentUsage = calculatePercentUsage(bytesUsage, bytesLimit)
        val isShowGetMoreSpace = isNeedToShowGetMoreSpace(percentUsage, localUsage, bytesLimit)
        val isShowSpaceUsedWarning = isShowSpaceUsedWarning(percentUsage)
        val activeSpaceId = vmParams.spaceId.id
        val activeSpace = spaces.firstOrNull { it.targetSpaceId == activeSpaceId }

        val segmentLegendItems = getSegmentLegendItems(nodeUsageInfo, activeSpace)
        val segmentLineItems = getSegmentLineItems(nodeUsageInfo, activeSpace, spaces)

        return SpacesStorageScreenState(
            spaceLimit = bytesLimit?.readableFileSize().orEmpty(),
            spaceUsage = bytesUsage?.readableFileSize().orEmpty(),
            isShowSpaceUsedWarning = isShowSpaceUsedWarning,
            isShowGetMoreSpace = isShowGetMoreSpace,
            segmentLegendItems = segmentLegendItems,
            segmentLineItems = segmentLineItems
        )
    }

    private fun calculatePercentUsage(bytesUsage: Long?, bytesLimit: Long?): Float? {
        return if (bytesUsage != null && bytesLimit != null && bytesLimit != 0L) {
            (bytesUsage.toFloat() / bytesLimit.toFloat())
        } else {
            null
        }
    }

    private fun proceedWithGettingNodeUsageInfo() {
        viewModelScope.launch {
            spacesUsageInfo.async(Unit).fold(
                onSuccess = { nodeUsageInfo -> _nodeUsageInfo.value = nodeUsageInfo },
                onFailure = { Timber.e(it, "Error while getting file space usage") }
            )
        }
    }

    private fun subscribeToMiddlewareEvents() {
        jobs += viewModelScope.launch {
            interceptFileLimitEvents.run(Unit)
                .onEach { events ->
                    val currentState = _nodeUsageInfo.value
                    val newState = currentState.updateState(events)
                    _nodeUsageInfo.value = newState
                }
                .collect()
        }
    }

    private fun NodeUsageInfo.updateState(events: List<FileLimitsEvent>): NodeUsageInfo {
        return events.fold(this) { currentState, event ->
            when (event) {
                is FileLimitsEvent.LocalUsage -> currentState.copy(
                    nodeUsage = currentState.nodeUsage.copy(
                        localBytesUsage = event.bytesUsage
                    )
                )

                is FileLimitsEvent.SpaceUsage -> {
                    val spaceIndex = currentState.spaces.indexOfFirst { it.space == event.space }
                    if (spaceIndex != -1) {
                        currentState.copy(
                            spaces = currentState.spaces.toMutableList().apply {
                                set(
                                    spaceIndex,
                                    currentState.spaces[spaceIndex].copy(bytesUsage = event.bytesUsage)
                                )
                            }
                        )
                    } else {
                        currentState
                    }
                }

                else -> currentState
            }
        }
    }

    fun event(event: Event) {
        Timber.d("Event : [$event]")
        viewModelScope.launch { events.emit(event) }
    }

    private fun isNeedToShowGetMoreSpace(
        percentUsage: Float?,
        localUsage: Long?,
        bytesLimit: Long?
    ): Boolean {
        val localPercentUsage =
            if (localUsage != null && bytesLimit != null && bytesLimit != 0L) {
                (localUsage.toFloat() / bytesLimit.toFloat())
            } else {
                null
            }
        return (percentUsage != null && percentUsage >= FilesStorageViewModel.WARNING_PERCENT)
                || (localPercentUsage != null && localPercentUsage >= FilesStorageViewModel.WARNING_PERCENT)
    }

    private fun isShowSpaceUsedWarning(
        percentUsage: Float?
    ): Boolean {
        return percentUsage != null && percentUsage >= FilesStorageViewModel.WARNING_PERCENT
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

    private fun onGetMoreSpaceClicked() {
        viewModelScope.launch {
            val activeTier = (_activeTier.value as? ActiveTierState.Success)?.tierId
            val isPossibleToUpgrade =
                activeTier?.isPossibleToUpgrade(reason = MembershipUpgradeReason.StorageSpace)
            if (isPossibleToUpgrade == true) {
                commands.emit(Command.ShowMembershipScreen)
            } else {
                val config = spaceManager.getConfig() ?: return@launch
                val params = StoreSearchByIdsParams(
                    subscription = PROFILE_SUBSCRIPTION_ID,
                    keys = listOf(Relations.ID, Relations.NAME),
                    targets = listOf(config.profile)
                )
                combine(
                    getAccount.asFlow(Unit),
                    storelessSubscriptionContainer.subscribe(params)
                ) { account: Account, profileObj: List<ObjectWrapper.Basic> ->
                    Command.SendGetMoreSpaceEmail(
                        account = account.id,
                        name = profileObj.firstOrNull()?.name.orEmpty(),
                        limit = _viewState.value?.spaceLimit.orEmpty()
                    )
                }
                    .catch { Timber.e(it, "onGetMoreSpaceClicked error") }
                    .flowOn(appCoroutineDispatchers.io)
                    .collect { commands.emit(it) }
            }
        }
    }

    private suspend fun getSegmentLegendItems(
        nodeUsageInfo: NodeUsageInfo,
        activeSpace: ObjectWrapper.Basic?
    ): List<SegmentLegendItem> {
        val result = mutableListOf<SegmentLegendItem>()
        val currentSpace = nodeUsageInfo.spaces.firstOrNull { it.space == vmParams.spaceId.id }
        val otherSpaces = nodeUsageInfo.spaces.filter { it.space != vmParams.spaceId.id }
        var otherSpacesUsages = 0L
        otherSpaces.forEach { spaceUsage ->
            otherSpacesUsages += spaceUsage.bytesUsage
        }
        val currentBytesUsage = currentSpace?.bytesUsage ?: 0L
        result.add(
            SegmentLegendItem.Active(
                name = activeSpace?.name.orEmpty(),
                usage = currentBytesUsage.readableFileSize()
            )
        )
        result.add(
            SegmentLegendItem.Other(
                legend = otherSpacesUsages.readableFileSize()
            )
        )
        val freeSpace = nodeUsageInfo.nodeUsage.bytesLeft?.readableFileSize()
        result.add(
            SegmentLegendItem.Free(
                legend = freeSpace.orEmpty()
            )
        )
        return result
    }

    private fun getSegmentLineItems(
        nodeUsageInfo: NodeUsageInfo,
        activeSpace: ObjectWrapper.Basic?,
        allSpaces: List<ObjectWrapper.Basic> = emptyList()
    ): List<SegmentLineItem> {
        val bytesLimit = nodeUsageInfo.nodeUsage.bytesLimit?.toFloat()
        val nodeSpaces = nodeUsageInfo.spaces

        if (activeSpace == null || bytesLimit == null || bytesLimit == 0F) {
            Timber.e("SpacesStorage, Space Id or Node bytesLimit is null or 0")
            return emptyList()
        }

        val items = allSpaces.mapNotNull { space ->
            val spaceId = space.targetSpaceId?.let(::SpaceId) ?: return@mapNotNull null
            val spacePermission = userPermissionProvider.get(spaceId)
            if (spacePermission != SpaceMemberPermissions.OWNER) return@mapNotNull null

            val spaceUsage = nodeSpaces.firstOrNull { it.space == space.targetSpaceId }
            val value = spaceUsage?.bytesUsage?.toFloat()?.div(bytesLimit) ?: 0F
            if (space.targetSpaceId == activeSpace.targetSpaceId) {
                SegmentLineItem.Active(value)
            } else {
                SegmentLineItem.Other(value)
            }
        }.sortedByDescending { it is SegmentLineItem.Active }

        return buildList {
            addAll(items)
            val freeSpacesLeft = nodeUsageInfo.nodeUsage.bytesLeft?.toFloat()?.div(bytesLimit) ?: 0F
            add(
                SegmentLineItem.Free(freeSpacesLeft)
            )
        }
    }

    data class SpacesStorageScreenState(
        val spaceLimit: String,
        val spaceUsage: String,
        val isShowGetMoreSpace: Boolean,
        val isShowSpaceUsedWarning: Boolean,
        val segmentLegendItems: List<SegmentLegendItem> = emptyList(),
        val segmentLineItems: List<SegmentLineItem> = emptyList()
    )

    sealed class SegmentLegendItem {
        data class Active(val name: String, val usage: String) : SegmentLegendItem()
        data class Other(val legend: String) : SegmentLegendItem()
        data class Free(val legend: String) : SegmentLegendItem()
    }

    sealed class SegmentLineItem {
        abstract val value: Float

        data class Active(override val value: Float) : SegmentLineItem()
        data class Other(override val value: Float) : SegmentLineItem()
        data class Free(override val value: Float) : SegmentLineItem()
    }

    sealed class Event {
        object OnManageFilesClicked : Event()
        object OnGetMoreSpaceClicked : Event()
    }

    sealed class Command {
        data class OpenRemoteFilesManageScreen(val subscription: Id, val space: SpaceId) : Command()
        data class SendGetMoreSpaceEmail(val account: Id, val name: String, val limit: String) :
            Command()
        data object ShowMembershipScreen : Command()
    }

    //Active membership status of the current user
    sealed class ActiveTierState {
        data object Init : ActiveTierState()
        data class Success(val tierId: TierId) : ActiveTierState()
    }

    data class VmParams(
        val spaceId: SpaceId
    )

    companion object {
        private const val SPACES_STORAGE_SUBSCRIPTION_ID = "spaces_storage_view_model_subscription"
    }
}