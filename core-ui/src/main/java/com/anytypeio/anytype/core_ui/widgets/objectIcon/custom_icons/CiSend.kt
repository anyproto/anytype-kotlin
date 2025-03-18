package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiSend: ImageVector
    get() {
        if (_CiSend != null) {
            return _CiSend!!
        }
        _CiSend = ImageVector.Builder(
            name = "CiSend",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(476.59f, 227.05f)
                lineToRelative(-0.16f, -0.07f)
                lineTo(49.35f, 49.84f)
                arcTo(23.56f, 23.56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 27.14f, 52f)
                arcTo(24.65f, 24.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 72.59f)
                verticalLineTo(185.88f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19.52f, 23.57f)
                lineToRelative(232.93f, 43.07f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 7.86f)
                lineTo(35.53f, 303.45f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 327f)
                verticalLineTo(440.31f)
                arcTo(23.57f, 23.57f, 0f, isMoreThanHalf = false, isPositiveArc = false, 26.59f, 460f)
                arcToRelative(23.94f, 23.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.22f, 4f)
                arcToRelative(24.55f, 24.55f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.52f, -1.93f)
                lineTo(476.4f, 285.94f)
                lineToRelative(0.19f, -0.09f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -58.8f)
                close()
            }
        }.build()

        return _CiSend!!
    }

@Suppress("ObjectPropertyName")
private var _CiSend: ImageVector? = null
