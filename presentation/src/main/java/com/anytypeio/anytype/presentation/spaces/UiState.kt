package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class UiSpaceSettingsState {
    data object Initial : UiSpaceSettingsState()
    data class SpaceSettings(
        val items: List<UiSpaceSettingsItem>,
        val isEditEnabled: Boolean
    ) : UiSpaceSettingsState() {
        val name: String = items.filterIsInstance<UiSpaceSettingsItem.Name>()
            .firstOrNull()?.name ?: EMPTY_STRING_VALUE
        val description = items.filterIsInstance<UiSpaceSettingsItem.Description>()
            .firstOrNull()?.description ?: EMPTY_STRING_VALUE
    }
    data class SpaceSettingsError(val message: String) : UiSpaceSettingsState()
}

sealed class UiSpaceSettingsItem {

    sealed class Section : UiSpaceSettingsItem() {
        data object Collaboration : Section()
        data object ContentModel : Section()
        data object Preferences : Section()
        data object DataManagement : Section()
        data object Misc : Section()
    }

    data class Spacer(val height: Int) : UiSpaceSettingsItem()
    data class Icon(val icon: SpaceIconView) : UiSpaceSettingsItem()
    data class Name(val name: String) : UiSpaceSettingsItem()
    data class Description(val description: String) : UiSpaceSettingsItem()
    data object Multiplayer : UiSpaceSettingsItem()
    data class Members(val count: Int) : UiSpaceSettingsItem()
    data class Chat(val isOn: Boolean) : UiSpaceSettingsItem()
    data object ObjectTypes : UiSpaceSettingsItem()
    data class DefaultObjectType(val name: String, val icon: ObjectIcon) : UiSpaceSettingsItem()
    data class Wallpapers(val color: ThemeColor) : UiSpaceSettingsItem()
    data class RemoteStorage(val size: Int) : UiSpaceSettingsItem()
    data object SpaceInfo : UiSpaceSettingsItem()
    data object DeleteSpace : UiSpaceSettingsItem()

}