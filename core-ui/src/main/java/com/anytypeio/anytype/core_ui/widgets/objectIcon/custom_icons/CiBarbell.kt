package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBarbell: ImageVector
    get() {
        if (_CiBarbell != null) {
            return _CiBarbell!!
        }
        _CiBarbell = ImageVector.Builder(
            name = "CiBarbell",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(467f, 176f)
                arcToRelative(29.94f, 29.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, -25.32f, 12.5f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.64f, -1.14f)
                verticalLineTo(150.71f)
                curveToRelative(0f, -20.75f, -16.34f, -38.21f, -37.08f, -38.7f)
                arcTo(38f, 38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 362f, 150f)
                verticalLineToRelative(82f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, 2f)
                horizontalLineTo(152f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, -2f)
                verticalLineTo(150.71f)
                curveToRelative(0f, -20.75f, -16.34f, -38.21f, -37.08f, -38.7f)
                arcTo(38f, 38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 74f, 150f)
                verticalLineToRelative(37.38f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.64f, 1.14f)
                arcTo(29.94f, 29.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 45f, 176f)
                curveToRelative(-16.3f, 0.51f, -29f, 14.31f, -29f, 30.62f)
                verticalLineToRelative(98.72f)
                curveToRelative(0f, 16.31f, 12.74f, 30.11f, 29f, 30.62f)
                arcToRelative(29.94f, 29.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 25.32f, -12.5f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 74f, 324.62f)
                verticalLineToRelative(36.67f)
                curveTo(74f, 382f, 90.34f, 399.5f, 111.08f, 400f)
                arcTo(38f, 38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 150f, 362f)
                verticalLineTo(280f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, -2f)
                horizontalLineTo(360f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 2f)
                verticalLineToRelative(81.29f)
                curveToRelative(0f, 20.75f, 16.34f, 38.21f, 37.08f, 38.7f)
                arcTo(38f, 38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 438f, 362f)
                verticalLineTo(324.62f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.64f, -1.14f)
                arcTo(29.94f, 29.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 467f, 336f)
                curveToRelative(16.3f, -0.51f, 29f, -14.31f, 29f, -30.62f)
                verticalLineTo(206.64f)
                curveTo(496f, 190.33f, 483.26f, 176.53f, 467f, 176f)
                close()
            }
        }.build()

        return _CiBarbell!!
    }

@Suppress("ObjectPropertyName")
private var _CiBarbell: ImageVector? = null
