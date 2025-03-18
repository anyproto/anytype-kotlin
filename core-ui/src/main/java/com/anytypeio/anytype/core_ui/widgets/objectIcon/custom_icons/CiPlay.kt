package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPlay: ImageVector
    get() {
        if (_CiPlay != null) {
            return _CiPlay!!
        }
        _CiPlay = ImageVector.Builder(
            name = "CiPlay",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(133f, 440f)
                arcToRelative(35.37f, 35.37f, 0f, isMoreThanHalf = false, isPositiveArc = true, -17.5f, -4.67f)
                curveToRelative(-12f, -6.8f, -19.46f, -20f, -19.46f, -34.33f)
                verticalLineTo(111f)
                curveToRelative(0f, -14.37f, 7.46f, -27.53f, 19.46f, -34.33f)
                arcToRelative(35.13f, 35.13f, 0f, isMoreThanHalf = false, isPositiveArc = true, 35.77f, 0.45f)
                lineTo(399.12f, 225.48f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 61f)
                lineTo(151.23f, 434.88f)
                arcTo(35.5f, 35.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 133f, 440f)
                close()
            }
        }.build()

        return _CiPlay!!
    }

@Suppress("ObjectPropertyName")
private var _CiPlay: ImageVector? = null
