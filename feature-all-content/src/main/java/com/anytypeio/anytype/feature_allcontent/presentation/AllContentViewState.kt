package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.compose.runtime.Stable
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.TabsViewState
import com.anytypeio.anytype.feature_allcontent.models.TopBarViewState

@Stable
sealed class AllContentUiState {
    data object Idle : AllContentUiState()

    data class Loading(
        val tab: AllContentTab,
        val menuMode: AllContentMenuMode,
        val topToolbarState: TopBarViewState
    ) : AllContentUiState()

    data class Error(
        val tab: AllContentTab,
        val menuMode: AllContentMenuMode,
        val message: String,
        val topToolbarState: TopBarViewState
    ) : AllContentUiState()

    data class Content(
        val tab: AllContentTab,
        val mode: AllContentMode,
        val menuMode: AllContentMenuMode,
        val items: List<ObjectWrapper>,
        val topToolbarState: TopBarViewState,
        val tabs: TabsViewState
    ) : AllContentUiState()
}