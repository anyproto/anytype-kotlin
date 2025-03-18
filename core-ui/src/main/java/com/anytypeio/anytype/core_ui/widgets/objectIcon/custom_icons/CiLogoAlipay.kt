package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiLogoAlipay: ImageVector
    get() {
        if (_CiLogoAlipay != null) {
            return _CiLogoAlipay!!
        }
        _CiLogoAlipay = ImageVector.Builder(
            name = "CiLogoAlipay",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(102.41f, 32f)
                curveTo(62.38f, 32f, 32f, 64.12f, 32f, 103.78f)
                verticalLineTo(408.23f)
                curveTo(32f, 447.86f, 64.38f, 480f, 104.41f, 480f)
                horizontalLineToRelative(303.2f)
                curveToRelative(40f, 0f, 72.39f, -32.14f, 72.39f, -71.77f)
                verticalLineToRelative(-3.11f)
                curveToRelative(-1.35f, -0.56f, -115.47f, -48.57f, -174.5f, -76.7f)
                curveToRelative(-39.82f, 48.57f, -91.18f, 78f, -144.5f, 78f)
                curveToRelative(-90.18f, 0f, -120.8f, -78.22f, -78.1f, -129.72f)
                curveToRelative(9.31f, -11.22f, 25.15f, -21.94f, 49.73f, -28f)
                curveToRelative(38.45f, -9.36f, 99.64f, 5.85f, 157f, 24.61f)
                arcToRelative(309.41f, 309.41f, 0f, isMoreThanHalf = false, isPositiveArc = false, 25.46f, -61.67f)
                horizontalLineTo(138.34f)
                verticalLineTo(194f)
                horizontalLineToRelative(91.13f)
                verticalLineTo(162.17f)
                horizontalLineTo(119.09f)
                verticalLineTo(144.42f)
                horizontalLineTo(229.47f)
                verticalLineTo(99f)
                reflectiveCurveToRelative(0f, -7.65f, 7.82f, -7.65f)
                horizontalLineToRelative(44.55f)
                verticalLineToRelative(53f)
                horizontalLineTo(391f)
                verticalLineToRelative(17.75f)
                horizontalLineTo(281.84f)
                verticalLineTo(194f)
                horizontalLineToRelative(89.08f)
                arcToRelative(359.41f, 359.41f, 0f, isMoreThanHalf = false, isPositiveArc = true, -37.72f, 94.43f)
                curveToRelative(27f, 9.69f, 49.31f, 18.88f, 67.39f, 24.89f)
                curveToRelative(60.32f, 20f, 77.23f, 22.45f, 79.41f, 22.7f)
                verticalLineTo(103.78f)
                curveTo(480f, 64.12f, 447.6f, 32f, 407.61f, 32f)
                horizontalLineTo(102.41f)
                close()
                moveTo(152f, 274.73f)
                quadToRelative(-5.81f, 0.06f, -11.67f, 0.63f)
                curveToRelative(-11.3f, 1.13f, -32.5f, 6.07f, -44.09f, 16.23f)
                curveToRelative(-34.74f, 30f, -13.94f, 84.93f, 56.37f, 84.93f)
                curveToRelative(40.87f, 0f, 81.71f, -25.9f, 113.79f, -67.37f)
                curveToRelative(-41.36f, -20f, -77f, -34.85f, -114.4f, -34.42f)
                close()
            }
        }.build()

        return _CiLogoAlipay!!
    }

@Suppress("ObjectPropertyName")
private var _CiLogoAlipay: ImageVector? = null
