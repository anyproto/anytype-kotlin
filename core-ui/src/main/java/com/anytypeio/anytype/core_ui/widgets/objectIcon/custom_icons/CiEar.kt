package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiEar: ImageVector
    get() {
        if (_CiEar != null) {
            return _CiEar!!
        }
        _CiEar = ImageVector.Builder(
            name = "CiEar",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 16f)
                curveTo(159f, 16f, 80f, 95f, 80f, 192f)
                lineTo(80f, 398.57f)
                arcToRelative(97.59f, 97.59f, 0f, isMoreThanHalf = false, isPositiveArc = false, 28f, 68.49f)
                arcTo(94.51f, 94.51f, 0f, isMoreThanHalf = false, isPositiveArc = false, 176f, 496f)
                curveToRelative(36.86f, 0f, 67.18f, -15.62f, 90.12f, -46.42f)
                curveToRelative(4.48f, -6f, 9.55f, -14.74f, 15.42f, -24.85f)
                curveToRelative(15.32f, -26.37f, 36.29f, -62.47f, 63.17f, -80.74f)
                curveToRelative(25.77f, -17.51f, 47.23f, -39.54f, 62f, -63.72f)
                curveTo(423.51f, 252.94f, 432f, 223.24f, 432f, 192f)
                curveTo(432f, 95f, 353.05f, 16f, 256f, 16f)
                close()
                moveTo(352f, 200f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, -16f)
                curveToRelative(0f, -39.7f, -35.89f, -72f, -80f, -72f)
                reflectiveCurveToRelative(-80f, 32.3f, -80f, 72f)
                verticalLineToRelative(30.42f)
                curveToRelative(27.19f, -7.84f, 58.4f, -6.72f, 64.28f, -6.42f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 38.6f, 75.9f)
                curveToRelative(-0.3f, 0.41f, -0.61f, 0.81f, -0.95f, 1.2f)
                curveToRelative(-16.55f, 19f, -36f, 45.48f, -38.46f, 55f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -30.94f, -8.14f)
                curveToRelative(5.51f, -20.94f, 36.93f, -58.2f, 44.66f, -67.15f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 239.82f, 240f)
                lineToRelative(-0.88f, 0f)
                curveToRelative(-16.6f, -0.89f, -45.89f, 0.8f, -62.94f, 8.31f)
                lineTo(176f, 304f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 0f)
                lineTo(144f, 184f)
                curveToRelative(0f, -57.35f, 50.24f, -104f, 112f, -104f)
                reflectiveCurveToRelative(112f, 46.65f, 112f, 104f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 352f, 200f)
                close()
            }
        }.build()

        return _CiEar!!
    }

@Suppress("ObjectPropertyName")
private var _CiEar: ImageVector? = null
