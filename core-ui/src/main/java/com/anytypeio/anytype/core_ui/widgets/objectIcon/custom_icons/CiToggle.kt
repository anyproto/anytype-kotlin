package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiToggle: ImageVector
    get() {
        if (_CiToggle != null) {
            return _CiToggle!!
        }
        _CiToggle = ImageVector.Builder(
            name = "CiToggle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(368f, 112f)
                lineTo(144f, 112f)
                curveTo(64.6f, 112f, 0f, 176.6f, 0f, 256f)
                reflectiveCurveTo(64.6f, 400f, 144f, 400f)
                lineTo(368f, 400f)
                curveToRelative(79.4f, 0f, 144f, -64.6f, 144f, -144f)
                reflectiveCurveTo(447.4f, 112f, 368f, 112f)
                close()
                moveTo(368f, 368f)
                arcTo(112f, 112f, 0f, isMoreThanHalf = true, isPositiveArc = true, 480f, 256f)
                arcTo(112.12f, 112.12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 368f, 368f)
                close()
            }
        }.build()

        return _CiToggle!!
    }

@Suppress("ObjectPropertyName")
private var _CiToggle: ImageVector? = null
