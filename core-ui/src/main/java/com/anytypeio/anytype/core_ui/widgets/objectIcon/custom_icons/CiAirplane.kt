package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiAirplane: ImageVector
    get() {
        if (_CiAirplane != null) {
            return _CiAirplane!!
        }
        _CiAirplane = ImageVector.Builder(
            name = "CiAirplane",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(186.62f, 464f)
                horizontalLineTo(160f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.57f, -22.6f)
                lineToRelative(64.46f, -142.25f)
                lineTo(113.1f, 297f)
                lineTo(77.8f, 339.77f)
                curveTo(71.07f, 348.23f, 65.7f, 352f, 52f, 352f)
                horizontalLineTo(34.08f)
                arcToRelative(17.66f, 17.66f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.7f, -7.06f)
                curveToRelative(-2.38f, -3.21f, -4.72f, -8.65f, -2.44f, -16.41f)
                lineToRelative(19.82f, -71f)
                curveToRelative(0.15f, -0.53f, 0.33f, -1.06f, 0.53f, -1.58f)
                arcToRelative(0.38f, 0.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -0.15f)
                arcToRelative(14.82f, 14.82f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.53f, -1.59f)
                lineTo(16.92f, 182.76f)
                curveToRelative(-2.15f, -7.61f, 0.2f, -12.93f, 2.56f, -16.06f)
                arcToRelative(16.83f, 16.83f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.6f, -6.7f)
                horizontalLineTo(52f)
                curveToRelative(10.23f, 0f, 20.16f, 4.59f, 26f, 12f)
                lineToRelative(34.57f, 42.05f)
                lineToRelative(97.32f, -1.44f)
                lineToRelative(-64.44f, -142f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 160f, 48f)
                horizontalLineToRelative(26.91f)
                arcToRelative(25f, 25f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19.35f, 9.8f)
                lineToRelative(125.05f, 152f)
                lineToRelative(57.77f, -1.52f)
                curveToRelative(4.23f, -0.23f, 15.95f, -0.31f, 18.66f, -0.31f)
                curveTo(463f, 208f, 496f, 225.94f, 496f, 256f)
                curveToRelative(0f, 9.46f, -3.78f, 27f, -29.07f, 38.16f)
                curveToRelative(-14.93f, 6.6f, -34.85f, 9.94f, -59.21f, 9.94f)
                curveToRelative(-2.68f, 0f, -14.37f, -0.08f, -18.66f, -0.31f)
                lineToRelative(-57.76f, -1.54f)
                lineToRelative(-125.36f, 152f)
                arcTo(25f, 25f, 0f, isMoreThanHalf = false, isPositiveArc = true, 186.62f, 464f)
                close()
            }
        }.build()

        return _CiAirplane!!
    }

@Suppress("ObjectPropertyName")
private var _CiAirplane: ImageVector? = null
