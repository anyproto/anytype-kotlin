package com.anytypeio.anytype.presentation.settings

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.FileLimitsEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NodeUsageInfo
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.readableFileSize
import com.anytypeio.anytype.device.BuildProvider
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.workspace.InterceptFileLimitEvents
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.SpacesUsageInfo
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class SpacesStorageViewModel(
    private val analytics: Analytics,
    private val spaceManager: SpaceManager,
    private val urlBuilder: UrlBuilder,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val spacesUsageInfo: SpacesUsageInfo,
    private val interceptFileLimitEvents: InterceptFileLimitEvents,
    private val buildProvider: BuildProvider,
    private val getAccount: GetAccount,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer
) : BaseViewModel() {

    private val _nodeUsage = MutableStateFlow(NodeUsageInfo())
    private val _viewState: MutableStateFlow<SpacesStorageScreenState?> = MutableStateFlow(null)
    val viewState: StateFlow<SpacesStorageScreenState?> = _viewState
    val events = MutableSharedFlow<Event>(replay = 0)
    val commands = MutableSharedFlow<Command>(replay = 0)
    private val jobs = mutableListOf<Job>()

    fun onStart() {
        subscribeToSpaces()
        subscribeToMiddlewareEvents()
        proceedWithGettingNodeUsageInfo()
    }

    fun onStop() {
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(
                listOf(SPACES_STORAGE_SUBSCRIPTION_ID)
            )
        }
        jobs.cancel()
    }

    private fun subscribeToSpaces() {
        jobs += viewModelScope.launch {
            val subscribeParams = StoreSearchParams(
                subscription = SPACES_STORAGE_SUBSCRIPTION_ID,
                keys = listOf(
                    Relations.ID,
                    Relations.TARGET_SPACE_ID,
                    Relations.SPACE_ACCOUNT_STATUS,
                    Relations.NAME,
                    Relations.SPACE_ID
                ),
                filters = listOf(
                    DVFilter(
                        relation = Relations.LAYOUT,
                        value = ObjectType.Layout.SPACE_VIEW.code.toDouble(),
                        condition = DVFilterCondition.EQUAL
                    ),
                    DVFilter(
                        relation = Relations.SPACE_ACCOUNT_STATUS,
                        value = SpaceStatus.DELETED.code.toDouble(),
                        condition = DVFilterCondition.NOT_EQUAL
                    ),
                    DVFilter(
                        relation = Relations.SPACE_LOCAL_STATUS,
                        value = SpaceStatus.OK.code.toDouble(),
                        condition = DVFilterCondition.EQUAL
                    )
                )
            )
            combine(
                _nodeUsage,
                storelessSubscriptionContainer.subscribe(subscribeParams)
            ) { nodeUsageInfo, spaces ->
                val bytesUsage = nodeUsageInfo.nodeUsage.bytesUsage
                val bytesLimit = nodeUsageInfo.nodeUsage.bytesLimit
                val localeUsage = nodeUsageInfo.nodeUsage.localBytesUsage
                val percentUsage =
                    if (bytesUsage != null && bytesLimit != null && bytesLimit != 0L) {
                        (bytesUsage.toFloat() / bytesLimit.toFloat())
                    } else {
                        null
                    }
                val isShowGetMoreSpace = isNeedToShowGetMoreSpace(
                    percentUsage = percentUsage,
                    localUsage = localeUsage,
                    bytesLimit = bytesLimit
                )
                val isShowSpaceUsedWarning = isShowSpaceUsedWarning(
                    percentUsage = percentUsage
                )
                val activeSpaceId = spaceManager.get()
                Log.d("Test1983", "Active space id: $activeSpaceId")
                val activeSpace = spaces.firstOrNull { it.targetSpaceId == activeSpaceId }

                Log.d("Test1983", "Active space: $activeSpace")

                val segmentLegendItems = getSegmentLegendItems(
                    nodeUsageInfo = nodeUsageInfo,
                    activeSpace = activeSpace
                )
                val segmentLineItems = getSegmentLineItems(
                    nodeUsageInfo = nodeUsageInfo,
                    activeSpace = activeSpace,
                    allSpaces = spaces
                )

                SpacesStorageScreenState(
                    spaceLimit = bytesLimit?.readableFileSize().orEmpty(),
                    spaceUsage = bytesUsage?.readableFileSize().orEmpty(),
                    isShowSpaceUsedWarning = isShowSpaceUsedWarning,
                    isShowGetMoreSpace = isShowGetMoreSpace,
                    segmentLegendItems = segmentLegendItems,
                    segmentLineItems = segmentLineItems
                )
            }
                .flowOn(appCoroutineDispatchers.io)
                .collect { _viewState.value = it }
        }
    }

    private fun proceedWithGettingNodeUsageInfo() {
        viewModelScope.launch {
            spacesUsageInfo.async(Unit).fold(
                onSuccess = { nodeUsageInfo ->
                    _nodeUsage.value = nodeUsageInfo
                },
                onFailure = {
                    Timber.e(it, "Error while getting file space usage")
                }
            )
        }
    }

    private fun subscribeToMiddlewareEvents() {
        jobs += viewModelScope.launch {
            interceptFileLimitEvents.run(Unit)
                .onEach { events ->
                    val currentState = _nodeUsage.value
                    val newState = currentState.updateState(events)
                    _nodeUsage.value = newState
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

    private fun onGetMoreSpaceClicked() {
//        viewModelScope.launch {
//            val config = spaceManager.getConfig() ?: return@launch
//            val params = StoreSearchByIdsParams(
//                subscription = PROFILE_SUBSCRIPTION_ID,
//                keys = listOf(Relations.ID, Relations.NAME),
//                targets = listOf(config.profile)
//            )
//            combine(
//                getAccount.asFlow(Unit),
//                storelessSubscriptionContainer.subscribe(params)
//            ) { account: Account, profileObj: List<ObjectWrapper.Basic> ->
//                FilesStorageViewModel.Command.SendGetMoreSpaceEmail(
//                    account = account.id,
//                    name = profileObj.firstOrNull()?.name.orEmpty(),
//                    limit = _state.value.spaceLimit
//                )
//            }
//                .catch { Timber.e(it, "onGetMoreSpaceClicked error") }
//                .flowOn(appCoroutineDispatchers.io)
//                .collect { commands.emit(it) }
//        }
    }

    private suspend fun getSegmentLegendItems(
        nodeUsageInfo: NodeUsageInfo,
        activeSpace: ObjectWrapper.Basic?
    ): List<SegmentLegendItem> {
        val result = mutableListOf<SegmentLegendItem>()
        val currentSpace = nodeUsageInfo.spaces.firstOrNull { it.space == spaceManager.get() }
        val otherSpaces = nodeUsageInfo.spaces.filter { it.space != spaceManager.get() }
        var otherSpacesUsages = 0L
        otherSpaces.forEach { spaceUsage ->
            otherSpacesUsages += spaceUsage.bytesUsage
        }
        result.add(
            SegmentLegendItem.Active(
                name = activeSpace?.name.orEmpty(),
                usage = currentSpace?.bytesUsage?.readableFileSize().orEmpty(),
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
        val result = mutableListOf<SegmentLineItem>()
        val bytesLimit = nodeUsageInfo.nodeUsage.bytesLimit?.toFloat()
        if (activeSpace == null || bytesLimit == null || bytesLimit == 0F) {
            Timber.e("SpacesStorage, Space Id or Node bytesLimit is null or 0")
            return result
        }

        val nodeSpaces = nodeUsageInfo.spaces
        val items = allSpaces.map { s ->
            val space = nodeSpaces.firstOrNull { it.space == s.targetSpaceId }
            if (space == null) {
                SegmentLineItem.Other(0F)
            } else {
                val value = space.bytesUsage.toFloat() / bytesLimit
                if (space.space == activeSpace.targetSpaceId) {
                    SegmentLineItem.Active(value)
                } else {
                    SegmentLineItem.Other(value)
                }
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
        data class OpenRemoteFilesManageScreen(val subscription: Id) : Command()
        data class SendGetMoreSpaceEmail(val account: Id, val name: String, val limit: String) :
            Command()
    }

    companion object {
        private const val SPACES_STORAGE_SUBSCRIPTION_ID = "spaces_storage_view_model_subscription"
    }
}