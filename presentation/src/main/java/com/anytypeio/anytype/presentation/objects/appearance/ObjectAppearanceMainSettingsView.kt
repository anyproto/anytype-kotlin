package com.anytypeio.anytype.presentation.objects.appearance

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.MenuItem


sealed interface ObjectAppearanceMainSettingsView {

    sealed interface Toggle : ObjectAppearanceMainSettingsView {
        val checked: Boolean
    }

    sealed interface List : ObjectAppearanceMainSettingsView
    data class PreviewLayout(val previewLayoutState: MenuItem.PreviewLayout) : List
    data class Icon(val icon: MenuItem.Icon) : List
    data class Cover(val coverState: MenuItem.Cover): ObjectAppearanceMainSettingsView

    object FeaturedRelationsSection : ObjectAppearanceMainSettingsView


    sealed interface Relation : ObjectAppearanceMainSettingsView {
        object Name : Relation
        data class Description(val description: MenuItem.Description) : Relation, List
        data class ObjectType(val objectType: MenuItem.ObjectType) : Relation, Toggle {
            override val checked: Boolean
                get() = objectType.isChecked()
        }
    }
}
