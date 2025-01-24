package com.anytypeio.anytype.presentation.navigation

class NavPanelState(
    val isCreateObjectButtonEnabled: Boolean,
    val leftButtonState: LeftButtonState
) {
    sealed class LeftButtonState {
        data object ViewMembers : LeftButtonState()
        data class AddMembers(val isActive: Boolean): LeftButtonState()
        data class Comment(val isActive: Boolean): LeftButtonState()
    }
}