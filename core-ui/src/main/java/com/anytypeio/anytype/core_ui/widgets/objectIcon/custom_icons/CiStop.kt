package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiStop: ImageVector
    get() {
        if (_CiStop != null) {
            return _CiStop!!
        }
        _CiStop = ImageVector.Builder(
            name = "CiStop",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(392f, 432f)
                horizontalLineTo(120f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, -40f, -40f)
                verticalLineTo(120f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, 40f, -40f)
                horizontalLineTo(392f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, 40f, 40f)
                verticalLineTo(392f)
                arcTo(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, 392f, 432f)
                close()
            }
        }.build()

        return _CiStop!!
    }

@Suppress("ObjectPropertyName")
private var _CiStop: ImageVector? = null
