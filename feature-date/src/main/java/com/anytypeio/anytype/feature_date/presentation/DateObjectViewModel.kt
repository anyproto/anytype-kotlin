package com.anytypeio.anytype.feature_date.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.DATE_PICKER_YEAR_RANGE
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.TimeInSeconds
import com.anytypeio.anytype.core_models.getSingleValue
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncNetwork
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.objects.ObjectDateByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.relations.RelationListWithValue
import com.anytypeio.anytype.feature_date.models.UiCalendarState
import com.anytypeio.anytype.feature_date.models.DateObjectBottomMenu
import com.anytypeio.anytype.feature_date.models.DateObjectHeaderState
import com.anytypeio.anytype.feature_date.models.DateObjectHorizontalListState
import com.anytypeio.anytype.feature_date.models.DateObjectSheetState
import com.anytypeio.anytype.feature_date.models.DateObjectTopToolbarState
import com.anytypeio.anytype.feature_date.models.DateObjectVerticalListState
import com.anytypeio.anytype.feature_date.models.UiContentState
import com.anytypeio.anytype.feature_date.models.UiErrorState
import com.anytypeio.anytype.feature_date.models.UiErrorState.Reason
import com.anytypeio.anytype.feature_date.models.UiHorizontalListItem
import com.anytypeio.anytype.feature_date.models.UiVerticalListItem
import com.anytypeio.anytype.feature_date.models.toUiHorizontalListItems
import com.anytypeio.anytype.feature_date.models.toUiVerticalListItem
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentResult
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentScreen
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.objects.getCreateObjectParams
import com.anytypeio.anytype.presentation.relations.values
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultKeys
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.sync.updateStatus
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
    private val objectDateByTimestamp: ObjectDateByTimestamp,
    private val dateProvider: DateProvider,
    private val spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider,
    private val createObject: CreateObject
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
    val uiCalendarState = MutableStateFlow<UiCalendarState>(UiCalendarState.Empty)
    val commands = MutableSharedFlow<Command>()
    val uiContentState = MutableStateFlow<UiContentState>(UiContentState.Idle())
    val errorState = MutableStateFlow<UiErrorState>(UiErrorState.Hidden)
    val showCalendar = MutableStateFlow(false)
    val syncStatusWidget = MutableStateFlow<SyncStatusWidgetState>(SyncStatusWidgetState.Hidden)

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
        proceedWithObservingSyncStatus()
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

    private fun proceedWithReopenDateObjectByTimestamp(timestamp: TimeInSeconds) {
        proceedWithGettingDateByTimestamp(
            timestamp = timestamp
        ) { dateObject ->
            val id = dateObject?.getSingleValue<String>(Relations.ID)
            if (id != null) {
                reopenDateObject(id)
            } else {
                Timber.e("GettingDateByTimestamp error, object has no id")
            }
        }
    }

    private fun reopenDateObject(dateObjectId: Id) {
        Timber.d("Reopen date object: $dateObjectId")
        canPaginate.value = false
        resetLimit()
        shouldScrollToTopItems = true
        //uiHeaderState.value = DateObjectHeaderState.Loading
        uiHorizontalListState.value = DateObjectHorizontalListState.empty()
        uiVerticalListState.value = DateObjectVerticalListState.empty()
        uiSheetState.value = DateObjectSheetState.Empty
//        uiContentState.value = UiContentState.Empty
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

    private fun proceedWithObservingSyncStatus() {
        viewModelScope.launch {
            spaceSyncAndP2PStatusProvider
                .observe()
                .catch {
                    Timber.e(it, "Error while observing sync status")
                }
                .collect { syncAndP2pState ->
                    Timber.d("Sync status: $syncAndP2pState")
                    val uiTopToolbarStateValue = uiTopToolbarState.value
                    uiTopToolbarState.value = when (uiTopToolbarStateValue) {
                        is DateObjectTopToolbarState.Content -> {
                            uiTopToolbarStateValue.copy(
                                status = syncAndP2pState
                            )
                        }

                        DateObjectTopToolbarState.Empty -> uiTopToolbarStateValue
                    }
                    syncStatusWidget.value = syncStatusWidget.value.updateStatus(syncAndP2pState)
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
                            val items =
                                result.toUiHorizontalListItems(storeOfRelations = storeOfRelations)
                            initHorizontalListState(items)
                            initSheetState(items)
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
                            val timestampInSeconds = obj.details[objId]?.getSingleValue<Double>(
                                Relations.TIMESTAMP
                            )?.toLong() ?: 0
                            _dateObjDetails.value = DateObjectDetails(
                                id = obj.root,
                                timestamp = timestampInSeconds
                            )
                            uiTopToolbarState.value = DateObjectTopToolbarState.Content(
                                status = SpaceSyncAndP2PStatusState.Success(
                                    spaceSyncUpdate = SpaceSyncUpdate.Update(
                                        id = "fdsfsd",
                                        status = SpaceSyncStatus.SYNCING,
                                        network = SpaceSyncNetwork.ANYTYPE,
                                        error = SpaceSyncError.NULL,
                                        syncingObjectsCounter = 0
                                    ),
                                    p2PStatusUpdate = P2PStatusUpdate.Initial
                                )
                            )
                            val (formattedDate, _) = dateProvider.formatTimestampToDateAndTime(
                                timestamp = timestampInSeconds * 1000,
                            )
                            uiHeaderState.value = DateObjectHeaderState.Content(
                                title = formattedDate
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
                    uiContentState.value = UiContentState.Idle()
                    uiVerticalListState.value = DateObjectVerticalListState.loadingState()
                    restartSubscription.value++
                    updateHorizontalListState(selectedItem = item)
                } else {
                    shouldScrollToTopItems = true
                    resetLimit()
                    canPaginate.value = false
                    uiContentState.value = UiContentState.Idle()
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

        val newTimestamp = timestamp + offset

        val isValid = dateProvider.isTimestampWithinYearRange(
            timeStampInMillis = newTimestamp * 1000,
            yearRange = DATE_PICKER_YEAR_RANGE
        )

        if (isValid) {
            proceedWithReopenDateObjectByTimestamp(
                timestamp = newTimestamp
            )
        } else {
            showDateOutOfRangeError()
        }
    }

    fun onTopToolbarActions(action: DateObjectTopToolbarState.Action) {
        when (action) {
            DateObjectTopToolbarState.Action.Calendar -> {
                val timestamp = _dateObjDetails.value?.timestamp
                if (timestamp == null) {
                    uiCalendarState.value = UiCalendarState.Calendar(
                        timeInMillis = null
                    )
                } else {
                    val timeInMillis = dateProvider.adjustToStartOfDayInUserTimeZone(timestamp)
                    val isValid = dateProvider.isTimestampWithinYearRange(
                        timeStampInMillis = timeInMillis,
                        yearRange = DATE_PICKER_YEAR_RANGE
                    )
                    if (isValid) {
                        uiCalendarState.value = UiCalendarState.Calendar(
                            timeInMillis = timeInMillis
                        )
                        showCalendar.value = true
                    } else {
                        showDateOutOfRangeError()
                    }
                }
            }

            DateObjectTopToolbarState.Action.SyncStatus -> {
                Timber.d("onSyncStatusClicked")
            }
        }
    }

    fun onCalendarDateSelected(selectedDate: TimeInMillis?) {
        Timber.d("Selected date in millis: $selectedDate")
        if (selectedDate == null) return
        val newTimeInSeconds = dateProvider.adjustFromStartOfDayInUserTimeZoneToUTC(
            timestamp = (selectedDate / 1000),
        )
        proceedWithReopenDateObjectByTimestamp(
            timestamp = newTimeInSeconds
        )
    }

    fun onTodayClicked() {
        val timestamp = dateProvider.getTimestampForTodayAtStartOfDay()
        proceedWithReopenDateObjectByTimestamp(
            timestamp = timestamp
        )
    }

    fun onTomorrowClicked() {
        val timestamp = dateProvider.getTimestampForTomorrowAtStartOfDay()
        proceedWithReopenDateObjectByTimestamp(
            timestamp = timestamp
        )
    }

    fun onDismissCalendar() {
        showCalendar.value = false
    }

    fun onBottomMenuAction(action: DateObjectBottomMenu.Action) {
        when (action) {
            DateObjectBottomMenu.Action.AddDoc -> {
                proceedWithCreateDoc()
            }
            DateObjectBottomMenu.Action.Back -> {
                viewModelScope.launch {
                    commands.emit(Command.Back)
                }
            }
            DateObjectBottomMenu.Action.BackLong -> {
                viewModelScope.launch {
                    commands.emit(Command.ExitToSpaceWidgets)
                }
            }
            DateObjectBottomMenu.Action.CreateObjectLong -> {
                viewModelScope.launch {
                    commands.emit(Command.TypeSelectionScreen)
                }
            }
            DateObjectBottomMenu.Action.GlobalSearch -> {
                viewModelScope.launch {
                    commands.emit(Command.OpenGlobalSearch)
                }
            }
        }
    }

    private fun proceedWithCreateDoc(
        objType: ObjectWrapper.Type? = null
    ) {
        val startTime = System.currentTimeMillis()
        val params = objType?.uniqueKey.getCreateObjectParams(
            space = vmParams.spaceId,
            objType?.defaultTemplateId
        )
        viewModelScope.launch {
            createObject.async(params).fold(
                onSuccess = { result ->
                    proceedWithNavigation(
                        navigation = result.obj.navigation()
                    )
                    sendAnalyticsObjectCreateEvent(
                        analytics = analytics,
                        route = EventsDictionary.Routes.objDate,
                        startTime = startTime,
                        objType = objType ?: storeOfObjectTypes.getByKey(result.typeKey.key),
                        view = EventsDictionary.View.viewHome,
                        spaceParams = provideParams(space = vmParams.spaceId.id)
                    )
                },
                onFailure = { e -> Timber.e(e, "Error while creating a new object") }
            )
        }
    }

    private fun proceedWithNavigation(navigation: OpenObjectNavigation) {
        viewModelScope.launch {
            when (navigation) {
                is OpenObjectNavigation.OpenDataView -> {
                    commands.emit(
                        Command.NavigateToSetOrCollection(
                            id = navigation.target,
                            space = navigation.space
                        )
                    )
                }

                is OpenObjectNavigation.OpenEditor -> {
                    commands.emit(
                        Command.NavigateToEditor(
                            id = navigation.target,
                            space = navigation.space
                        )
                    )
                }

                is OpenObjectNavigation.UnexpectedLayoutError -> {
                    Timber.e("Unexpected layout: ${navigation.layout}")
                    commands.emit(Command.SendToast.UnexpectedLayout(navigation.layout?.name.orEmpty()))
                }
                is OpenObjectNavigation.OpenDiscussion -> {
                    commands.emit(
                        Command.OpenChat(
                            target = navigation.target,
                            space = navigation.space
                        )
                    )
                }
                OpenObjectNavigation.NonValidObject -> {
                    Timber.e("Object id is missing")
                }
                is OpenObjectNavigation.OpenDataObject -> {
                    commands.emit(
                        Command.NavigateToEditor(
                            id = navigation.target,
                            space = navigation.space
                        )
                    )
                }
            }
        }
    }

    fun onItemClicked(item: UiVerticalListItem) {
        Timber.d("onItemClicked: ${item.id}")
        when (item) {
            is UiVerticalListItem.Item -> {
                val layout = item.layout ?: return
                proceedWithNavigation(
                    navigation = layout.navigation(
                        target = item.id,
                        space = vmParams.spaceId.id
                    )
                )
                viewModelScope.launch {
                    //sendAnalyticsAllContentResult(analytics = analytics)
                }
            }
            is UiVerticalListItem.Loading -> {
                Timber.d("Loading item clicked")
            }
        }
    }

    fun onCreateObjectOfTypeClicked(objType: ObjectWrapper.Type) {
        proceedWithCreateDoc(objType)
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

    private fun initSheetState(items: List<UiHorizontalListItem.Item>) {
        uiSheetState.value = DateObjectSheetState.Content(items)
    }

    private fun handleError(e: Throwable) {
        uiContentState.value = UiContentState.Error(
            message = e.message ?: "An error occurred while loading data."
        )
    }

    fun hideError() {
        errorState.value = UiErrorState.Hidden
    }

    fun showDateOutOfRangeError() {
        viewModelScope.launch {
            errorState.emit(
                UiErrorState.Show(
                    Reason.YearOutOfRange(
                        min = DATE_PICKER_YEAR_RANGE.first,
                        max = DATE_PICKER_YEAR_RANGE.last
                    )
                )
            )
        }
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
        data class NavigateToDateObject(val objectId: Id, val space: SpaceId) : Command()
        data object TypeSelectionScreen : Command()
        data object ExitToSpaceWidgets : Command()
        sealed class SendToast : Command() {
            data class Error(val message: String) : SendToast()
            data class ObjectArchived(val name: String) : SendToast()
            data class UnexpectedLayout(val layout: String) : SendToast()
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