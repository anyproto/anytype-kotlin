package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPeople: ImageVector
    get() {
        if (_CiPeople != null) {
            return _CiPeople!!
        }
        _CiPeople = ImageVector.Builder(
            name = "CiPeople",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(336f, 256f)
                curveToRelative(-20.56f, 0f, -40.44f, -9.18f, -56f, -25.84f)
                curveToRelative(-15.13f, -16.25f, -24.37f, -37.92f, -26f, -61f)
                curveToRelative(-1.74f, -24.62f, 5.77f, -47.26f, 21.14f, -63.76f)
                reflectiveCurveTo(312f, 80f, 336f, 80f)
                curveToRelative(23.83f, 0f, 45.38f, 9.06f, 60.7f, 25.52f)
                curveToRelative(15.47f, 16.62f, 23f, 39.22f, 21.26f, 63.63f)
                horizontalLineToRelative(0f)
                curveToRelative(-1.67f, 23.11f, -10.9f, 44.77f, -26f, 61f)
                curveTo(376.44f, 246.82f, 356.57f, 256f, 336f, 256f)
                close()
                moveTo(402f, 168f)
                horizontalLineToRelative(0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(467.83f, 432f)
                horizontalLineTo(204.18f)
                arcToRelative(27.71f, 27.71f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22f, -10.67f)
                arcToRelative(30.22f, 30.22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.26f, -25.79f)
                curveToRelative(8.42f, -33.81f, 29.28f, -61.85f, 60.32f, -81.08f)
                curveTo(264.79f, 297.4f, 299.86f, 288f, 336f, 288f)
                curveToRelative(36.85f, 0f, 71f, 9f, 98.71f, 26.05f)
                curveToRelative(31.11f, 19.13f, 52f, 47.33f, 60.38f, 81.55f)
                arcToRelative(30.27f, 30.27f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.32f, 25.78f)
                arcTo(27.68f, 27.68f, 0f, isMoreThanHalf = false, isPositiveArc = true, 467.83f, 432f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(147f, 260f)
                curveToRelative(-35.19f, 0f, -66.13f, -32.72f, -69f, -72.93f)
                curveTo(76.58f, 166.47f, 83f, 147.42f, 96f, 133.45f)
                curveTo(108.86f, 119.62f, 127f, 112f, 147f, 112f)
                reflectiveCurveToRelative(38f, 7.66f, 50.93f, 21.57f)
                curveToRelative(13.1f, 14.08f, 19.5f, 33.09f, 18f, 53.52f)
                curveTo(213.06f, 227.29f, 182.13f, 260f, 147f, 260f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(212.66f, 291.45f)
                curveToRelative(-17.59f, -8.6f, -40.42f, -12.9f, -65.65f, -12.9f)
                curveToRelative(-29.46f, 0f, -58.07f, 7.68f, -80.57f, 21.62f)
                curveTo(40.93f, 316f, 23.77f, 339.05f, 16.84f, 366.88f)
                arcToRelative(27.39f, 27.39f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.79f, 23.36f)
                arcTo(25.32f, 25.32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 41.72f, 400f)
                horizontalLineToRelative(111f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.87f, -6.57f)
                curveToRelative(0.11f, -0.63f, 0.25f, -1.26f, 0.41f, -1.88f)
                curveToRelative(8.48f, -34.06f, 28.35f, -62.84f, 57.71f, -83.82f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.63f, -13.39f)
                curveTo(216.51f, 293.42f, 214.71f, 292.45f, 212.66f, 291.45f)
                close()
            }
        }.build()

        return _CiPeople!!
    }

@Suppress("ObjectPropertyName")
private var _CiPeople: ImageVector? = null
