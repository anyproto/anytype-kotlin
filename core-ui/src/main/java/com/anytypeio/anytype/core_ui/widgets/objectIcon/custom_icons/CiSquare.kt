package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiSquare: ImageVector
    get() {
        if (_CiSquare != null) {
            return _CiSquare!!
        }
        _CiSquare = ImageVector.Builder(
            name = "CiSquare",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(416f, 464f)
                horizontalLineTo(96f)
                arcToRelative(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, -48f, -48f)
                verticalLineTo(96f)
                arcTo(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, 96f, 48f)
                horizontalLineTo(416f)
                arcToRelative(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, 48f, 48f)
                verticalLineTo(416f)
                arcTo(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, 416f, 464f)
                close()
            }
        }.build()

        return _CiSquare!!
    }

@Suppress("ObjectPropertyName")
private var _CiSquare: ImageVector? = null
