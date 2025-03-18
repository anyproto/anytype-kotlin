package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiLogoAmplify: ImageVector
    get() {
        if (_CiLogoAmplify != null) {
            return _CiLogoAmplify!!
        }
        _CiLogoAmplify = ImageVector.Builder(
            name = "CiLogoAmplify",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(112.31f, 268f)
                lineToRelative(40.36f, -68.69f)
                lineToRelative(34.65f, 59f)
                lineToRelative(-67.54f, 115f)
                horizontalLineToRelative(135f)
                lineTo(289.31f, 432f)
                lineTo(16f, 432f)
                close()
                moveTo(170.88f, 168.24f)
                lineTo(204.15f, 111.57f)
                lineTo(392.44f, 432f)
                lineTo(325.76f, 432f)
                close()
                moveTo(222.67f, 80f)
                horizontalLineToRelative(66.59f)
                lineTo(496f, 432f)
                lineTo(429.32f, 432f)
                close()
            }
        }.build()

        return _CiLogoAmplify!!
    }

@Suppress("ObjectPropertyName")
private var _CiLogoAmplify: ImageVector? = null
