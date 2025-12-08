package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType

sealed class NavPanelState {

    data object Init : NavPanelState()

    data class Default(
        val isCreateEnabled: Boolean,
        val left: LeftButtonState
    ) : NavPanelState()

    data class Chat(
        val isCreateEnabled: Boolean,
        val left: LeftButtonState
    ) : NavPanelState()

    sealed class LeftButtonState {
        data object Home : LeftButtonState()
        data object Chat : LeftButtonState()
        data object ViewMembers : LeftButtonState()
        data class AddMembers(val isActive: Boolean) : LeftButtonState()

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
                Home, Chat -> Unit
            }
        }
    }

    companion object {
        fun fromPermission(
            permission: SpaceMemberPermissions?,
            forceHome: Boolean = true,
            spaceAccess: SpaceAccessType? = null,
            spaceUxType: SpaceUxType
        ): NavPanelState {
            val isChat = (spaceUxType == SpaceUxType.CHAT || spaceUxType == SpaceUxType.ONE_TO_ONE)
            val createEnabled = when (permission) {
                SpaceMemberPermissions.WRITER,
                SpaceMemberPermissions.OWNER -> true
                else -> false
            }
            val leftButton = when (permission) {
                SpaceMemberPermissions.OWNER ->
                    defaultLeft(forceHome, isChat = isChat, isActive = spaceAccess != SpaceAccessType.DEFAULT)
                SpaceMemberPermissions.WRITER,
                SpaceMemberPermissions.READER,
                SpaceMemberPermissions.NO_PERMISSIONS ->
                    defaultLeft(forceHome, isChat = isChat, isActive = false)
                else -> null
            }
            return when {
                leftButton != null && isChat -> Chat(createEnabled, leftButton)
                leftButton != null -> Default(createEnabled, leftButton)
                else -> Init
            }
        }

        private fun defaultLeft(
            forceHome: Boolean,
            isChat: Boolean,
            isActive: Boolean
        ): LeftButtonState = when {
            forceHome && isChat -> LeftButtonState.Chat
            forceHome -> LeftButtonState.Home
            !forceHome && isActive -> LeftButtonState.AddMembers(true)
            !forceHome -> LeftButtonState.ViewMembers
            else -> LeftButtonState.ViewMembers
        }
    }
}

suspend fun NavPanelState.leftButtonClickAnalytics(analytics: Analytics) {
    when (this) {
        is NavPanelState.Default,
        is NavPanelState.Chat -> NavPanelState.LeftButtonState.Chat.sendAnalytics(analytics)
        else -> Unit
    }
}