package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.compose.runtime.Immutable
import com.anytypeio.anytype.feature_allcontent.models.TabsViewState
import com.anytypeio.anytype.feature_allcontent.models.TopBarViewState
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView

sealed class AllContentUiState {

    data class Initial(
        val topToolbarState: TopBarViewState,
        val tabsViewState: TabsViewState.Default
    ) : AllContentUiState()

    data object Loading : AllContentUiState()

    data class Error(
        val message: String,
    ) : AllContentUiState()

    @Immutable
    data class Content(
        val items: List<DefaultObjectView>,
    ) : AllContentUiState()
}