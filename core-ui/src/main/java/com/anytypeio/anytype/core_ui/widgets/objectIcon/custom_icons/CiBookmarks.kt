package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBookmarks: ImageVector
    get() {
        if (_CiBookmarks != null) {
            return _CiBookmarks!!
        }
        _CiBookmarks = ImageVector.Builder(
            name = "CiBookmarks",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(400f, 0f)
                horizontalLineTo(176f)
                arcToRelative(64.11f, 64.11f, 0f, isMoreThanHalf = false, isPositiveArc = false, -62f, 48f)
                horizontalLineTo(342f)
                arcToRelative(74f, 74f, 0f, isMoreThanHalf = false, isPositiveArc = true, 74f, 74f)
                verticalLineTo(426.89f)
                lineToRelative(22f, 17.6f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19.34f, 0.5f)
                arcTo(16.41f, 16.41f, 0f, isMoreThanHalf = false, isPositiveArc = false, 464f, 431.57f)
                verticalLineTo(64f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 400f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(320f, 80f)
                horizontalLineTo(112f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64f, 64f)
                verticalLineTo(495.62f)
                arcTo(16.36f, 16.36f, 0f, isMoreThanHalf = false, isPositiveArc = false, 54.6f, 509f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19.71f, -0.71f)
                lineTo(216f, 388.92f)
                lineTo(357.69f, 508.24f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19.6f, 0.79f)
                arcTo(16.4f, 16.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 384f, 495.59f)
                verticalLineTo(144f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 320f, 80f)
                close()
            }
        }.build()

        return _CiBookmarks!!
    }

@Suppress("ObjectPropertyName")
private var _CiBookmarks: ImageVector? = null
