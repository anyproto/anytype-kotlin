package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPlayBackCircle: ImageVector
    get() {
        if (_CiPlayBackCircle != null) {
            return _CiPlayBackCircle!!
        }
        _CiPlayBackCircle = ImageVector.Builder(
            name = "CiPlayBackCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(48f, 256f)
                curveToRelative(0f, 114.69f, 93.31f, 208f, 208f, 208f)
                reflectiveCurveToRelative(208f, -93.31f, 208f, -208f)
                reflectiveCurveTo(370.69f, 48f, 256f, 48f)
                reflectiveCurveTo(48f, 141.31f, 48f, 256f)
                close()
                moveTo(117.23f, 246.7f)
                lineTo(231.68f, 177.56f)
                arcTo(10.78f, 10.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, 248f, 186.87f)
                verticalLineToRelative(53.32f)
                lineToRelative(103.68f, -62.63f)
                arcTo(10.78f, 10.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, 368f, 186.87f)
                lineTo(368f, 325.13f)
                arcToRelative(10.78f, 10.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16.32f, 9.31f)
                lineTo(248f, 271.81f)
                verticalLineToRelative(53.32f)
                arcToRelative(10.78f, 10.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16.32f, 9.31f)
                lineTo(117.23f, 265.3f)
                arcTo(10.89f, 10.89f, 0f, isMoreThanHalf = false, isPositiveArc = true, 117.23f, 246.7f)
                close()
            }
        }.build()

        return _CiPlayBackCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiPlayBackCircle: ImageVector? = null
