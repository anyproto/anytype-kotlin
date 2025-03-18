package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiVolumeLow: ImageVector
    get() {
        if (_CiVolumeLow != null) {
            return _CiVolumeLow!!
        }
        _CiVolumeLow = ImageVector.Builder(
            name = "CiVolumeLow",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(296f, 416.19f)
                arcToRelative(23.92f, 23.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.21f, -4.69f)
                lineToRelative(-0.66f, -0.51f)
                lineToRelative(-91.46f, -75f)
                horizontalLineTo(120f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, -24f)
                verticalLineTo(200f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, -24f)
                horizontalLineToRelative(69.65f)
                lineToRelative(91.46f, -75f)
                lineToRelative(0.66f, -0.51f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 320f, 119.83f)
                verticalLineTo(392.17f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, 24f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(384f, 336f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.29f, -23.18f)
                curveToRelative(9.49f, -18.9f, 14.3f, -38f, 14.3f, -56.82f)
                curveToRelative(0f, -19.36f, -4.66f, -37.92f, -14.25f, -56.73f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.5f, -14.54f)
                curveTo(410.2f, 208.16f, 416f, 231.47f, 416f, 256f)
                curveToRelative(0f, 23.83f, -6f, 47.78f, -17.7f, 71.18f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 384f, 336f)
                close()
            }
        }.build()

        return _CiVolumeLow!!
    }

@Suppress("ObjectPropertyName")
private var _CiVolumeLow: ImageVector? = null
