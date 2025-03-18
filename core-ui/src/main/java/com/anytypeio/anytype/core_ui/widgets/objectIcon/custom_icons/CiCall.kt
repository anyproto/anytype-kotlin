package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCall: ImageVector
    get() {
        if (_CiCall != null) {
            return _CiCall!!
        }
        _CiCall = ImageVector.Builder(
            name = "CiCall",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(391f, 480f)
                curveToRelative(-19.52f, 0f, -46.94f, -7.06f, -88f, -30f)
                curveToRelative(-49.93f, -28f, -88.55f, -53.85f, -138.21f, -103.38f)
                curveTo(116.91f, 298.77f, 93.61f, 267.79f, 61f, 208.45f)
                curveToRelative(-36.84f, -67f, -30.56f, -102.12f, -23.54f, -117.13f)
                curveTo(45.82f, 73.38f, 58.16f, 62.65f, 74.11f, 52f)
                arcTo(176.3f, 176.3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 102.75f, 36.8f)
                curveToRelative(1f, -0.43f, 1.93f, -0.84f, 2.76f, -1.21f)
                curveToRelative(4.95f, -2.23f, 12.45f, -5.6f, 21.95f, -2f)
                curveToRelative(6.34f, 2.38f, 12f, 7.25f, 20.86f, 16f)
                curveToRelative(18.17f, 17.92f, 43f, 57.83f, 52.16f, 77.43f)
                curveToRelative(6.15f, 13.21f, 10.22f, 21.93f, 10.23f, 31.71f)
                curveToRelative(0f, 11.45f, -5.76f, 20.28f, -12.75f, 29.81f)
                curveToRelative(-1.31f, 1.79f, -2.61f, 3.5f, -3.87f, 5.16f)
                curveToRelative(-7.61f, 10f, -9.28f, 12.89f, -8.18f, 18.05f)
                curveToRelative(2.23f, 10.37f, 18.86f, 41.24f, 46.19f, 68.51f)
                reflectiveCurveToRelative(57.31f, 42.85f, 67.72f, 45.07f)
                curveToRelative(5.38f, 1.15f, 8.33f, -0.59f, 18.65f, -8.47f)
                curveToRelative(1.48f, -1.13f, 3f, -2.3f, 4.59f, -3.47f)
                curveToRelative(10.66f, -7.93f, 19.08f, -13.54f, 30.26f, -13.54f)
                horizontalLineToRelative(0.06f)
                curveToRelative(9.73f, 0f, 18.06f, 4.22f, 31.86f, 11.18f)
                curveToRelative(18f, 9.08f, 59.11f, 33.59f, 77.14f, 51.78f)
                curveToRelative(8.77f, 8.84f, 13.66f, 14.48f, 16.05f, 20.81f)
                curveToRelative(3.6f, 9.53f, 0.21f, 17f, -2f, 22f)
                curveToRelative(-0.37f, 0.83f, -0.78f, 1.74f, -1.21f, 2.75f)
                arcToRelative(176.49f, 176.49f, 0f, isMoreThanHalf = false, isPositiveArc = true, -15.29f, 28.58f)
                curveToRelative(-10.63f, 15.9f, -21.4f, 28.21f, -39.38f, 36.58f)
                arcTo(67.42f, 67.42f, 0f, isMoreThanHalf = false, isPositiveArc = true, 391f, 480f)
                close()
            }
        }.build()

        return _CiCall!!
    }

@Suppress("ObjectPropertyName")
private var _CiCall: ImageVector? = null
