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
        val isCreateEnabled: Boolean,
        val left: LeftButtonState
    ) : NavPanelState()

    sealed class LeftButtonState {
        data object Home : LeftButtonState()
        data object ViewMembers : LeftButtonState()
        data class AddMembers(val isActive: Boolean) : LeftButtonState()
        data object Hidden : LeftButtonState()

        suspend fun sendAnalytics(analytics: Analytics) {
            when (this) {
                is AddMembers -> analytics.sendEvent(
                    eventName = EventsDictionary.screenSettingsSpaceShare,
                    props = Props(mapOf(
                        EventsPropertiesKey.route to EventsDictionary.Routes.navigation
                    ))
                )
                is ViewMembers -> analytics.sendEvent(
                    eventName = EventsDictionary.screenSettingsSpaceMembers,
                    props = Props(mapOf(
                        EventsPropertiesKey.route to EventsDictionary.Routes.navigation
                    ))
                )
                Home, Hidden -> Unit
            }
        }
    }

    companion object {
        fun fromPermission(
            permission: SpaceMemberPermissions?,
            forceHome: Boolean = true,
            spaceAccess: SpaceAccessType? = null,
            isOneToOneSpace: Boolean
        ): NavPanelState {
            val createEnabled = when (permission) {
                SpaceMemberPermissions.WRITER,
                SpaceMemberPermissions.OWNER -> true
                else -> false
            }
            val leftButton = when (permission) {
                SpaceMemberPermissions.OWNER ->
                    defaultLeft(
                        forceHome = forceHome,
                        isOneToOneSpace = isOneToOneSpace,
                        isActive = spaceAccess != SpaceAccessType.DEFAULT
                    )
                SpaceMemberPermissions.WRITER,
                SpaceMemberPermissions.READER,
                SpaceMemberPermissions.NO_PERMISSIONS ->
                    defaultLeft(
                        forceHome = forceHome,
                        isOneToOneSpace = isOneToOneSpace,
                        isActive = false
                    )
                else -> null
            }
            return if (leftButton != null) Default(createEnabled, leftButton) else Init
        }

        private fun defaultLeft(
            forceHome: Boolean,
            isOneToOneSpace: Boolean,
            isActive: Boolean
        ): LeftButtonState = when {
            isOneToOneSpace -> LeftButtonState.Hidden
            forceHome -> LeftButtonState.Home
            !forceHome && isActive -> LeftButtonState.AddMembers(true)
            else -> LeftButtonState.ViewMembers
        }
    }
}

@Suppress("UNUSED_PARAMETER")
suspend fun NavPanelState.leftButtonClickAnalytics(analytics: Analytics) {
    // No-op. Pre-refactor this delegated to `LeftButtonState.Chat.sendAnalytics(...)`,
    // which in turn hit the `Chat -> Unit` branch of `LeftButtonState.sendAnalytics`
    // and never emitted any event. Preserving that exact behavior here while the
    // `LeftButtonState.Chat` variant is removed; fixing the analytics to actually
    // fire on left-button clicks is a separate task.
}
