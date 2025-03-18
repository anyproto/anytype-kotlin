package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiIceCream: ImageVector
    get() {
        if (_CiIceCream != null) {
            return _CiIceCream!!
        }
        _CiIceCream = ImageVector.Builder(
            name = "CiIceCream",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(183f, 352f)
                curveToRelative(-21.84f, -0.52f, -39f, -18.9f, -39f, -40.74f)
                verticalLineTo(277.19f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6f, -7.74f)
                curveTo(104.25f, 260.6f, 80f, 229.74f, 80f, 192f)
                arcToRelative(80.14f, 80.14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 66.27f, -78.82f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.62f, -6.83f)
                arcToRelative(104f, 104f, 0f, isMoreThanHalf = false, isPositiveArc = true, 206.22f, 0f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.62f, 6.83f)
                arcTo(80f, 80f, 0f, isMoreThanHalf = false, isPositiveArc = true, 352f, 272f)
                arcToRelative(74.33f, 74.33f, 0f, isMoreThanHalf = false, isPositiveArc = true, -47.45f, -17.41f)
                arcToRelative(7.93f, 7.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, -9.92f, -0.14f)
                arcTo(62.89f, 62.89f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 268f)
                arcToRelative(80.47f, 80.47f, 0f, isMoreThanHalf = false, isPositiveArc = true, -21.8f, -3.18f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -10.2f, 7.69f)
                verticalLineTo(312f)
                arcTo(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, 183f, 352f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(263.39f, 299.7f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -7.39f, 7.91f)
                verticalLineTo(312f)
                arcToRelative(72.11f, 72.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, -50.69f, 68.76f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4.91f, 10.78f)
                lineToRelative(40.91f, 94.8f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 496f)
                horizontalLineToRelative(0f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 14.69f, -9.7f)
                lineToRelative(73.78f, -172.15f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.2f, -11.07f)
                arcToRelative(106.31f, 106.31f, 0f, isMoreThanHalf = false, isPositiveArc = true, -35.9f, -11.59f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -7.13f, -0.2f)
                arcTo(95f, 95f, 0f, isMoreThanHalf = false, isPositiveArc = true, 263.39f, 299.7f)
                close()
            }
        }.build()

        return _CiIceCream!!
    }

@Suppress("ObjectPropertyName")
private var _CiIceCream: ImageVector? = null
