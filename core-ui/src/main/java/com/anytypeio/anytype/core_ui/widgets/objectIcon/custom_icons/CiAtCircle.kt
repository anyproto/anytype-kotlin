package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiAtCircle: ImageVector
    get() {
        if (_CiAtCircle != null) {
            return _CiAtCircle!!
        }
        _CiAtCircle = ImageVector.Builder(
            name = "CiAtCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(255.46f, 48.74f)
                curveToRelative(-114.84f, 0f, -208f, 93.11f, -208f, 208f)
                reflectiveCurveToRelative(93.12f, 208f, 208f, 208f)
                reflectiveCurveToRelative(208f, -93.12f, 208f, -208f)
                reflectiveCurveTo(370.31f, 48.74f, 255.46f, 48.74f)
                close()
                moveTo(380.28f, 252f)
                curveToRelative(-2.85f, 32.63f, -16.79f, 49.7f, -28f, 58.26f)
                reflectiveCurveTo(327.61f, 322.58f, 316f, 320.5f)
                arcToRelative(41.61f, 41.61f, 0f, isMoreThanHalf = false, isPositiveArc = true, -26.82f, -17.19f)
                arcToRelative(62.06f, 62.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, -44f, 17.57f)
                arcToRelative(51.66f, 51.66f, 0f, isMoreThanHalf = false, isPositiveArc = true, -38.55f, -16.83f)
                curveToRelative(-11.38f, -12.42f, -17f, -30.36f, -15.32f, -49.23f)
                curveToRelative(3f, -35f, 30.91f, -57.39f, 56.87f, -61.48f)
                curveToRelative(27.2f, -4.29f, 52.23f, 6.54f, 62.9f, 19.46f)
                lineToRelative(3.85f, 4.66f)
                lineToRelative(-6.34f, 50.38f)
                curveToRelative(-1.19f, 14.34f, 3.28f, 23.48f, 12.29f, 25.1f)
                curveToRelative(2.39f, 0.42f, 8.1f, -0.13f, 14.37f, -4.93f)
                curveToRelative(6.72f, -5.15f, 15.14f, -16f, 17.1f, -38.47f)
                curveTo(354.7f, 223f, 348f, 200.35f, 333.1f, 184.05f)
                curveToRelative(-15.49f, -16.9f, -39.09f, -25.84f, -68.23f, -25.84f)
                curveToRelative(-54f, 0f, -101.81f, 44.43f, -106.58f, 99f)
                curveToRelative(-2.28f, 26.2f, 5.67f, 50.68f, 22.4f, 68.93f)
                curveTo(197.05f, 344f, 220f, 353.88f, 245.35f, 353.88f)
                curveToRelative(19f, 0f, 30.61f, -2.05f, 49.48f, -8.78f)
                arcToRelative(14f, 14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.4f, 26.38f)
                curveToRelative(-21.82f, 7.77f, -36.68f, 10.4f, -58.88f, 10.4f)
                curveToRelative(-33.28f, 0f, -63.57f, -13.06f, -85.3f, -36.77f)
                curveTo(138f, 321f, 127.42f, 288.94f, 130.4f, 254.82f)
                curveToRelative(2.91f, -33.33f, 18.45f, -64.63f, 43.77f, -88.12f)
                reflectiveCurveToRelative(57.57f, -36.49f, 90.7f, -36.49f)
                curveToRelative(37.2f, 0f, 67.93f, 12.08f, 88.87f, 34.93f)
                curveTo(373.83f, 187.05f, 383.25f, 217.89f, 380.28f, 252f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(252.57f, 221f)
                curveToRelative(-14.83f, 2.33f, -31.56f, 15.84f, -33.34f, 36.26f)
                curveToRelative(-1f, 11.06f, 2f, 21.22f, 8.07f, 27.87f)
                arcToRelative(23.65f, 23.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17.91f, 7.75f)
                curveToRelative(20.31f, 0f, 34.73f, -14.94f, 36.75f, -38.06f)
                arcToRelative(14f, 14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.34f, -2.07f)
                lineToRelative(3.2f, -25.45f)
                arcToRelative(49.61f, 49.61f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32.93f, -6.3f)
                close()
            }
        }.build()

        return _CiAtCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiAtCircle: ImageVector? = null
