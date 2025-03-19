package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiEllipse: ImageVector
    get() {
        if (_CiEllipse != null) {
            return _CiEllipse!!
        }
        _CiEllipse = ImageVector.Builder(
            name = "CiEllipse",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 464f)
                curveTo(141.31f, 464f, 48f, 370.69f, 48f, 256f)
                reflectiveCurveTo(141.31f, 48f, 256f, 48f)
                reflectiveCurveToRelative(208f, 93.31f, 208f, 208f)
                reflectiveCurveTo(370.69f, 464f, 256f, 464f)
                close()
            }
        }.build()

        return _CiEllipse!!
    }

@Suppress("ObjectPropertyName")
private var _CiEllipse: ImageVector? = null
