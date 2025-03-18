package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiArrowRedoCircle: ImageVector
    get() {
        if (_CiArrowRedoCircle != null) {
            return _CiArrowRedoCircle!!
        }
        _CiArrowRedoCircle = ImageVector.Builder(
            name = "CiArrowRedoCircle",
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
                moveTo(144f, 322.67f)
                curveToRelative(5.45f, -61.45f, 34.14f, -117.09f, 122.87f, -117.09f)
                lineTo(266.87f, 168.26f)
                arcToRelative(8.32f, 8.32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, -6f)
                lineTo(365.42f, 242f)
                arcToRelative(8.2f, 8.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 11.94f)
                lineTo(281f, 333.71f)
                arcToRelative(8.32f, 8.32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14f, -6f)
                lineTo(267f, 290.42f)
                curveToRelative(-57.07f, 0f, -84.51f, 13.47f, -108.58f, 38.68f)
                curveTo(152.93f, 334.75f, 143.35f, 330.42f, 144f, 322.67f)
                close()
            }
        }.build()

        return _CiArrowRedoCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiArrowRedoCircle: ImageVector? = null
