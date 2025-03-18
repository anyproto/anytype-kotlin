package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiVolumeMute: ImageVector
    get() {
        if (_CiVolumeMute != null) {
            return _CiVolumeMute!!
        }
        _CiVolumeMute = ImageVector.Builder(
            name = "CiVolumeMute",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(416f, 432f)
                lineTo(64f, 80f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(243.33f, 98.86f)
                arcToRelative(23.89f, 23.89f, 0f, isMoreThanHalf = false, isPositiveArc = false, -25.55f, 1.82f)
                lineToRelative(-0.66f, 0.51f)
                lineTo(188.6f, 124.54f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.59f, 11.85f)
                lineToRelative(54.33f, 54.33f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 185.06f)
                verticalLineTo(120.57f)
                arcTo(24.51f, 24.51f, 0f, isMoreThanHalf = false, isPositiveArc = false, 243.33f, 98.86f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(251.33f, 335.29f)
                lineTo(96.69f, 180.69f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 85.38f, 176f)
                horizontalLineTo(56f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, -24f, 24f)
                verticalLineTo(312f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24f, 24f)
                horizontalLineToRelative(69.76f)
                lineToRelative(92f, 75.31f)
                arcTo(23.9f, 23.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 243.63f, 413f)
                arcTo(24.51f, 24.51f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 391.45f)
                verticalLineTo(346.59f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 251.33f, 335.29f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(352f, 256f)
                curveToRelative(0f, -24.56f, -5.81f, -47.87f, -17.75f, -71.27f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = false, -28.5f, 14.55f)
                curveTo(315.34f, 218.06f, 320f, 236.62f, 320f, 256f)
                quadToRelative(0f, 4f, -0.31f, 8.13f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.32f, 6.25f)
                lineToRelative(14.36f, 14.36f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.55f, -4.31f)
                arcTo(146f, 146f, 0f, isMoreThanHalf = false, isPositiveArc = false, 352f, 256f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(416f, 256f)
                curveToRelative(0f, -51.18f, -13.08f, -83.89f, -34.18f, -120.06f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -27.64f, 16.12f)
                curveTo(373.07f, 184.44f, 384f, 211.83f, 384f, 256f)
                curveToRelative(0f, 23.83f, -3.29f, 42.88f, -9.37f, 60.65f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.9f, 8.26f)
                lineTo(389f, 337.4f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.13f, -2.79f)
                curveTo(411f, 311.76f, 416f, 287.26f, 416f, 256f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(480f, 256f)
                curveToRelative(0f, -74.25f, -20.19f, -121.11f, -50.51f, -168.61f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = false, -27f, 17.22f)
                curveTo(429.82f, 147.38f, 448f, 189.5f, 448f, 256f)
                curveToRelative(0f, 46.19f, -8.43f, 80.27f, -22.43f, 110.53f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.59f, 9f)
                lineToRelative(11.92f, 11.92f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 452f, 385.29f)
                curveTo(471.6f, 344.9f, 480f, 305f, 480f, 256f)
                close()
            }
        }.build()

        return _CiVolumeMute!!
    }

@Suppress("ObjectPropertyName")
private var _CiVolumeMute: ImageVector? = null
