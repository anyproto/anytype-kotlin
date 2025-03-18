package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPin: ImageVector
    get() {
        if (_CiPin != null) {
            return _CiPin!!
        }
        _CiPin = ImageVector.Builder(
            name = "CiPin",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(336f, 96f)
                arcToRelative(80f, 80f, 0f, isMoreThanHalf = true, isPositiveArc = false, -96f, 78.39f)
                lineTo(240f, 457.56f)
                arcToRelative(32.09f, 32.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.49f, 12.38f)
                lineToRelative(10.07f, 24f)
                arcToRelative(3.92f, 3.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.88f, 0f)
                lineToRelative(10.07f, -24f)
                arcTo(32.09f, 32.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 272f, 457.56f)
                lineTo(272f, 174.39f)
                arcTo(80.13f, 80.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, 336f, 96f)
                close()
                moveTo(280f, 96f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, 24f, -24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 280f, 96f)
                close()
            }
        }.build()

        return _CiPin!!
    }

@Suppress("ObjectPropertyName")
private var _CiPin: ImageVector? = null
