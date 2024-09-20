package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel.Companion.DEFAULT_DEBOUNCE_DURATION
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : ViewModel() {

    val data: StateFlow<String> = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AllContentUiState> = data
        .flatMapLatest { result -> parseData(data = result) }
        .catch {
            Timber.e(it, "Error parsing data")
            emit(AllContentUiState.Error(it.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AllContentUiState.Idle
        )

    private val userInput = MutableStateFlow("")
    private val searchQuery = userInput
        .take(1)
        .onCompletion {
            emitAll(userInput.drop(1).debounce(DEFAULT_DEBOUNCE_DURATION).distinctUntilChanged())
        }


    init {
        Timber.d("AllContentViewModel created with params: $vmParams")
    }

    fun onStart() {
        Timber.d("AllContentViewModel started")
    }

    private fun parseData(data: String): Flow<AllContentUiState> {
        return flowOf(AllContentUiState.Idle)
    }

    data class VmParams(
        val spaceId: SpaceId
    )
}
