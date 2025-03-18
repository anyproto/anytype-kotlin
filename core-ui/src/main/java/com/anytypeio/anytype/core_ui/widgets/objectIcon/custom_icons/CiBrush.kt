package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBrush: ImageVector
    get() {
        if (_CiBrush != null) {
            return _CiBrush!!
        }
        _CiBrush = ImageVector.Builder(
            name = "CiBrush",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(233.15f, 360.11f)
                arcToRelative(15.7f, 15.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.92f, -0.77f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -10.92f, -13f)
                curveToRelative(-2.15f, -15f, -19.95f, -32.46f, -36.62f, -35.85f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 172f, 284.16f)
                lineTo(383.09f, 49.06f)
                curveToRelative(0.19f, -0.22f, 0.39f, -0.43f, 0.59f, -0.63f)
                arcToRelative(56.57f, 56.57f, 0f, isMoreThanHalf = false, isPositiveArc = true, 79.89f, 0f)
                horizontalLineToRelative(0f)
                arcToRelative(56.51f, 56.51f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.11f, 79.78f)
                lineToRelative(-219f, 227f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 233.15f, 360.11f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(119.89f, 480.11f)
                curveToRelative(-32.14f, 0f, -65.45f, -16.89f, -84.85f, -43f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12.85f, -25.54f)
                curveToRelative(5.34f, 0f, 20f, -4.87f, 20f, -20.57f)
                curveToRelative(0f, -39.07f, 31.4f, -70.86f, 70f, -70.86f)
                reflectiveCurveToRelative(70f, 31.79f, 70f, 70.86f)
                curveTo(207.89f, 440.12f, 168.41f, 480.11f, 119.89f, 480.11f)
                close()
            }
        }.build()

        return _CiBrush!!
    }

@Suppress("ObjectPropertyName")
private var _CiBrush: ImageVector? = null
