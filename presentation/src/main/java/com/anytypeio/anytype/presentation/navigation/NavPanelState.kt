package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions

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

    companion object {
        fun fromPermission(permission: SpaceMemberPermissions?) : NavPanelState {
            return when(permission) {
                SpaceMemberPermissions.READER -> {
                    Default(
                        isCreateObjectButtonEnabled = false,
                        leftButtonState = NavPanelState.LeftButtonState.ViewMembers
                    )
                }
                SpaceMemberPermissions.WRITER -> {
                    Default(
                        isCreateObjectButtonEnabled = true,
                        leftButtonState = LeftButtonState.ViewMembers
                    )
                }
                SpaceMemberPermissions.OWNER -> {
                    Default(
                        isCreateObjectButtonEnabled = true,
                        leftButtonState = LeftButtonState.AddMembers(
                            isActive = true
                        )
                    )
                }
                SpaceMemberPermissions.NO_PERMISSIONS -> {
                    Default(
                        isCreateObjectButtonEnabled = false,
                        leftButtonState = LeftButtonState.ViewMembers
                    )
                }
                else -> {
                    Init
                }
            }
        }
    }
}