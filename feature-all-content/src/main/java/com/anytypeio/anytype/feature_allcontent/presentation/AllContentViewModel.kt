package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.all_content.RestoreAllContentState
import com.anytypeio.anytype.domain.all_content.UpdateAllContentState
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentSort
import com.anytypeio.anytype.feature_allcontent.models.AllContentState
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.AllContentTitleViewState
import com.anytypeio.anytype.feature_allcontent.models.MenuButtonViewState
import com.anytypeio.anytype.feature_allcontent.models.TabsViewState
import com.anytypeio.anytype.feature_allcontent.models.TopBarViewState
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel.Companion.DEFAULT_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Named
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
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
    private val restoreAllContentState: RestoreAllContentState
) : ViewModel() {

    // Initial states
    private val _tabsState = MutableStateFlow<AllContentTab>(AllContentTab.OBJECTS)
    private val _modeState = MutableStateFlow<AllContentMode>(AllContentMode.AllContent)
    private val _sortState = MutableStateFlow<AllContentSort>(AllContentSort.ByName())
    private val _limitState = MutableStateFlow(0)
    private val userInput = MutableStateFlow("")
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
        searchQuery,
        _limitState
    ) { mode, tab, sort, query, limit ->
        Result(mode, tab, sort, query, limit)
    }.flatMapLatest { currentState ->
        Timber.d("AllContentNewState:$currentState")
        loadData(currentState)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            initialUiState()
        )

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

        val searchParams = createSearchParams()

        val dataFlow = storelessSubscriptionContainer.subscribe(searchParams)

        emitAll(
            dataFlow
                .map { items ->
                    Timber.d("Loaded data: ${items.size}")
                    AllContentUiState.Content(
                        mode = result.mode,
                        menuMode = getMenuMode(result.mode),
                        items = items
                    )
                }
                .catch { e ->
                    emit(
                        AllContentUiState.Error(
                            menuMode = getMenuMode(result.mode),
                            message = e.message ?: "Error loading data"
                        )
                    )
                }
        )
    }

    // Function to create search parameters
    private fun createSearchParams(
        activeTab: AllContentTab,
        activeSort: AllContentSort
    ): StoreSearchParams {
        val filters = activeTab.filtersForSubscribe(spaces = listOf(vmParams.spaceId.id))
        return StoreSearchParams(
            filters = filters,
            sorts = listOf(activeSort.toDVSort()),
            keys = ObjectSearchConstants.defaultKeys,
            subscription = "all-content-subscription"
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
        _tabsState.value = tab
    }

    fun onAllContentModeClicked(mode: AllContentMode) {
        _modeState.value = mode
    }

    fun onSortClicked(sort: AllContentSort) {
        _sortState.value = sort
    }

    fun onFilterChanged(filter: String) {
        userInput.value = filter
    }

    fun onLimitUpdated(limit: Int) {
        _limitState.value = limit
    }

    data class VmParams(
        val spaceId: SpaceId
    )

    internal data class Result(
        val mode: AllContentMode,
        val tab: AllContentTab,
        val sort: AllContentSort,
        val query: String,
        val limit: Int
    )
}
