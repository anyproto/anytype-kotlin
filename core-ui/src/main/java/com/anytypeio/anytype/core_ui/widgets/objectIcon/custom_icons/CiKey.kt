package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiKey: ImageVector
    get() {
        if (_CiKey != null) {
            return _CiKey!!
        }
        _CiKey = ImageVector.Builder(
            name = "CiKey",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(218.1f, 167.17f)
                curveToRelative(0f, 13f, 0f, 25.6f, 4.1f, 37.4f)
                curveToRelative(-43.1f, 50.6f, -156.9f, 184.3f, -167.5f, 194.5f)
                arcToRelative(20.17f, 20.17f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.7f, 15f)
                curveToRelative(0f, 8.5f, 5.2f, 16.7f, 9.6f, 21.3f)
                curveToRelative(6.6f, 6.9f, 34.8f, 33f, 40f, 28f)
                curveToRelative(15.4f, -15f, 18.5f, -19f, 24.8f, -25.2f)
                curveToRelative(9.5f, -9.3f, -1f, -28.3f, 2.3f, -36f)
                reflectiveCurveToRelative(6.8f, -9.2f, 12.5f, -10.4f)
                reflectiveCurveToRelative(15.8f, 2.9f, 23.7f, 3f)
                curveToRelative(8.3f, 0.1f, 12.8f, -3.4f, 19f, -9.2f)
                curveToRelative(5f, -4.6f, 8.6f, -8.9f, 8.7f, -15.6f)
                curveToRelative(0.2f, -9f, -12.8f, -20.9f, -3.1f, -30.4f)
                reflectiveCurveToRelative(23.7f, 6.2f, 34f, 5f)
                reflectiveCurveToRelative(22.8f, -15.5f, 24.1f, -21.6f)
                reflectiveCurveToRelative(-11.7f, -21.8f, -9.7f, -30.7f)
                curveToRelative(0.7f, -3f, 6.8f, -10f, 11.4f, -11f)
                reflectiveCurveToRelative(25f, 6.9f, 29.6f, 5.9f)
                curveToRelative(5.6f, -1.2f, 12.1f, -7.1f, 17.4f, -10.4f)
                curveToRelative(15.5f, 6.7f, 29.6f, 9.4f, 47.7f, 9.4f)
                curveToRelative(68.5f, 0f, 124f, -53.4f, 124f, -119.2f)
                reflectiveCurveTo(408.5f, 48f, 340f, 48f)
                reflectiveCurveTo(218.1f, 101.37f, 218.1f, 167.17f)
                close()
                moveTo(400f, 144f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, -32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 400f, 144f)
                close()
            }
        }.build()

        return _CiKey!!
    }

@Suppress("ObjectPropertyName")
private var _CiKey: ImageVector? = null
