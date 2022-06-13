package com.anytypeio.anytype.presentation.objects.appearance

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.MenuItem


sealed interface ObjectAppearanceMainSettingsView {

    sealed interface Toggle: ObjectAppearanceMainSettingsView

    object FeaturedRelationsSection : ObjectAppearanceMainSettingsView

    data class PreviewLayout(val previewLayoutState: MenuItem.PreviewLayout) :
        ObjectAppearanceMainSettingsView

    data class Icon(val icon: MenuItem.Icon) : ObjectAppearanceMainSettingsView
    data class Cover(val coverState: MenuItem.Cover) : ObjectAppearanceMainSettingsView

    sealed interface Relation : ObjectAppearanceMainSettingsView {
        object Name : Relation
        data class Description(val description: MenuItem.Description) : Relation, Toggle
    }
}
