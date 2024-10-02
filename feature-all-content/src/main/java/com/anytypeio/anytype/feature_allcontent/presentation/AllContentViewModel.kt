package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.all_content.RestoreAllContentState
import com.anytypeio.anytype.domain.all_content.UpdateAllContentState
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentSort
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.MenuSortsItem
import com.anytypeio.anytype.feature_allcontent.models.UiContentItem
import com.anytypeio.anytype.feature_allcontent.models.UiContentState
import com.anytypeio.anytype.feature_allcontent.models.UiMenuState
import com.anytypeio.anytype.feature_allcontent.models.UiTabsState
import com.anytypeio.anytype.feature_allcontent.models.UiTitleState
import com.anytypeio.anytype.feature_allcontent.models.createSubscriptionParams
import com.anytypeio.anytype.feature_allcontent.models.filtersForSearch
import com.anytypeio.anytype.feature_allcontent.models.mapRelationKeyToSort
import com.anytypeio.anytype.feature_allcontent.models.toUiContentItems
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
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
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewState: @see [UiContentState]
 * Factory: @see [AllContentViewModelFactory]
 * Screen: @see [com.anytypeio.anytype.feature_allcontent.ui.AllContentWrapperScreen]
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
    private val localeProvider: LocaleProvider
) : ViewModel() {

    private val searchResultIds = MutableStateFlow<List<Id>>(emptyList())
    private val sortState = MutableStateFlow<AllContentSort>(DEFAULT_INITIAL_SORT)

    val uiTitleState = MutableStateFlow<UiTitleState>(UiTitleState.Hidden)
    val uiTabsState = MutableStateFlow<UiTabsState>(UiTabsState.Hidden)
    val uiMenuState = MutableStateFlow<UiMenuState>(UiMenuState.Hidden)
    val uiItemsState = MutableStateFlow<List<UiContentItem>>(emptyList())
    val uiContentState = MutableStateFlow<UiContentState>(UiContentState.Idle())

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
    private val limitUpdateTrigger = MutableStateFlow(0)

    private var shouldScrollToTopItems = false

    init {
        Timber.d("AllContentViewModel init, spaceId:[${vmParams.spaceId.id}]")
        setupInitialStateParams()
        setupUiStateFlow()
        setupSearchStateFlow()
        setupMenuFlow()
    }

    private fun setupInitialStateParams() {
        uiTitleState.value = UiTitleState.AllContent
        uiTabsState.value = UiTabsState.Default(
            tabs = AllContentTab.entries,
            selectedTab = DEFAULT_INITIAL_TAB
        )
        viewModelScope.launch {
            if (vmParams.useHistory) {
                runCatching {
                    val initialParams = restoreAllContentState.run(
                        RestoreAllContentState.Params(vmParams.spaceId)
                    )
                    if (!initialParams.activeSort.isNullOrEmpty()) {
                        sortState.value = initialParams.activeSort.mapRelationKeyToSort()
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
                } else {
                    val activeTab = uiTabsState.value
                    if (activeTab is UiTabsState.Default) {
                        resetLimit()
                        val searchParams = createSearchParams(
                            activeTab = activeTab.selectedTab,
                            activeQuery = query
                        )
                        searchObjects(searchParams).process(
                            success = { searchResults ->
                                Timber.d("Search objects by query:[$query], size: : ${searchResults.size}")
                                searchResultIds.value = searchResults.map { it.id }
                            },
                            failure = {
                                Timber.e(it, "Error searching objects by query")
                            }
                        )
                    } else {
                        Timber.w("Unexpected tabs state: $activeTab")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupUiStateFlow() {
        viewModelScope.launch {
            combine(
                uiTitleState,
                uiTabsState.filterIsInstance<UiTabsState.Default>(),
                sortState,
                searchResultIds,
                limitUpdateTrigger
            ) { mode, tab, sort, limitedObjectIds, _ ->
                Result(mode, tab, sort, limitedObjectIds)
            }
                .flatMapLatest { currentState ->
                    Timber.d("New params:$currentState, restart subscription")
                    loadData(currentState)
                }.collect {
                    uiItemsState.value = it
                }
        }
    }

    private fun subscriptionId() = "all_content_subscription_${vmParams.spaceId.id}"

    private fun loadData(
        result: Result
    ): Flow<List<UiContentItem>> = flow {

        if (itemsLimit == DEFAULT_SEARCH_LIMIT) {
            uiContentState.value = UiContentState.InitLoading
        } else {
            uiContentState.value = UiContentState.Paging
        }

        val searchParams = createSubscriptionParams(
            activeTab = result.tab.selectedTab,
            activeSort = result.sort,
            limitedObjectIds = result.limitedObjectIds,
            limit = itemsLimit,
            subscriptionId = subscriptionId(),
            spaceId = vmParams.spaceId.id,
            activeMode = result.mode
        )

        val dataFlow = storelessSubscriptionContainer.subscribe(searchParams)
        emitAll(
            dataFlow.map { objWrappers ->
                canPaginate.value = objWrappers.size == itemsLimit
                val items = mapToUiContentItems(
                    objectWrappers = objWrappers,
                    activeSort = result.sort
                )
                uiContentState.value = if (items.isEmpty()) {
                    UiContentState.Empty
                } else {
                    UiContentState.Idle(scrollToTop = shouldScrollToTopItems).also {
                        shouldScrollToTopItems = false
                    }
                }
                items
            }.catch { e ->
                uiContentState.value = UiContentState.Error(
                    message = e.message ?: "An error occurred while loading data."
                )
                emit(emptyList())
            }
        )
    }

    private suspend fun mapToUiContentItems(
        objectWrappers: List<ObjectWrapper.Basic>,
        activeSort: AllContentSort
    ): List<UiContentItem> {
        val items = objectWrappers.toUiContentItems(
            space = vmParams.spaceId,
            urlBuilder = urlBuilder,
            objectTypes = storeOfObjectTypes.getAll()
        )
        return when (activeSort) {
            is AllContentSort.ByDateCreated -> {
                groupItemsByDate(items = items, isSortByDateCreated = true)
            }
            is AllContentSort.ByDateUpdated -> {
                groupItemsByDate(items = items, isSortByDateCreated = false)
            }
            is AllContentSort.ByName -> {
                items
            }
        }
    }

    private fun groupItemsByDate(
        items: List<UiContentItem.Item>,
        isSortByDateCreated: Boolean
    ): List<UiContentItem> {
        val groupedItems = mutableListOf<UiContentItem>()
        var currentGroupKey: String? = null

        for (item in items) {
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
            filters = filters,
            keys = listOf(Relations.ID),
            fulltext = activeQuery
        )
    }

    private fun setupMenuFlow() {
        viewModelScope.launch {
            combine(
                uiTitleState,
                sortState
            ) { mode, sort ->
                mode to sort
            }.collectLatest { (mode, sort) ->
                val uiMode = listOf(
                    AllContentMenuMode.AllContent(isSelected = mode == UiTitleState.AllContent),
                    AllContentMenuMode.Unlinked(isSelected = mode == UiTitleState.OnlyUnlinked)
                )
                val container = MenuSortsItem.Container(sort = sort)
                val uiSorts = listOf(
                    MenuSortsItem.Sort(
                        sort = AllContentSort.ByName(isSelected = sort is AllContentSort.ByName)
                    ),
                    MenuSortsItem.Sort(
                        sort = AllContentSort.ByDateUpdated(isSelected = sort is AllContentSort.ByDateUpdated)
                    ),
                    MenuSortsItem.Sort(
                        sort = AllContentSort.ByDateCreated(isSelected = sort is AllContentSort.ByDateCreated)
                    )
                )
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
                    types = uiSortTypes
                )
            }
        }
    }

    fun onTabClicked(tab: AllContentTab) {
        Timber.d("onTabClicked: $tab")
        if (tab == AllContentTab.TYPES) {
            viewModelScope.launch {
                commands.emit(Command.SendToast("Not implemented yet"))
            }
            return
        }
        shouldScrollToTopItems = true
        resetLimit()
        uiItemsState.value = emptyList()
        uiTabsState.value = UiTabsState.Default(
            tabs = AllContentTab.entries,
            selectedTab = tab
        )
    }

    fun onAllContentModeClicked(mode: AllContentMenuMode) {
        Timber.d("onAllContentModeClicked: $mode")
        shouldScrollToTopItems = true
        uiItemsState.value = emptyList()
        uiTitleState.value = when (mode) {
            is AllContentMenuMode.AllContent -> UiTitleState.AllContent
            is AllContentMenuMode.Unlinked -> UiTitleState.OnlyUnlinked
        }
    }

    fun onSortClicked(sort: AllContentSort) {
        Timber.d("onSortClicked: $sort")
        shouldScrollToTopItems = true
        uiItemsState.value = emptyList()
        sortState.value = sort
        proceedWithSortSaving(sort)
    }

    private fun proceedWithSortSaving(sort: AllContentSort) {
        viewModelScope.launch {
            val params = UpdateAllContentState.Params(
                spaceId = vmParams.spaceId,
                sort = sort.relationKey.key
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
        userInput.value = filter
    }

    fun onViewBinClicked() {
        viewModelScope.launch {
            commands.emit(Command.NavigateToBin(vmParams.spaceId.id))
        }
    }

    fun onItemClicked(item: UiContentItem.Item) {
        Timber.d("onItemClicked: ${item.id}")
        val layout = item.layout ?: return
        viewModelScope.launch {
            when (val navigation = layout.navigation(
                target = item.id,
                space = vmParams.spaceId.id
            )) {
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
                    commands.emit(Command.SendToast("Unexpected layout: ${navigation.layout}"))
                }
            }
        }
    }

    fun onStop() {
        Timber.d("onStop")
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(listOf(subscriptionId()))
        }
    }

    /**
     * Updates the limit for the number of items fetched and triggers data reload.
     */
    fun updateLimit() {
        Timber.d("Update limit, canPaginate: ${canPaginate.value} uiContentState: ${uiContentState.value}")
        if (canPaginate.value && uiContentState.value is UiContentState.Idle) {
            itemsLimit += DEFAULT_SEARCH_LIMIT
            limitUpdateTrigger.value++
        }
    }

    override fun onCleared() {
        super.onCleared()
        uiItemsState.value = emptyList()
        resetLimit()
    }

    private fun resetLimit() {
        Timber.d("Reset limit")
        itemsLimit = DEFAULT_SEARCH_LIMIT
    }

    data class VmParams(
        val spaceId: SpaceId,
        val useHistory: Boolean = true
    )

    internal data class Result(
        val mode: UiTitleState,
        val tab: UiTabsState.Default,
        val sort: AllContentSort,
        val limitedObjectIds: List<String>
    )

    sealed class Command {
        data class NavigateToEditor(val id: Id, val space: Id) : Command()
        data class NavigateToSetOrCollection(val id: Id, val space: Id) : Command()
        data class NavigateToBin(val space: Id) : Command()
        data class SendToast(val message: String) : Command()
    }

    companion object {
        const val DEFAULT_DEBOUNCE_DURATION = 300L

        //INITIAL STATE
        const val DEFAULT_SEARCH_LIMIT = 100
        val DEFAULT_INITIAL_TAB = AllContentTab.PAGES
        val DEFAULT_INITIAL_SORT = AllContentSort.ByName()
        val DEFAULT_QUERY = ""
    }
}
