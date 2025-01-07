package com.anytypeio.anytype.core_ui.lists.objects

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class UiObjectsListItem {

    abstract val id: String

    data class Loading(override val id: String) : UiObjectsListItem()

    data class Item(
        override val id: String,
        val name: String,
        val space: SpaceId,
        val type: String? = null,
        val typeName: String? = null,
        val createdBy: String? = null,
        val layout: ObjectType.Layout? = null,
        val icon: ObjectIcon = ObjectIcon.None,
        val isPossibleToDelete: Boolean = false
    ) : UiObjectsListItem()
}

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