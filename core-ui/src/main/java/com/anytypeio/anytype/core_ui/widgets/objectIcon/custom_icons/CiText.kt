package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiText: ImageVector
    get() {
        if (_CiText != null) {
            return _CiText!!
        }
        _CiText = ImageVector.Builder(
            name = "CiText",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(292.6f, 407.78f)
                lineToRelative(-120f, -320f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, -41.2f, 0f)
                lineToRelative(-120f, 320f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 41.2f, 15.44f)
                lineTo(88.76f, 326.8f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.87f, -1.3f)
                lineTo(213.37f, 325.5f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.87f, 1.3f)
                lineToRelative(36.16f, 96.42f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 41.2f, -15.44f)
                close()
                moveTo(106.76f, 278.78f)
                lineTo(150.13f, 163.13f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.74f, 0f)
                lineTo(197.24f, 278.8f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.87f, 2.7f)
                lineTo(108.63f, 281.5f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 106.76f, 278.8f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(400.77f, 169.5f)
                curveToRelative(-41.72f, -0.3f, -79.08f, 23.87f, -95f, 61.4f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 40.5f, 17.2f)
                curveToRelative(8.88f, -20.89f, 29.77f, -34.44f, 53.32f, -34.6f)
                curveTo(431.91f, 213.28f, 458f, 240f, 458f, 272.35f)
                horizontalLineToRelative(0f)
                arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.45f, 1.5f)
                curveToRelative(-21.92f, 0.61f, -47.92f, 2.07f, -71.12f, 4.8f)
                curveTo(330.68f, 285.09f, 298f, 314.94f, 298f, 358.5f)
                curveToRelative(0f, 23.19f, 8.76f, 44f, 24.67f, 58.68f)
                curveTo(337.6f, 430.93f, 358f, 438.5f, 380f, 438.5f)
                curveToRelative(31f, 0f, 57.69f, -8f, 77.94f, -23.22f)
                curveToRelative(0f, 0f, 0.06f, 0f, 0.06f, 0f)
                horizontalLineToRelative(0f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = true, isPositiveArc = false, 44f, 0.19f)
                verticalLineToRelative(-143f)
                curveTo(502f, 216.29f, 457f, 169.91f, 400.77f, 169.5f)
                close()
                moveTo(380f, 394.5f)
                curveToRelative(-17.53f, 0f, -38f, -9.43f, -38f, -36f)
                curveToRelative(0f, -10.67f, 3.83f, -18.14f, 12.43f, -24.23f)
                curveToRelative(8.37f, -5.93f, 21.2f, -10.16f, 36.14f, -11.92f)
                curveToRelative(21.12f, -2.49f, 44.82f, -3.86f, 65.14f, -4.47f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 2.1f)
                curveTo(455f, 370.1f, 429.46f, 394.5f, 380f, 394.5f)
                close()
            }
        }.build()

        return _CiText!!
    }

@Suppress("ObjectPropertyName")
private var _CiText: ImageVector? = null
