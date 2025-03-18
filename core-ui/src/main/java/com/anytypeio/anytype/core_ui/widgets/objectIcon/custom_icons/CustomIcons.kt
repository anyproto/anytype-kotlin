package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.vector.ImageVector
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIcon

object CustomIcons {

    fun getImageVector(icon: CustomIcon): ImageVector? {
        return iconsMap[icon.rawValue]
    }

    fun getImageVector(name: String): ImageVector? {
        return iconsMap[name]
    }

    val iconsMap: Map<String, ImageVector> by lazy {
        mapOf()
    }
}

