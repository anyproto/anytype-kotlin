package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiGrid: ImageVector
    get() {
        if (_CiGrid != null) {
            return _CiGrid!!
        }
        _CiGrid = ImageVector.Builder(
            name = "CiGrid",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(204f, 240f)
                horizontalLineTo(68f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, -36f, -36f)
                verticalLineTo(68f)
                arcTo(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 68f, 32f)
                horizontalLineTo(204f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, 36f)
                verticalLineTo(204f)
                arcTo(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 204f, 240f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(444f, 240f)
                horizontalLineTo(308f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, -36f, -36f)
                verticalLineTo(68f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, -36f)
                horizontalLineTo(444f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, 36f)
                verticalLineTo(204f)
                arcTo(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 444f, 240f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(204f, 480f)
                horizontalLineTo(68f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, -36f, -36f)
                verticalLineTo(308f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, -36f)
                horizontalLineTo(204f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, 36f)
                verticalLineTo(444f)
                arcTo(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 204f, 480f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(444f, 480f)
                horizontalLineTo(308f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, -36f, -36f)
                verticalLineTo(308f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, -36f)
                horizontalLineTo(444f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, 36f)
                verticalLineTo(444f)
                arcTo(36f, 36f, 0f, isMoreThanHalf = false, isPositiveArc = true, 444f, 480f)
                close()
            }
        }.build()

        return _CiGrid!!
    }

@Suppress("ObjectPropertyName")
private var _CiGrid: ImageVector? = null
