package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiDisc: ImageVector
    get() {
        if (_CiDisc != null) {
            return _CiDisc!!
        }
        _CiDisc = ImageVector.Builder(
            name = "CiDisc",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 176f)
                arcToRelative(80f, 80f, 0f, isMoreThanHalf = true, isPositiveArc = false, 80f, 80f)
                arcTo(80.09f, 80.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 176f)
                close()
                moveTo(256f, 288f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 288f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(414.39f, 97.61f)
                arcTo(224f, 224f, 0f, isMoreThanHalf = true, isPositiveArc = false, 97.61f, 414.39f)
                arcTo(224f, 224f, 0f, isMoreThanHalf = true, isPositiveArc = false, 414.39f, 97.61f)
                close()
                moveTo(256f, 368f)
                arcTo(112f, 112f, 0f, isMoreThanHalf = true, isPositiveArc = true, 368f, 256f)
                arcTo(112.12f, 112.12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 368f)
                close()
            }
        }.build()

        return _CiDisc!!
    }

@Suppress("ObjectPropertyName")
private var _CiDisc: ImageVector? = null
