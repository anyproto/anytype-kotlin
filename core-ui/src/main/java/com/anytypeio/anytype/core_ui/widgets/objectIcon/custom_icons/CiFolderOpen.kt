package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFolderOpen: ImageVector
    get() {
        if (_CiFolderOpen != null) {
            return _CiFolderOpen!!
        }
        _CiFolderOpen = ImageVector.Builder(
            name = "CiFolderOpen",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(408f, 96f)
                horizontalLineTo(252.11f)
                arcToRelative(23.89f, 23.89f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.31f, -4f)
                lineTo(211f, 73.41f)
                arcTo(55.77f, 55.77f, 0f, isMoreThanHalf = false, isPositiveArc = false, 179.89f, 64f)
                horizontalLineTo(104f)
                arcToRelative(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, -56f, 56f)
                verticalLineToRelative(24f)
                horizontalLineTo(464f)
                curveTo(464f, 113.12f, 438.88f, 96f, 408f, 96f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(423.75f, 448f)
                horizontalLineTo(88.25f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = true, -55.93f, -55.15f)
                lineTo(16.18f, 228.11f)
                lineToRelative(0f, -0.28f)
                arcTo(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 64f, 176f)
                horizontalLineToRelative(384.1f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 47.8f, 51.83f)
                lineToRelative(0f, 0.28f)
                lineTo(479.68f, 392.85f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = true, 423.75f, 448f)
                close()
                moveTo(479.9f, 226.55f)
                horizontalLineToRelative(0f)
                close()
            }
        }.build()

        return _CiFolderOpen!!
    }

@Suppress("ObjectPropertyName")
private var _CiFolderOpen: ImageVector? = null
