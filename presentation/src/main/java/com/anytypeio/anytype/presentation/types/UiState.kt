package com.anytypeio.anytype.presentation.types

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class UiItemsState{
    data object Empty : UiItemsState()
    data class Content(val items: List<UiContentItem>) : UiItemsState()
}

data class UiContentItem(
    val id: Id,
    val name: String,
    val icon: ObjectIcon
)