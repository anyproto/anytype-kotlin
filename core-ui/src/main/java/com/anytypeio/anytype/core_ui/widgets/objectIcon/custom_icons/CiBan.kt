package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBan: ImageVector
    get() {
        if (_CiBan != null) {
            return _CiBan!!
        }
        _CiBan = ImageVector.Builder(
            name = "CiBan",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 48f
            ) {
                moveTo(256f, 256f)
                moveToRelative(-200f, 0f)
                arcToRelative(200f, 200f, 0f, isMoreThanHalf = true, isPositiveArc = true, 400f, 0f)
                arcToRelative(200f, 200f, 0f, isMoreThanHalf = true, isPositiveArc = true, -400f, 0f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 48f
            ) {
                moveTo(114.58f, 114.58f)
                lineTo(397.42f, 397.42f)
            }
        }.build()

        return _CiBan!!
    }

@Suppress("ObjectPropertyName")
private var _CiBan: ImageVector? = null
