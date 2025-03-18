package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiChevronForwardCircle: ImageVector
    get() {
        if (_CiChevronForwardCircle != null) {
            return _CiChevronForwardCircle!!
        }
        _CiChevronForwardCircle = ImageVector.Builder(
            name = "CiChevronForwardCircle",
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
                moveTo(305.37f, 256f)
                lineTo(220.69f, 171.31f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.62f, -22.62f)
                lineToRelative(96f, 96f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 22.62f)
                lineToRelative(-96f, 96f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.62f, -22.62f)
                close()
            }
        }.build()

        return _CiChevronForwardCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiChevronForwardCircle: ImageVector? = null
