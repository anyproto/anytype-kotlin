package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPlayForwardCircle: ImageVector
    get() {
        if (_CiPlayForwardCircle != null) {
            return _CiPlayForwardCircle!!
        }
        _CiPlayForwardCircle = ImageVector.Builder(
            name = "CiPlayForwardCircle",
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
                moveTo(394.77f, 265.3f)
                lineTo(280.32f, 334.44f)
                arcTo(10.78f, 10.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, 264f, 325.13f)
                verticalLineTo(271.81f)
                lineTo(160.32f, 334.44f)
                arcTo(10.78f, 10.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, 144f, 325.13f)
                verticalLineTo(186.87f)
                arcToRelative(10.78f, 10.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16.32f, -9.31f)
                lineTo(264f, 240.19f)
                verticalLineTo(186.87f)
                arcToRelative(10.78f, 10.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16.32f, -9.31f)
                lineTo(394.77f, 246.7f)
                arcTo(10.89f, 10.89f, 0f, isMoreThanHalf = false, isPositiveArc = true, 394.77f, 265.3f)
                close()
            }
        }.build()

        return _CiPlayForwardCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiPlayForwardCircle: ImageVector? = null
