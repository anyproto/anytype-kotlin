package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.presentation.sets.model.SortingView

sealed class ViewerSortByViewState {

    object Init : ViewerSortByViewState()
    data class Success(val items: List<SortingView>) : ViewerSortByViewState()
}