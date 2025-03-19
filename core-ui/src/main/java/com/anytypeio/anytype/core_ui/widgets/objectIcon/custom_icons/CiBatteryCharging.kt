package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBatteryCharging: ImageVector
    get() {
        if (_CiBatteryCharging != null) {
            return _CiBatteryCharging!!
        }
        _CiBatteryCharging = ImageVector.Builder(
            name = "CiBatteryCharging",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(48f, 322.3f)
                verticalLineTo(189.7f)
                arcTo(29.74f, 29.74f, 0f, isMoreThanHalf = false, isPositiveArc = true, 77.7f, 160f)
                horizontalLineTo(215.14f)
                lineToRelative(24.4f, -32f)
                horizontalLineTo(77.7f)
                arcTo(61.77f, 61.77f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 189.7f)
                verticalLineTo(322.3f)
                arcTo(61.77f, 61.77f, 0f, isMoreThanHalf = false, isPositiveArc = false, 77.7f, 384f)
                horizontalLineToRelative(96.85f)
                arcToRelative(22.57f, 22.57f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.26f, -7.32f)
                lineToRelative(0.15f, -0.75f)
                lineToRelative(0.21f, -0.73f)
                lineToRelative(6.5f, -23.2f)
                horizontalLineTo(77.7f)
                arcTo(29.74f, 29.74f, 0f, isMoreThanHalf = false, isPositiveArc = true, 48f, 322.3f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(386.3f, 128f)
                horizontalLineTo(287.66f)
                arcToRelative(22.69f, 22.69f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.27f, 7.2f)
                lineToRelative(-0.15f, 0.74f)
                lineToRelative(-0.21f, 0.73f)
                lineTo(280.49f, 160f)
                horizontalLineTo(386.3f)
                arcTo(29.74f, 29.74f, 0f, isMoreThanHalf = false, isPositiveArc = true, 416f, 189.7f)
                verticalLineTo(322.3f)
                arcTo(29.74f, 29.74f, 0f, isMoreThanHalf = false, isPositiveArc = true, 386.3f, 352f)
                horizontalLineTo(247f)
                lineToRelative(-24.42f, 32f)
                horizontalLineTo(386.3f)
                arcTo(61.77f, 61.77f, 0f, isMoreThanHalf = false, isPositiveArc = false, 448f, 322.3f)
                verticalLineTo(189.7f)
                arcTo(61.77f, 61.77f, 0f, isMoreThanHalf = false, isPositiveArc = false, 386.3f, 128f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(162.65f, 294.16f)
                arcToRelative(24.37f, 24.37f, 0f, isMoreThanHalf = false, isPositiveArc = true, -21.56f, -13f)
                arcToRelative(25f, 25f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.42f, -25.83f)
                lineToRelative(0.31f, -0.46f)
                lineToRelative(0.33f, -0.44f)
                lineTo(197.62f, 183f)
                horizontalLineTo(89.69f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, -20f, 20f)
                verticalLineTo(309f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, 20f, 20f)
                horizontalLineToRelative(98.42f)
                lineToRelative(9.78f, -34.86f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(276.07f, 280.89f)
                lineToRelative(27.07f, -35.49f)
                arcToRelative(5.2f, 5.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.77f, -1.91f)
                arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.08f, -0.66f)
                arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.08f, -1.29f)
                arcToRelative(5.11f, 5.11f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.68f, -1.75f)
                arcToRelative(4.76f, 4.76f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.78f, -0.95f)
                arcToRelative(3.48f, 3.48f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.48f, -0.38f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.11f, -0.55f)
                arcToRelative(4.28f, 4.28f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.31f, -0.2f)
                horizontalLineTo(237.93f)
                lineToRelative(12.12f, -43.21f)
                lineTo(253.28f, 183f)
                lineToRelative(6.21f, -22.16f)
                lineTo(260f, 159f)
                lineToRelative(7.79f, -27.76f)
                horizontalLineToRelative(0f)
                arcToRelative(3.51f, 3.51f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.05f, -0.55f)
                curveToRelative(0f, -0.06f, 0f, -0.11f, 0f, -0.16f)
                reflectiveCurveToRelative(0f, -0.26f, -0.05f, -0.38f)
                reflectiveCurveToRelative(0f, -0.09f, 0f, -0.14f)
                arcToRelative(2.2f, 2.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.17f, -0.45f)
                horizontalLineToRelative(0f)
                arcToRelative(3.77f, 3.77f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.26f, -0.39f)
                lineToRelative(-0.09f, -0.1f)
                arcToRelative(2.73f, 2.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.25f, -0.23f)
                lineToRelative(-0.1f, -0.08f)
                arcToRelative(3.14f, 3.14f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.39f, -0.24f)
                horizontalLineToRelative(0f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.41f, -0.14f)
                lineToRelative(-0.13f, 0f)
                lineToRelative(-0.33f, 0f)
                horizontalLineToRelative(-0.13f)
                arcToRelative(2.3f, 2.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.45f, 0f)
                horizontalLineToRelative(0f)
                arcToRelative(1.9f, 1.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.42f, 0.15f)
                lineToRelative(-0.13f, 0.07f)
                lineToRelative(-0.3f, 0.21f)
                lineToRelative(-0.11f, 0.1f)
                arcToRelative(2.4f, 2.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.36f, 0.41f)
                horizontalLineToRelative(0f)
                lineToRelative(-18f, 23.63f)
                lineToRelative(-13.14f, 17.22f)
                lineTo(222.77f, 183f)
                lineToRelative(-63.71f, 83.55f)
                arcToRelative(5.72f, 5.72f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.44f, 0.8f)
                arcToRelative(4.78f, 4.78f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.35f, 1.09f)
                arcToRelative(4.7f, 4.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.08f, 1.29f)
                arcToRelative(4.86f, 4.86f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2f, 3.71f)
                arcToRelative(4.74f, 4.74f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.54f, 0.31f)
                arcToRelative(4.31f, 4.31f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.89f, 0.43f)
                horizontalLineToRelative(61.62f)
                lineTo(194.42f, 380.6f)
                arcToRelative(3.64f, 3.64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 0.56f)
                reflectiveCurveToRelative(0f, 0.1f, 0f, 0.15f)
                arcToRelative(2.32f, 2.32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.06f, 0.38f)
                arcToRelative(0.58f, 0.58f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 0.14f)
                arcToRelative(2.2f, 2.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.17f, 0.45f)
                horizontalLineToRelative(0f)
                arcToRelative(3.62f, 3.62f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.26f, 0.38f)
                lineToRelative(0.09f, 0.1f)
                lineToRelative(0.25f, 0.24f)
                arcToRelative(0.39f, 0.39f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.1f, 0.08f)
                arcToRelative(2.22f, 2.22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.39f, 0.23f)
                horizontalLineToRelative(0f)
                arcToRelative(2.83f, 2.83f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.41f, 0.14f)
                lineToRelative(0.13f, 0f)
                arcToRelative(1.86f, 1.86f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.33f, 0f)
                horizontalLineToRelative(0.13f)
                arcToRelative(2.32f, 2.32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.45f, -0.06f)
                horizontalLineToRelative(0f)
                arcToRelative(2.05f, 2.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.41f, -0.16f)
                lineToRelative(0.13f, -0.07f)
                lineToRelative(0.3f, -0.21f)
                lineToRelative(0.11f, -0.09f)
                arcToRelative(2.4f, 2.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.36f, -0.41f)
                horizontalLineToRelative(0f)
                lineTo(221.82f, 352f)
                lineToRelative(17.53f, -23f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(319.5f, 256.93f)
                lineToRelative(-0.46f, 0.6f)
                lineTo(264.51f, 329f)
                horizontalLineToRelative(109.8f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, 20f, -20f)
                verticalLineTo(203f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, -20f, -20f)
                horizontalLineTo(274.05f)
                lineToRelative(-9.74f, 34.73f)
                horizontalLineToRelative(35.24f)
                arcTo(24.35f, 24.35f, 0f, isMoreThanHalf = false, isPositiveArc = true, 321f, 230.5f)
                arcToRelative(25.21f, 25.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, 25.79f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(480f, 202.67f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 16f)
                verticalLineToRelative(74.66f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 0f)
                verticalLineTo(218.67f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 480f, 202.67f)
                close()
            }
        }.build()

        return _CiBatteryCharging!!
    }

@Suppress("ObjectPropertyName")
private var _CiBatteryCharging: ImageVector? = null
