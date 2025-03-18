package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCaretForwardCircle: ImageVector
    get() {
        if (_CiCaretForwardCircle != null) {
            return _CiCaretForwardCircle!!
        }
        _CiCaretForwardCircle = ImageVector.Builder(
            name = "CiCaretForwardCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(464f, 256f)
                curveToRelative(0f, -114.87f, -93.13f, -208f, -208f, -208f)
                reflectiveCurveTo(48f, 141.13f, 48f, 256f)
                reflectiveCurveToRelative(93.13f, 208f, 208f, 208f)
                reflectiveCurveTo(464f, 370.87f, 464f, 256f)
                close()
                moveTo(212f, 330.14f)
                verticalLineTo(181.86f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26.23f, -12.29f)
                lineToRelative(89.09f, 74.13f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 24.6f)
                lineToRelative(-89.09f, 74.13f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 212f, 330.14f)
                close()
            }
        }.build()

        return _CiCaretForwardCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiCaretForwardCircle: ImageVector? = null
