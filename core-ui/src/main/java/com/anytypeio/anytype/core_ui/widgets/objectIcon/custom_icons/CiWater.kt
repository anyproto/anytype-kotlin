package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiWater: ImageVector
    get() {
        if (_CiWater != null) {
            return _CiWater!!
        }
        _CiWater = ImageVector.Builder(
            name = "CiWater",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(265.12f, 60.12f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, -18.23f, 0f)
                curveTo(215.23f, 97.15f, 112f, 225.17f, 112f, 320f)
                curveToRelative(0f, 88.37f, 55.64f, 144f, 144f, 144f)
                reflectiveCurveToRelative(144f, -55.63f, 144f, -144f)
                curveTo(400f, 225.17f, 296.77f, 97.15f, 265.12f, 60.12f)
                close()
                moveTo(272f, 412f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.34f, -16f)
                arcToRelative(11.89f, 11.89f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.41f, -8f)
                arcTo(60.06f, 60.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 332f, 328.07f)
                arcToRelative(11.89f, 11.89f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, -11.41f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 356f, 328f)
                arcTo(84.09f, 84.09f, 0f, isMoreThanHalf = false, isPositiveArc = true, 272f, 412f)
                close()
            }
        }.build()

        return _CiWater!!
    }

@Suppress("ObjectPropertyName")
private var _CiWater: ImageVector? = null
