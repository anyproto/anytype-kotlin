package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiEyedrop: ImageVector
    get() {
        if (_CiEyedrop != null) {
            return _CiEyedrop!!
        }
        _CiEyedrop = ImageVector.Builder(
            name = "CiEyedrop",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(461.05f, 51f)
                arcToRelative(65f, 65f, 0f, isMoreThanHalf = false, isPositiveArc = false, -45.71f, -19f)
                horizontalLineToRelative(-0.76f)
                arcToRelative(61.81f, 61.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, -44.36f, 19.25f)
                arcToRelative(12.81f, 12.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.07f, 1.25f)
                lineToRelative(-54f, 69.76f)
                curveToRelative(-5.62f, 7.1f, -12.74f, 8.68f, -16.78f, 4.64f)
                lineTo(296.47f, 125f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, -67.92f, 67.92f)
                lineToRelative(9.91f, 9.91f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 2.83f)
                lineTo(58.7f, 385.38f)
                curveTo(54f, 390.05f, 46.9f, 399.85f, 38.85f, 431f)
                curveToRelative(-4.06f, 15.71f, -6.51f, 29.66f, -6.61f, 30.24f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 480f)
                arcToRelative(15.68f, 15.68f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.64f, -0.22f)
                curveToRelative(0.58f, -0.1f, 14.44f, -2.43f, 30.13f, -6.44f)
                curveToRelative(31.07f, -7.94f, 41.05f, -15.24f, 45.85f, -20f)
                lineTo(306.39f, 273.55f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.82f, 0f)
                lineToRelative(9.92f, 9.92f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 67.92f, -67.93f)
                lineTo(385.46f, 214f)
                curveToRelative(-5f, -5f, -2.52f, -12.11f, 4.32f, -17.14f)
                lineToRelative(69.75f, -53.94f)
                arcTo(17.82f, 17.82f, 0f, isMoreThanHalf = false, isPositiveArc = false, 461f, 141.6f)
                arcToRelative(63.2f, 63.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19f, -45f)
                arcTo(63.88f, 63.88f, 0f, isMoreThanHalf = false, isPositiveArc = false, 461.05f, 51f)
                close()
                moveTo(250.78f, 283.9f)
                curveToRelative(-2.92f, 2.92f, -16.18f, 7.92f, -23.39f, 0.71f)
                reflectiveCurveToRelative(-2.24f, -20.42f, 0.69f, -23.35f)
                lineToRelative(33f, -33f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.83f, 0f)
                lineToRelative(19.84f, 19.83f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 2.83f)
                close()
            }
        }.build()

        return _CiEyedrop!!
    }

@Suppress("ObjectPropertyName")
private var _CiEyedrop: ImageVector? = null
