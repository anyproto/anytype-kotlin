package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiVolumeHigh: ImageVector
    get() {
        if (_CiVolumeHigh != null) {
            return _CiVolumeHigh!!
        }
        _CiVolumeHigh = ImageVector.Builder(
            name = "CiVolumeHigh",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(232f, 416f)
                arcToRelative(23.88f, 23.88f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.2f, -4.68f)
                arcToRelative(8.27f, 8.27f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.66f, -0.51f)
                lineTo(125.76f, 336f)
                lineTo(56f, 336f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, -24f)
                lineTo(32f, 200f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, -24f)
                horizontalLineToRelative(69.75f)
                lineToRelative(91.37f, -74.81f)
                arcToRelative(8.27f, 8.27f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.66f, -0.51f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 120f)
                lineTo(256f, 392f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, 24f)
                close()
                moveTo(125.82f, 336f)
                close()
                moveTo(125.55f, 176.14f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(320f, 336f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.29f, -23.19f)
                curveToRelative(9.49f, -18.87f, 14.3f, -38f, 14.3f, -56.81f)
                curveToRelative(0f, -19.38f, -4.66f, -37.94f, -14.25f, -56.73f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.5f, -14.54f)
                curveTo(346.19f, 208.12f, 352f, 231.44f, 352f, 256f)
                curveToRelative(0f, 23.86f, -6f, 47.81f, -17.7f, 71.19f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 320f, 336f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(368f, 384f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.86f, -24f)
                curveTo(373.05f, 327.09f, 384f, 299.51f, 384f, 256f)
                curveToRelative(0f, -44.17f, -10.93f, -71.56f, -29.82f, -103.94f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 27.64f, -16.12f)
                curveTo(402.92f, 172.11f, 416f, 204.81f, 416f, 256f)
                curveToRelative(0f, 50.43f, -13.06f, 83.29f, -34.13f, 120f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 368f, 384f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(416f, 432f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.39f, -24.74f)
                curveTo(429.85f, 365.47f, 448f, 323.76f, 448f, 256f)
                curveToRelative(0f, -66.5f, -18.18f, -108.62f, -45.49f, -151.39f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 27f, -17.22f)
                curveTo(459.81f, 134.89f, 480f, 181.74f, 480f, 256f)
                curveToRelative(0f, 64.75f, -14.66f, 113.63f, -50.6f, 168.74f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 416f, 432f)
                close()
            }
        }.build()

        return _CiVolumeHigh!!
    }

@Suppress("ObjectPropertyName")
private var _CiVolumeHigh: ImageVector? = null
