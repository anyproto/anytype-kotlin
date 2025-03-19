package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiAlbums: ImageVector
    get() {
        if (_CiAlbums != null) {
            return _CiAlbums!!
        }
        _CiAlbums = ImageVector.Builder(
            name = "CiAlbums",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(368f, 96f)
                horizontalLineTo(144f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineTo(368f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(400f, 144f)
                horizontalLineTo(112f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineTo(400f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(419.13f, 448f)
                horizontalLineTo(92.87f)
                arcTo(44.92f, 44.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, 48f, 403.13f)
                verticalLineTo(204.87f)
                arcTo(44.92f, 44.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, 92.87f, 160f)
                horizontalLineTo(419.13f)
                arcTo(44.92f, 44.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, 464f, 204.87f)
                verticalLineTo(403.13f)
                arcTo(44.92f, 44.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, 419.13f, 448f)
                close()
            }
        }.build()

        return _CiAlbums!!
    }

@Suppress("ObjectPropertyName")
private var _CiAlbums: ImageVector? = null
