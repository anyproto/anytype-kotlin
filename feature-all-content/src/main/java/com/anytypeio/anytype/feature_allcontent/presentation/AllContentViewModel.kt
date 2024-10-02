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
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentSort
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.MenuButtonViewState
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
import com.anytypeio.anytype.feature_allcontent.models.view
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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
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
    private val storeOfRelations: StoreOfRelations,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val updateAllContentState: UpdateAllContentState,
    private val restoreAllContentState: RestoreAllContentState,
    private val searchObjects: SearchObjects,
    private val localeProvider: LocaleProvider
) : ViewModel() {

    private val _limitedObjectIds: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    private val _tabsState = MutableStateFlow<AllContentTab>(DEFAULT_INITIAL_TAB)
    private val _modeState = MutableStateFlow<AllContentMode>(DEFAULT_INITIAL_MODE)
    private val _sortState = MutableStateFlow<AllContentSort>(DEFAULT_INITIAL_SORT)

    private val _uiTitleState = MutableStateFlow<UiTitleState>(UiTitleState.Hidden)
    val uiTitleState: StateFlow<UiTitleState> = _uiTitleState.asStateFlow()

    private val _uiMenuButtonState =
        MutableStateFlow<MenuButtonViewState>(MenuButtonViewState.Hidden)
    val uiMenuButtonState: StateFlow<MenuButtonViewState> = _uiMenuButtonState.asStateFlow()

    private val _uiTabsState = MutableStateFlow<UiTabsState>(UiTabsState.Hidden)
    val uiTabsState: StateFlow<UiTabsState> = _uiTabsState.asStateFlow()

    private val _uiItemsState = MutableStateFlow<List<UiContentItem>>(emptyList())
    val uiItemsState: StateFlow<List<UiContentItem>> = _uiItemsState.asStateFlow()

    private val _uiMenu = MutableStateFlow(UiMenuState.empty())
    val uiMenu: StateFlow<UiMenuState> = _uiMenu.asStateFlow()

    private val _uiContentState = MutableStateFlow<UiContentState>(UiContentState.Idle())
    val uiContentState: StateFlow<UiContentState> = _uiContentState.asStateFlow()

    private val _commands = MutableSharedFlow<Command>()
    val commands: SharedFlow<Command> = _commands

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
     * Could be true only after the first subscription results
     */
    val canPaginate = MutableStateFlow(false)
    private var subscriptionLimit = DEFAULT_SEARCH_LIMIT
    private val limitUpdateTrigger = MutableStateFlow(0)

    private var shouldScrollToTop = false

    init {
        Timber.d("AllContentViewModel init, spaceId:[${vmParams.spaceId.id}]")
        setupInitialStateParams()
        proceedWithUiTitleStateSetup()
        proceedWithUiTabsStateSetup()
        proceedWithUiStateSetup()
        proceedWithSearchStateSetup()
        proceedWithMenuSetup()
    }

    private fun proceedWithUiTitleStateSetup() {
        viewModelScope.launch {
            _modeState.collectLatest { result ->
                Timber.d("New mode: [$result]")
                _uiTitleState.value = result.view()
                _uiMenuButtonState.value = MenuButtonViewState.Visible
            }
        }
    }

    private fun proceedWithUiTabsStateSetup() {
        viewModelScope.launch {
            _tabsState.collectLatest { result ->
                Timber.d("New tab: [$result]")
                _uiTabsState.value = UiTabsState.Default(
                    tabs = AllContentTab.entries,
                    selectedTab = result
                )
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
                    if (!initialParams.activeSort.isNullOrEmpty()) {
                        _sortState.value = initialParams.activeSort.mapRelationKeyToSort()
                    }
                }.onFailure { e ->
                    Timber.e(e, "Error restoring state")
                }
            }
        }
    }

    private fun proceedWithSearchStateSetup() {
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                Timber.d("New query: [$query]")
                if (query.isBlank()) {
                    _limitedObjectIds.value = emptyList()
                } else {
                    resetLimit()
                    val searchParams = createSearchParams(
                        activeTab = _tabsState.value,
                        activeQuery = query
                    )
                    searchObjects(searchParams).process(
                        success = { searchResults ->
                            Timber.d("Search objects by query:[$query], size: : ${searchResults.size}")
                            _limitedObjectIds.value = searchResults.map { it.id }
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
    private fun proceedWithUiStateSetup() {
        viewModelScope.launch {
            combine(
                _modeState,
                _tabsState,
                _sortState,
                _limitedObjectIds,
                limitUpdateTrigger
            ) { mode, tab, sort, limitedObjectIds, _ ->
                Result(mode, tab, sort, limitedObjectIds)
            }
                .flatMapLatest { currentState ->
                    Timber.d("New params:$currentState, restart subscription")
                    loadData(currentState)
                }.collect {
                    this@AllContentViewModel._uiItemsState.value = it
                }
        }
    }

    fun subscriptionId() = "all_content_subscription_${vmParams.spaceId.id}"

    private fun loadData(
        result: Result
    ): Flow<List<UiContentItem>> = flow {

        if (subscriptionLimit == DEFAULT_SEARCH_LIMIT) {
            _uiContentState.value = UiContentState.InitLoading
        } else {
            _uiContentState.value = UiContentState.Paging
        }

        val searchParams = createSubscriptionParams(
            activeTab = result.tab,
            activeSort = result.sort,
            limitedObjectIds = result.limitedObjectIds,
            limit = subscriptionLimit,
            subscriptionId = subscriptionId(),
            spaceId = vmParams.spaceId.id,
            activeMode = result.mode
        )

        val dataFlow = storelessSubscriptionContainer.subscribe(searchParams)
        emitAll(
            dataFlow.map { objWrappers ->
                canPaginate.value = objWrappers.size == subscriptionLimit
                val items = mapToUiContentItems(
                    objectWrappers = objWrappers,
                    activeSort = result.sort
                )
                _uiContentState.value = if (items.isEmpty()) {
                    UiContentState.Empty
                } else {
                    UiContentState.Idle(scrollToTop = shouldScrollToTop).also {
                        shouldScrollToTop = false
                    }
                }
                items
            }.catch { e ->
                _uiContentState.value = UiContentState.Error(
                    message = e.message ?: "Error loading objects by subscription"
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
        return if (activeSort.canGroupByDate) {
            groupItemsByDate(
                items = items,
                activeSort = activeSort
            )
        } else {
            items
        }
    }

    private fun groupItemsByDate(
        items: List<UiContentItem.Item>,
        activeSort: AllContentSort
    ): List<UiContentItem> {
        val groupedItems = mutableListOf<UiContentItem>()
        var currentGroupKey: String? = null

        for (item in items) {
            val timestamp = when (activeSort) {
                is AllContentSort.ByDateCreated -> item.createdDate
                is AllContentSort.ByDateUpdated -> item.lastModifiedDate
                is AllContentSort.ByName -> 0L
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

    // Function to create search parameters
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

    private fun proceedWithMenuSetup() {
        viewModelScope.launch {
            combine(
                _modeState,
                _sortState
            ) { mode, sort ->
                mode to sort
            }.collectLatest { (mode, sort) ->
                val uiMode = listOf(
                    AllContentMenuMode.AllContent(isSelected = mode == AllContentMode.AllContent),
                    AllContentMenuMode.Unlinked(isSelected = mode == AllContentMode.Unlinked)
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
                _uiMenu.value = UiMenuState(
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
                _commands.emit(Command.SendToast("Not implemented yet"))
            }
            return
        }
        shouldScrollToTop = true
        resetLimit()
        _uiItemsState.value = emptyList()
        _tabsState.value = tab
    }

    fun onAllContentModeClicked(mode: AllContentMenuMode) {
        Timber.d("onAllContentModeClicked: $mode")
        shouldScrollToTop = true
        _uiItemsState.value = emptyList()
        _modeState.value = when (mode) {
            is AllContentMenuMode.AllContent -> AllContentMode.AllContent
            is AllContentMenuMode.Unlinked -> AllContentMode.Unlinked
        }
    }

    fun onSortClicked(sort: AllContentSort) {
        Timber.d("onSortClicked: $sort")
        shouldScrollToTop = true
        _uiItemsState.value = emptyList()
        _sortState.value = sort
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
            _commands.emit(Command.NavigateToBin(vmParams.spaceId.id))
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
                    _commands.emit(
                        Command.NavigateToSetOrCollection(
                            id = navigation.target,
                            space = navigation.space
                        )
                    )
                }

                is OpenObjectNavigation.OpenEditor -> {
                    _commands.emit(
                        Command.NavigateToEditor(
                            id = navigation.target,
                            space = navigation.space
                        )
                    )
                }

                is OpenObjectNavigation.UnexpectedLayoutError -> {
                    _commands.emit(Command.SendToast("Unexpected layout: ${navigation.layout}"))
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

    fun updateLimit() {
        Timber.d("Update limit, canPaginate: ${canPaginate.value} uiContentState: ${_uiContentState.value}")
        if (canPaginate.value && _uiContentState.value is UiContentState.Idle) {
            subscriptionLimit += DEFAULT_SEARCH_LIMIT
            limitUpdateTrigger.value++
        }
    }

    override fun onCleared() {
        super.onCleared()
        _uiItemsState.value = emptyList()
        resetLimit()
    }

    private fun resetLimit() {
        Timber.d("Reset limit")
        subscriptionLimit = DEFAULT_SEARCH_LIMIT
    }

    data class VmParams(
        val spaceId: SpaceId,
        val useHistory: Boolean = true
    )

    internal data class Result(
        val mode: AllContentMode,
        val tab: AllContentTab,
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
        val DEFAULT_INITIAL_MODE = AllContentMode.AllContent
        val DEFAULT_QUERY = ""
    }
}
