package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceIconState

sealed class ObjectAppearanceSettingView {

    sealed class Section : ObjectAppearanceSettingView() {
        object FeaturedRelations : Section()
    }

    sealed class Settings : ObjectAppearanceSettingView() {
        data class PreviewLayout(val style: Double?) : Settings()
        data class Icon(val state: ObjectAppearanceIconState) : Settings()
        data class Cover(val withCover: Boolean?) : Settings()
    }

    sealed class Relation : ObjectAppearanceSettingView() {
        data class Name(val withName: Boolean?) : Relation()
        data class Description(val withDescription: Boolean?) : Relation()
    }

    sealed class Icon : ObjectAppearanceSettingView() {
        data class Small(val isSelected: Boolean) : Icon()
        data class Medium(val isSelected: Boolean) : Icon()
        data class Large(val isSelected: Boolean) : Icon()
        data class None(val isSelected: Boolean) : Icon()
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
