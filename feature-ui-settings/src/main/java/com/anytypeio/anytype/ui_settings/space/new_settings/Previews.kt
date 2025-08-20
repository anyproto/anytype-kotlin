package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.compose.runtime.Composable
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.SpaceTechInfo
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsState

@Composable
@DefaultPreviews
fun NewSpaceSettingsScreenPreview() {
    NewSpaceSettingsScreen(
        uiState = UiSpaceSettingsState.SpaceSettings(
            items = listOf(
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.Icon(SpaceIconView.Placeholder()),
                UiSpaceSettingsItem.Spacer(height = 16),
                UiSpaceSettingsItem.Name("Dream team"),
                UiSpaceSettingsItem.Spacer(height = 4),
                UiSpaceSettingsItem.EntrySpace,
                UiSpaceSettingsItem.MembersSmall(2),
                UiSpaceSettingsItem.Spacer(height = 12),
                UiSpaceSettingsItem.InviteLink("linkl"),
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.Section.Collaboration,
                UiSpaceSettingsItem.Members(1, true),
                UiSpaceSettingsItem.Section.ContentModel,
                UiSpaceSettingsItem.ObjectTypes,
                UiSpaceSettingsItem.Section.Preferences,
                UiSpaceSettingsItem.DefaultObjectType(
                    id = "some id",
                    name = "Taskwithveryverlylongname",
                    icon = ObjectIcon.TypeIcon.Default.DEFAULT,
                ),
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.Wallpapers(null),
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.Section.DataManagement,
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.Section.Misc,
                UiSpaceSettingsItem.SpaceInfo,
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.DeleteSpace,
                UiSpaceSettingsItem.Spacer(height = 32),

                ),
            isEditEnabled = true,
            spaceTechInfo = SpaceTechInfo(
                spaceId = SpaceId("space-id"),
                createdBy = "Thomas",
                creationDateInMillis = null,
                networkId = "random network id",
                isDebugVisible = false,
                deviceToken = null
            ),
            notificationState = NotificationState.ALL,
            targetSpaceId = "space-view-id"
        ),
        uiEvent = {},
    )
}