package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.all_content.RestoreAllContentState
import com.anytypeio.anytype.domain.all_content.UpdateAllContentState
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.feature_allcontent.models.AllContentMode
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel.Companion.DEFAULT_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import javax.inject.Named
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
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

    val data: StateFlow<String> = MutableStateFlow("")

    private val userInput = MutableStateFlow("")
    private val searchQuery = userInput
        .take(1)
        .onCompletion {
            emitAll(userInput.drop(1).debounce(DEFAULT_DEBOUNCE_DURATION).distinctUntilChanged())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AllContentUiState> = data
        .flatMapLatest { result -> subscribe(result) }
        .catch {
            Timber.e(it, "Error parsing data")
            AllContentUiState.Error(it.message ?: "Error parsing data")
        }
        .map { items ->
            AllContentUiState.Content(items)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AllContentUiState.Loading
        )


    init {
        Timber.d("AllContentViewModel created with params: $vmParams")
    }

    fun onStart() {
        Timber.d("AllContentViewModel started")
    }

    private suspend fun subscribe(data: String): Flow<List<ObjectWrapper.Basic>> {
        delay(1000)
        val searchParams = StoreSearchParams(
            filters = ObjectSearchConstants.filterSearchObjects(
                spaces = listOf(vmParams.spaceId.id),
            ),
            sorts = emptyList(),
            keys = ObjectSearchConstants.defaultKeys,
            subscription = "all-content-subscription"
        )

        return storelessSubscriptionContainer.subscribe(searchParams)
    }

    data class VmParams(
        val spaceId: SpaceId
    )
}
