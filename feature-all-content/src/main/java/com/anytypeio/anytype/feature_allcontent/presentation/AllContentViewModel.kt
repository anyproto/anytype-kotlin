package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.all_content.RestoreAllContentState
import com.anytypeio.anytype.domain.all_content.UpdateAllContentState
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentSort
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.AllContentTitleViewState
import com.anytypeio.anytype.feature_allcontent.models.MenuButtonViewState
import com.anytypeio.anytype.feature_allcontent.models.TabsViewState
import com.anytypeio.anytype.feature_allcontent.models.TopBarViewState
import com.anytypeio.anytype.feature_allcontent.models.filtersForSearch
import com.anytypeio.anytype.feature_allcontent.models.filtersForSubscribe
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.objects.toView
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Named
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
import kotlinx.coroutines.flow.stateIn
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
    private val searchObjects: SearchObjects
) : ViewModel() {

    private val susbcriptionId = "all_content_subscription_${vmParams.spaceId.id}"
    private val _limitedObjectIds: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    // Initial states
    private val _tabsState = MutableStateFlow<AllContentTab>(AllContentTab.PAGES)
    private val _modeState = MutableStateFlow<AllContentMode>(AllContentMode.AllContent)
    private val _sortState = MutableStateFlow<AllContentSort>(AllContentSort.ByName())
    private val _limitState = MutableStateFlow(DEFAULT_SEARCH_LIMIT)
    private val userInput = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val searchQuery = userInput
        .take(1)
        .onCompletion {
            emitAll(userInput.drop(1).debounce(DEFAULT_DEBOUNCE_DURATION).distinctUntilChanged())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AllContentUiState> = combine(
        _modeState,
        _tabsState,
        _sortState,
        _limitedObjectIds,
        _limitState
    ) { mode, tab, sort, limitedObjectIds, limit ->
        Result(mode, tab, sort, limitedObjectIds, limit)
    }.flatMapLatest { currentState ->
        Timber.d("AllContentNewState:$currentState, restart subscription")
        loadData(currentState)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(TIMEOUT),
            initialUiState()
        )

    init {
        Timber.d("AllContentViewModel init, spaceId:[${vmParams.spaceId.id}]")
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

    private fun initialUiState(): AllContentUiState.Initial {
        return AllContentUiState.Initial(
            tabsViewState = TabsViewState.Default(
                tabs = AllContentTab.entries,
                selectedTab = _tabsState.value
            ),
            topToolbarState = TopBarViewState.Default(
                titleState = AllContentTitleViewState.AllContent,
                menuButtonState = MenuButtonViewState.Visible
            )
        )
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
            limit = result.limit
        )

        val dataFlow = storelessSubscriptionContainer.subscribe(searchParams)

        emitAll(
            dataFlow
                .map { items ->
                    Timber.d("Objects by subscription,  size:[${items.size}]")
                    AllContentUiState.Content(
                        items = items.map {
                            it.toView(
                                urlBuilder = urlBuilder,
                                objectTypes = storeOfObjectTypes.getAll(),
                            )
                        }
                    )
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

    // Function to create subscription params
    private fun createSubscriptionParams(
        activeTab: AllContentTab,
        activeSort: AllContentSort,
        limitedObjectIds: List<String>,
        limit: Int
    ): StoreSearchParams {
        val (filters, sorts) = activeTab.filtersForSubscribe(
            spaces = listOf(vmParams.spaceId.id),
            activeSort = activeSort,
            limitedObjectIds = limitedObjectIds
        )
        return StoreSearchParams(
            filters = filters,
            sorts = sorts,
            keys = ObjectSearchConstants.defaultKeys,
            limit = limit,
            subscription = susbcriptionId
        )
    }

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
        val spaceId: SpaceId
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
        private const val DEFAULT_SEARCH_LIMIT = 50
        private const val TIMEOUT = 5000L
        val DEFAULT_KEYS = buildList {
            addAll(ObjectSearchConstants.defaultKeys)
            add(Relations.LINKS)
            add(Relations.BACKLINKS)
        }
    }
}
