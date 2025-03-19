package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFootsteps: ImageVector
    get() {
        if (_CiFootsteps != null) {
            return _CiFootsteps!!
        }
        _CiFootsteps = ImageVector.Builder(
            name = "CiFootsteps",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(133.83f, 361.27f)
                curveToRelative(-22.61f, 0f, -41f, -8.17f, -54.79f, -24.39f)
                reflectiveCurveTo(56.2f, 296.59f, 50.93f, 261.57f)
                curveToRelative(-7.76f, -51.61f, -0.06f, -95.11f, 21.68f, -122.48f)
                curveToRelative(12.8f, -16.12f, 29.6f, -25.44f, 48.58f, -26.94f)
                curveToRelative(16.25f, -1.3f, 40.54f, 5.29f, 64f, 44f)
                curveToRelative(14.69f, 24.24f, 25.86f, 56.44f, 30.65f, 88.34f)
                horizontalLineToRelative(0f)
                curveToRelative(5.79f, 38.51f, 1.48f, 66.86f, -13.18f, 86.65f)
                curveToRelative(-11.64f, 15.72f, -29.54f, 25.46f, -53.21f, 29f)
                arcTo(106.46f, 106.46f, 0f, isMoreThanHalf = false, isPositiveArc = true, 133.83f, 361.27f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(173f, 496f)
                curveToRelative(-13.21f, 0f, -26.6f, -4.23f, -38.66f, -12.36f)
                arcToRelative(79.79f, 79.79f, 0f, isMoreThanHalf = false, isPositiveArc = true, -33.52f, -50.6f)
                curveToRelative(-2.85f, -14.66f, -1.14f, -26.31f, 5.22f, -35.64f)
                curveToRelative(10.33f, -15.15f, 28.87f, -18.56f, 48.49f, -22.18f)
                curveToRelative(2.07f, -0.38f, 4.17f, -0.76f, 6.3f, -1.17f)
                curveToRelative(4.52f, -0.86f, 9.14f, -2f, 13.62f, -3.11f)
                curveToRelative(16.78f, -4.14f, 34.14f, -8.43f, 48.47f, 1.75f)
                curveToRelative(9.59f, 6.8f, 15f, 18.36f, 16.62f, 35.32f)
                horizontalLineToRelative(0f)
                curveToRelative(1.84f, 19.57f, -2.36f, 39.1f, -11.83f, 55f)
                curveToRelative(-10.19f, 17.11f, -25.47f, 28.42f, -43f, 31.86f)
                arcTo(61f, 61f, 0f, isMoreThanHalf = false, isPositiveArc = true, 173f, 496f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(378.17f, 265.27f)
                arcToRelative(106.69f, 106.69f, 0f, isMoreThanHalf = false, isPositiveArc = true, -15.6f, -1.2f)
                curveToRelative(-23.66f, -3.5f, -41.56f, -13.25f, -53.2f, -29f)
                curveToRelative(-14.66f, -19.79f, -19f, -48.13f, -13.18f, -86.65f)
                curveToRelative(4.79f, -31.93f, 15.93f, -64.1f, 30.55f, -88.25f)
                curveToRelative(23.34f, -38.57f, 47.66f, -45.26f, 64f, -44.08f)
                curveToRelative(18.92f, 1.38f, 35.69f, 10.57f, 48.51f, 26.6f)
                curveToRelative(21.89f, 27.37f, 29.65f, 71f, 21.86f, 122.84f)
                curveToRelative(-5.27f, 35f, -14.2f, 58.95f, -28.11f, 75.31f)
                reflectiveCurveTo(400.78f, 265.27f, 378.17f, 265.27f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(339f, 400f)
                arcToRelative(61f, 61f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.68f, -1.13f)
                curveToRelative(-17.56f, -3.44f, -32.84f, -14.75f, -43f, -31.86f)
                curveToRelative(-9.47f, -15.9f, -13.67f, -35.43f, -11.83f, -55f)
                horizontalLineToRelative(0f)
                curveToRelative(1.6f, -17f, 7f, -28.52f, 16.62f, -35.33f)
                curveToRelative(14.33f, -10.17f, 31.69f, -5.89f, 48.47f, -1.74f)
                curveToRelative(4.48f, 1.1f, 9.1f, 2.24f, 13.62f, 3.11f)
                lineToRelative(6.29f, 1.17f)
                curveToRelative(19.63f, 3.61f, 38.17f, 7f, 48.5f, 22.17f)
                curveToRelative(6.36f, 9.33f, 8.07f, 21f, 5.22f, 35.64f)
                arcToRelative(79.78f, 79.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, -33.52f, 50.61f)
                curveTo(365.56f, 395.78f, 352.17f, 400f, 339f, 400f)
                close()
            }
        }.build()

        return _CiFootsteps!!
    }

@Suppress("ObjectPropertyName")
private var _CiFootsteps: ImageVector? = null
