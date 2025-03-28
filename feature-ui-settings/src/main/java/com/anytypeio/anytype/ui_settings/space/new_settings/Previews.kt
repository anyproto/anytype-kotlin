package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.compose.runtime.Composable
import com.anytypeio.anytype.core_models.ThemeColor
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
                UiSpaceSettingsItem.Spacer(height = 12),
                UiSpaceSettingsItem.Description("This is a dream team space"),
                UiSpaceSettingsItem.Spacer(height = 12),
                UiSpaceSettingsItem.Multiplayer,
                UiSpaceSettingsItem.Spacer(height = 8),
                UiSpaceSettingsItem.Section.Collaboration,
                UiSpaceSettingsItem.Members(5),
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
                networkId = "random network id"
            )
        ),
        uiEvent = {},
    )
}