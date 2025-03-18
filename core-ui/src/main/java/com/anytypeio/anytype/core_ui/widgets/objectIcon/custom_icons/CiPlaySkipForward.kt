package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPlaySkipForward: ImageVector
    get() {
        if (_CiPlaySkipForward != null) {
            return _CiPlaySkipForward!!
        }
        _CiPlaySkipForward = ImageVector.Builder(
            name = "CiPlaySkipForward",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(400f, 64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 16f)
                verticalLineTo(216.43f)
                lineTo(151.23f, 77.11f)
                arcToRelative(35.13f, 35.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -35.77f, -0.44f)
                curveTo(103.46f, 83.47f, 96f, 96.63f, 96f, 111f)
                verticalLineTo(401f)
                curveToRelative(0f, 14.37f, 7.46f, 27.53f, 19.46f, 34.33f)
                arcToRelative(35.14f, 35.14f, 0f, isMoreThanHalf = false, isPositiveArc = false, 35.77f, -0.45f)
                lineTo(384f, 295.57f)
                verticalLineTo(432f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 0f)
                verticalLineTo(80f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 400f, 64f)
                close()
            }
        }.build()

        return _CiPlaySkipForward!!
    }

@Suppress("ObjectPropertyName")
private var _CiPlaySkipForward: ImageVector? = null
