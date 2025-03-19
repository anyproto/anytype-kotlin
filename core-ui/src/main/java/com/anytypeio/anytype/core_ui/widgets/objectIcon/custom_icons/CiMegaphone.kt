package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMegaphone: ImageVector
    get() {
        if (_CiMegaphone != null) {
            return _CiMegaphone!!
        }
        _CiMegaphone = ImageVector.Builder(
            name = "CiMegaphone",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(48f, 176f)
                verticalLineToRelative(0.66f)
                arcToRelative(17.38f, 17.38f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.2f, 11.23f)
                lineToRelative(0f, 0.05f)
                curveTo(38.4f, 194.32f, 32f, 205.74f, 32f, 224f)
                curveToRelative(0f, 16.55f, 5.3f, 28.23f, 11.68f, 35.91f)
                arcTo(19f, 19f, 0f, isMoreThanHalf = false, isPositiveArc = true, 48f, 272f)
                horizontalLineToRelative(0f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 32f)
                horizontalLineToRelative(8f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, -8f)
                verticalLineTo(152f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, -8f)
                horizontalLineTo(80f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 176f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(452.18f, 186.55f)
                lineToRelative(-0.93f, -0.17f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.25f, -3.93f)
                verticalLineTo(62f)
                curveToRelative(0f, -12.64f, -8.39f, -24f, -20.89f, -28.32f)
                curveToRelative(-11.92f, -4.11f, -24.34f, -0.76f, -31.68f, 8.53f)
                arcTo(431.18f, 431.18f, 0f, isMoreThanHalf = false, isPositiveArc = true, 344.12f, 93.9f)
                curveToRelative(-23.63f, 20f, -46.24f, 34.25f, -67f, 42.31f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.15f, 7.47f)
                verticalLineTo(299f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.69f, 14.69f)
                curveToRelative(19.34f, 8.29f, 40.24f, 21.83f, 62f, 40.28f)
                arcToRelative(433.74f, 433.74f, 0f, isMoreThanHalf = false, isPositiveArc = true, 51.68f, 52.16f)
                arcTo(26.22f, 26.22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 416.44f, 416f)
                arcToRelative(33.07f, 33.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 10.44f, -1.74f)
                curveTo(439.71f, 410f, 448f, 399.05f, 448f, 386.4f)
                verticalLineTo(265.53f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.33f, -3.94f)
                lineToRelative(0.85f, -0.14f)
                curveTo(461.8f, 258.84f, 480f, 247.67f, 480f, 224f)
                reflectiveCurveTo(461.8f, 189.16f, 452.18f, 186.55f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(240f, 320f)
                verticalLineTo(152f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, -8f)
                horizontalLineTo(136f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, 8f)
                verticalLineTo(456f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24f, 24f)
                horizontalLineToRelative(52.45f)
                arcToRelative(32.66f, 32.66f, 0f, isMoreThanHalf = false, isPositiveArc = false, 25.93f, -12.45f)
                arcToRelative(31.65f, 31.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.21f, -29.05f)
                curveToRelative(-1.62f, -5.18f, -3.63f, -11f, -5.77f, -17.19f)
                curveToRelative(-7.91f, -22.9f, -18.34f, -37.07f, -21.12f, -69.32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 240f, 320f)
                close()
            }
        }.build()

        return _CiMegaphone!!
    }

@Suppress("ObjectPropertyName")
private var _CiMegaphone: ImageVector? = null
