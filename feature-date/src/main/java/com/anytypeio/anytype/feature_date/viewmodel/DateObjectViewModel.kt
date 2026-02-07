package com.anytypeio.anytype.feature_date.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.DATE_PICKER_YEAR_RANGE
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationListWithValueItem
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.TimeInSeconds
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.getSingleValue
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.misc.navigation
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds
import com.anytypeio.anytype.core_ui.lists.objects.UiContentState
import com.anytypeio.anytype.core_ui.lists.objects.UiContentState.Idle
import com.anytypeio.anytype.core_ui.lists.objects.UiObjectsListState
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.relations.GetObjectRelationListById
import com.anytypeio.anytype.feature_date.mapping.toUiFieldsItem
import com.anytypeio.anytype.feature_date.ui.models.DateEvent
import com.anytypeio.anytype.feature_date.viewmodel.UiErrorState.Reason
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsClickDateBack
import com.anytypeio.anytype.presentation.extension.sendAnalyticsClickDateCalendarView
import com.anytypeio.anytype.presentation.extension.sendAnalyticsClickDateForward
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectListSort
import com.anytypeio.anytype.presentation.extension.sendAnalyticsScreenDate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSwitchRelationDate
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem
import com.anytypeio.anytype.presentation.objects.getCreateObjectParams
import com.anytypeio.anytype.presentation.objects.toUiObjectsListItem
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel.Companion.DEFAULT_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.sync.toSyncStatusWidgetState
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewState: @see [UiContentState]
 * Factory: @see [DateObjectVMFactory]
 * Screen: @see [com.anytypeio.anytype.feature_date.ui.DateMainScreen]
 * Models: @see [UiObjectsListState]
 */
