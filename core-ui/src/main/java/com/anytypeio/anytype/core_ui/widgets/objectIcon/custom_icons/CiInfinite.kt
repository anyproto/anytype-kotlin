package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiInfinite: ImageVector
    get() {
        if (_CiInfinite != null) {
            return _CiInfinite!!
        }
        _CiInfinite = ImageVector.Builder(
            name = "CiInfinite",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 48f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(256f, 256f)
                reflectiveCurveToRelative(-48f, -96f, -126f, -96f)
                curveToRelative(-54.12f, 0f, -98f, 43f, -98f, 96f)
                reflectiveCurveToRelative(43.88f, 96f, 98f, 96f)
                curveToRelative(30f, 0f, 56.45f, -13.18f, 78f, -32f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 48f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(256f, 256f)
                reflectiveCurveToRelative(48f, 96f, 126f, 96f)
                curveToRelative(54.12f, 0f, 98f, -43f, 98f, -96f)
                reflectiveCurveToRelative(-43.88f, -96f, -98f, -96f)
                curveToRelative(-29.37f, 0f, -56.66f, 13.75f, -78f, 32f)
            }
        }.build()

        return _CiInfinite!!
    }

@Suppress("ObjectPropertyName")
private var _CiInfinite: ImageVector? = null
