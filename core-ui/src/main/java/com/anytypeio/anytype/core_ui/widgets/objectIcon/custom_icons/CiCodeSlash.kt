package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCodeSlash: ImageVector
    get() {
        if (_CiCodeSlash != null) {
            return _CiCodeSlash!!
        }
        _CiCodeSlash = ImageVector.Builder(
            name = "CiCodeSlash",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(160f, 389f)
                arcToRelative(20.91f, 20.91f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.82f, -5.2f)
                lineToRelative(-128f, -112f)
                arcToRelative(21f, 21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -31.6f)
                lineToRelative(128f, -112f)
                arcToRelative(21f, 21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 27.66f, 31.61f)
                lineTo(63.89f, 256f)
                lineToRelative(109.94f, 96.19f)
                arcTo(21f, 21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 160f, 389f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(352f, 389f)
                arcToRelative(21f, 21f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.84f, -36.81f)
                lineTo(448.11f, 256f)
                lineTo(338.17f, 159.81f)
                arcToRelative(21f, 21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 27.66f, -31.61f)
                lineToRelative(128f, 112f)
                arcToRelative(21f, 21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 31.6f)
                lineToRelative(-128f, 112f)
                arcTo(20.89f, 20.89f, 0f, isMoreThanHalf = false, isPositiveArc = true, 352f, 389f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(208f, 437f)
                arcToRelative(21f, 21f, 0f, isMoreThanHalf = false, isPositiveArc = true, -20.12f, -27f)
                lineToRelative(96f, -320f)
                arcTo(21f, 21f, 0f, isMoreThanHalf = true, isPositiveArc = true, 324.11f, 102f)
                lineToRelative(-96f, 320f)
                arcTo(21f, 21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 208f, 437f)
                close()
            }
        }.build()

        return _CiCodeSlash!!
    }

@Suppress("ObjectPropertyName")
private var _CiCodeSlash: ImageVector? = null
