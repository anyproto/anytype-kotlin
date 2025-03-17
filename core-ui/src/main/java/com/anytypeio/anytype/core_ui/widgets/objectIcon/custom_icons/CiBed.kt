package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBed: ImageVector
    get() {
        if (_CiBed != null) {
            return _CiBed!!
        }
        _CiBed = ImageVector.Builder(
            name = "CiBed",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(432f, 230.7f)
                arcToRelative(79.44f, 79.44f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, -6.7f)
                horizontalLineTo(112f)
                arcToRelative(79.51f, 79.51f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, 6.69f)
                horizontalLineToRelative(0f)
                arcTo(80.09f, 80.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 304f)
                verticalLineTo(416f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 0f)
                verticalLineToRelative(-8f)
                arcToRelative(8.1f, 8.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, -8f)
                horizontalLineTo(440f)
                arcToRelative(8.1f, 8.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 8f)
                verticalLineToRelative(8f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 0f)
                verticalLineTo(304f)
                arcTo(80.09f, 80.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 432f, 230.7f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(376f, 80f)
                horizontalLineTo(136f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, -56f, 56f)
                verticalLineToRelative(72f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.11f, 3.84f)
                arcTo(95.5f, 95.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 112f, 208f)
                horizontalLineToRelative(4.23f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, -3.55f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 152f, 176f)
                horizontalLineToRelative(56f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.8f, 28.45f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, 3.55f)
                horizontalLineToRelative(24.46f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, -3.55f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 304f, 176f)
                horizontalLineToRelative(56f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.8f, 28.45f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, 3.55f)
                horizontalLineTo(400f)
                arcToRelative(95.51f, 95.51f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26.89f, 3.85f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 432f, 208f)
                verticalLineTo(136f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 376f, 80f)
                close()
            }
        }.build()

        return _CiBed!!
    }

@Suppress("ObjectPropertyName")
private var _CiBed: ImageVector? = null
