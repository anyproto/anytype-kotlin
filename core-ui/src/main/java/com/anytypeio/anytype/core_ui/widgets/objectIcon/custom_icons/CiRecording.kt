package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiRecording: ImageVector
    get() {
        if (_CiRecording != null) {
            return _CiRecording!!
        }
        _CiRecording = ImageVector.Builder(
            name = "CiRecording",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(380.79f, 144.05f)
                curveTo(321.69f, 145.7f, 273.67f, 193.76f, 272f, 252.86f)
                arcToRelative(111.64f, 111.64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 30.36f, 79.77f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 301f, 336f)
                horizontalLineTo(211f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.44f, -3.37f)
                arcTo(111.64f, 111.64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 240f, 252.86f)
                curveToRelative(-1.63f, -59.1f, -49.65f, -107.16f, -108.75f, -108.81f)
                arcTo(112.12f, 112.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 255.53f)
                curveTo(15.75f, 317.77f, 67f, 368f, 129.24f, 368f)
                horizontalLineTo(382.76f)
                curveTo(445f, 368f, 496.25f, 317.77f, 496f, 255.53f)
                arcTo(112.12f, 112.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 380.79f, 144.05f)
                close()
            }
        }.build()

        return _CiRecording!!
    }

@Suppress("ObjectPropertyName")
private var _CiRecording: ImageVector? = null
