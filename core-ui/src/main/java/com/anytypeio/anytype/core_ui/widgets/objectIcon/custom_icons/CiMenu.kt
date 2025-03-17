package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMenu: ImageVector
    get() {
        if (_CiMenu != null) {
            return _CiMenu!!
        }
        _CiMenu = ImageVector.Builder(
            name = "CiMenu",
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
                moveTo(88f, 152f)
                lineTo(424f, 152f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 48f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(88f, 256f)
                lineTo(424f, 256f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 48f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(88f, 360f)
                lineTo(424f, 360f)
            }
        }.build()

        return _CiMenu!!
    }

@Suppress("ObjectPropertyName")
private var _CiMenu: ImageVector? = null
