package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMap: ImageVector
    get() {
        if (_CiMap != null) {
            return _CiMap!!
        }
        _CiMap = ImageVector.Builder(
            name = "CiMap",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(48.17f, 113.34f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 141.24f)
                verticalLineTo(438f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 47f, 28.37f)
                curveToRelative(0.43f, -0.23f, 0.85f, -0.47f, 1.26f, -0.74f)
                lineToRelative(84.14f, -55.05f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.63f, -6.72f)
                verticalLineTo(46.45f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -12.51f, -6.63f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(212.36f, 39.31f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 200f, 46f)
                verticalLineTo(403.56f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.63f, 6.72f)
                lineToRelative(96f, 62.42f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 312f, 466f)
                verticalLineTo(108.67f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.64f, -6.73f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(464.53f, 46.47f)
                arcToRelative(31.64f, 31.64f, 0f, isMoreThanHalf = false, isPositiveArc = false, -31.5f, -0.88f)
                arcToRelative(12.07f, 12.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.25f, 0.74f)
                lineToRelative(-84.15f, 55f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.63f, 6.72f)
                verticalLineTo(465.51f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12.52f, 6.63f)
                lineToRelative(107.07f, -73.46f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16.41f, -28f)
                verticalLineToRelative(-296f)
                arcTo(32.76f, 32.76f, 0f, isMoreThanHalf = false, isPositiveArc = false, 464.53f, 46.47f)
                close()
            }
        }.build()

        return _CiMap!!
    }

@Suppress("ObjectPropertyName")
private var _CiMap: ImageVector? = null
