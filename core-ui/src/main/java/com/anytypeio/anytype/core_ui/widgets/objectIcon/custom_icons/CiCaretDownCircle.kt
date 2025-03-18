package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCaretDownCircle: ImageVector
    get() {
        if (_CiCaretDownCircle != null) {
            return _CiCaretDownCircle!!
        }
        _CiCaretDownCircle = ImageVector.Builder(
            name = "CiCaretDownCircle",
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
                moveTo(342.43f, 238.23f)
                lineTo(268.3f, 327.32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24.6f, 0f)
                lineToRelative(-74.13f, -89.09f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 181.86f, 212f)
                horizontalLineTo(330.14f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 342.43f, 238.23f)
                close()
            }
        }.build()

        return _CiCaretDownCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiCaretDownCircle: ImageVector? = null
