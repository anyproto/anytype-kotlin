package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
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
            forceHome: Boolean = true,
            spaceAccessType: SpaceAccessType? = null
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
                                isActive = spaceAccessType != SpaceAccessType.DEFAULT
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

suspend fun NavPanelState.leftButtonClickAnalytics(analytics: Analytics) {
    when (val state = this) {
        is NavPanelState.Default -> {
            when (state.leftButtonState) {
                is NavPanelState.LeftButtonState.AddMembers -> {
                    analytics.sendEvent(
                        eventName = EventsDictionary.screenSettingsSpaceShare,
                        props = Props(
                            mapOf(
                                EventsPropertiesKey.route to EventsDictionary.Routes.navigation
                            )
                        )
                    )
                }

                is NavPanelState.LeftButtonState.Comment -> {
                    analytics.sendEvent(eventName = EventsDictionary.clickQuote)
                }

                NavPanelState.LeftButtonState.Home -> {
                    // Do nothing.
                }

                NavPanelState.LeftButtonState.ViewMembers -> {
                    analytics.sendEvent(
                        eventName = EventsDictionary.screenSettingsSpaceMembers,
                        props = Props(
                            mapOf(
                                EventsPropertiesKey.route to EventsDictionary.Routes.navigation
                            )
                        )
                    )
                }
            }
        }

        NavPanelState.Init -> {
            // Do nothing.
        }
    }
}