package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiTrendingDown: ImageVector
    get() {
        if (_CiTrendingDown != null) {
            return _CiTrendingDown!!
        }
        _CiTrendingDown = ImageVector.Builder(
            name = "CiTrendingDown",
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
                moveTo(352f, 368f)
                lineToRelative(112f, 0f)
                lineToRelative(0f, -112f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(48f, 144f)
                lineTo(169.37f, 265.37f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 45.26f, 0f)
                lineToRelative(50.74f, -50.74f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 45.26f, 0f)
                lineTo(448f, 352f)
            }
        }.build()

        return _CiTrendingDown!!
    }

@Suppress("ObjectPropertyName")
private var _CiTrendingDown: ImageVector? = null
