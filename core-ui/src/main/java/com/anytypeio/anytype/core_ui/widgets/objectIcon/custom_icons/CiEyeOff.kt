package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiEyeOff: ImageVector
    get() {
        if (_CiEyeOff != null) {
            return _CiEyeOff!!
        }
        _CiEyeOff = ImageVector.Builder(
            name = "CiEyeOff",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(432f, 448f)
                arcToRelative(15.92f, 15.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.31f, -4.69f)
                lineToRelative(-352f, -352f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 91.31f, 68.69f)
                lineToRelative(352f, 352f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 432f, 448f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(248f, 315.85f)
                lineToRelative(-51.79f, -51.79f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.39f, 1.69f)
                arcToRelative(64.11f, 64.11f, 0f, isMoreThanHalf = false, isPositiveArc = false, 53.49f, 53.49f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 248f, 315.85f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(264f, 196.15f)
                lineTo(315.87f, 248f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.4f, -1.69f)
                arcToRelative(64.13f, 64.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -53.55f, -53.55f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 264f, 196.15f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(491f, 273.36f)
                arcToRelative(32.2f, 32.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.1f, -34.76f)
                curveToRelative(-26.46f, -40.92f, -60.79f, -75.68f, -99.27f, -100.53f)
                curveTo(349f, 110.55f, 302f, 96f, 255.68f, 96f)
                arcToRelative(226.54f, 226.54f, 0f, isMoreThanHalf = false, isPositiveArc = false, -71.82f, 11.79f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.56f, 6.63f)
                lineToRelative(47.24f, 47.24f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.82f, 1.05f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = false, isPositiveArc = true, 116f, 116f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.05f, 3.81f)
                lineToRelative(67.95f, 68f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.4f, 0.24f)
                arcTo(343.81f, 343.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, 491f, 273.36f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 352f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = false, isPositiveArc = true, -93.3f, -118.63f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.05f, -3.81f)
                lineTo(94.81f, 162.69f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.41f, -0.23f)
                curveToRelative(-24.39f, 20.81f, -47f, 46.13f, -67.67f, 75.72f)
                arcToRelative(31.92f, 31.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.64f, 35.54f)
                curveToRelative(26.41f, 41.33f, 60.39f, 76.14f, 98.28f, 100.65f)
                curveTo(162.06f, 402f, 207.92f, 416f, 255.68f, 416f)
                arcToRelative(238.22f, 238.22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 72.64f, -11.55f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.61f, -6.64f)
                lineToRelative(-47.47f, -47.46f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.81f, -1.05f)
                arcTo(96f, 96f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 352f)
                close()
            }
        }.build()

        return _CiEyeOff!!
    }

@Suppress("ObjectPropertyName")
private var _CiEyeOff: ImageVector? = null
