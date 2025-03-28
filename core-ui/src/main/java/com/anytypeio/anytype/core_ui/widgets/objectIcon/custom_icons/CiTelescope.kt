package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiTelescope: ImageVector
    get() {
        if (_CiTelescope != null) {
            return _CiTelescope!!
        }
        _CiTelescope = ImageVector.Builder(
            name = "CiTelescope",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(107.56f, 250f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -21.85f, -5.86f)
                lineTo(36f, 272.81f)
                arcTo(39.71f, 39.71f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17.2f, 297.72f)
                arcToRelative(40.9f, 40.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.55f, 30.35f)
                lineToRelative(4.36f, 7.54f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = false, 54.62f, 14.62f)
                lineTo(130.4f, 321.6f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.87f, -21.86f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(382.84f, 440.8f)
                lineTo(288.72f, 254f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.79f, -2.63f)
                lineToRelative(8.3f, -4.79f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.86f, -21.86f)
                lineToRelative(-47.53f, -82.33f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -21.86f, -5.87f)
                lineToRelative(-86.38f, 49.8f)
                arcToRelative(39.73f, 39.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, -18.65f, 24.28f)
                arcToRelative(34.82f, 34.82f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.37f, 9.76f)
                curveToRelative(0.06f, 7.6f, 9.2f, 22.7f, 18.12f, 38.28f)
                curveToRelative(9.59f, 16.75f, 19.24f, 33.88f, 26.34f, 38.15f)
                curveToRelative(4.52f, 2.72f, 12.5f, 4.9f, 19.21f, 4.9f)
                lineToRelative(0.84f, 0f)
                lineTo(113.07f, 473.29f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = false, 29.05f, 13.42f)
                lineTo(235.8f, 284.06f)
                arcToRelative(7.94f, 7.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.26f, -3.57f)
                lineToRelative(19.21f, -11.08f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.78f, 0.84f)
                lineToRelative(93.21f, 185f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 28.58f, -14.4f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(490.21f, 115.74f)
                lineTo(444.09f, 36f)
                arcToRelative(40.08f, 40.08f, 0f, isMoreThanHalf = false, isPositiveArc = false, -54.63f, -14.62f)
                lineTo(296.12f, 75.16f)
                arcToRelative(39.69f, 39.69f, 0f, isMoreThanHalf = false, isPositiveArc = false, -18.65f, 24.28f)
                arcToRelative(32.76f, 32.76f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.27f, 13.25f)
                curveToRelative(1.74f, 12.62f, 13f, 30.4f, 26.41f, 53.89f)
                curveToRelative(13.58f, 23.73f, 28.91f, 50.48f, 36.93f, 56.27f)
                arcToRelative(40.18f, 40.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 23.18f, 7.37f)
                arcToRelative(39.77f, 39.77f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19.92f, -5.34f)
                lineTo(476f, 171.07f)
                arcToRelative(39.72f, 39.72f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18.79f, -24.84f)
                arcTo(41f, 41f, 0f, isMoreThanHalf = false, isPositiveArc = false, 490.21f, 115.74f)
                close()
            }
        }.build()

        return _CiTelescope!!
    }

@Suppress("ObjectPropertyName")
private var _CiTelescope: ImageVector? = null
