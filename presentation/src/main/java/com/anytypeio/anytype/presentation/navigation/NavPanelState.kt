package com.anytypeio.anytype.presentation.navigation

sealed class NavPanelState {

    data object Init : NavPanelState()

    data class Default(
        val isCreateObjectButtonEnabled: Boolean,
        val leftButtonState: LeftButtonState
    ) : NavPanelState()

    sealed class LeftButtonState {
        data object ViewMembers : LeftButtonState()
        data class AddMembers(val isActive: Boolean): LeftButtonState()
        data class Comment(val isActive: Boolean): LeftButtonState()
    }
}