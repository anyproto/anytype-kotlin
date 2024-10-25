package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.libraryScreenRelation
import com.anytypeio.anytype.analytics.base.EventsDictionary.libraryScreenType
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_utils.ext.orNull
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.domain.all_content.RestoreAllContentState
import com.anytypeio.anytype.domain.all_content.UpdateAllContentState
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.RemoveObjectsFromWorkspace
import com.anytypeio.anytype.feature_allcontent.models.AllContentBottomMenu
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentSort
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.MenuSortsItem
import com.anytypeio.anytype.feature_allcontent.models.UiContentItem
import com.anytypeio.anytype.feature_allcontent.models.UiContentState
import com.anytypeio.anytype.feature_allcontent.models.UiItemsState
import com.anytypeio.anytype.feature_allcontent.models.UiMenuState
import com.anytypeio.anytype.feature_allcontent.models.UiSnackbarState
import com.anytypeio.anytype.feature_allcontent.models.UiTabsState
import com.anytypeio.anytype.feature_allcontent.models.UiTitleState
import com.anytypeio.anytype.feature_allcontent.models.createSubscriptionParams
import com.anytypeio.anytype.feature_allcontent.models.filtersForSearch
import com.anytypeio.anytype.feature_allcontent.models.mapToSort
import com.anytypeio.anytype.feature_allcontent.models.toAnalyticsModeType
import com.anytypeio.anytype.feature_allcontent.models.toAnalyticsSortType
import com.anytypeio.anytype.feature_allcontent.models.toAnalyticsTabType
import com.anytypeio.anytype.feature_allcontent.models.toUiContentItems
import com.anytypeio.anytype.feature_allcontent.models.toUiContentRelations
import com.anytypeio.anytype.feature_allcontent.models.toUiContentTypes
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentChangeMode
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentChangeSort
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentChangeType
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentResult
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentScreen
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentSearchInput
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAllContentToBin
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.library.LibraryViewModel.LibraryItem
import com.anytypeio.anytype.presentation.objects.getCreateObjectParams
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewState: @see [UiContentState]
 * Factory: @see [AllContentViewModelFactory]
 * Screen: @see [com.anytypeio.anytype.feature_allcontent.ui.AllContentWrapperScreen]
 * Models: @see [com.anytypeio.anytype.feature_allcontent.models.UiContentState]
 */

