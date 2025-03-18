package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiVolumeOff: ImageVector
    get() {
        if (_CiVolumeOff != null) {
            return _CiVolumeOff!!
        }
        _CiVolumeOff = ImageVector.Builder(
            name = "CiVolumeOff",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(344f, 416f)
                arcToRelative(23.92f, 23.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.21f, -4.69f)
                curveToRelative(-0.23f, -0.16f, -0.44f, -0.33f, -0.66f, -0.51f)
                lineToRelative(-91.46f, -74.9f)
                horizontalLineTo(168f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, -24f)
                verticalLineTo(200.07f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, -24f)
                horizontalLineToRelative(69.65f)
                lineToRelative(91.46f, -74.9f)
                curveToRelative(0.22f, -0.18f, 0.43f, -0.35f, 0.66f, -0.51f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 368f, 120f)
                verticalLineTo(392f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, 24f)
                close()
            }
        }.build()

        return _CiVolumeOff!!
    }

@Suppress("ObjectPropertyName")
private var _CiVolumeOff: ImageVector? = null
