package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions

sealed class NavPanelState {

    data object Init : NavPanelState()

    data class Default(
        val isCreateObjectButtonEnabled: Boolean,
        val leftButtonState: LeftButtonState
    ) : NavPanelState()

    sealed class LeftButtonState {
        data object Home : LeftButtonState()
        data object ViewMembers : LeftButtonState()
        data class AddMembers(val isActive: Boolean): LeftButtonState()
        data class Comment(val isActive: Boolean): LeftButtonState()
    }

    companion object {
        fun fromPermission(
            permission: SpaceMemberPermissions?,
            forceHome: Boolean = true
        ) : NavPanelState {
            return when(permission) {
                SpaceMemberPermissions.READER -> {
                    Default(
                        isCreateObjectButtonEnabled = false,
                        leftButtonState = if (forceHome)
                            LeftButtonState.Home
                        else
                            LeftButtonState.ViewMembers
                    )
                }
                SpaceMemberPermissions.WRITER -> {
                    Default(
                        isCreateObjectButtonEnabled = true,
                        leftButtonState = if (forceHome)
                            LeftButtonState.Home
                        else
                            LeftButtonState.ViewMembers
                    )
                }
                SpaceMemberPermissions.OWNER -> {
                    Default(
                        isCreateObjectButtonEnabled = true,
                        leftButtonState = if (forceHome)
                            LeftButtonState.Home
                        else
                            LeftButtonState.AddMembers(
                                isActive = true
                            )
                    )
                }
                SpaceMemberPermissions.NO_PERMISSIONS -> {
                    Default(
                        isCreateObjectButtonEnabled = false,
                        leftButtonState = if (forceHome)
                            LeftButtonState.Home
                        else
                            LeftButtonState.ViewMembers
                    )
                }
                else -> {
                    Init
                }
            }
        }
    }
}