package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFlashOff: ImageVector
    get() {
        if (_CiFlashOff != null) {
            return _CiFlashOff!!
        }
        _CiFlashOff = ImageVector.Builder(
            name = "CiFlashOff",
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
                moveTo(431.5f, 204f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 416f, 192f)
                horizontalLineTo(307.19f)
                lineTo(335.4f, 37.63f)
                curveToRelative(0.05f, -0.3f, 0.1f, -0.59f, 0.13f, -0.89f)
                arcTo(18.45f, 18.45f, 0f, isMoreThanHalf = false, isPositiveArc = false, 302.73f, 23f)
                lineTo(210.15f, 137.46f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.29f, 5.35f)
                lineToRelative(151f, 151f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.94f, -0.31f)
                lineToRelative(60.8f, -75.16f)
                arcTo(16.37f, 16.37f, 0f, isMoreThanHalf = false, isPositiveArc = false, 431.5f, 204f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(301.57f, 369.19f)
                lineToRelative(-151f, -151f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.93f, 0.31f)
                lineTo(83.8f, 293.64f)
                arcTo(16.37f, 16.37f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80.5f, 308f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 96f, 320f)
                horizontalLineTo(204.83f)
                lineTo(176.74f, 474.36f)
                lineToRelative(0f, 0.11f)
                arcTo(18.37f, 18.37f, 0f, isMoreThanHalf = false, isPositiveArc = false, 209.24f, 489f)
                lineToRelative(92.61f, -114.46f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 301.57f, 369.19f)
                close()
            }
        }.build()

        return _CiFlashOff!!
    }

@Suppress("ObjectPropertyName")
private var _CiFlashOff: ImageVector? = null
