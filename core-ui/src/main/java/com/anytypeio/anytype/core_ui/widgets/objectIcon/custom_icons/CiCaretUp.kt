package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCaretUp: ImageVector
    get() {
        if (_CiCaretUp != null) {
            return _CiCaretUp!!
        }
        _CiCaretUp = ImageVector.Builder(
            name = "CiCaretUp",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(414f, 321.94f)
                lineTo(274.22f, 158.82f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, -36.44f, 0f)
                lineTo(98f, 321.94f)
                curveToRelative(-13.34f, 15.57f, -2.28f, 39.62f, 18.22f, 39.62f)
                horizontalLineTo(395.82f)
                curveTo(416.32f, 361.56f, 427.38f, 337.51f, 414f, 321.94f)
                close()
            }
        }.build()

        return _CiCaretUp!!
    }

@Suppress("ObjectPropertyName")
private var _CiCaretUp: ImageVector? = null
