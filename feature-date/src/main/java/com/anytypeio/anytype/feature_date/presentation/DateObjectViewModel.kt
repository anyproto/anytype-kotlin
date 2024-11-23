package com.anytypeio.anytype.feature_date.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.TimeInSeconds
import com.anytypeio.anytype.core_models.getSingleValue
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.objects.ObjectDateByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.RelationListWithValue
import com.anytypeio.anytype.feature_date.models.DateObjectBottomMenu
import com.anytypeio.anytype.feature_date.models.DateObjectHeaderState
import com.anytypeio.anytype.feature_date.models.DateObjectHorizontalListState
import com.anytypeio.anytype.feature_date.models.DateObjectSheetState
import com.anytypeio.anytype.feature_date.models.DateObjectTopToolbarState
import com.anytypeio.anytype.feature_date.models.DateObjectVerticalListState
import com.anytypeio.anytype.feature_date.models.UiContentState
import com.anytypeio.anytype.feature_date.models.UiHorizontalListItem
import com.anytypeio.anytype.feature_date.models.UiVerticalListItem
import com.anytypeio.anytype.feature_date.models.toUiHorizontalListItems
import com.anytypeio.anytype.feature_date.models.toUiVerticalListItem
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentScreen
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewState: @see [com.anytypeio.anytype.feature_date.models.UiContentState]
 * Factory: @see [DateObjectViewModelFactory]
 * Screen: @see [com.anytypeio.anytype.feature_date.ui.DateObjectScreen]
 * Models: @see [com.anytypeio.anytype.feature_date.models.DateObjectVerticalListState]
 */
