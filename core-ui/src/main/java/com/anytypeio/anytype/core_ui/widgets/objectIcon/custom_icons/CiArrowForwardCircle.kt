package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiArrowForwardCircle: ImageVector
    get() {
        if (_CiArrowForwardCircle != null) {
            return _CiArrowForwardCircle!!
        }
        _CiArrowForwardCircle = ImageVector.Builder(
            name = "CiArrowForwardCircle",
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
                moveTo(251.35f, 347.36f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.09f, -22.63f)
                lineTo(303.58f, 272f)
                horizontalLineTo(170f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineTo(303.58f)
                lineToRelative(-52.32f, -52.73f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 274f, 164.73f)
                lineToRelative(79.39f, 80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 22.54f)
                lineToRelative(-79.39f, 80f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 251.35f, 347.36f)
                close()
            }
        }.build()

        return _CiArrowForwardCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiArrowForwardCircle: ImageVector? = null
