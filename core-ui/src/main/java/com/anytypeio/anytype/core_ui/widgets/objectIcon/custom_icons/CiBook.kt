package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBook: ImageVector
    get() {
        if (_CiBook != null) {
            return _CiBook!!
        }
        _CiBook = ImageVector.Builder(
            name = "CiBook",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(202.24f, 74f)
                curveTo(166.11f, 56.75f, 115.61f, 48.3f, 48f, 48f)
                horizontalLineToRelative(0f)
                arcToRelative(31.36f, 31.36f, 0f, isMoreThanHalf = false, isPositiveArc = false, -17.92f, 5.33f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 79.9f)
                verticalLineTo(366f)
                curveToRelative(0f, 19.34f, 13.76f, 33.93f, 32f, 33.93f)
                curveToRelative(71.07f, 0f, 142.36f, 6.64f, 185.06f, 47f)
                arcToRelative(4.11f, 4.11f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.94f, -3f)
                verticalLineTo(106.82f)
                arcToRelative(15.89f, 15.89f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.46f, -12f)
                arcTo(143f, 143f, 0f, isMoreThanHalf = false, isPositiveArc = false, 202.24f, 74f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(481.92f, 53.3f)
                arcTo(31.33f, 31.33f, 0f, isMoreThanHalf = false, isPositiveArc = false, 464f, 48f)
                horizontalLineToRelative(0f)
                curveToRelative(-67.61f, 0.3f, -118.11f, 8.71f, -154.24f, 26f)
                arcToRelative(143.31f, 143.31f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32.31f, 20.78f)
                arcToRelative(15.93f, 15.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.45f, 12f)
                verticalLineTo(443.91f)
                arcToRelative(3.93f, 3.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.68f, 2.81f)
                curveToRelative(25.67f, -25.5f, 70.72f, -46.82f, 185.36f, -46.81f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, -32f)
                verticalLineToRelative(-288f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 481.92f, 53.3f)
                close()
            }
        }.build()

        return _CiBook!!
    }

@Suppress("ObjectPropertyName")
private var _CiBook: ImageVector? = null
