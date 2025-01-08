package com.anytypeio.anytype.core_ui.lists.objects

import com.anytypeio.anytype.presentation.objects.UiObjectsListItem

sealed class UiContentState {
    data class Idle(val scrollToTop: Boolean = false) : UiContentState()
    data object InitLoading : UiContentState()
    data object Paging : UiContentState()
    data object Empty : UiContentState()
}

data class UiObjectsListState(
    val items: List<UiObjectsListItem>
) {
    companion object {

        val Empty = UiObjectsListState(items = emptyList())
        val LoadingState = UiObjectsListState(
            items = listOf(
                UiObjectsListItem.Loading("Loading-Item-1"),
                UiObjectsListItem.Loading("Loading-Item-2"),
                UiObjectsListItem.Loading("Loading-Item-3"),
                UiObjectsListItem.Loading("Loading-Item-4"),
            )
        )
    }
}