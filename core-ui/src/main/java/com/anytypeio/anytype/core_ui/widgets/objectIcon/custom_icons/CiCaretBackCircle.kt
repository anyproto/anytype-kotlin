package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCaretBackCircle: ImageVector
    get() {
        if (_CiCaretBackCircle != null) {
            return _CiCaretBackCircle!!
        }
        _CiCaretBackCircle = ImageVector.Builder(
            name = "CiCaretBackCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(48f, 256f)
                curveToRelative(0f, 114.87f, 93.13f, 208f, 208f, 208f)
                reflectiveCurveToRelative(208f, -93.13f, 208f, -208f)
                reflectiveCurveTo(370.87f, 48f, 256f, 48f)
                reflectiveCurveTo(48f, 141.13f, 48f, 256f)
                close()
                moveTo(300f, 181.86f)
                lineTo(300f, 330.14f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -26.23f, 12.29f)
                lineTo(184.68f, 268.3f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -24.6f)
                lineToRelative(89.09f, -74.13f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 300f, 181.86f)
                close()
            }
        }.build()

        return _CiCaretBackCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiCaretBackCircle: ImageVector? = null
