package com.anytypeio.anytype.presentation.objects

sealed class ObjectAppearanceSettingView {

    sealed class Section : ObjectAppearanceSettingView() {
        object FeaturedRelations : Section()
    }

    sealed class Settings : ObjectAppearanceSettingView() {
        data class PreviewLayout(val style: Double?) : Settings()
        data class Icon(val size: Double?, val withIcon: Boolean?) : Settings()
        data class Cover(val withCover: Boolean?) : Settings()
    }

    sealed class Relation : ObjectAppearanceSettingView() {
        data class Name(val withName: Boolean?) : Relation()
        data class Description(val withDescription: Boolean?) : Relation()
    }
}
