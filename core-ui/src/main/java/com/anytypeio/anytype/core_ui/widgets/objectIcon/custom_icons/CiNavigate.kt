package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiNavigate: ImageVector
    get() {
        if (_CiNavigate != null) {
            return _CiNavigate!!
        }
        _CiNavigate = ImageVector.Builder(
            name = "CiNavigate",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(272f, 464f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, -16.42f)
                verticalLineTo(264.13f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, -8f)
                horizontalLineTo(64.41f)
                arcToRelative(16.31f, 16.31f, 0f, isMoreThanHalf = false, isPositiveArc = true, -15.49f, -10.65f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.41f, -19.87f)
                lineToRelative(384f, -176.15f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21.22f, 21.19f)
                lineToRelative(-176f, 384f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 272f, 464f)
                close()
            }
        }.build()

        return _CiNavigate!!
    }

@Suppress("ObjectPropertyName")
private var _CiNavigate: ImageVector? = null
