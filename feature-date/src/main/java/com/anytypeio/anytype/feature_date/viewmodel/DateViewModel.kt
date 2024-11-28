package com.anytypeio.anytype.feature_date.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.DATE_PICKER_YEAR_RANGE
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.TimeInSeconds
import com.anytypeio.anytype.core_models.getSingleValue
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds
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
import com.anytypeio.anytype.feature_date.viewmodel.UiErrorState.Reason
import com.anytypeio.anytype.feature_date.mapping.toUiFieldsItem
import com.anytypeio.anytype.feature_date.mapping.toUiObjectsListItem
import com.anytypeio.anytype.feature_date.ui.models.DateEvent
import com.anytypeio.anytype.feature_date.viewmodel.UiContentState.*
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentScreen
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.objects.getCreateObjectParams
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel.Companion.DEFAULT_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultKeys
import com.anytypeio.anytype.presentation.sync.toSyncStatusWidgetState
import kotlin.collections.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewState: @see [UiContentState]
 * Factory: @see [DateVMFactory]
 * Screen: @see [com.anytypeio.anytype.feature_date.ui.DateMainScreen]
 * Models: @see [UiObjectsListState]
 */
class DateViewModel(
    private val vmParams: DateVmParams,
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

    val uiCalendarIconState = MutableStateFlow<UiCalendarIconState>(UiCalendarIconState.Hidden)
    val uiSyncStatusBadgeState =
        MutableStateFlow<UiSyncStatusBadgeState>(UiSyncStatusBadgeState.Hidden)
    val uiHeaderState = MutableStateFlow<UiHeaderState>(UiHeaderState.Empty)
    val uiNavigationWidget = MutableStateFlow<UiNavigationWidget>(UiNavigationWidget.Hidden)
    val uiFieldsState = MutableStateFlow<UiFieldsState>(UiFieldsState.Empty)
    val uiFieldsSheetState = MutableStateFlow<UiFieldsSheetState>(UiFieldsSheetState.Hidden)
    val uiObjectsListState = MutableStateFlow<UiObjectsListState>(UiObjectsListState.Empty)
    val uiContentState = MutableStateFlow<UiContentState>(UiContentState.Idle())
    val uiCalendarState = MutableStateFlow<UiCalendarState>(UiCalendarState.Hidden)
    val uiSyncStatusWidgetState =
        MutableStateFlow<UiSyncStatusWidgetState>(UiSyncStatusWidgetState.Hidden)

    val effects = MutableSharedFlow<DateEffect>()
    val errorState = MutableStateFlow<UiErrorState>(UiErrorState.Hidden)

    private val _dateId = MutableStateFlow<Id?>(null)
    private val _dateTimestamp = MutableStateFlow<TimeInSeconds?>(null)
    private val _activeField = MutableStateFlow<ActiveField?>(null)

    /**
     * Paging and subscription limit. If true, we can paginate after reaching bottom items.
     * Could be true only after the first subscription results (if results size == limit)
     */
    val canPaginate = MutableStateFlow(false)
    private var _itemsLimit = DEFAULT_SEARCH_LIMIT
    private val restartSubscription = MutableStateFlow(0L)

    /**
     * Search query
     */
    private val userInput = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val searchQuery = userInput
        .take(1)
        .onCompletion {
            emitAll(userInput.drop(1).debounce(DEFAULT_DEBOUNCE_DURATION).distinctUntilChanged())
        }

    private var shouldScrollToTopItems = false

    private val permission = MutableStateFlow(userPermissionProvider.get(vmParams.spaceId))

    init {
        Timber.d("Init DateObjectViewModel, date object id: [${vmParams.objectId}], space: [${vmParams.spaceId}]")
        uiHeaderState.value = UiHeaderState.Loading
        uiFieldsState.value = UiFieldsState.LoadingState
        uiObjectsListState.value = UiObjectsListState.LoadingState
        proceedWithObservingPermissions()
        proceedWithGettingDateObject()
        proceedWithGettingDateObjectRelationList()
        proceedWithObservingSyncStatus()
        setupSearchStateFlow()
        _dateId.value = vmParams.objectId
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
        uiObjectsListState.value = UiObjectsListState.Empty
        uiContentState.value = UiContentState.Empty
    }

    override fun onCleared() {
        Timber.d("onCleared")
        super.onCleared()
        uiContentState.value = UiContentState.Empty
        uiHeaderState.value = UiHeaderState.Empty
        uiCalendarIconState.value = UiCalendarIconState.Hidden
        uiSyncStatusBadgeState.value = UiSyncStatusBadgeState.Hidden
        uiFieldsState.value = UiFieldsState.Empty
        uiObjectsListState.value = UiObjectsListState.Empty
        uiFieldsSheetState.value = UiFieldsSheetState.Hidden
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
        uiFieldsState.value = UiFieldsState.Empty
        uiObjectsListState.value = UiObjectsListState.Empty
        uiFieldsSheetState.value = UiFieldsSheetState.Hidden
        _activeField.value = null
        _dateId.value = dateObjectId
    }

    private fun setupSearchStateFlow() {
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                if (uiFieldsSheetState.value is UiFieldsSheetState.Hidden) return@collectLatest
                val items = uiFieldsState.value.items
                if (items.isEmpty()) return@collectLatest
                val filteredItems = if (query.isBlank()) {
                    items
                } else {
                    items.filterIsInstance<UiFieldsItem.Item>()
                        .filter { it.title.contains(query, ignoreCase = true) }
                }
                uiFieldsSheetState.value = UiFieldsSheetState.Visible(
                    items = filteredItems
                )
            }
        }
    }

    //region Initialization
    private fun proceedWithObservingPermissions() {
        viewModelScope.launch {
            userPermissionProvider
                .observe(space = vmParams.spaceId)
                .collect { result ->
                    uiNavigationWidget.value = if (result?.isOwnerOrEditor() == true) {
                        UiNavigationWidget.Editor
                    } else {
                        UiNavigationWidget.Viewer
                    }
                    permission.value = result
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
                    uiSyncStatusBadgeState.value = UiSyncStatusBadgeState.Visible(syncAndP2pState)
                    val state = uiSyncStatusWidgetState.value
                    uiSyncStatusWidgetState.value = when (state) {
                        UiSyncStatusWidgetState.Hidden -> UiSyncStatusWidgetState.Hidden
                        is UiSyncStatusWidgetState.Visible -> state.copy(
                            status = syncAndP2pState.toSyncStatusWidgetState()
                        )
                    }
                }
        }
    }

    private fun proceedWithGettingDateObjectRelationList() {
        viewModelScope.launch {
            _dateId
                .filterNotNull()
                .collect { id ->
                    val params = RelationListWithValue.Params(
                        space = vmParams.spaceId,
                        value = id
                    )
                    Timber.d("Start RelationListWithValue with params: $params")
                    relationListWithValue.async(params).fold(
                        onSuccess = { result ->
                            Timber.d("RelationListWithValue Success: $result")
                            val items =
                                result.toUiFieldsItem(storeOfRelations = storeOfRelations)
                            initFieldsState(items)
                        },
                        onFailure = { e ->
                            Timber.e(e, "RelationListWithValue Error")
                            errorState.value = UiErrorState.Show(
                                Reason.ErrorGettingFields(
                                    msg = e.message ?: "Error getting fields"
                                )
                            )
                        }
                    )
                }
        }
    }

    private fun proceedWithGettingDateObject() {
        viewModelScope.launch {
            _dateId
                .filterNotNull()
                .collect { id ->
                    val params = GetObject.Params(
                        target = id,
                        space = vmParams.spaceId
                    )
                    Timber.d("Start GetObject with params: $params")
                    getObject.async(params).fold(
                        onSuccess = { obj ->
                            Timber.d("GetObject Success, obj:[$obj]")
                            val timestampInSeconds =
                                obj.details[id]?.getSingleValue<Double>(
                                    Relations.TIMESTAMP
                                )?.toLong()
                            if (timestampInSeconds != null) {
                                _dateTimestamp.value = timestampInSeconds
                                val (formattedDate, _) = dateProvider.formatTimestampToDateAndTime(
                                    timestamp = timestampInSeconds * 1000,
                                )
                                uiCalendarIconState.value = UiCalendarIconState.Visible(
                                    timestampInSeconds = TimestampInSeconds(timestampInSeconds)
                                )
                                uiHeaderState.value = UiHeaderState.Content(
                                    title = formattedDate
                                )
                            }
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
                _dateId.filterNotNull(),
                _dateTimestamp.filterNotNull(),
                _activeField.filterNotNull(),
                restartSubscription
            ) { dateId, timestamp, activeField, _ ->
                loadData(dateId = dateId, timestamp = timestamp, field = activeField)
            }
                .catch {
                    errorState.value = UiErrorState.Show(
                        Reason.Other(it.message ?: "Error getting data")
                    )
                }.flatMapLatest { items ->
                    items
                }.collectLatest { items ->
                    uiObjectsListState.value = UiObjectsListState(items)
                }
        }
    }

    private fun loadData(
        dateId: Id,
        timestamp: TimeInSeconds,
        field: ActiveField,
    ): Flow<List<UiObjectsListItem>> {

        val searchParams = createSearchParams(
            dateId = dateId,
            timestamp = timestamp,
            space = vmParams.spaceId,
            itemsLimit = _itemsLimit,
            field = field
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
                errorState.value = UiErrorState.Show(
                    Reason.ErrorGettingObjects(
                        e.message ?: "Error getting objects"
                    )
                )
            }
    }

    private suspend fun handleData(
        objWrappers: List<ObjectWrapper.Basic>
    ): List<UiObjectsListItem> {

        canPaginate.value = objWrappers.size == _itemsLimit

        val items = objWrappers.map {
            it.toUiObjectsListItem(
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
        dateId: Id,
        timestamp: TimeInSeconds,
        field: ActiveField,
        space: SpaceId,
        itemsLimit: Int
    ): StoreSearchParams {
        val (filters, sorts) = filtersAndSortsForSearch(
            spaces = listOf(space.id),
            field = field,
            timestamp = timestamp,
            dateId = dateId
        )
        return StoreSearchParams(
            space = space,
            filters = filters,
            sorts = sorts,
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
    private fun onFieldsEvent(item: UiFieldsItem, needToScroll: Boolean = false) {
        when (item) {
            is UiFieldsItem.Item -> {
                if (_activeField.value?.key == item.key) {
                    val value = _activeField.value
                    val activeSort = _activeField.value?.sort ?: DEFAULT_SORT_TYPE
                    _activeField.value = value?.copy(
                        sort = if (activeSort == DVSortType.ASC) {
                            DVSortType.DESC
                        } else {
                            DVSortType.ASC
                        }
                    )
                    shouldScrollToTopItems = true
                    resetLimit()
                    canPaginate.value = false
                    uiContentState.value = Idle()
                    uiObjectsListState.value = UiObjectsListState.Empty
                    restartSubscription.value++
                    updateHorizontalListState(selectedItem = item, needToScroll = needToScroll)
                } else {
                    shouldScrollToTopItems = true
                    resetLimit()
                    canPaginate.value = false
                    uiContentState.value = Idle()
                    _activeField.value = ActiveField(
                        key = item.key,
                        format = item.relationFormat
                    )
                    restartSubscription.value++
                    updateHorizontalListState(selectedItem = item, needToScroll = needToScroll)
                }
            }

            is UiFieldsItem.Settings -> {
                val items = uiFieldsState.value.items
                uiFieldsSheetState.value = UiFieldsSheetState.Visible(
                    items = items.filterIsInstance<UiFieldsItem.Item>()
                )
            }

            else -> {}
        }
    }

    private fun proceedWithReopeningDate(offset: Int) {
        val timestamp = _dateTimestamp.value
        if (timestamp == null) {
            Timber.w("Error getting timestamp")
            return
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
                    effects.emit(
                        DateEffect.NavigateToSetOrCollection(
                            id = navigation.target,
                            space = SpaceId(navigation.space)
                        )
                    )
                }

                is OpenObjectNavigation.OpenEditor -> {
                    effects.emit(
                        DateEffect.NavigateToEditor(
                            id = navigation.target,
                            space = SpaceId(navigation.space)
                        )
                    )
                }

                is OpenObjectNavigation.UnexpectedLayoutError -> {
                    Timber.e("Unexpected layout: ${navigation.layout}")
                    effects.emit(DateEffect.SendToast.UnexpectedLayout(navigation.layout?.name.orEmpty()))
                }

                is OpenObjectNavigation.OpenDiscussion -> {
                    effects.emit(
                        DateEffect.OpenChat(
                            target = navigation.target,
                            space = SpaceId(navigation.space)
                        )
                    )
                }

                OpenObjectNavigation.NonValidObject -> {
                    Timber.e("Object id is missing")
                }

                is OpenObjectNavigation.OpenDataObject -> {
                    effects.emit(
                        DateEffect.NavigateToEditor(
                            id = navigation.target,
                            space = SpaceId(navigation.space)
                        )
                    )
                }
            }
        }
    }

    private fun onItemClicked(item: UiObjectsListItem) {
        Timber.d("onItemClicked: ${item.id}")
        when (item) {
            is UiObjectsListItem.Item -> {
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

            is UiObjectsListItem.Loading -> {
                Timber.d("Loading item clicked")
            }
        }
    }

    fun onCreateObjectOfTypeClicked(objType: ObjectWrapper.Type) {
        proceedWithCreateDoc(objType)
    }

    fun onDateEvent(event: DateEvent) {
        when (event) {
            is DateEvent.Calendar -> onCalendarEvent(event)
            is DateEvent.TopToolbar -> onTopToolbarEvent(event)
            is DateEvent.Header -> onHeaderEvent(event)
            is DateEvent.FieldsSheet -> onFieldsSheetEvent(event)
            is DateEvent.FieldsList -> onFieldsListEvent(event)
            is DateEvent.NavigationWidget -> onNavigationWidgetEvent(event)
            is DateEvent.ObjectsList -> onObjectsListEvent(event)
            is DateEvent.SyncStatusWidget -> onSyncStatusWidgetEvent(event)
        }
    }

    private fun onFieldsListEvent(event: DateEvent.FieldsList) {
        when (event) {
            DateEvent.FieldsList.OnScrolledToItemDismiss -> {
                uiFieldsState.value = uiFieldsState.value.copy(
                    needToScrollTo = false
                )
            }
            is DateEvent.FieldsList.OnFieldClick -> onFieldsEvent(event.item)
        }
    }

    private fun onSyncStatusWidgetEvent(event: DateEvent.SyncStatusWidget) {
        when (event) {
            DateEvent.SyncStatusWidget.OnSyncStatusDismiss -> {
                uiSyncStatusWidgetState.value = UiSyncStatusWidgetState.Hidden
            }
        }
    }

    private fun onObjectsListEvent(event: DateEvent.ObjectsList) {
        when (event) {
            DateEvent.ObjectsList.OnLoadMore -> updateLimit()
            is DateEvent.ObjectsList.OnObjectClicked -> onItemClicked(event.item)
        }
    }

    private fun onTopToolbarEvent(event: DateEvent.TopToolbar) {
        when (event) {
            is DateEvent.TopToolbar.OnCalendarClick -> {
                val timestampInSeconds = event.timestampInSeconds
                val timeInMillis = dateProvider.adjustToStartOfDayInUserTimeZone(
                    timestamp = timestampInSeconds.time
                )
                val isValid = dateProvider.isTimestampWithinYearRange(
                    timeStampInMillis = timeInMillis,
                    yearRange = DATE_PICKER_YEAR_RANGE
                )
                if (isValid) {
                    uiCalendarState.value = UiCalendarState.Calendar(
                        timeInMillis = timeInMillis
                    )
                } else {
                    showDateOutOfRangeError()
                }
            }

            is DateEvent.TopToolbar.OnSyncStatusClick -> {
                uiSyncStatusWidgetState.value =
                    UiSyncStatusWidgetState.Visible(
                        status = event.status.toSyncStatusWidgetState()
                    )
            }
        }
    }

    private fun onHeaderEvent(event: DateEvent.Header) {
        when (event) {
            DateEvent.Header.OnNextClick -> proceedWithReopeningDate(offset = SECONDS_IN_DAY)
            DateEvent.Header.OnPreviousClick -> proceedWithReopeningDate(offset = -SECONDS_IN_DAY)
        }
    }

    private fun onCalendarEvent(event: DateEvent.Calendar) {
        when (event) {
            is DateEvent.Calendar.OnCalendarDateSelected -> {
                uiCalendarState.value = UiCalendarState.Hidden
                val timeInMillis = event.timeInMillis
                Timber.d("Selected date in millis: [$timeInMillis]")
                if (timeInMillis == null) return
                proceedWithReopenDateObjectByTimestamp(
                    timestamp = dateProvider.adjustFromStartOfDayInUserTimeZoneToUTC(
                        timeInMillis = timeInMillis
                    )
                )
            }

            DateEvent.Calendar.OnCalendarDismiss -> {
                uiCalendarState.value = UiCalendarState.Hidden
            }

            DateEvent.Calendar.OnTodayClick -> {
                uiCalendarState.value = UiCalendarState.Hidden
                proceedWithReopenDateObjectByTimestamp(
                    timestamp = dateProvider.getTimestampForTodayAtStartOfDay()
                )
            }

            DateEvent.Calendar.OnTomorrowClick -> {
                uiCalendarState.value = UiCalendarState.Hidden
                proceedWithReopenDateObjectByTimestamp(
                    timestamp = dateProvider.getTimestampForTomorrowAtStartOfDay()
                )
            }
        }
    }

    private fun onFieldsSheetEvent(event: DateEvent.FieldsSheet) {
        when (event) {
            is DateEvent.FieldsSheet.OnFieldClick -> {
                uiFieldsSheetState.value = UiFieldsSheetState.Hidden
                onFieldsEvent(event.item, needToScroll = true)
            }

            is DateEvent.FieldsSheet.OnSearchQueryChanged -> {
                Timber.d("Search query: ${event.query}")
                userInput.value = event.query
            }

            DateEvent.FieldsSheet.OnSheetDismiss -> {
                uiFieldsSheetState.value = UiFieldsSheetState.Hidden
            }
        }
    }

    private fun onNavigationWidgetEvent(event: DateEvent.NavigationWidget) {
        when (event) {
            DateEvent.NavigationWidget.OnAddDocClick -> {
                proceedWithCreateDoc()
            }

            DateEvent.NavigationWidget.OnAddDocLongClick -> {
                viewModelScope.launch {
                    effects.emit(DateEffect.TypeSelectionScreen)
                }
            }

            DateEvent.NavigationWidget.OnBackClick -> {
                viewModelScope.launch {
                    effects.emit(DateEffect.Back)
                }
            }

            DateEvent.NavigationWidget.OnBackLongClick -> {
                viewModelScope.launch {
                    effects.emit(DateEffect.ExitToSpaceWidgets)
                }
            }

            DateEvent.NavigationWidget.OnGlobalSearchClick -> {
                viewModelScope.launch {
                    effects.emit(DateEffect.OpenGlobalSearch)
                }
            }
        }
    }
    //endregion

    //region Ui State
    private fun initFieldsState(relations: List<UiFieldsItem.Item>) {
        val relation = relations.getOrNull(0)
        if (relation == null) {
            Timber.e("Error getting relation")
            return
        }
        _activeField.value = ActiveField(
            key = relation.key,
            format = relation.relationFormat
        )
        restartSubscription.value++
        uiFieldsState.value = UiFieldsState(
            items = buildList {
                add(UiFieldsItem.Settings())
                addAll(relations)
            },
            selectedRelationKey = _activeField.value?.key
        )
        if (relations.isEmpty()) {
            uiContentState.value = UiContentState.Empty
            uiObjectsListState.value = UiObjectsListState.Empty
        }
    }

    private fun updateHorizontalListState(selectedItem: UiFieldsItem.Item, needToScroll: Boolean = false) {
        uiFieldsState.value = uiFieldsState.value.copy(
            selectedRelationKey = selectedItem.key,
            needToScrollTo = needToScroll
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

    companion object {
        //INITIAL STATE
        const val SECONDS_IN_DAY = 86400
        const val DEFAULT_SEARCH_LIMIT = 25
        val DEFAULT_SORT_TYPE = DVSortType.DESC
    }
}