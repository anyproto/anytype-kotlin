package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiStopwatch: ImageVector
    get() {
        if (_CiStopwatch != null) {
            return _CiStopwatch!!
        }
        _CiStopwatch = ImageVector.Builder(
            name = "CiStopwatch",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 272f)
                moveToRelative(-16f, 0f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, 0f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, -32f, 0f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(280f, 81.5f)
                verticalLineTo(72f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, -48f, 0f)
                verticalLineToRelative(9.5f)
                arcToRelative(191f, 191f, 0f, isMoreThanHalf = false, isPositiveArc = false, -84.43f, 32.13f)
                lineTo(137f, 103f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 103f, 137f)
                lineToRelative(8.6f, 8.6f)
                arcTo(191.17f, 191.17f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 272f)
                curveToRelative(0f, 105.87f, 86.13f, 192f, 192f, 192f)
                reflectiveCurveToRelative(192f, -86.13f, 192f, -192f)
                curveTo(448f, 174.26f, 374.58f, 93.34f, 280f, 81.5f)
                close()
                moveTo(256f, 320f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, -93.25f)
                verticalLineTo(152f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 0f)
                verticalLineToRelative(74.75f)
                arcTo(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 320f)
                close()
            }
        }.build()

        return _CiStopwatch!!
    }

@Suppress("ObjectPropertyName")
private var _CiStopwatch: ImageVector? = null
