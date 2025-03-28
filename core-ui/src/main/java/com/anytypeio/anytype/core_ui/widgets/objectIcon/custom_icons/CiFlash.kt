package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFlash: ImageVector
    get() {
        if (_CiFlash != null) {
            return _CiFlash!!
        }
        _CiFlash = ImageVector.Builder(
            name = "CiFlash",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(194.82f, 496f)
                arcToRelative(18.36f, 18.36f, 0f, isMoreThanHalf = false, isPositiveArc = true, -18.1f, -21.53f)
                lineToRelative(0f, -0.11f)
                lineTo(204.83f, 320f)
                horizontalLineTo(96f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -12.44f, -26.06f)
                lineTo(302.73f, 23f)
                arcToRelative(18.45f, 18.45f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32.8f, 13.71f)
                curveToRelative(0f, 0.3f, -0.08f, 0.59f, -0.13f, 0.89f)
                lineTo(307.19f, 192f)
                horizontalLineTo(416f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12.44f, 26.06f)
                lineTo(209.24f, 489f)
                arcTo(18.45f, 18.45f, 0f, isMoreThanHalf = false, isPositiveArc = true, 194.82f, 496f)
                close()
            }
        }.build()

        return _CiFlash!!
    }

@Suppress("ObjectPropertyName")
private var _CiFlash: ImageVector? = null
