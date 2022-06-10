package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.MenuItem


sealed class ObjectAppearanceSettingView {

    sealed class Section : ObjectAppearanceSettingView() {
        object FeaturedRelations : Section()
    }

    sealed class Settings : ObjectAppearanceSettingView() {
        data class PreviewLayout(val previewLayoutState: MenuItem.PreviewLayout) : Settings()
        data class Icon(val icon: MenuItem.Icon) : Settings()
        data class Cover(val coverState: MenuItem.Cover) : Settings()
    }

    sealed class Relation : ObjectAppearanceSettingView() {
        object Name: Relation()
        data class Description(val description: MenuItem.Description) : Relation()
    }

    sealed class Icon : ObjectAppearanceSettingView() {
        data class None(val isSelected: Boolean) : Icon()
        data class Small(val isSelected: Boolean) : Icon()
        data class Medium(val isSelected: Boolean) : Icon()
    }

    sealed class Cover : ObjectAppearanceSettingView() {
        data class None(val isSelected: Boolean) : Cover()
        data class Visible(val isSelected: Boolean) : Cover()
    }

    sealed class PreviewLayout : ObjectAppearanceSettingView() {
        data class Text(val isSelected: Boolean) : PreviewLayout()
        data class Card(val isSelected: Boolean) : PreviewLayout()
    }
}
