package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiShield: ImageVector
    get() {
        if (_CiShield != null) {
            return _CiShield!!
        }
        _CiShield = ImageVector.Builder(
            name = "CiShield",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(479.07f, 111.35f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 465.92f, 96.6f)
                curveTo(379.89f, 81.18f, 343.69f, 69.12f, 266f, 34.16f)
                curveToRelative(-7.76f, -2.89f, -12.57f, -2.84f, -20f, 0f)
                curveToRelative(-77.69f, 35f, -113.89f, 47f, -199.92f, 62.44f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -13.15f, 14.75f)
                curveToRelative(-3.85f, 61.1f, 4.34f, 118f, 24.36f, 169.15f)
                arcToRelative(348.86f, 348.86f, 0f, isMoreThanHalf = false, isPositiveArc = false, 71.43f, 112.41f)
                curveToRelative(44.67f, 47.43f, 94.2f, 75.12f, 119.74f, 85.6f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.11f, 0f)
                curveToRelative(27f, -10.92f, 74.69f, -37.82f, 119.71f, -85.62f)
                arcTo(348.86f, 348.86f, 0f, isMoreThanHalf = false, isPositiveArc = false, 454.71f, 280.5f)
                curveTo(474.73f, 229.36f, 482.92f, 172.45f, 479.07f, 111.35f)
                close()
            }
        }.build()

        return _CiShield!!
    }

@Suppress("ObjectPropertyName")
private var _CiShield: ImageVector? = null
