package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiHourglass: ImageVector
    get() {
        if (_CiHourglass != null) {
            return _CiHourglass!!
        }
        _CiHourglass = ImageVector.Builder(
            name = "CiHourglass",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(415.7f, 427.13f)
                curveToRelative(-8.74f, -76.89f, -43.83f, -108.76f, -69.46f, -132f)
                curveTo(328.52f, 279f, 320f, 270.61f, 320f, 256f)
                curveToRelative(0f, -14.41f, 8.49f, -22.64f, 26.16f, -38.44f)
                curveToRelative(25.93f, -23.17f, 61.44f, -54.91f, 69.56f, -132.84f)
                arcToRelative(47f, 47f, 0f, isMoreThanHalf = false, isPositiveArc = false, -12f, -36.26f)
                arcTo(50.3f, 50.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 366.39f, 32f)
                horizontalLineTo(145.61f)
                arcToRelative(50.34f, 50.34f, 0f, isMoreThanHalf = false, isPositiveArc = false, -37.39f, 16.46f)
                arcTo(47.05f, 47.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 96.28f, 84.72f)
                curveToRelative(8.09f, 77.68f, 43.47f, 109.19f, 69.3f, 132.19f)
                curveTo(183.42f, 232.8f, 192f, 241.09f, 192f, 256f)
                curveToRelative(0f, 15.1f, -8.6f, 23.56f, -26.5f, 39.75f)
                curveTo(140f, 318.85f, 105f, 350.48f, 96.3f, 427.13f)
                arcTo(46.59f, 46.59f, 0f, isMoreThanHalf = false, isPositiveArc = false, 108f, 463.33f)
                arcTo(50.44f, 50.44f, 0f, isMoreThanHalf = false, isPositiveArc = false, 145.61f, 480f)
                horizontalLineTo(366.39f)
                arcTo(50.44f, 50.44f, 0f, isMoreThanHalf = false, isPositiveArc = false, 404f, 463.33f)
                arcTo(46.59f, 46.59f, 0f, isMoreThanHalf = false, isPositiveArc = false, 415.7f, 427.13f)
                close()
                moveTo(343.3f, 432f)
                horizontalLineTo(169.13f)
                curveToRelative(-15.6f, 0f, -20f, -18f, -9.06f, -29.16f)
                curveTo(186.55f, 376f, 240f, 356.78f, 240f, 326f)
                verticalLineTo(224f)
                curveToRelative(0f, -19.85f, -38f, -35f, -61.51f, -67.2f)
                curveToRelative(-3.88f, -5.31f, -3.49f, -12.8f, 6.37f, -12.8f)
                horizontalLineTo(327.59f)
                curveToRelative(8.41f, 0f, 10.22f, 7.43f, 6.4f, 12.75f)
                curveTo(310.82f, 189f, 272f, 204.05f, 272f, 224f)
                verticalLineTo(326f)
                curveToRelative(0f, 30.53f, 55.71f, 47f, 80.4f, 76.87f)
                curveTo(362.35f, 414.91f, 358.87f, 432f, 343.3f, 432f)
                close()
            }
        }.build()

        return _CiHourglass!!
    }

@Suppress("ObjectPropertyName")
private var _CiHourglass: ImageVector? = null
