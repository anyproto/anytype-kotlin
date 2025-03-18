package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPlanet: ImageVector
    get() {
        if (_CiPlanet != null) {
            return _CiPlanet!!
        }
        _CiPlanet = ImageVector.Builder(
            name = "CiPlanet",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(96.85f, 286.62f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -12.53f, 8.25f)
                curveTo(102.07f, 373.28f, 172.3f, 432f, 256f, 432f)
                arcToRelative(175.31f, 175.31f, 0f, isMoreThanHalf = false, isPositiveArc = false, 52.41f, -8f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.79f, -15f)
                arcToRelative(1120f, 1120f, 0f, isMoreThanHalf = false, isPositiveArc = true, -109.48f, -55.61f)
                arcTo(1126.24f, 1126.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 96.85f, 286.62f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(492.72f, 339.51f)
                curveToRelative(-4.19f, -5.58f, -9.11f, -11.44f, -14.7f, -17.53f)
                arcToRelative(15.83f, 15.83f, 0f, isMoreThanHalf = false, isPositiveArc = false, -26.56f, 5.13f)
                curveToRelative(0f, 0.16f, -0.11f, 0.31f, -0.17f, 0.47f)
                arcToRelative(15.75f, 15.75f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.15f, 16.06f)
                curveToRelative(22.74f, 25f, 26.42f, 38.51f, 25.48f, 41.36f)
                curveToRelative(-2f, 2.23f, -17.05f, 6.89f, -58.15f, -3.53f)
                quadToRelative(-8.83f, -2.24f, -19.32f, -5.46f)
                quadToRelative(-6.76f, -2.08f, -13.79f, -4.49f)
                horizontalLineToRelative(0f)
                arcToRelative(176.76f, 176.76f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19.54f, -27.25f)
                curveToRelative(0.17f, -0.29f, 0.35f, -0.58f, 0.52f, -0.88f)
                arcTo(175.39f, 175.39f, 0f, isMoreThanHalf = false, isPositiveArc = false, 432f, 256f)
                arcTo(178.87f, 178.87f, 0f, isMoreThanHalf = false, isPositiveArc = false, 431f, 237f)
                curveTo(421.43f, 148.83f, 346.6f, 80f, 256f, 80f)
                arcTo(175.37f, 175.37f, 0f, isMoreThanHalf = false, isPositiveArc = false, 149.6f, 115.89f)
                arcToRelative(177.4f, 177.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -45.83f, 51.84f)
                curveToRelative(-0.16f, 0.29f, -0.34f, 0.58f, -0.51f, 0.87f)
                arcToRelative(175.48f, 175.48f, 0f, isMoreThanHalf = false, isPositiveArc = false, -13.83f, 30.52f)
                quadToRelative(-5.59f, -4.87f, -10.79f, -9.67f)
                curveToRelative(-5.39f, -5f, -10.17f, -9.63f, -14.42f, -14f)
                curveTo(34.65f, 145.19f, 31.13f, 129.84f, 32.06f, 127f)
                curveToRelative(2f, -2.23f, 15.54f, -5.87f, 48.62f, 1.31f)
                arcTo(15.82f, 15.82f, 0f, isMoreThanHalf = false, isPositiveArc = false, 96.22f, 123f)
                lineToRelative(0.36f, -0.44f)
                arcToRelative(15.74f, 15.74f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8.67f, -25.43f)
                arcTo(237.38f, 237.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64.13f, 93f)
                curveTo(33.41f, 89.47f, 13.3f, 95.52f, 4.35f, 111f)
                curveTo(1.11f, 116.58f, -2f, 126.09f, 1.63f, 139.6f)
                curveTo(7f, 159.66f, 26.14f, 184f, 53.23f, 209.5f)
                curveToRelative(8.63f, 8.13f, 18.06f, 16.37f, 28.12f, 24.64f)
                curveToRelative(7.32f, 6f, 15f, 12.06f, 22.9f, 18.08f)
                quadToRelative(7.91f, 6f, 16.15f, 12f)
                reflectiveQuadTo(137.1f, 276f)
                curveToRelative(25.41f, 17.61f, 52.26f, 34.52f, 78.59f, 49.69f)
                quadToRelative(14.34f, 8.26f, 28.64f, 16f)
                reflectiveQuadToRelative(28.37f, 14.81f)
                curveToRelative(21.9f, 11f, 43.35f, 20.92f, 63.86f, 29.43f)
                quadToRelative(13.19f, 5.48f, 25.81f, 10.16f)
                curveToRelative(11.89f, 4.42f, 23.37f, 8.31f, 34.31f, 11.59f)
                lineToRelative(1.1f, 0.33f)
                curveToRelative(25.73f, 7.66f, 47.42f, 11.69f, 64.48f, 12f)
                horizontalLineTo(464f)
                curveToRelative(21.64f, 0f, 36.3f, -6.38f, 43.58f, -19f)
                curveTo(516.67f, 385.39f, 511.66f, 364.69f, 492.72f, 339.51f)
                close()
            }
        }.build()

        return _CiPlanet!!
    }

@Suppress("ObjectPropertyName")
private var _CiPlanet: ImageVector? = null
