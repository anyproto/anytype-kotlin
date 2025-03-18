package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiRepeat: ImageVector
    get() {
        if (_CiRepeat != null) {
            return _CiRepeat!!
        }
        _CiRepeat = ImageVector.Builder(
            name = "CiRepeat",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(320f, 120f)
                lineToRelative(48f, 48f)
                lineToRelative(-48f, 48f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(352f, 168f)
                horizontalLineTo(144f)
                arcToRelative(80.24f, 80.24f, 0f, isMoreThanHalf = false, isPositiveArc = false, -80f, 80f)
                verticalLineToRelative(16f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(192f, 392f)
                lineToRelative(-48f, -48f)
                lineToRelative(48f, -48f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(160f, 344f)
                horizontalLineTo(368f)
                arcToRelative(80.24f, 80.24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, -80f)
                verticalLineTo(248f)
            }
        }.build()

        return _CiRepeat!!
    }

@Suppress("ObjectPropertyName")
private var _CiRepeat: ImageVector? = null
