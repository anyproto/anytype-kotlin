package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFunnel: ImageVector
    get() {
        if (_CiFunnel != null) {
            return _CiFunnel!!
        }
        _CiFunnel = ImageVector.Builder(
            name = "CiFunnel",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(296f, 464f)
                arcToRelative(23.88f, 23.88f, 0f, isMoreThanHalf = false, isPositiveArc = true, -7.55f, -1.23f)
                lineToRelative(-80.15f, -26.67f)
                arcTo(23.92f, 23.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, 192f, 413.32f)
                verticalLineTo(294.11f)
                arcToRelative(0.44f, 0.44f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.09f, -0.13f)
                lineTo(23.26f, 97.54f)
                arcTo(30f, 30f, 0f, isMoreThanHalf = false, isPositiveArc = true, 46.05f, 48f)
                horizontalLineTo(466f)
                arcToRelative(30f, 30f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.79f, 49.54f)
                lineTo(320.09f, 294f)
                arcToRelative(0.77f, 0.77f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.09f, 0.13f)
                verticalLineTo(440f)
                arcToRelative(23.93f, 23.93f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, 24f)
                close()
            }
        }.build()

        return _CiFunnel!!
    }

@Suppress("ObjectPropertyName")
private var _CiFunnel: ImageVector? = null
