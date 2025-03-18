package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCaretBack: ImageVector
    get() {
        if (_CiCaretBack != null) {
            return _CiCaretBack!!
        }
        _CiCaretBack = ImageVector.Builder(
            name = "CiCaretBack",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(321.94f, 98f)
                lineTo(158.82f, 237.78f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 36.44f)
                lineTo(321.94f, 414f)
                curveToRelative(15.57f, 13.34f, 39.62f, 2.28f, 39.62f, -18.22f)
                verticalLineTo(116.18f)
                curveTo(361.56f, 95.68f, 337.51f, 84.62f, 321.94f, 98f)
                close()
            }
        }.build()

        return _CiCaretBack!!
    }

@Suppress("ObjectPropertyName")
private var _CiCaretBack: ImageVector? = null
