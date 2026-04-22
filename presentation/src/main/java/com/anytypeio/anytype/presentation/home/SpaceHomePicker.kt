package com.anytypeio.anytype.presentation.home

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ui.ObjectIcon

sealed class SpaceHomePickerState {
    data object Hidden : SpaceHomePickerState()
    data class Visible(
        val query: String,
        val candidates: List<SpaceHomePickerItem>,
        val currentHomepage: String?,
        val isLoading: Boolean
    ) : SpaceHomePickerState()
}

data class SpaceHomePickerItem(
    val objectId: Id,
    val name: String,
    val icon: ObjectIcon,
    val type: String
)
