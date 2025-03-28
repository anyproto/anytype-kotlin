package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBatteryHalf: ImageVector
    get() {
        if (_CiBatteryHalf != null) {
            return _CiBatteryHalf!!
        }
        _CiBatteryHalf = ImageVector.Builder(
            name = "CiBatteryHalf",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Square
            ) {
                moveTo(77.7f, 144f)
                lineTo(386.3f, 144f)
                arcTo(45.7f, 45.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 432f, 189.7f)
                lineTo(432f, 322.3f)
                arcTo(45.7f, 45.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 386.3f, 368f)
                lineTo(77.7f, 368f)
                arcTo(45.7f, 45.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 322.3f)
                lineTo(32f, 189.7f)
                arcTo(45.7f, 45.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 77.7f, 144f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Square
            ) {
                moveTo(89.69f, 198.93f)
                lineTo(236f, 198.93f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 240f, 202.93f)
                lineTo(240f, 309.06f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 236f, 313.06f)
                lineTo(89.69f, 313.06f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 85.69f, 309.06f)
                lineTo(85.69f, 202.93f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 89.69f, 198.93f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(480f, 218.67f)
                lineTo(480f, 293.33f)
            }
        }.build()

        return _CiBatteryHalf!!
    }

@Suppress("ObjectPropertyName")
private var _CiBatteryHalf: ImageVector? = null
