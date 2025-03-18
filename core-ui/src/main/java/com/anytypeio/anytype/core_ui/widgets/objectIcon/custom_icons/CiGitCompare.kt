package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiGitCompare: ImageVector
    get() {
        if (_CiGitCompare != null) {
            return _CiGitCompare!!
        }
        _CiGitCompare = ImageVector.Builder(
            name = "CiGitCompare",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(218.31f, 340.69f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 191f, 352f)
                verticalLineToRelative(32f)
                horizontalLineTo(171f)
                arcToRelative(28f, 28f, 0f, isMoreThanHalf = false, isPositiveArc = true, -28f, -28f)
                verticalLineTo(152f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = false, -64f, -1.16f)
                verticalLineTo(356f)
                arcToRelative(92.1f, 92.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 92f, 92f)
                horizontalLineToRelative(20f)
                verticalLineToRelative(32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 27.31f, 11.31f)
                lineToRelative(64f, -64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -22.62f)
                close()
                moveTo(112f, 64f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 80f, 96f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 112f, 64f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(432f, 360.61f)
                verticalLineTo(156f)
                arcToRelative(92.1f, 92.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -92f, -92f)
                horizontalLineTo(320f)
                verticalLineTo(32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -27.31f, -11.31f)
                lineToRelative(-64f, 64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 22.62f)
                lineToRelative(64f, 64f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 320f, 160f)
                verticalLineTo(128f)
                horizontalLineToRelative(20f)
                arcToRelative(28f, 28f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28f, 28f)
                verticalLineTo(360.61f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = false, 64f, 0f)
                close()
                moveTo(400f, 448f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 400f, 448f)
                close()
            }
        }.build()

        return _CiGitCompare!!
    }

@Suppress("ObjectPropertyName")
private var _CiGitCompare: ImageVector? = null
