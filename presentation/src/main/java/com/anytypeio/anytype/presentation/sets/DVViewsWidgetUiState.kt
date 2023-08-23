package com.anytypeio.anytype.presentation.sets

data class DVViewsWidgetUiState(
    val showWidget: Boolean,
    val isEditing: Boolean,
    val items: List<ManageViewerViewModel.ViewerView>
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
}
