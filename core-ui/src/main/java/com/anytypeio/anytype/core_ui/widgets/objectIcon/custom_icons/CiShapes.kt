package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiShapes: ImageVector
    get() {
        if (_CiShapes != null) {
            return _CiShapes!!
        }
        _CiShapes = ImageVector.Builder(
            name = "CiShapes",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(336f, 336f)
                horizontalLineTo(32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14f, -23.81f)
                lineToRelative(152f, -272f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 27.94f, 0f)
                lineToRelative(152f, 272f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 336f, 336f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(336f, 160f)
                arcToRelative(161.07f, 161.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32.57f, 3.32f)
                lineTo(377.9f, 296.59f)
                arcTo(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 336f, 368f)
                horizontalLineTo(183.33f)
                arcTo(160f, 160f, 0f, isMoreThanHalf = true, isPositiveArc = false, 336f, 160f)
                close()
            }
        }.build()

        return _CiShapes!!
    }

@Suppress("ObjectPropertyName")
private var _CiShapes: ImageVector? = null
