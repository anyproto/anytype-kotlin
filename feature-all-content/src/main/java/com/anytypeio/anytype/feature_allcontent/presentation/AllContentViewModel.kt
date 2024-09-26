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
import com.anytypeio.anytype.feature_allcontent.models.TabViewState
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
 * Screen: @see [com.anytypeio.anytype.feature_allcontent.ui.AllContentScreenWrapper]
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

    // Initial state
    private val _state = MutableStateFlow<AllContentState>(
        AllContentState.Default(
            activeTab = AllContentTab.OBJECTS,
            activeMode = AllContentMode.AllContent,
            activeSort = AllContentSort.ByName(),
            filter = "",
            limit = 50
        )
    )
    val state: StateFlow<AllContentState> = _state.asStateFlow()

    private val userInput = MutableStateFlow("")
    private val searchQuery = userInput
        .take(1)
        .onCompletion {
            emitAll(userInput.drop(1).debounce(DEFAULT_DEBOUNCE_DURATION).distinctUntilChanged())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AllContentUiState> = _state
        .filterIsInstance<AllContentState.Default>()
        .flatMapLatest { currentState ->
            loadData(currentState)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            initialUiState()
        )

    // Initial UI state when the ViewModel is created
    private fun initialUiState(): AllContentUiState {
        val currentState = _state.value as AllContentState.Default
        return AllContentUiState.Loading(
            tab = currentState.activeTab,
            menuMode = getMenuMode(currentState.activeMode),
            topToolbarState = getTopToolbarViewState()
        )
    }

    private fun loadData(currentState: AllContentState.Default): Flow<AllContentUiState> = flow {
        emit(
            AllContentUiState.Loading(
                tab = currentState.activeTab,
                menuMode = getMenuMode(currentState.activeMode),
                topToolbarState = getTopToolbarViewState()
            )
        )

        // Build search parameters based on the current state
        val searchParams = createSearchParams(currentState)

        // Fetch data from the repository, which returns a Flow
        val dataFlow = storelessSubscriptionContainer.subscribe(searchParams)

        // Map the data flow to UI states
        emitAll(
            dataFlow
                .map { items ->
                    Timber.d("Loaded data: ${items.size}")
                    AllContentUiState.Content(
                        tab = currentState.activeTab,
                        mode = currentState.activeMode,
                        menuMode = getMenuMode(currentState.activeMode),
                        items = items,
                        topToolbarState = getTopToolbarViewState(),
                        tabs = getTabsViewState()
                    )
                }
                .catch { e ->
                    emit(
                        AllContentUiState.Error(
                            tab = currentState.activeTab,
                            menuMode = getMenuMode(currentState.activeMode),
                            message = e.message ?: "Error loading data",
                            topToolbarState = getTopToolbarViewState()
                        )
                    )
                }
        )
    }

    private fun getTopToolbarViewState(): TopBarViewState {
        val currentState = _state.value as AllContentState.Default
        val mode = getMenuMode(currentState.activeMode)
        return TopBarViewState(
            titleState = when (mode) {
                is AllContentMenuMode.AllContent -> AllContentTitleViewState.AllContent
                is AllContentMenuMode.Unlinked -> AllContentTitleViewState.OnlyUnlinked
            },
            menuButtonState = MenuButtonViewState.Visible
        )
    }

    private fun getTabsViewState(): TabsViewState {
        val currentState = _state.value as AllContentState.Default
        return TabsViewState.Visible(
            tabs = AllContentTab.entries.map { tab ->
                TabViewState(
                    tab = tab,
                    isSelected = tab == currentState.activeTab
                )
            }
        )
    }

    // Function to create search parameters
    private fun createSearchParams(state: AllContentState.Default): StoreSearchParams {
        // Implement logic to create search parameters based on the state
        return StoreSearchParams(
            filters = ObjectSearchConstants.filterSearchObjects(
                spaces = listOf(vmParams.spaceId.id),
            ),
            sorts = emptyList(),
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

//    @OptIn(ExperimentalCoroutinesApi::class)
//    val uiState: StateFlow<AllContentUiState> =
//        screenState
//        .filterIsInstance<AllContentState.Default>()
//        .flatMapLatest { result -> subscribe(result) }
//        .catch {
//            Timber.e(it, "Error parsing data")
//            AllContentUiState.Error(message = it.message ?: "Error parsing data")
//        }
//        .map { items ->
//            AllContentUiState.Content(items)
//        }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5_000),
//            initialValue = AllContentUiState.Loading
//        )
//
//
    init {
        Timber.d("AllContentViewModel created with params: $vmParams")
    }

    fun onTabClicked(tab: AllContentTab) {
        val state = _state.value as? AllContentState.Default ?: return
        _state.value = state.copy(activeTab = tab)
    }

    fun onAllContentModeClicked(mode: AllContentMode) {
        val state = _state.value as? AllContentState.Default ?: return
        _state.value = state.copy(activeMode = mode)
    }

    fun onSortClicked(sort: AllContentSort) {
        val state = _state.value as? AllContentState.Default ?: return
        _state.value = state.copy(activeSort = sort)
    }

    fun onFilterChanged(filter: String) {
        val state = _state.value as? AllContentState.Default ?: return
        _state.value = state.copy(filter = filter)
    }

    fun onLimitUpdated(limit: Int) {
        val state = _state.value as? AllContentState.Default ?: return
        _state.value = state.copy(limit = limit)
    }

    data class VmParams(
        val spaceId: SpaceId
    )
}
