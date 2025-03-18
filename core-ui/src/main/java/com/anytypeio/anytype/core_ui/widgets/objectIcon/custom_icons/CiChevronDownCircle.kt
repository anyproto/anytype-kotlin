package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiChevronDownCircle: ImageVector
    get() {
        if (_CiChevronDownCircle != null) {
            return _CiChevronDownCircle!!
        }
        _CiChevronDownCircle = ImageVector.Builder(
            name = "CiChevronDownCircle",
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
                moveTo(363.31f, 227.31f)
                lineToRelative(-96f, 96f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.62f, 0f)
                lineToRelative(-96f, -96f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.62f, -22.62f)
                lineTo(256f, 289.37f)
                lineToRelative(84.69f, -84.68f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.62f, 22.62f)
                close()
            }
        }.build()

        return _CiChevronDownCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiChevronDownCircle: ImageVector? = null
