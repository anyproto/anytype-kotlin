package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPencil: ImageVector
    get() {
        if (_CiPencil != null) {
            return _CiPencil!!
        }
        _CiPencil = ImageVector.Builder(
            name = "CiPencil",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 44f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(358.62f, 129.28f)
                lineToRelative(-272.13f, 272.8f)
                lineToRelative(-16.49f, 39.92f)
                lineToRelative(39.92f, -16.49f)
                lineToRelative(272.8f, -272.13f)
                lineToRelative(-24.1f, -24.1f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 44f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(413.07f, 74.84f)
                lineTo(401.28f, 86.62f)
                lineToRelative(24.1f, 24.1f)
                lineToRelative(11.79f, -11.79f)
                arcToRelative(16.51f, 16.51f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -23.34f)
                lineToRelative(-0.75f, -0.75f)
                arcTo(16.51f, 16.51f, 0f, isMoreThanHalf = false, isPositiveArc = false, 413.07f, 74.84f)
                close()
            }
        }.build()

        return _CiPencil!!
    }

@Suppress("ObjectPropertyName")
private var _CiPencil: ImageVector? = null
