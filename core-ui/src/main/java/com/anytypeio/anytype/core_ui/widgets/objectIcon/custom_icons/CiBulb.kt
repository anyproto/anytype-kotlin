package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBulb: ImageVector
    get() {
        if (_CiBulb != null) {
            return _CiBulb!!
        }
        _CiBulb = ImageVector.Builder(
            name = "CiBulb",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(288f, 464f)
                horizontalLineTo(224f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineToRelative(64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(304f, 416f)
                horizontalLineTo(208f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineToRelative(96f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(369.42f, 62.69f)
                curveTo(339.35f, 32.58f, 299.07f, 16f, 256f, 16f)
                arcTo(159.62f, 159.62f, 0f, isMoreThanHalf = false, isPositiveArc = false, 96f, 176f)
                curveToRelative(0f, 46.62f, 17.87f, 90.23f, 49f, 119.64f)
                lineToRelative(4.36f, 4.09f)
                curveTo(167.37f, 316.57f, 192f, 339.64f, 192f, 360f)
                verticalLineToRelative(24f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                horizontalLineToRelative(24f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, -8f)
                verticalLineTo(274.82f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.13f, -7.47f)
                arcTo(130.73f, 130.73f, 0f, isMoreThanHalf = false, isPositiveArc = true, 208.71f, 253f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 227.29f, 227f)
                curveToRelative(7.4f, 5.24f, 21.65f, 13f, 28.71f, 13f)
                reflectiveCurveToRelative(21.31f, -7.78f, 28.73f, -13f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 303.29f, 253f)
                arcToRelative(130.73f, 130.73f, 0f, isMoreThanHalf = false, isPositiveArc = true, -26.16f, 14.32f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.13f, 7.47f)
                verticalLineTo(392f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 8f)
                horizontalLineToRelative(24f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, -16f)
                verticalLineTo(360f)
                curveToRelative(0f, -19.88f, 24.36f, -42.93f, 42.15f, -59.77f)
                lineToRelative(4.91f, -4.66f)
                curveTo(399.08f, 265f, 416f, 223.61f, 416f, 176f)
                arcTo(159.16f, 159.16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 369.42f, 62.69f)
                close()
            }
        }.build()

        return _CiBulb!!
    }

@Suppress("ObjectPropertyName")
private var _CiBulb: ImageVector? = null
