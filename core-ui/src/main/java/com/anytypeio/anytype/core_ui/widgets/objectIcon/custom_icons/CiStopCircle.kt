package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiStopCircle: ImageVector
    get() {
        if (_CiStopCircle != null) {
            return _CiStopCircle!!
        }
        _CiStopCircle = ImageVector.Builder(
            name = "CiStopCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 48f)
                curveTo(141.31f, 48f, 48f, 141.31f, 48f, 256f)
                reflectiveCurveToRelative(93.31f, 208f, 208f, 208f)
                reflectiveCurveToRelative(208f, -93.31f, 208f, -208f)
                reflectiveCurveTo(370.69f, 48f, 256f, 48f)
                close()
                moveTo(336f, 310.4f)
                arcTo(25.62f, 25.62f, 0f, isMoreThanHalf = false, isPositiveArc = true, 310.4f, 336f)
                lineTo(201.6f, 336f)
                arcTo(25.62f, 25.62f, 0f, isMoreThanHalf = false, isPositiveArc = true, 176f, 310.4f)
                lineTo(176f, 201.6f)
                arcTo(25.62f, 25.62f, 0f, isMoreThanHalf = false, isPositiveArc = true, 201.6f, 176f)
                lineTo(310.4f, 176f)
                arcTo(25.62f, 25.62f, 0f, isMoreThanHalf = false, isPositiveArc = true, 336f, 201.6f)
                close()
            }
        }.build()

        return _CiStopCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiStopCircle: ImageVector? = null