class DateObjectViewModel(
    private val vmParams: VmParams,
    private val getObject: GetObject,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val userPermissionProvider: UserPermissionProvider,
    private val relationListWithValue: RelationListWithValue,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val objectDateByTimestamp: ObjectDateByTimestamp
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val uiTopToolbarState =
        MutableStateFlow<DateObjectTopToolbarState>(DateObjectTopToolbarState.Empty)
    val uiBottomMenu = MutableStateFlow(DateObjectBottomMenu())
    val uiHeaderState = MutableStateFlow<DateObjectHeaderState>(DateObjectHeaderState.Empty)
    val uiHorizontalListState =
        MutableStateFlow<DateObjectHorizontalListState>(DateObjectHorizontalListState.empty())
    val uiVerticalListState =
        MutableStateFlow<DateObjectVerticalListState>(DateObjectVerticalListState.empty())
    val uiSheetState = MutableStateFlow<DateObjectSheetState>(DateObjectSheetState.Empty)
    val commands = MutableSharedFlow<Command>()
    val uiContentState = MutableStateFlow<UiContentState>(UiContentState.Idle())

    private val _dateObjId = MutableStateFlow<Id?>(null)
    val dateObjId: StateFlow<Id?> = _dateObjId
    private val _dateObjDetails = MutableStateFlow<DateObjectDetails?>(null)
    private val _activeRelation = MutableStateFlow<ActiveRelation?>(null)

    /**
     * Paging and subscription limit. If true, we can paginate after reaching bottom items.
     * Could be true only after the first subscription results (if results size == limit)
     */
    val canPaginate = MutableStateFlow(false)
    private var _itemsLimit = DEFAULT_SEARCH_LIMIT
    private val restartSubscription = MutableStateFlow(0L)

    private var shouldScrollToTopItems = false

    private val permission = MutableStateFlow(userPermissionProvider.get(vmParams.spaceId))

    private val _objectsSortType = MutableStateFlow<DVSortType>(DVSortType.DESC)

    init {
        Timber.d("Init DateObjectViewModel, date object id: [${vmParams.objectId}], space: [${vmParams.spaceId}]")
        uiHeaderState.value = DateObjectHeaderState.Loading
        uiHorizontalListState.value = DateObjectHorizontalListState.loadingState()
        uiVerticalListState.value = DateObjectVerticalListState.loadingState()
        proceedWithObservingPermissions()
        proceedWithGettingDateObject()
        proceedWithGettingDateObjectRelationList()
        _dateObjId.value = vmParams.objectId
    }

    fun onStart() {
        Timber.d("onStart")
        setupUiStateFlow()
        viewModelScope.launch {
            sendAnalyticsAllContentScreen(
                analytics = analytics
            )
        }
    }

    fun onStop() {
        unsubscribe()
        resetLimit()
        canPaginate.value = false
        uiVerticalListState.value = DateObjectVerticalListState.empty()
        uiContentState.value = UiContentState.Empty
    }

    override fun onCleared() {
        Timber.d("onCleared")
        super.onCleared()
        uiContentState.value = UiContentState.Empty
        uiHeaderState.value = DateObjectHeaderState.Empty
        uiTopToolbarState.value = DateObjectTopToolbarState.Empty
        uiHorizontalListState.value = DateObjectHorizontalListState.empty()
        uiVerticalListState.value = DateObjectVerticalListState.empty()
        uiSheetState.value = DateObjectSheetState.Empty
        resetLimit()
    }

    private fun reopenDateObject(dateObjectId: Id) {
        Timber.d("Reopen date object: $dateObjectId")
        canPaginate.value = false
        resetLimit()
        shouldScrollToTopItems = true
        //uiHeaderState.value = DateObjectHeaderState.Loading
        //uiHorizontalListState.value = DateObjectHorizontalListState.loadingState()
        //uiVerticalListState.value = DateObjectVerticalListState.loadingState()
        uiSheetState.value = DateObjectSheetState.Empty
        uiContentState.value = UiContentState.Empty
        _dateObjDetails.value = null
        _activeRelation.value = null
        _dateObjId.value = dateObjectId
    }

    //region Initialization
    private fun proceedWithObservingPermissions() {
        viewModelScope.launch {
            userPermissionProvider
                .observe(space = vmParams.spaceId)
                .collect {
                    uiBottomMenu.value =
                        DateObjectBottomMenu(isOwnerOrEditor = it?.isOwnerOrEditor() == true)
                    permission.value = it
                }
        }
    }

    private fun proceedWithGettingDateObjectRelationList() {
        viewModelScope.launch {
            _dateObjId
                .filterNotNull()
                .collect { objId ->
                    val params = RelationListWithValue.Params(
                        space = vmParams.spaceId,
                        value = objId
                    )
                    Timber.d("Start RelationListWithValue with params: $params")
                    relationListWithValue.async(params).fold(
                        onSuccess = { result ->
                            Timber.d("RelationListWithValue Success: $result")
                            val items = result.toUiHorizontalListItems(storeOfRelations)
                            initHorizontalListState(items)
                        },
                        onFailure = { e -> Timber.e(e, "RelationListWithValue Error") }
                    )
                }
        }
    }

    private fun proceedWithGettingDateObject() {
        viewModelScope.launch {
            _dateObjId
                .filterNotNull()
                .collect { objId ->
                    val params = GetObject.Params(
                        target = objId,
                        space = vmParams.spaceId
                    )
                    Timber.d("Start GetObject with params: $params")
                    getObject.async(params).fold(
                        onSuccess = { obj ->
                            Timber.d("GetObject Success, obj:[$obj]")
                            _dateObjDetails.value = DateObjectDetails(
                                id = obj.root,
                                timestamp = obj.details[objId]?.getSingleValue<Double>(
                                    Relations.TIMESTAMP
                                )?.toLong() ?: 0
                            )
                            uiTopToolbarState.value = DateObjectTopToolbarState.Content(
                                syncStatus = SpaceSyncStatus.SYNCING
                            )
                            val root = ObjectWrapper.Basic(obj.details[objId].orEmpty())
                            //todo should be formatted with dateProvider by timestamp
                            uiHeaderState.value = DateObjectHeaderState.Content(
                                title = root.name.orEmpty()
                            )
                        },
                        onFailure = { e -> Timber.e(e, "GetObject Error") }
                    )
                }
        }
    }

    private fun proceedWithGettingDateByTimestamp(timestamp: Long, action: (Struct?) -> Unit) {
        val params = ObjectDateByTimestamp.Params(
            space = vmParams.spaceId,
            timestamp = timestamp
        )
        Timber.d("Start ObjectDateByTimestamp with params: [$params]")
        viewModelScope.launch {
            objectDateByTimestamp.async(params).fold(
                onSuccess = { dateObject ->
                    Timber.d("ObjectDateByTimestamp Success, dateObject: [$dateObject]")
                    action(dateObject)
                },
                onFailure = { e -> Timber.e(e, "ObjectDateByTimestamp Error") }
            )
        }
    }
    //endregion

    //region Subscription
    private fun subscriptionId() = "date_object_subscription_${vmParams.spaceId}"

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupUiStateFlow() {
        viewModelScope.launch {
            combine(
                _activeRelation.filterNotNull(),
                _dateObjDetails.filterNotNull(),
                _objectsSortType,
                restartSubscription
            ) { relationKey, dateObjectDetails, sortType, _ ->
                loadData(relationKey, dateObjectDetails, sortType)
            }.flatMapLatest { items ->
                items
            }.collectLatest { items ->
                uiVerticalListState.value = DateObjectVerticalListState(items)
            }
        }
    }

    private fun loadData(
        relationKey: ActiveRelation,
        dateObjectDetails: DateObjectDetails,
        activeSort: DVSortType
    ): Flow<List<UiVerticalListItem>> {

        val searchParams = createSearchParams(
            relation = relationKey,
            objTimestamp = dateObjectDetails.timestamp,
            space = vmParams.spaceId,
            activeSort = activeSort,
            itemsLimit = _itemsLimit,
            dateObjectId = dateObjectDetails.id
        )

        return storelessSubscriptionContainer.subscribe(searchParams)
            .onStart {
                uiContentState.value = if (_itemsLimit == DEFAULT_SEARCH_LIMIT) {
                    UiContentState.InitLoading
                } else {
                    UiContentState.Paging
                }
                Timber.d("Restart subscription: with params: $searchParams")
            }
            .map { objWrappers ->
                handleData(objWrappers)
            }.catch { e ->
                Timber.e("Error loading data: $e")
                handleError(e)
            }
    }

    private suspend fun handleData(
        objWrappers: List<ObjectWrapper.Basic>
    ): List<UiVerticalListItem> {

        canPaginate.value = objWrappers.size == _itemsLimit

        val items = objWrappers.map {
            it.toUiVerticalListItem(
                space = vmParams.spaceId,
                urlBuilder = urlBuilder,
                objectTypes = storeOfObjectTypes.getAll()
            )
        }
        uiContentState.value = if (items.isEmpty()) {
            UiContentState.Empty
        } else {
            UiContentState.Idle(scrollToTop = shouldScrollToTopItems).also {
                shouldScrollToTopItems = false
            }
        }
        return items
    }

    private fun createSearchParams(
        dateObjectId: Id,
        space: SpaceId,
        objTimestamp: TimeInSeconds,
        relation: ActiveRelation,
        activeSort: DVSortType,
        itemsLimit: Int
    ): StoreSearchParams {
        val filters = filtersForSearch(
            spaces = listOf(space.id),
            relation = relation,
            timestamp = objTimestamp,
            dateObjectId = dateObjectId
        )
        return StoreSearchParams(
            space = space,
            filters = filters,
            sorts = buildList {
                add(
                    DVSort(
                        relationKey = relation.key.key,
                        type = activeSort,
                        relationFormat = RelationFormat.DATE
                    )
                )
            },
            keys = defaultKeys,
            limit = itemsLimit,
            subscription = subscriptionId()
        )
    }

    /**
     * Updates the limit for the number of items fetched and triggers data reload.
     */
    fun updateLimit() {
        Timber.d("Update limit, canPaginate: ${canPaginate.value} uiContentState: ${uiContentState.value}")
        if (canPaginate.value && uiContentState.value is UiContentState.Idle) {
            _itemsLimit += DEFAULT_SEARCH_LIMIT
            restartSubscription.value++
        }
    }

    private fun resetLimit() {
        Timber.d("Reset limit")
        _itemsLimit = DEFAULT_SEARCH_LIMIT
    }

    private fun unsubscribe() {
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(listOf(subscriptionId()))
        }
    }
    //endregion

    //region Ui Actions
    fun onHorizontalItemClicked(item: UiHorizontalListItem) {
        when (item) {
            is UiHorizontalListItem.Item -> {
                if (_activeRelation.value?.key == item.key) {
                    _objectsSortType.value = if (_objectsSortType.value == DVSortType.ASC) {
                        DVSortType.DESC
                    } else {
                        DVSortType.ASC
                    }
                    shouldScrollToTopItems = true
                    resetLimit()
                    canPaginate.value = false
                    uiContentState.value = UiContentState.Empty
                    uiVerticalListState.value = DateObjectVerticalListState.loadingState()
                    restartSubscription.value++
                    updateHorizontalListState(selectedItem = item)
                } else {
                    shouldScrollToTopItems = true
                    resetLimit()
                    canPaginate.value = false
                    uiContentState.value = UiContentState.Empty
                    _activeRelation.value = ActiveRelation(
                        key = item.key,
                        format = item.relationFormat
                    )
                    restartSubscription.value++
                    updateHorizontalListState(selectedItem = item)
                }
            }

            else -> {
                // Do nothing
            }
        }
    }

    fun onVerticalItemClicked(item: UiVerticalListItem) {
    }

    fun onHeaderActions(action: DateObjectHeaderState.Action) {
        Timber.d("onHeaderActions: $action")

        val timestamp = _dateObjDetails.value?.timestamp
        if (timestamp == null) {
            Timber.e("Error getting timestamp")
            return
        }

        val offset = when (action) {
            DateObjectHeaderState.Action.Next -> 86400
            DateObjectHeaderState.Action.Previous -> -86400
        }

        proceedWithGettingDateByTimestamp(
            timestamp = timestamp + offset
        ) { dateObject ->
            val id = dateObject?.getSingleValue<String>(Relations.ID)
            if (id != null) {
                reopenDateObject(id)
            } else {
                Timber.e("GettingDateByTimestamp error, object has no id")
            }
        }
    }

    fun onTopToolbarActions(action: DateObjectTopToolbarState.Action) {
        when (action) {
            DateObjectTopToolbarState.Action.Calendar -> {
                uiSheetState.value = DateObjectSheetState.Calendar(selectedDate = null)
            }

            DateObjectTopToolbarState.Action.SyncStatus -> TODO()
        }
    }

    fun onSyncStatusClicked() {}

    fun onCalendarDateSelected(selectedDate: Long?) {
        //TODO get proper Id from timestamp
        Timber.d("Selected date: $selectedDate")
        viewModelScope.launch {
            unsubscribe()
            commands.emit(
                Command.NavigateToDateObject(
                    objectId = "_date_2025-04-22",
                    space = vmParams.spaceId
                )
            )
        }
    }
    //endregion

    //region Ui State
    private fun initHorizontalListState(relations: List<UiHorizontalListItem.Item>) {
        val relation = relations.getOrNull(0)
        if (relation == null) {
            Timber.e("Error getting relation")
            return
        }
        _activeRelation.value = ActiveRelation(
            key = relation.key,
            format = relation.relationFormat
        )
        restartSubscription.value++
        uiHorizontalListState.value = DateObjectHorizontalListState(
            items = buildList {
                add(UiHorizontalListItem.Settings())
                addAll(relations)
            },
            selectedRelationKey = _activeRelation.value?.key
        )
        if (relations.isEmpty()) {
            uiContentState.value = UiContentState.Empty
            uiVerticalListState.value = DateObjectVerticalListState.empty()
        }
    }

    private fun updateHorizontalListState(selectedItem: UiHorizontalListItem.Item) {
        uiHorizontalListState.value = uiHorizontalListState.value.copy(
            selectedRelationKey = selectedItem.key
        )
    }

    private fun handleError(e: Throwable) {
        uiContentState.value = UiContentState.Error(
            message = e.message ?: "An error occurred while loading data."
        )
    }
    //endregion

    //region VmParams + Command
    data class VmParams(
        val objectId: Id,
        val spaceId: SpaceId
    )

    sealed class Command {
        data class OpenChat(val target: Id, val space: Id) : Command()
        data class NavigateToEditor(val id: Id, val space: Id) : Command()
        data class NavigateToSetOrCollection(val id: Id, val space: Id) : Command()
        data class NavigateToBin(val space: Id) : Command()
        data class NavigateToDateObject(val objectId: Id, val space: SpaceId) : Command()
        sealed class SendToast : Command() {
            data class Error(val message: String) : SendToast()
            data class RelationRemoved(val name: String) : SendToast()
            data class TypeRemoved(val name: String) : SendToast()
            data class UnexpectedLayout(val layout: String) : SendToast()
            data class ObjectArchived(val name: String) : SendToast()
        }

        data object OpenGlobalSearch : Command()
        data object ExitToVault : Command()
        data object Back : Command()
    }

    data class DateObjectDetails(
        val id: Id,
        val timestamp: TimeInSeconds
    )

    data class ActiveRelation(
        val key: RelationKey,
        val format: RelationFormat
    )

    //endregion

    companion object {
        //INITIAL STATE
        const val DEFAULT_SEARCH_LIMIT = 25
    }
}