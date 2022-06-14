package com.anytypeio.anytype.presentation.objects.appearance.choose

sealed interface ObjectAppearanceChooseSettingsView {

    val isSelected: Boolean

    sealed class Icon : ObjectAppearanceChooseSettingsView {
        data class None(override val isSelected: Boolean) : Icon()
        data class Small(override val isSelected: Boolean) : Icon()
        data class Medium(override val isSelected: Boolean) : Icon()
    }

    sealed class Cover : ObjectAppearanceChooseSettingsView {
        data class None(override val isSelected: Boolean) : Cover()
        data class Visible(override val isSelected: Boolean) : Cover()
    }

    sealed class PreviewLayout : ObjectAppearanceChooseSettingsView {
        data class Text(override val isSelected: Boolean) : PreviewLayout()
        data class Card(override val isSelected: Boolean) : PreviewLayout()
    }

    sealed interface Description : ObjectAppearanceChooseSettingsView {
        data class None(override val isSelected: Boolean) : Description
        data class Added(override val isSelected: Boolean) : Description
        data class Content(override val isSelected: Boolean) : Description
    }
}