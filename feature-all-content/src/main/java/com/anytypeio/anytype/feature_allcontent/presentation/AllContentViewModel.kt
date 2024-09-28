package com.anytypeio.anytype.feature_allcontent.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.all_content.RestoreAllContentState
import com.anytypeio.anytype.domain.all_content.UpdateAllContentState
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_allcontent.models.AllContentItem
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentSort
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.UiTitleState
import com.anytypeio.anytype.feature_allcontent.models.AllContentUiState
import com.anytypeio.anytype.feature_allcontent.models.MenuButtonViewState
import com.anytypeio.anytype.feature_allcontent.models.UiTabsState
import com.anytypeio.anytype.feature_allcontent.models.createSubscriptionParams
import com.anytypeio.anytype.feature_allcontent.models.filtersForSearch
import com.anytypeio.anytype.feature_allcontent.models.view
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.toView
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import javax.inject.Named
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
 * ViewState: @see [AllContentUiState]
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
    @Named("AllContent") private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val updateAllContentState: UpdateAllContentState,
    private val restoreAllContentState: RestoreAllContentState,
    private val searchObjects: SearchObjects,
    private val localeProvider: LocaleProvider
) : ViewModel() {

    private val susbcriptionId = "all_content_subscription_${vmParams.spaceId.id}"
    private val _limitedObjectIds: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    // Initial states
    private val _tabsState = MutableStateFlow<AllContentTab>(DEFAULT_INITIAL_TAB)
    private val _modeState = MutableStateFlow<AllContentMode>(DEFAULT_INITIAL_MODE)
    private val _sortState = MutableStateFlow<AllContentSort>(DEFAULT_INITIAL_SORT)
    private val _limitState = MutableStateFlow(DEFAULT_SEARCH_LIMIT)
    private val userInput = MutableStateFlow(DEFAULT_QUERY)

    @OptIn(FlowPreview::class)
    private val searchQuery = userInput
        .take(1)
        .onCompletion {
            emitAll(userInput.drop(1).debounce(DEFAULT_DEBOUNCE_DURATION).distinctUntilChanged())
        }

    private val _uiTitleState = MutableStateFlow<UiTitleState>(UiTitleState.Hidden)
    val uiTitleState: StateFlow<UiTitleState> = _uiTitleState.asStateFlow()

    private val _uiMenuButtonState =
        MutableStateFlow<MenuButtonViewState>(MenuButtonViewState.Hidden)
    val uiMenuButtonState: StateFlow<MenuButtonViewState> = _uiMenuButtonState.asStateFlow()

    private val _uiTabsState = MutableStateFlow<UiTabsState>(UiTabsState.Hidden)
    val uiTabsState: StateFlow<UiTabsState> = _uiTabsState.asStateFlow()

    private val _uiState = MutableStateFlow<AllContentUiState>(AllContentUiState.Hidden)
    val uiState: StateFlow<AllContentUiState> = _uiState.asStateFlow()

    init {
        Timber.d("AllContentViewModel init, spaceId:[${vmParams.spaceId.id}]")
        viewModelScope.launch {
            setupInitialScreenParams()
        }
        viewModelScope.launch {
            _modeState.collectLatest { result ->
                Timber.d("New mode: [$result]")
                val menuMode = getMenuMode(result)
                _uiTitleState.value = result.view()
                _uiMenuButtonState.value = MenuButtonViewState.Visible
            }
        }
        viewModelScope.launch {
            _tabsState.collectLatest { result ->
                Timber.d("New tab: [$result]")
                _uiTabsState.value = UiTabsState.Default(
                    tabs = AllContentTab.entries,
                    selectedTab = result
                )
            }
        }
        proceedWithUiStateSetup()
        viewModelScope.launch {
            searchQuery.collectLatest { query ->
                Timber.d("New query: [$query]")
                if (query.isBlank()) {
                    _limitedObjectIds.value = emptyList()
                } else {
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

    private suspend fun setupInitialScreenParams() {
        if (vmParams.useHistory) {
            runCatching {
                val initialParams = restoreAllContentState.run(
                    RestoreAllContentState.Params(vmParams.spaceId)
                )
                _sortState.value = mapRelationKeyToSort(initialParams.activeSort?.relationKey)
                _tabsState.value = DEFAULT_INITIAL_TAB
            }.onFailure { e ->
                Timber.e(e, "Error restoring state")
            }
        }
    }

    private fun mapRelationKeyToSort(relationKey: Key?): AllContentSort {
        return when (relationKey) {
            Relations.CREATED_DATE -> AllContentSort.ByDateCreated()
            Relations.LAST_OPENED_DATE -> AllContentSort.ByDateUpdated()
            else -> DEFAULT_INITIAL_SORT
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
                _limitState
            ) { mode, tab, sort, limitedObjectIds, limit ->
                Result(mode, tab, sort, limitedObjectIds, limit)
            }
                .flatMapLatest { currentState ->
                    Timber.d("AllContentNewState:$currentState, restart subscription")
                    loadData(currentState)
                }.collect {
                    _uiState.value = it
                }
        }
    }

    private fun loadData(
        result: Result
    ): Flow<AllContentUiState> = flow {

        emit(AllContentUiState.Loading)

        delay(300)

        val searchParams = createSubscriptionParams(
            activeTab = result.tab,
            activeSort = result.sort,
            limitedObjectIds = result.limitedObjectIds,
            limit = result.limit,
            subscriptionId = susbcriptionId,
            spaceId = vmParams.spaceId.id
        )

        val dataFlow = storelessSubscriptionContainer.subscribe(searchParams)

        emitAll(
            dataFlow
                .map { items ->
//                    items.forEach {
//                        Log.d("Test1983", "items, : ${it.getValue<Double?>(Relations.CREATED_DATE)
//                            ?.toLong()}")
//                    }
                    Timber.d("Objects by subscription,  size:[${items.size}]")
                    if (result.sort.canGroupByDate) {
                        val views = items.map {
                            it.toView(
                                urlBuilder = urlBuilder,
                                objectTypes = storeOfObjectTypes.getAll(),
                            )
                        }
                        val groupItems = groupItemsByDate(views)
                        AllContentUiState.Content(
                            items = emptyList()// groupItemsByDate(views)
                        )

                    } else {
                        AllContentUiState.Content(
                            items = items.map {
                                val view = it.toView(
                                    urlBuilder = urlBuilder,
                                    objectTypes = storeOfObjectTypes.getAll(),
                                )
                                AllContentItem.Object(
                                    id = view.id,
                                    obj = view
                                )
                            }
                        )
                    }
                }
                .catch { e ->
                    emit(
                        AllContentUiState.Error(
                            message = e.message ?: "Error loading objects by subscription"
                        )
                    )
                }
        )
    }

    private fun groupItemsByDate(items: List<DefaultObjectView>): List<AllContentItem> {
        val groupedItems = mutableListOf<AllContentItem>()
        var currentGroupKey: String? = null

        for (item in items) {
            val timestamp = item.lastOpenedDate
            val (groupKey, group) = getDateGroup(timestamp)

            if (currentGroupKey != groupKey) {
                groupedItems.add(group)
                currentGroupKey = groupKey
            }

            groupedItems.add(
                AllContentItem.Object(
                    id = item.id, obj = item
                )
            )
        }

        return groupedItems
    }

    private fun getDateGroup(timestamp: Long): Pair<String, AllContentItem.Group> {
        val itemDate = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now(ZoneId.systemDefault())
        val daysAgo = ChronoUnit.DAYS.between(itemDate, today)

        return when {
            daysAgo == 0L -> TODAY_ID to AllContentItem.Group.Today(TODAY_ID)
            daysAgo == 1L -> YESTERDAY_ID to AllContentItem.Group.Yesterday(YESTERDAY_ID)
            daysAgo in 2..7 -> PREVIOUS_7_DAYS_ID to AllContentItem.Group.Previous7Days(
                PREVIOUS_7_DAYS_ID
            )

            daysAgo in 8..14 -> PREVIOUS_14_DAYS_ID to AllContentItem.Group.Previous14Days(
                PREVIOUS_14_DAYS_ID
            )

            itemDate.year == today.year -> {
                val monthName =
                    itemDate.month.getDisplayName(TextStyle.FULL, localeProvider.locale())
                val id = "$MONTH_ID-$monthName"
                id to AllContentItem.Group.Month(id = id, title = monthName)
            }

            else -> {
                val monthAndYear = "${
                    itemDate.month.getDisplayName(
                        TextStyle.FULL,
                        localeProvider.locale()
                    )
                } ${itemDate.year}"
                val id = "$MONTH_AND_YEAR_ID-$monthAndYear"
                id to AllContentItem.Group.MonthAndYear(id = id, title = monthAndYear)
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

//    // Function to create subscription params
//    private fun createSubscriptionParams(
//        activeTab: AllContentTab,
//        activeSort: AllContentSort,
//        limitedObjectIds: List<String>,
//        limit: Int
//    ): StoreSearchParams {
//        val (filters, sorts) = activeTab.filtersForSubscribe(
//            spaces = listOf(vmParams.spaceId.id),
//            activeSort = activeSort,
//            limitedObjectIds = limitedObjectIds
//        )
//        return StoreSearchParams(
//            filters = filters,
//            sorts = sorts,
//            keys = DEFAULT_KEYS,
//            limit = limit,
//            subscription = susbcriptionId
//        )
//    }

    // Function to get the menu mode based on the active mode
    private fun getMenuMode(mode: AllContentMode): AllContentMenuMode {
        return when (mode) {
            AllContentMode.AllContent -> AllContentMenuMode.AllContent(isSelected = true)
            AllContentMode.Unlinked -> AllContentMenuMode.Unlinked(isSelected = true)
        }
    }

    fun onTabClicked(tab: AllContentTab) {
        Timber.d("onTabClicked: $tab")
        _tabsState.value = tab
    }

    fun onAllContentModeClicked(mode: AllContentMode) {
        Timber.d("onAllContentModeClicked: $mode")
        _modeState.value = mode
    }

    fun onSortClicked(sort: AllContentSort) {
        Timber.d("onSortClicked: $sort")
        _sortState.value = sort
    }

    fun onFilterChanged(filter: String) {
        Timber.d("onFilterChanged: $filter")
        userInput.value = filter
    }

    fun onLimitUpdated(limit: Int) {
        Timber.d("onLimitUpdated: $limit")
        _limitState.value = limit
    }

    fun onStop() {
        Timber.d("onStop")
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(listOf(susbcriptionId))
        }
    }

    data class VmParams(
        val spaceId: SpaceId,
        val useHistory: Boolean = true
    )

    internal data class Result(
        val mode: AllContentMode,
        val tab: AllContentTab,
        val sort: AllContentSort,
        val limitedObjectIds: List<String>,
        val limit: Int
    )

    companion object {
        const val DEFAULT_DEBOUNCE_DURATION = 300L
        private const val TIMEOUT = 5000L
        const val TODAY_ID = "Today"
        const val YESTERDAY_ID = "Yesterday"
        const val PREVIOUS_7_DAYS_ID = "Previous7Days"
        const val PREVIOUS_14_DAYS_ID = "Previous14Days"
        const val MONTH_ID = "Month"
        const val MONTH_AND_YEAR_ID = "MonthAndYear"

        //INITIAL STATE
        private const val DEFAULT_SEARCH_LIMIT = 50
        private val DEFAULT_INITIAL_TAB = AllContentTab.PAGES
        private val DEFAULT_INITIAL_SORT = AllContentSort.ByName()
        private val DEFAULT_INITIAL_MODE = AllContentMode.AllContent
        private val DEFAULT_QUERY = ""
    }
}
