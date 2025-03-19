package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiTriangle: ImageVector
    get() {
        if (_CiTriangle != null) {
            return _CiTriangle!!
        }
        _CiTriangle = ImageVector.Builder(
            name = "CiTriangle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(464f, 464f)
                horizontalLineTo(48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.07f, -23.62f)
                lineToRelative(208f, -384f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.14f, 0f)
                lineToRelative(208f, 384f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 464f, 464f)
                close()
            }
        }.build()

        return _CiTriangle!!
    }

@Suppress("ObjectPropertyName")
private var _CiTriangle: ImageVector? = null
