package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiGameController: ImageVector
    get() {
        if (_CiGameController != null) {
            return _CiGameController!!
        }
        _CiGameController = ImageVector.Builder(
            name = "CiGameController",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(483.13f, 245.38f)
                curveTo(461.92f, 149.49f, 430f, 98.31f, 382.65f, 84.33f)
                arcTo(107.13f, 107.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, 352f, 80f)
                curveToRelative(-13.71f, 0f, -25.65f, 3.34f, -38.28f, 6.88f)
                curveTo(298.5f, 91.15f, 281.21f, 96f, 256f, 96f)
                reflectiveCurveToRelative(-42.51f, -4.84f, -57.76f, -9.11f)
                curveTo(185.6f, 83.34f, 173.67f, 80f, 160f, 80f)
                arcToRelative(115.74f, 115.74f, 0f, isMoreThanHalf = false, isPositiveArc = false, -31.73f, 4.32f)
                curveToRelative(-47.1f, 13.92f, -79f, 65.08f, -100.52f, 161f)
                curveTo(4.61f, 348.54f, 16f, 413.71f, 59.69f, 428.83f)
                arcToRelative(56.62f, 56.62f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18.64f, 3.22f)
                curveToRelative(29.93f, 0f, 53.93f, -24.93f, 70.33f, -45.34f)
                curveToRelative(18.53f, -23.1f, 40.22f, -34.82f, 107.34f, -34.82f)
                curveToRelative(59.95f, 0f, 84.76f, 8.13f, 106.19f, 34.82f)
                curveToRelative(13.47f, 16.78f, 26.2f, 28.52f, 38.9f, 35.91f)
                curveToRelative(16.89f, 9.82f, 33.77f, 12f, 50.16f, 6.37f)
                curveToRelative(25.82f, -8.81f, 40.62f, -32.1f, 44f, -69.24f)
                curveTo(497.82f, 331.27f, 493.86f, 293.86f, 483.13f, 245.38f)
                close()
                moveTo(208f, 240f)
                lineTo(176f, 240f)
                verticalLineToRelative(32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 0f)
                lineTo(144f, 240f)
                lineTo(112f, 240f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineToRelative(32f)
                lineTo(144f, 176f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 0f)
                verticalLineToRelative(32f)
                horizontalLineToRelative(32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
                moveTo(292f, 244f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20f, -20f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 292f, 244f)
                close()
                moveTo(336f, 288f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20f, -19.95f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 336f, 288f)
                close()
                moveTo(336f, 200f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20f, -20f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 336f, 200f)
                close()
                moveTo(380f, 244f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20f, -20f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 380f, 244f)
                close()
            }
        }.build()

        return _CiGameController!!
    }

@Suppress("ObjectPropertyName")
private var _CiGameController: ImageVector? = null
