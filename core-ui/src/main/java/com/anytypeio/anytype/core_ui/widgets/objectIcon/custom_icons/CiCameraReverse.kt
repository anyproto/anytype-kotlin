package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCameraReverse: ImageVector
    get() {
        if (_CiCameraReverse != null) {
            return _CiCameraReverse!!
        }
        _CiCameraReverse = ImageVector.Builder(
            name = "CiCameraReverse",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(432f, 144f)
                lineTo(373f, 144f)
                curveToRelative(-3f, 0f, -6.72f, -1.94f, -9.62f, -5f)
                lineTo(337.44f, 98.06f)
                arcToRelative(15.52f, 15.52f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.37f, -1.85f)
                curveTo(327.11f, 85.76f, 315f, 80f, 302f, 80f)
                lineTo(210f, 80f)
                curveToRelative(-13f, 0f, -25.11f, 5.76f, -34.07f, 16.21f)
                arcToRelative(15.52f, 15.52f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.37f, 1.85f)
                lineToRelative(-25.94f, 41f)
                curveToRelative(-2.22f, 2.42f, -5.34f, 5f, -8.62f, 5f)
                verticalLineToRelative(-8f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, -16f)
                lineTo(100f, 120.06f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 16f)
                verticalLineToRelative(8f)
                lineTo(80f, 144.06f)
                arcToRelative(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, -48f, 48f)
                lineTo(32f, 384f)
                arcToRelative(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 48f)
                lineTo(432f, 432f)
                arcToRelative(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, -48f)
                lineTo(480f, 192f)
                arcTo(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 432f, 144f)
                close()
                moveTo(316.84f, 346.3f)
                arcToRelative(96.06f, 96.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, -155.66f, -59.18f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16.49f, -26.43f)
                lineToRelative(20f, -20f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.62f, 0f)
                lineToRelative(20f, 20f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 196f, 288f)
                arcToRelative(17.31f, 17.31f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, -0.14f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 102.66f, 33.63f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20.21f, 24.81f)
                close()
                moveTo(367.31f, 283.3f)
                lineTo(347.31f, 303.3f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.62f, 0f)
                lineToRelative(-20f, -20f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.09f, -27.2f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 215f, 222.64f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 194.61f, 198f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = false, isPositiveArc = true, 156f, 59f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16.72f, 26.35f)
                close()
            }
        }.build()

        return _CiCameraReverse!!
    }

@Suppress("ObjectPropertyName")
private var _CiCameraReverse: ImageVector? = null
