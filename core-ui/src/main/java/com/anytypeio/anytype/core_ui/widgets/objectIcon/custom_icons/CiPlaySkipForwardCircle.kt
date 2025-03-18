package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPlaySkipForwardCircle: ImageVector
    get() {
        if (_CiPlaySkipForwardCircle != null) {
            return _CiPlaySkipForwardCircle!!
        }
        _CiPlaySkipForwardCircle = ImageVector.Builder(
            name = "CiPlaySkipForwardCircle",
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
                moveTo(336f, 320f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 0f)
                lineTo(304f, 267f)
                lineTo(192.32f, 334.44f)
                arcTo(10.78f, 10.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, 176f, 325.13f)
                lineTo(176f, 186.87f)
                arcToRelative(10.78f, 10.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16.32f, -9.31f)
                lineTo(304f, 245f)
                lineTo(304f, 192f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 0f)
                close()
            }
        }.build()

        return _CiPlaySkipForwardCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiPlaySkipForwardCircle: ImageVector? = null
