package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMoon: ImageVector
    get() {
        if (_CiMoon != null) {
            return _CiMoon!!
        }
        _CiMoon = ImageVector.Builder(
            name = "CiMoon",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(264f, 480f)
                arcTo(232f, 232f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 248f)
                curveTo(32f, 154f, 86f, 69.72f, 169.61f, 33.33f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21.06f, 21.06f)
                curveTo(181.07f, 76.43f, 176f, 104.66f, 176f, 136f)
                curveToRelative(0f, 110.28f, 89.72f, 200f, 200f, 200f)
                curveToRelative(31.34f, 0f, 59.57f, -5.07f, 81.61f, -14.67f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21.06f, 21.06f)
                curveTo(442.28f, 426f, 358f, 480f, 264f, 480f)
                close()
            }
        }.build()

        return _CiMoon!!
    }

@Suppress("ObjectPropertyName")
private var _CiMoon: ImageVector? = null
