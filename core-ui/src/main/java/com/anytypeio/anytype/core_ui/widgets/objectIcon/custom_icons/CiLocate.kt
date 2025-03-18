package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiLocate: ImageVector
    get() {
        if (_CiLocate != null) {
            return _CiLocate!!
        }
        _CiLocate = ImageVector.Builder(
            name = "CiLocate",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 48f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(256f, 96f)
                lineTo(256f, 56f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 48f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(256f, 456f)
                lineTo(256f, 416f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 48f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(256f, 112f)
                arcTo(144f, 144f, 0f, isMoreThanHalf = true, isPositiveArc = false, 400f, 256f)
                arcTo(144f, 144f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 112f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 48f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(416f, 256f)
                lineTo(456f, 256f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 48f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(56f, 256f)
                lineTo(96f, 256f)
            }
        }.build()

        return _CiLocate!!
    }

@Suppress("ObjectPropertyName")
private var _CiLocate: ImageVector? = null
