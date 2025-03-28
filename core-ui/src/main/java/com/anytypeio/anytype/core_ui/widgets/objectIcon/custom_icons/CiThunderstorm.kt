package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiThunderstorm: ImageVector
    get() {
        if (_CiThunderstorm != null) {
            return _CiThunderstorm!!
        }
        _CiThunderstorm = ImageVector.Builder(
            name = "CiThunderstorm",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(96f, 416f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.3f, -23.16f)
                lineToRelative(24f, -48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.62f, 14.32f)
                lineToRelative(-24f, 48f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 96f, 416f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(120f, 480f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.3f, -23.16f)
                lineToRelative(16f, -32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.62f, 14.32f)
                lineToRelative(-16f, 32f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 120f, 480f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(376f, 416f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.3f, -23.16f)
                lineToRelative(24f, -48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.62f, 14.32f)
                lineToRelative(-24f, 48f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 376f, 416f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(400f, 480f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.3f, -23.16f)
                lineToRelative(16f, -32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.62f, 14.32f)
                lineToRelative(-16f, 32f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 400f, 480f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(405.84f, 136.9f)
                arcTo(151.25f, 151.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 358.24f, 55f)
                arcToRelative(153f, 153f, 0f, isMoreThanHalf = false, isPositiveArc = false, -241.81f, 51.86f)
                curveTo(60.5f, 110.16f, 16f, 156.65f, 16f, 213.33f)
                curveTo(16f, 272.15f, 63.91f, 320f, 122.8f, 320f)
                horizontalLineToRelative(66.31f)
                lineToRelative(-12.89f, 77.37f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 192f, 416f)
                horizontalLineToRelative(32f)
                verticalLineToRelative(64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 29f, 9.3f)
                lineToRelative(80f, -112f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 320f, 352f)
                horizontalLineTo(292.49f)
                lineToRelative(8f, -32f)
                horizontalLineTo(404.33f)
                arcToRelative(91.56f, 91.56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.51f, -183.1f)
                close()
            }
        }.build()

        return _CiThunderstorm!!
    }

@Suppress("ObjectPropertyName")
private var _CiThunderstorm: ImageVector? = null
