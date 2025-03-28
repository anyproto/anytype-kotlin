package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiEye: ImageVector
    get() {
        if (_CiEye != null) {
            return _CiEye!!
        }
        _CiEye = ImageVector.Builder(
            name = "CiEye",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 256f)
                moveToRelative(-64f, 0f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = true, 128f, 0f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = true, -128f, 0f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(490.84f, 238.6f)
                curveToRelative(-26.46f, -40.92f, -60.79f, -75.68f, -99.27f, -100.53f)
                curveTo(349f, 110.55f, 302f, 96f, 255.66f, 96f)
                curveToRelative(-42.52f, 0f, -84.33f, 12.15f, -124.27f, 36.11f)
                curveTo(90.66f, 156.54f, 53.76f, 192.23f, 21.71f, 238.18f)
                arcToRelative(31.92f, 31.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.64f, 35.54f)
                curveToRelative(26.41f, 41.33f, 60.4f, 76.14f, 98.28f, 100.65f)
                curveTo(162f, 402f, 207.9f, 416f, 255.66f, 416f)
                curveToRelative(46.71f, 0f, 93.81f, -14.43f, 136.2f, -41.72f)
                curveToRelative(38.46f, -24.77f, 72.72f, -59.66f, 99.08f, -100.92f)
                arcTo(32.2f, 32.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 490.84f, 238.6f)
                close()
                moveTo(256f, 352f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = true, isPositiveArc = true, 96f, -96f)
                arcTo(96.11f, 96.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 352f)
                close()
            }
        }.build()

        return _CiEye!!
    }

@Suppress("ObjectPropertyName")
private var _CiEye: ImageVector? = null
