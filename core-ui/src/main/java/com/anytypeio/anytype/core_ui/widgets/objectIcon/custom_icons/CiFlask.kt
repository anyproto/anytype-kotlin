package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFlask: ImageVector
    get() {
        if (_CiFlask != null) {
            return _CiFlask!!
        }
        _CiFlask = ImageVector.Builder(
            name = "CiFlask",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(452.32f, 365f)
                lineTo(327.4f, 167.12f)
                arcTo(48.07f, 48.07f, 0f, isMoreThanHalf = false, isPositiveArc = true, 320f, 141.48f)
                verticalLineTo(64f)
                horizontalLineToRelative(15.56f)
                curveToRelative(8.61f, 0f, 16f, -6.62f, 16.43f, -15.23f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 336f, 32f)
                horizontalLineTo(176.45f)
                curveToRelative(-8.61f, 0f, -16f, 6.62f, -16.43f, 15.23f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 176f, 64f)
                horizontalLineToRelative(16f)
                verticalLineToRelative(77.48f)
                arcToRelative(47.92f, 47.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, -7.41f, 25.63f)
                lineTo(59.68f, 365f)
                arcToRelative(74f, 74f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.5f, 75.84f)
                curveTo(70.44f, 465.19f, 96.36f, 480f, 124.13f, 480f)
                horizontalLineTo(387.87f)
                curveToRelative(27.77f, 0f, 53.69f, -14.81f, 66.95f, -39.21f)
                arcTo(74f, 74f, 0f, isMoreThanHalf = false, isPositiveArc = false, 452.32f, 365f)
                close()
                moveTo(211.66f, 184.2f)
                arcTo(79.94f, 79.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 224f, 141.48f)
                verticalLineTo(68f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, -4f)
                horizontalLineToRelative(56f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 4f)
                verticalLineToRelative(73.48f)
                arcToRelative(79.94f, 79.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12.35f, 42.72f)
                lineToRelative(57.8f, 91.53f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 351.37f, 288f)
                horizontalLineTo(160.63f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6.77f, -12.27f)
                close()
            }
        }.build()

        return _CiFlask!!
    }

@Suppress("ObjectPropertyName")
private var _CiFlask: ImageVector? = null
