package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiColorPalette: ImageVector
    get() {
        if (_CiColorPalette != null) {
            return _CiColorPalette!!
        }
        _CiColorPalette = ImageVector.Builder(
            name = "CiColorPalette",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(441f, 336.2f)
                lineToRelative(-0.06f, -0.05f)
                curveToRelative(-9.93f, -9.18f, -22.78f, -11.34f, -32.16f, -12.92f)
                lineToRelative(-0.69f, -0.12f)
                curveToRelative(-9.05f, -1.49f, -10.48f, -2.5f, -14.58f, -6.17f)
                curveToRelative(-2.44f, -2.17f, -5.35f, -5.65f, -5.35f, -9.94f)
                reflectiveCurveToRelative(2.91f, -7.77f, 5.34f, -9.94f)
                lineToRelative(30.28f, -26.87f)
                curveToRelative(25.92f, -22.91f, 40.2f, -53.66f, 40.2f, -86.59f)
                reflectiveCurveTo(449.73f, 119.92f, 423.78f, 97f)
                curveToRelative(-35.89f, -31.59f, -85f, -49f, -138.37f, -49f)
                curveTo(223.72f, 48f, 162f, 71.37f, 116f, 112.11f)
                curveToRelative(-43.87f, 38.77f, -68f, 90.71f, -68f, 146.24f)
                reflectiveCurveToRelative(24.16f, 107.47f, 68f, 146.23f)
                curveToRelative(21.75f, 19.24f, 47.49f, 34.18f, 76.52f, 44.42f)
                arcToRelative(266.17f, 266.17f, 0f, isMoreThanHalf = false, isPositiveArc = false, 86.87f, 15f)
                horizontalLineToRelative(1.81f)
                curveToRelative(61f, 0f, 119.09f, -20.57f, 159.39f, -56.4f)
                curveToRelative(9.7f, -8.56f, 15.15f, -20.83f, 15.34f, -34.56f)
                curveTo(456.14f, 358.87f, 450.56f, 345.09f, 441f, 336.2f)
                close()
                moveTo(112f, 208f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, 32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 112f, 208f)
                close()
                moveTo(152f, 343f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 152f, 343f)
                close()
                moveTo(192f, 144f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, 32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 192f, 144f)
                close()
                moveTo(256f, 415f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = true, isPositiveArc = true, 48f, -48f)
                arcTo(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 415f)
                close()
                moveTo(328f, 176f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 328f, 176f)
                close()
            }
        }.build()

        return _CiColorPalette!!
    }

@Suppress("ObjectPropertyName")
private var _CiColorPalette: ImageVector? = null