class AllContentViewModel(
    private val vmParams: VmParams,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val updateAllContentState: UpdateAllContentState,
    private val restoreAllContentState: RestoreAllContentState,
    private val searchObjects: SearchObjects,
    private val localeProvider: LocaleProvider,
    private val createObject: CreateObject,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val setObjectDetails: SetObjectDetails,
    private val removeObjectsFromWorkspace: RemoveObjectsFromWorkspace,
    private val userPermissionProvider: UserPermissionProvider
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    private val searchResultIds = MutableStateFlow<List<Id>>(emptyList())
    private val sortState = MutableStateFlow<AllContentSort>(AllContentSort.ByName())
    val uiTitleState = MutableStateFlow<UiTitleState>(DEFAULT_INITIAL_MODE)
    val uiTabsState = MutableStateFlow<UiTabsState>(UiTabsState())
    val uiMenuState = MutableStateFlow<UiMenuState>(UiMenuState.Hidden)
    val uiItemsState = MutableStateFlow<UiItemsState>(UiItemsState.Empty)
    val uiContentState = MutableStateFlow<UiContentState>(UiContentState.Idle())
    val uiBottomMenu = MutableStateFlow<AllContentBottomMenu>(AllContentBottomMenu())
    val uiSnackbarState = MutableStateFlow<UiSnackbarState>(UiSnackbarState.Hidden)

    val commands = MutableSharedFlow<Command>()

    /**
     * Search query
     */
    private val userInput = MutableStateFlow(DEFAULT_QUERY)

    @OptIn(FlowPreview::class)
    private val searchQuery = userInput
        .take(1)
        .onCompletion {
            emitAll(userInput.drop(1).debounce(DEFAULT_DEBOUNCE_DURATION).distinctUntilChanged())
        }

    /**
     * Paging and subscription limit. If true, we can paginate after reaching bottom items.
     * Could be true only after the first subscription results (if results size == limit)
     */
    val canPaginate = MutableStateFlow(false)
    private var itemsLimit = DEFAULT_SEARCH_LIMIT
    private val restartSubscription = MutableStateFlow(0L)

    private var shouldScrollToTopItems = false

    private val permission = MutableStateFlow(userPermissionProvider.get(vmParams.spaceId))

    init {
        Timber.d("AllContentViewModel init, spaceId:[${vmParams.spaceId.id}]")
        setupInitialStateParams()
        setupSearchStateFlow()
        setupMenuFlow()
        proceedWithObservingPermissions()
    }

    private fun proceedWithObservingPermissions() {
        viewModelScope.launch {
            userPermissionProvider
                .observe(space = vmParams.spaceId)
                .collect {
                    uiBottomMenu.value =
                        AllContentBottomMenu(isOwnerOrEditor = it?.isOwnerOrEditor() == true)
                    permission.value = it
                }
        }
    }

    private fun setupInitialStateParams() {
        viewModelScope.launch {
            if (vmParams.useHistory) {
                runCatching {
                    val initialParams = restoreAllContentState.run(
                        RestoreAllContentState.Params(vmParams.spaceId)
                    )
                    when (initialParams) {
                        RestoreAllContentState.Response.Empty -> {
                            //do nothing
                        }
                        is RestoreAllContentState.Response.Success -> {
                            sortState.value = initialParams.mapToSort()
                            restartSubscription.value++
                        }
                    }
                }.onFailure { e ->
                    Timber.e(e, "Error restoring state")
                }
            }
        }
    }

    private fun setupSearchStateFlow() {
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                Timber.d("New query: [$query]")
                if (query.isBlank()) {
                    searchResultIds.value = emptyList()
                    restartSubscription.value++
                } else {
                    val activeTab = uiTabsState.value
                    resetLimit()
                    val searchParams = createSearchParams(
                        activeTab = activeTab.selectedTab,
                        activeQuery = query
                    )
                    searchObjects(searchParams).process(
                        success = { searchResults ->
                            Timber.d("Search objects by query:[$query], size: : ${searchResults.size}")
                            if (searchResults.isEmpty()) {
                                uiItemsState.value = UiItemsState.Empty
                                uiContentState.value = UiContentState.Empty
                            } else {
                                searchResultIds.value = searchResults.map { it.id }
                                restartSubscription.value++
                            }
                        },
                        failure = {
                            Timber.e(it, "Error searching objects by query")
                        }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupUiStateFlow() {
        viewModelScope.launch {
            restartSubscription.flatMapLatest {
                loadData()
            }.collectLatest { items ->
                if (items.isEmpty()) {
                    uiItemsState.value = UiItemsState.Empty
                    uiContentState.value = UiContentState.Empty
                } else {
                    uiItemsState.value = UiItemsState.Content(items)
                }
            }
        }
    }

    private fun loadData(): Flow<List<UiContentItem>> {

        val activeTab = uiTabsState.value.selectedTab
        val activeSort = sortState.value

        val searchParams = createSubscriptionParams(
            activeTab = activeTab,
            activeSort = activeSort,
            limitedObjectIds = searchResultIds.value,
            limit = itemsLimit,
            subscriptionId = subscriptionId(),
            spaceId = vmParams.spaceId.id,
            activeMode = uiTitleState.value
        )

        return storelessSubscriptionContainer.subscribe(searchParams)
            .onStart {
                uiContentState.value = if (itemsLimit == DEFAULT_SEARCH_LIMIT) {
                    UiContentState.InitLoading
                } else {
                    UiContentState.Paging
                }
                Timber.d("Restart subscription: with params: $searchParams")
            }
            .map { objWrappers ->
                handleData(objWrappers, activeSort, activeTab)
            }.catch { e ->
                handleError(e)
            }
    }

    private suspend fun handleData(
        objWrappers: List<ObjectWrapper.Basic>,
        activeSort: AllContentSort,
        activeTab: AllContentTab
    ): List<UiContentItem> {

        canPaginate.value = objWrappers.size == itemsLimit

        val items = mapToUiContentItems(
            objectWrappers = objWrappers,
            activeSort = activeSort,
            activeTab = activeTab
        )

        uiContentState.value = if (items.isEmpty()) {
            UiContentState.Empty
        } else {
            UiContentState.Idle(scrollToTop = shouldScrollToTopItems).also {
                shouldScrollToTopItems = false
            }
        }

        return items
    }

    private fun handleError(e: Throwable) {
        uiContentState.value = UiContentState.Error(
            message = e.message ?: "An error occurred while loading data."
        )
    }

    private suspend fun mapToUiContentItems(
        objectWrappers: List<ObjectWrapper.Basic>,
        activeSort: AllContentSort,
        activeTab: AllContentTab
    ): List<UiContentItem> {
        val isOwnerOrEditor = permission.value?.isOwnerOrEditor() == true
        return when (activeTab) {
            AllContentTab.TYPES -> {
                val items = objectWrappers.toUiContentTypes(
                    urlBuilder = urlBuilder,
                    isOwnerOrEditor = isOwnerOrEditor
                )
                buildList {
                    if (isOwnerOrEditor) add(UiContentItem.NewType)
                    addAll(items)
                }
            }

            AllContentTab.RELATIONS -> {
                val items = objectWrappers.toUiContentRelations(isOwnerOrEditor = isOwnerOrEditor)
                buildList {
                    if (isOwnerOrEditor) add(UiContentItem.NewRelation)
                    addAll(items)
                }
            }

            else -> {
                val items = objectWrappers.toUiContentItems(
                    space = vmParams.spaceId,
                    urlBuilder = urlBuilder,
                    objectTypes = storeOfObjectTypes.getAll(),
                    isOwnerOrEditor = isOwnerOrEditor
                )
                val result = when (activeSort) {
                    is AllContentSort.ByDateCreated -> {
                        groupItemsByDate(items = items, isSortByDateCreated = true, activeSort = activeSort)
                    }

                    is AllContentSort.ByDateUpdated -> {
                        groupItemsByDate(items = items, isSortByDateCreated = false, activeSort = activeSort)
                    }

                    is AllContentSort.ByName -> {
                        items
                    }

                    is AllContentSort.ByDateUsed -> {
                        items
                    }
                }
                if (uiTitleState.value == UiTitleState.OnlyUnlinked) {
                    buildList {
                        add(UiContentItem.UnlinkedDescription)
                        addAll(result)
                    }
                } else {
                    result
                }
            }
        }
    }

    private fun groupItemsByDate(
        items: List<UiContentItem.Item>,
        isSortByDateCreated: Boolean,
        activeSort: AllContentSort
    ): List<UiContentItem> {

        val groupedItems = mutableListOf<UiContentItem>()
        var currentGroupKey: String? = null

        val sortedItems = if (isSortByDateCreated) {
            if (activeSort.sortType == DVSortType.ASC) {
                items.sortedBy { it.createdDate }
            } else {
                items.sortedByDescending { it.createdDate }
            }
        } else {
            if (activeSort.sortType == DVSortType.ASC) {
                items.sortedBy { it.lastModifiedDate }
            } else {
                items.sortedByDescending { it.lastModifiedDate }
            }
        }

        for (item in sortedItems) {
            val timestamp = if (isSortByDateCreated) {
                item.createdDate
            } else {
                item.lastModifiedDate
            }
            val (groupKey, group) = getDateGroup(timestamp)

            if (currentGroupKey != groupKey) {
                groupedItems.add(group)
                currentGroupKey = groupKey
            }

            groupedItems.add(item)
        }

        return groupedItems
    }

    private fun getDateGroup(timestamp: Long): Pair<String, UiContentItem.Group> {
        val zoneId = ZoneId.systemDefault()
        val itemDate = Instant.ofEpochSecond(timestamp)
            .atZone(zoneId)
            .toLocalDate()
        val today = LocalDate.now(zoneId)
        val daysAgo = ChronoUnit.DAYS.between(itemDate, today)
        val todayGroup = UiContentItem.Group.Today()
        val yesterdayGroup = UiContentItem.Group.Yesterday()
        val previous7DaysGroup = UiContentItem.Group.Previous7Days()
        val previous14DaysGroup = UiContentItem.Group.Previous14Days()
        return when {
            daysAgo == 0L -> todayGroup.id to todayGroup
            daysAgo == 1L -> yesterdayGroup.id to yesterdayGroup
            daysAgo in 2..7 -> previous7DaysGroup.id to previous7DaysGroup
            daysAgo in 8..14 -> previous14DaysGroup.id to previous14DaysGroup
            itemDate.year == today.year -> {
                val monthName =
                    itemDate.month.getDisplayName(TextStyle.FULL, localeProvider.locale())
                monthName to UiContentItem.Group.Month(id = monthName, title = monthName)
            }

            else -> {
                val monthAndYear = "${
                    itemDate.month.getDisplayName(
                        TextStyle.FULL,
                        localeProvider.locale()
                    )
                } ${itemDate.year}"
                monthAndYear to UiContentItem.Group.MonthAndYear(
                    id = monthAndYear,
                    title = monthAndYear
                )
            }
        }
    }

    private fun createSearchParams(
        activeTab: AllContentTab,
        activeQuery: String,
    ): SearchObjects.Params {
        val filters = activeTab.filtersForSearch(
            spaces = listOf(vmParams.spaceId.id)
        )
        return SearchObjects.Params(
            space = vmParams.spaceId,
            filters = filters,
            keys = listOf(Relations.ID),
            fulltext = activeQuery
        )
    }

    private fun setupMenuFlow() {
        viewModelScope.launch {
            combine(
                uiTitleState,
                sortState,
                uiTabsState
            ) { mode, sort, tabs ->
                Triple(mode, sort, tabs)
            }.collectLatest { (mode, sort, tabs) ->
                val uiMode = tabs.selectedTab.menu(mode)
                val container = MenuSortsItem.Container(sort = sort)
                val uiSorts = tabs.selectedTab.sorts(activeSort = sort)
                val uiSortTypes = listOf(
                    MenuSortsItem.SortType(
                        sort = sort,
                        sortType = DVSortType.ASC,
                        isSelected = sort.sortType == DVSortType.ASC
                    ),
                    MenuSortsItem.SortType(
                        sort = sort,
                        sortType = DVSortType.DESC,
                        isSelected = sort.sortType == DVSortType.DESC
                    )
                )
                uiMenuState.value = UiMenuState.Visible(
                    mode = uiMode,
                    container = container,
                    sorts = uiSorts,
                    types = uiSortTypes,
                    showBin = true
                )
            }
        }
    }

    fun AllContentTab.sorts(activeSort: AllContentSort): List<MenuSortsItem.Sort> {
        return when (this) {
            AllContentTab.TYPES -> {
                listOf(
                    MenuSortsItem.Sort(
                        sort = AllContentSort.ByName(isSelected = activeSort is AllContentSort.ByName)
                    ),
                    MenuSortsItem.Sort(
                        sort = AllContentSort.ByDateUsed(isSelected = activeSort is AllContentSort.ByDateUsed)
                    )
                )
            }
            AllContentTab.RELATIONS -> {
                listOf(
                    MenuSortsItem.Sort(
                        sort = AllContentSort.ByName(isSelected = activeSort is AllContentSort.ByName)
                    )
                )
            }
            else -> {
                listOf(
                    MenuSortsItem.Sort(
                        sort = AllContentSort.ByDateUpdated(isSelected = activeSort is AllContentSort.ByDateUpdated)
                    ),
                    MenuSortsItem.Sort(
                        sort = AllContentSort.ByDateCreated(isSelected = activeSort is AllContentSort.ByDateCreated)
                    ),
                    MenuSortsItem.Sort(
                        sort = AllContentSort.ByName(isSelected = activeSort is AllContentSort.ByName)
                    )
                )
            }
        }
    }

    fun AllContentTab.menu(uiTitleState: UiTitleState): List<AllContentMenuMode> {
        return when (this) {
            AllContentTab.TYPES, AllContentTab.RELATIONS -> listOf()
            else -> {
                listOf(
                    AllContentMenuMode.AllContent(isSelected = uiTitleState == UiTitleState.AllContent),
                    AllContentMenuMode.Unlinked(isSelected = uiTitleState == UiTitleState.OnlyUnlinked)
                )
            }
        }
    }

    private fun AllContentTab.updateInitialState() {
        return when (this) {
            AllContentTab.TYPES -> {
                sortState.value = AllContentSort.ByName()
                userInput.value = DEFAULT_QUERY
                uiTitleState.value = UiTitleState.AllContent
            }
            AllContentTab.RELATIONS -> {
                sortState.value = AllContentSort.ByName()
                userInput.value = DEFAULT_QUERY
                uiTitleState.value = UiTitleState.AllContent
            }
            else -> {}
        }
    }

    fun onTabClicked(tab: AllContentTab) {
        Timber.d("onTabClicked: $tab")
        tab.updateInitialState()
        shouldScrollToTopItems = true
        resetLimit()
        uiItemsState.value = UiItemsState.Empty
        uiTabsState.value = uiTabsState.value.copy(selectedTab = tab)
        restartSubscription.value++
        viewModelScope.launch {
            sendAnalyticsAllContentChangeType(
                analytics = analytics,
                type = tab.toAnalyticsTabType()
            )
        }
    }

    fun onAllContentModeClicked(mode: AllContentMenuMode) {
        Timber.d("onAllContentModeClicked: $mode")
        shouldScrollToTopItems = true
        uiItemsState.value = UiItemsState.Empty
        uiTitleState.value = when (mode) {
            is AllContentMenuMode.AllContent -> UiTitleState.AllContent
            is AllContentMenuMode.Unlinked -> UiTitleState.OnlyUnlinked
        }
        restartSubscription.value++
        viewModelScope.launch {
            sendAnalyticsAllContentChangeMode(
                analytics = analytics,
                type = mode.toAnalyticsModeType()
            )
        }
    }

    fun onSortClicked(sort: AllContentSort) {
        Timber.d("onSortClicked: $sort")
        val newSort = when (sort) {
            is AllContentSort.ByDateCreated -> {
                sort.copy(isSelected = true)
            }
            is AllContentSort.ByDateUpdated -> {
                sort.copy(isSelected = true)
            }
            is AllContentSort.ByName -> {
                sort.copy(isSelected = true)
            }
            is AllContentSort.ByDateUsed -> {
                sort.copy(isSelected = true)
            }
        }
        shouldScrollToTopItems = true
        uiItemsState.value = UiItemsState.Empty
        sortState.value = newSort
        proceedWithSortSaving(uiTabsState.value, newSort)
        restartSubscription.value++
        viewModelScope.launch {
            sendAnalyticsAllContentChangeSort(
                analytics = analytics,
                type = sort.toAnalyticsSortType().first,
                sort = sort.toAnalyticsSortType().second
            )
        }
    }

    private fun proceedWithSortSaving(activeTab: UiTabsState, sort: AllContentSort) {
        if (activeTab.selectedTab == AllContentTab.TYPES
            || activeTab.selectedTab == AllContentTab.RELATIONS
        ) {
            return
        }
        viewModelScope.launch {
            val params = UpdateAllContentState.Params(
                spaceId = vmParams.spaceId,
                sort = sort.relationKey.key,
                isAsc = sort.sortType == DVSortType.ASC
            )
            updateAllContentState.async(params).fold(
                onSuccess = {
                    Timber.d("Sort updated")
                },
                onFailure = {
                    Timber.e(it, "Error updating sort")
                }
            )
        }
    }

    fun onFilterChanged(filter: String) {
        Timber.d("onFilterChanged: $filter")
        val currentFilter = userInput.value.isEmpty()
        viewModelScope.launch {
            if (currentFilter && filter.isNotEmpty()) {
                sendAnalyticsAllContentSearchInput(
                    analytics = analytics,
                    route = EventsDictionary.Routes.allContentRoute
                )
            }
        }
        userInput.value = filter
    }

    fun onViewBinClicked() {
        viewModelScope.launch {
            sendAnalyticsAllContentToBin(analytics = analytics)
        }
        viewModelScope.launch {
            commands.emit(Command.NavigateToBin(vmParams.spaceId.id))
        }
    }

    fun onItemClicked(item: UiContentItem.Item) {
        Timber.d("onItemClicked: ${item.id}")
        val layout = item.layout ?: return
        proceedWithNavigation(
            navigation = layout.navigation(
                target = item.id,
                space = vmParams.spaceId.id
            )
        )
        viewModelScope.launch {
            sendAnalyticsAllContentResult(analytics = analytics)
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
                OpenObjectNavigation.NonValidObject -> {
                    Timber.e("Object id is missing")
                }
            }
        }
    }

    fun onHomeClicked() {
        Timber.d("onHomeClicked")
        viewModelScope.launch {
            commands.emit(Command.ExitToVault)
        }
    }

    fun onGlobalSearchClicked() {
        Timber.d("onGlobalSearchClicked")
        viewModelScope.launch {
            commands.emit(Command.OpenGlobalSearch)
        }
    }

    fun onAddDockClicked() {
        Timber.d("onAddDockClicked")
        proceedWithCreateDoc()
    }

    fun onBackClicked() {
        Timber.d("onBackClicked")
        viewModelScope.launch {
            commands.emit(Command.Back)
        }
    }

    fun onCreateObjectOfTypeClicked(objType: ObjectWrapper.Type) {
        proceedWithCreateDoc(objType)
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
                        route = EventsDictionary.Routes.allContentRoute,
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

    fun onTypeClicked(item: UiContentItem) {
        Timber.d("onTypeClicked: $item")
        when (item) {
            UiContentItem.NewType -> {
                viewModelScope.launch {
                    commands.emit(Command.OpenTypeCreation)
                }
            }
            is UiContentItem.Type -> {
                viewModelScope.launch {
                    commands.emit(Command.OpenTypeEditing(item))
                }
            }
            else -> {
                //do nothing
            }
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = libraryScreenType
        )
    }

    fun onRelationClicked(item: UiContentItem) {
        Timber.d("onRelationClicked: $item")
        when (item) {
            UiContentItem.NewRelation -> {
                viewModelScope.launch {
                    commands.emit(
                        Command.OpenRelationCreation(
                            space = vmParams.spaceId.id
                        )
                    )
                }
            }
            is UiContentItem.Relation -> {
                viewModelScope.launch {
                    commands.emit(
                        Command.OpenRelationEditing(
                            typeName = item.name,
                            id = item.id,
                            iconUnicode = item.format.simpleIcon() ?: 0,
                            readOnly = item.readOnly
                        )
                    )
                }
            }
            else -> {
                //do nothing
            }
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = libraryScreenRelation
        )
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
        Timber.d("onStop")
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(listOf(subscriptionId()))
        }
        viewModelScope.launch {
            userInput.value = DEFAULT_QUERY
            searchResultIds.value = emptyList()
            uiItemsState.value = UiItemsState.Empty
            uiContentState.value = UiContentState.Empty
        }
    }

    fun proceedWithMoveToBin(item: UiContentItem.Item) {
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
                    commands.emit(Command.SendToast.Error("Error while archiving object"))
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
                    commands.emit(Command.SendToast.Error("Error while un-archiving object"))
                }
            )
        }
    }

    fun proceedWithDismissSnackbar() {
        uiSnackbarState.value = UiSnackbarState.Hidden
    }

    /**
     * Updates the limit for the number of items fetched and triggers data reload.
     */
    fun updateLimit() {
        Timber.d("Update limit, canPaginate: ${canPaginate.value} uiContentState: ${uiContentState.value}")
        if (canPaginate.value && uiContentState.value is UiContentState.Idle) {
            itemsLimit += DEFAULT_SEARCH_LIMIT
            restartSubscription.value++
        }
    }

    override fun onCleared() {
        super.onCleared()
        uiItemsState.value = UiItemsState.Empty
        resetLimit()
    }

    private fun resetLimit() {
        Timber.d("Reset limit")
        itemsLimit = DEFAULT_SEARCH_LIMIT
    }

    private fun subscriptionId() = "all_content_subscription_${vmParams.spaceId.id}"

    data class VmParams(
        val spaceId: SpaceId,
        val useHistory: Boolean = true
    )

    //region Types and Relations action
    fun updateObject(id: String, name: String, icon: String?) {
        viewModelScope.launch {
            setObjectDetails.execute(
                SetObjectDetails.Params(
                    ctx = id,
                    details = mapOf(
                        Relations.NAME to name,
                        Relations.ICON_EMOJI to icon.orNull(),
                    )
                )
            ).fold(
                onFailure = {
                    Timber.e(it, "Error while updating object details")
                },
                onSuccess = {
                    // do nothing
                }
            )
        }
    }

    fun uninstallObject(id: Id, type: LibraryItem, name: String) {
        viewModelScope.launch {
            removeObjectsFromWorkspace.execute(
                RemoveObjectsFromWorkspace.Params(listOf(id))
            ).fold(
                onFailure = {
                    Timber.e(it, "Error while uninstalling object")
                    commands.emit(Command.SendToast.Error("Error while uninstalling object"))
                },
                onSuccess = {
                    when (type) {
                        LibraryItem.TYPE -> {
                            commands.emit(Command.SendToast.TypeRemoved(name))
                        }
                        LibraryItem.RELATION -> {
                            commands.emit(Command.SendToast.RelationRemoved(name))
                        }
                    }
                }
            )
        }
    }
    //endregion

    sealed class Command {
        data class NavigateToEditor(val id: Id, val space: Id) : Command()
        data class NavigateToSetOrCollection(val id: Id, val space: Id) : Command()
        data class NavigateToBin(val space: Id) : Command()
        sealed class SendToast: Command() {
            data class Error(val message: String) : SendToast()
            data class RelationRemoved(val name: String) : SendToast()
            data class TypeRemoved(val name: String) : SendToast()
            data class UnexpectedLayout(val layout: String) : SendToast()
            data class ObjectArchived(val name: String) : SendToast()
        }
        data class OpenTypeEditing(val item: UiContentItem.Type) : Command()
        data object OpenTypeCreation: Command()
        data class OpenRelationEditing(
            val typeName: String,
            val id: Id,
            val iconUnicode: Int,
            val readOnly: Boolean
        ) : Command()
        data class OpenRelationCreation(val space: Id): Command()
        data object OpenGlobalSearch : Command()
        data object ExitToVault : Command()
        data object Back : Command()
    }

    companion object {
        const val DEFAULT_DEBOUNCE_DURATION = 300L

        //INITIAL STATE
        const val DEFAULT_SEARCH_LIMIT = 100
        val DEFAULT_INITIAL_MODE = UiTitleState.AllContent
        val DEFAULT_INITIAL_TAB = AllContentTab.PAGES
        val DEFAULT_QUERY = ""
    }
}
