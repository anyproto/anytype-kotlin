package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiRadioButtonOff: ImageVector
    get() {
        if (_CiRadioButtonOff != null) {
            return _CiRadioButtonOff!!
        }
        _CiRadioButtonOff = ImageVector.Builder(
            name = "CiRadioButtonOff",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(448f, 256f)
                curveToRelative(0f, -106f, -86f, -192f, -192f, -192f)
                reflectiveCurveTo(64f, 150f, 64f, 256f)
                reflectiveCurveToRelative(86f, 192f, 192f, 192f)
                reflectiveCurveTo(448f, 362f, 448f, 256f)
                close()
            }
        }.build()

        return _CiRadioButtonOff!!
    }

@Suppress("ObjectPropertyName")
private var _CiRadioButtonOff: ImageVector? = null
