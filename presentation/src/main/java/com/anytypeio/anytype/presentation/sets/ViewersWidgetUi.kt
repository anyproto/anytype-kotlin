package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.sets.viewer.ViewerView
import com.anytypeio.anytype.presentation.widgets.FromIndex
import com.anytypeio.anytype.presentation.widgets.ToIndex

data class ViewersWidgetUi(
    val showWidget: Boolean,
    val isEditing: Boolean,
    val isReadOnly: Boolean,
    val items: List<ViewerView>
) {

    fun dismiss() = copy(
        showWidget = false,
        isEditing = false
    )

    companion object {
        fun init() = ViewersWidgetUi(
            showWidget = false,
            isEditing = false,
            items = emptyList(),
            isReadOnly = true
        )
    }

    sealed class Action {
        data object Dismiss : Action()
        data object EditMode : Action()
        data object DoneMode : Action()
        data class Delete(val viewer: Id) : Action()
        data class Edit(val id: Id) : Action()
        data class OnMove(
            val currentViews: List<ViewerView>,
            val from: FromIndex,
            val to: ToIndex
        ) : Action()

        data class SetActive(val id: Id, val type: DVViewerType) : Action()

        data object Plus : Action()
    }
}


