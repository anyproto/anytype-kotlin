package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPersonAdd: ImageVector
    get() {
        if (_CiPersonAdd != null) {
            return _CiPersonAdd!!
        }
        _CiPersonAdd = ImageVector.Builder(
            name = "CiPersonAdd",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(288f, 256f)
                curveToRelative(52.79f, 0f, 99.43f, -49.71f, 104f, -110.82f)
                curveToRelative(2.27f, -30.7f, -7.36f, -59.33f, -27.12f, -80.6f)
                curveTo(345.33f, 43.57f, 318f, 32f, 288f, 32f)
                curveToRelative(-30.24f, 0f, -57.59f, 11.5f, -77f, 32.38f)
                curveToRelative(-19.63f, 21.11f, -29.2f, 49.8f, -27f, 80.78f)
                curveTo(188.49f, 206.28f, 235.12f, 256f, 288f, 256f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(495.38f, 439.76f)
                curveToRelative(-8.44f, -46.82f, -34.79f, -86.15f, -76.19f, -113.75f)
                curveTo(382.42f, 301.5f, 335.83f, 288f, 288f, 288f)
                reflectiveCurveToRelative(-94.42f, 13.5f, -131.19f, 38f)
                curveToRelative(-41.4f, 27.6f, -67.75f, 66.93f, -76.19f, 113.75f)
                curveToRelative(-1.93f, 10.73f, 0.69f, 21.34f, 7.19f, 29.11f)
                arcTo(30.94f, 30.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 112f, 480f)
                horizontalLineTo(464f)
                arcToRelative(30.94f, 30.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24.21f, -11.13f)
                curveTo(494.69f, 461.1f, 497.31f, 450.49f, 495.38f, 439.76f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(104f, 288f)
                verticalLineTo(248f)
                horizontalLineToRelative(40f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                horizontalLineTo(104f)
                verticalLineTo(176f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, 0f)
                verticalLineToRelative(40f)
                horizontalLineTo(32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineTo(72f)
                verticalLineToRelative(40f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 0f)
                close()
            }
        }.build()

        return _CiPersonAdd!!
    }

@Suppress("ObjectPropertyName")
private var _CiPersonAdd: ImageVector? = null
