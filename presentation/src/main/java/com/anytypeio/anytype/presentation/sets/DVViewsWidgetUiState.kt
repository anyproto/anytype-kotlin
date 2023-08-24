package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.sets.viewer.ViewerView

data class DVViewsWidgetUiState(
    val showWidget: Boolean,
    val isEditing: Boolean,
    val items: List<ViewerView>
) {

    fun dismiss() = copy(
        showWidget = false,
        isEditing = false
    )

    companion object {
        fun init() = DVViewsWidgetUiState(
            showWidget = false,
            isEditing = false,
            items = emptyList()
        )
    }

    sealed class Clicks {
        object Dismiss : Clicks()
        object EditMode : Clicks()
        object DoneMode : Clicks()
        data class Delete(val viewer: Id) : Clicks()
        data class Edit(val id: Id) : Clicks()
        data class Position(val id: Id, val position: Int) : Clicks()
        data class SetActive(val id: Id) : Clicks()
    }
}