class DateObjectViewModel(
    private val vmParams: DateObjectVmParams,
    private val getObject: GetObject,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val userPermissionProvider: UserPermissionProvider,
    private val getObjectRelationListById: GetObjectRelationListById,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val dateProvider: DateProvider,
    private val spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider,
    private val createObject: CreateObject,
    private val fieldParser: FieldParser,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val getDateObjectByTimestamp: GetDateObjectByTimestamp,
    private val spaceViews: SpaceViewSubscriptionContainer
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val uiCalendarIconState = MutableStateFlow<UiCalendarIconState>(UiCalendarIconState.Hidden)
    val uiSyncStatusBadgeState =
        MutableStateFlow<UiSyncStatusBadgeState>(UiSyncStatusBadgeState.Hidden)
    val uiHeaderState = MutableStateFlow<UiHeaderState>(UiHeaderState.Empty)
    val uiNavigationWidget = MutableStateFlow<NavPanelState>(NavPanelState.Init)
    val uiFieldsState = MutableStateFlow<UiFieldsState>(UiFieldsState.Empty)
    val uiFieldsSheetState = MutableStateFlow<UiFieldsSheetState>(UiFieldsSheetState.Hidden)
    val uiObjectsListState = MutableStateFlow<UiObjectsListState>(UiObjectsListState.Empty)
    val uiContentState = MutableStateFlow<UiContentState>(UiContentState.Idle())
    val uiCalendarState = MutableStateFlow<UiCalendarState>(UiCalendarState.Hidden)
    val uiSyncStatusWidgetState =
        MutableStateFlow<SyncStatusWidgetState>(SyncStatusWidgetState.Hidden)
    val uiSnackbarState = MutableStateFlow<UiSnackbarState>(UiSnackbarState.Hidden)

    val effects = MutableSharedFlow<DateObjectCommand>()
    val errorState = MutableStateFlow<UiErrorState>(UiErrorState.Hidden)

    private val _dateId = MutableStateFlow<Id?>(null)
    private val _dateTimestamp = MutableStateFlow<TimeInSeconds?>(null)
    private val _activeField = MutableStateFlow<ActiveField?>(null)
    private val _dateObjectFieldIds: MutableStateFlow<List<RelationListWithValueItem>> = MutableStateFlow(emptyList())

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
        proceedWithObservingRelationListWithValue()
        proceedWithObservingPermissions()
        observeDateIdForObject()
        observeDateIdForRelations()
        proceedWithObservingSyncStatus()
        setupSearchStateFlow()
        _dateId.value = vmParams.objectId
    }

    fun onStart() {
        Timber.d("onStart")
        setupUiStateFlow()
        viewModelScope.launch {
            sendAnalyticsScreenDate(
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

    private fun proceedWithReopenDateObjectByTimestamp(timeInSeconds: TimeInSeconds) {
        viewModelScope.launch {
            val params = GetDateObjectByTimestamp.Params(
                space = vmParams.spaceId,
                timestampInSeconds = timeInSeconds
            )
            getDateObjectByTimestamp.async(params).fold(
                onSuccess = { dateObject ->
                    val obj = ObjectWrapper.Basic(dateObject.orEmpty())
                    if (obj.isValid) {
                        reopenDateObject(obj.id)
                    } else {
                        Timber.w("Date object is invalid")
                        errorState.value = UiErrorState.Show(
                            Reason.Other(msg = "Couldn't open date object, object is invalid")
                        )
                    }
                },
                onFailure = { e ->
                    Timber.e(e, "Failed to get date object by timestamp :$timeInSeconds")
                    errorState.value = UiErrorState.Show(
                        Reason.Other(msg = "Couldn't open date object:\n${e.message?.take(30)}")
                    )
                }
            )
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
        _dateObjectFieldIds.value = emptyList()
        _dateId.value = null
        _dateTimestamp.value = null
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
                    val navPanelState = NavPanelState.fromPermission(
                        permission = result,
                        spaceUxType = spaceViews.get(space = vmParams.spaceId)?.spaceUxType ?: SpaceUxType.DATA,
                    )
                    uiNavigationWidget.value = navPanelState
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
                        SyncStatusWidgetState.Hidden -> SyncStatusWidgetState.Hidden
                        else -> syncAndP2pState.toSyncStatusWidgetState()
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun proceedWithObservingRelationListWithValue() {
        viewModelScope.launch {
            combine(
                _dateObjectFieldIds,
                storeOfRelations.observe()
            ) { relationIds, store ->
                relationIds to store
            }.collect { (relationIds, store) ->
                Timber.d("RelationListWithValue: $relationIds")
                if (store.isEmpty()) {
                    handleEmptyFieldsState()
                } else {
                    initFieldsState(
                        items = relationIds.toUiFieldsItem(storeOfRelations = storeOfRelations)
                    )
                }
            }
        }
    }

    private fun observeDateIdForObject() {
        viewModelScope.launch {
            _dateId.filterNotNull().collect { id ->
                proceedWithGettingDateObject(id)
            }
        }
    }

    private fun observeDateIdForRelations() {
        viewModelScope.launch {
            _dateId.filterNotNull().collect { id ->
                proceedWithGettingDateObjectRelationList(id)
            }
        }
    }

    private suspend fun proceedWithGettingDateObjectRelationList(id: Id) {
        val params = GetObjectRelationListById.Params(
            space = vmParams.spaceId,
            value = id
        )
        getObjectRelationListById.async(params).fold(
            onSuccess = { result ->
                Timber.d("RelationListWithValue success: $result")
                _dateObjectFieldIds.value = result
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

    private suspend fun proceedWithGettingDateObject(id: Id) {
        val params = GetObject.Params(
            target = id,
            space = vmParams.spaceId,
            saveAsLastOpened = true
        )
        getObject.async(params).fold(
            onSuccess = { obj ->
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
                        title = formattedDate,
                        relativeDate = dateProvider.calculateRelativeDates(
                            dateInSeconds = timestampInSeconds
                        )
                    )
                } else {
                    Timber.e("Error getting timestamp")
                    errorState.value = UiErrorState.Show(
                        Reason.Other(msg = "Error getting timestamp from date object")
                    )
                }
            },
            onFailure = { e ->
                Timber.e(e, "GetObject Error")
                errorState.value = UiErrorState.Show(
                    Reason.Other(
                        msg = "Error opening date object: ${e.message?.take(30) ?: "Unknown error"}"
                    )
                )
            }
        )
    }
    //endregion

    //region Subscription
    fun subscriptionId() = "date_object_subscription_${vmParams.spaceId}"

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupUiStateFlow() {
        viewModelScope.launch {
            combine(
                _dateId,
                _dateTimestamp,
                _activeField,
                restartSubscription
            ) { dateId, timestamp, activeField, _ ->
                Timber.d("setupUiStateFlow, Combine: dateId: $dateId, timestamp: $timestamp, activeField: $activeField")

                // If any of these are null, we return null to indicate we should skip loading data.
                if (dateId == null || timestamp == null || activeField == null) {
                    null
                } else {
                    createSearchParams(
                        dateId = dateId,
                        timestamp = timestamp,
                        space = vmParams.spaceId,
                        itemsLimit = _itemsLimit,
                        field = activeField
                    )
                }
            }
                .flatMapLatest { searchParams ->
                    if (searchParams == null) {
                        Timber.d("Search params are null, skipping loadData")
                        // If searchParams is null, we skip loadData and emit an empty list.
                        flowOf(emptyList())
                    } else {
                        Timber.d("Search params are not null, loading data")
                        loadData(searchParams)
                    }
                }
                .catch {
                    errorState.value = UiErrorState.Show(
                        Reason.Other(it.message ?: "Error getting data")
                    )
                }
                .collect { items ->
                    uiObjectsListState.value = UiObjectsListState(items)
                }
        }
    }

    private fun loadData(
        searchParams: StoreSearchParams
    ): Flow<List<UiObjectsListItem>> {

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
                objectTypes = storeOfObjectTypes.getAll(),
                fieldParser = fieldParser,
                isOwnerOrEditor = permission.value?.isOwnerOrEditor() == true,
                storeOfObjectTypes = storeOfObjectTypes
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
                    viewModelScope.launch {
                        sendAnalyticsObjectListSort(
                            analytics = analytics,
                            sortType = _activeField.value?.sort ?: DEFAULT_SORT_TYPE
                        )
                    }
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
                    viewModelScope.launch {
                        sendAnalyticsSwitchRelationDate(
                            analytics = analytics,
                            storeOfRelations = storeOfRelations,
                            relationKey = item.key
                        )
                    }
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
                timeInSeconds = newTimestamp
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
                        DateObjectCommand.NavigateToSetOrCollection(
                            id = navigation.target,
                            space = SpaceId(navigation.space)
                        )
                    )
                }

                is OpenObjectNavigation.OpenParticipant -> {
                    effects.emit(
                        DateObjectCommand.NavigateToParticipant(
                            objectId = navigation.target,
                            space = SpaceId(navigation.space)
                        )
                    )
                }

                is OpenObjectNavigation.OpenEditor -> {
                    effects.emit(
                        DateObjectCommand.NavigateToEditor(
                            id = navigation.target,
                            space = SpaceId(navigation.space)
                        )
                    )
                }

                is OpenObjectNavigation.UnexpectedLayoutError -> {
                    Timber.e("Unexpected layout: ${navigation.layout}")
                    effects.emit(DateObjectCommand.SendToast.UnexpectedLayout(navigation.layout?.name.orEmpty()))
                }

                is OpenObjectNavigation.OpenChat -> {
                    effects.emit(
                        DateObjectCommand.OpenChat(
                            target = navigation.target,
                            space = SpaceId(navigation.space)
                        )
                    )
                }
                is OpenObjectNavigation.OpenType -> {
                    effects.emit(
                        DateObjectCommand.OpenType(
                            target = navigation.target,
                            space = SpaceId(navigation.space)
                        )
                    )
                }
                is OpenObjectNavigation.OpenBookmarkUrl -> {
                    effects.emit(
                        DateObjectCommand.OpenUrl(url = navigation.url)
                    )
                }
                OpenObjectNavigation.NonValidObject -> {
                    Timber.e("Object id is missing")
                }

                is OpenObjectNavigation.OpenDateObject -> {
                    effects.emit(
                        DateObjectCommand.NavigateToEditor(
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
                proceedWithNavigation(
                    navigation = item.obj.navigation()
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
        Timber.d("onDateEvent: $event")
        when (event) {
            is DateEvent.Calendar -> onCalendarEvent(event)
            is DateEvent.TopToolbar -> onTopToolbarEvent(event)
            is DateEvent.Header -> onHeaderEvent(event)
            is DateEvent.FieldsSheet -> onFieldsSheetEvent(event)
            is DateEvent.FieldsList -> onFieldsListEvent(event)
            is DateEvent.NavigationWidget -> onNavigationWidgetEvent(event)
            is DateEvent.ObjectsList -> onObjectsListEvent(event)
            is DateEvent.SyncStatusWidget -> onSyncStatusWidgetEvent(event)
            is DateEvent.Snackbar -> onSnackbarEvent(event)
        }
    }

    private fun onSnackbarEvent(event: DateEvent.Snackbar) {
        when (event) {
            DateEvent.Snackbar.OnSnackbarDismiss -> {
                proceedWithDismissSnackbar()
            }
            is DateEvent.Snackbar.UndoMoveToBin -> {
                proceedWithUndoMoveToBin(event.objectId)
            }
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
                uiSyncStatusWidgetState.value = SyncStatusWidgetState.Hidden
            }
        }
    }

    private fun onObjectsListEvent(event: DateEvent.ObjectsList) {
        when (event) {
            DateEvent.ObjectsList.OnLoadMore -> updateLimit()
            is DateEvent.ObjectsList.OnObjectClicked -> onItemClicked(event.item)
            is DateEvent.ObjectsList.OnObjectMoveToBin -> proceedWithMoveToBin(event.item)
        }
    }

    private fun onTopToolbarEvent(event: DateEvent.TopToolbar) {
        when (event) {
            is DateEvent.TopToolbar.OnCalendarClick -> {
                proceedWithShowCalendar(timestampInSeconds = event.timestampInSeconds)
                viewModelScope.launch {
                    sendAnalyticsClickDateCalendarView(
                        analytics = analytics
                    )
                }
            }

            is DateEvent.TopToolbar.OnSyncStatusClick -> {
                uiSyncStatusWidgetState.value =
                    event.status.toSyncStatusWidgetState()
            }
        }
    }

    private fun proceedWithShowCalendar(timestampInSeconds: TimestampInSeconds) {
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

    private fun onHeaderEvent(event: DateEvent.Header) {
        when (event) {
            DateEvent.Header.OnNextClick -> {
                proceedWithReopeningDate(offset = SECONDS_IN_DAY)
                viewModelScope.launch {
                    sendAnalyticsClickDateForward(
                        analytics = analytics
                    )
                }

            }
            DateEvent.Header.OnPreviousClick -> {
                proceedWithReopeningDate(offset = -SECONDS_IN_DAY)
                viewModelScope.launch {
                    sendAnalyticsClickDateBack(
                        analytics = analytics
                    )
                }
            }
            is DateEvent.Header.OnHeaderClick -> {
                val timestampInSeconds = TimestampInSeconds(
                    time = event.timeInMillis / 1000
                )
                proceedWithShowCalendar(timestampInSeconds)
                viewModelScope.launch {
                    sendAnalyticsClickDateCalendarView(
                        analytics = analytics
                    )
                }
            }
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
                    timeInSeconds = dateProvider.adjustFromStartOfDayInUserTimeZoneToUTC(
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
                    timeInSeconds = dateProvider.getTimestampForTodayAtStartOfDay()
                )
            }

            DateEvent.Calendar.OnTomorrowClick -> {
                uiCalendarState.value = UiCalendarState.Hidden
                proceedWithReopenDateObjectByTimestamp(
                    timeInSeconds = dateProvider.getTimestampForTomorrowAtStartOfDay()
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
                    effects.emit(DateObjectCommand.TypeSelectionScreen)
                }
            }

            DateEvent.NavigationWidget.OnBackClick -> {
                viewModelScope.launch {
                    effects.emit(DateObjectCommand.Back)
                }
            }

            DateEvent.NavigationWidget.OnBackLongClick -> {
                viewModelScope.launch {
                    effects.emit(DateObjectCommand.ExitToHomeOrChat)
                }
            }

            DateEvent.NavigationWidget.OnGlobalSearchClick -> {
                viewModelScope.launch {
                    effects.emit(DateObjectCommand.OpenGlobalSearch)
                }
            }

            DateEvent.NavigationWidget.OnHomeClick -> {
                viewModelScope.launch {
                    effects.emit(DateObjectCommand.ExitToHomeOrChat)
                }
            }
        }
    }

    fun proceedWithMoveToBin(item: UiObjectsListItem.Item) {
        val params = SetObjectListIsArchived.Params(
            targets = listOf(item.id),
            isArchived = true
        )
        viewModelScope.launch {
            setObjectListIsArchived.async(params).fold(
                onSuccess = { ids ->
                    Timber.d("Successfully archived object: $ids")
                    val name = item.name
                    uiSnackbarState.value = UiSnackbarState.Visible(
                        message = name.take(10),
                        objId = item.id
                    )
                },
                onFailure = { e ->
                    Timber.e(e, "Error while archiving object")
                    effects.emit(DateObjectCommand.SendToast.Error("Error while archiving object"))
                }
            )
        }
    }

    fun proceedWithUndoMoveToBin(objectId: Id) {
        val params = SetObjectListIsArchived.Params(
            targets = listOf(objectId),
            isArchived = false
        )
        viewModelScope.launch {
            setObjectListIsArchived.async(params).fold(
                onSuccess = { ids ->
                    Timber.d("Successfully archived object: $ids")
                    uiSnackbarState.value = UiSnackbarState.Hidden
                },
                onFailure = { e ->
                    Timber.e(e, "Error while un-archiving object")
                    effects.emit(DateObjectCommand.SendToast.Error("Error while un-archiving object"))
                }
            )
        }
    }
    //endregion

    //region Ui State
    private fun initFieldsState(items: List<UiFieldsItem.Item>) {
        Timber.d("Init fields state with items: $items")
        if (items.isEmpty()) {
            handleEmptyFieldsState()
            return
        }

        val firstItem = items.first()
        _activeField.value = ActiveField(
            key = firstItem.key,
            format = firstItem.relationFormat
        )
        restartSubscription.value++
        uiFieldsState.value = UiFieldsState(
            items = buildList {
                add(UiFieldsItem.Settings())
                addAll(items)
            },
            selectedRelationKey = _activeField.value?.key
        )
    }

    private fun handleEmptyFieldsState() {
        uiFieldsState.value = UiFieldsState.Empty
        uiContentState.value = UiContentState.Empty
        uiObjectsListState.value = UiObjectsListState.Empty
        _activeField.value = null
        canPaginate.value = false
        resetLimit()
        shouldScrollToTopItems = true
        uiFieldsSheetState.value = UiFieldsSheetState.Hidden
        Timber.w("Error getting fields for date object:${_dateId.value}, fields are empty")
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

    fun proceedWithDismissSnackbar() {
        uiSnackbarState.value = UiSnackbarState.Hidden
    }
    //endregion

    companion object {
        //INITIAL STATE
        const val SECONDS_IN_DAY = 86400
        const val DEFAULT_SEARCH_LIMIT = 25
        val DEFAULT_SORT_TYPE = DVSortType.DESC
    }
}