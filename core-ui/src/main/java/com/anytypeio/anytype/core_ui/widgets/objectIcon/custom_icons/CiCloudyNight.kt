package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCloudyNight: ImageVector
    get() {
        if (_CiCloudyNight != null) {
            return _CiCloudyNight!!
        }
        _CiCloudyNight = ImageVector.Builder(
            name = "CiCloudyNight",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(340f, 480f)
                horizontalLineTo(106f)
                curveToRelative(-29.5f, 0f, -54.92f, -7.83f, -73.53f, -22.64f)
                curveTo(11.23f, 440.44f, 0f, 415.35f, 0f, 384.8f)
                curveToRelative(0f, -29.44f, 12.09f, -54.25f, 35f, -71.74f)
                curveToRelative(12.1f, -9.26f, 27.2f, -16.17f, 43.33f, -20.05f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 90.14f, 280.8f)
                curveToRelative(7.15f, -32.54f, 22.25f, -60.49f, 44.33f, -81.75f)
                arcTo(139.82f, 139.82f, 0f, isMoreThanHalf = false, isPositiveArc = true, 232f, 160f)
                curveToRelative(32.33f, 0f, 62.15f, 10.65f, 86.24f, 30.79f)
                arcToRelative(142.22f, 142.22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 37.65f, 49.54f)
                arcToRelative(16.06f, 16.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 11.12f, 9f)
                curveToRelative(24f, 5.22f, 45.42f, 15.78f, 61.62f, 30.56f)
                curveTo(451.77f, 301f, 464f, 329.82f, 464f, 363.2f)
                curveToRelative(0f, 32.85f, -13.13f, 62.87f, -37f, 84.52f)
                curveTo(404.11f, 468.54f, 373.2f, 480f, 340f, 480f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(510.53f, 209.79f)
                arcToRelative(16.34f, 16.34f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.35f, -15.8f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -19.57f, -5.58f)
                curveToRelative(-10.7f, 4.65f, -24.48f, 7.17f, -39.92f, 7.28f)
                curveToRelative(-55.3f, 0.4f, -101.38f, -45f, -101.38f, -100.31f)
                curveToRelative(0f, -15.75f, 2.48f, -29.84f, 7.18f, -40.76f)
                arcToRelative(16.3f, 16.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.85f, -16.33f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -19.1f, -5f)
                curveToRelative(-38.63f, 16.82f, -66.18f, 51.51f, -75.27f, 92.54f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.19f, 4.79f)
                arcToRelative(162.54f, 162.54f, 0f, isMoreThanHalf = false, isPositiveArc = true, 76.31f, 35.59f)
                arcToRelative(172.58f, 172.58f, 0f, isMoreThanHalf = false, isPositiveArc = true, 39.64f, 47.84f)
                arcToRelative(16.35f, 16.35f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.54f, 7.64f)
                curveToRelative(23.89f, 7.17f, 45.1f, 18.9f, 62.25f, 34.54f)
                quadToRelative(4.44f, 4.07f, 8.48f, 8.42f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.16f, 0.57f)
                arcTo(129.12f, 129.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 510.53f, 209.79f)
                close()
            }
        }.build()

        return _CiCloudyNight!!
    }

@Suppress("ObjectPropertyName")
private var _CiCloudyNight: ImageVector? = null
