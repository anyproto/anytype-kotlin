package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBackspace: ImageVector
    get() {
        if (_CiBackspace != null) {
            return _CiBackspace!!
        }
        _CiBackspace = ImageVector.Builder(
            name = "CiBackspace",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(403.13f, 96f)
                horizontalLineTo(156.87f)
                arcToRelative(44.9f, 44.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, -33.68f, 15.27f)
                arcToRelative(15.88f, 15.88f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.91f, 2.7f)
                lineTo(32f, 247.75f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 16.5f)
                lineToRelative(89.15f, 133.57f)
                arcToRelative(16.24f, 16.24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2f, 2.88f)
                arcToRelative(44.89f, 44.89f, 0f, isMoreThanHalf = false, isPositiveArc = false, 33.7f, 15.3f)
                horizontalLineTo(403.13f)
                arcTo(44.92f, 44.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, 448f, 371.13f)
                verticalLineTo(140.87f)
                arcTo(44.92f, 44.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, 403.13f, 96f)
                close()
                moveTo(348f, 311f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, -22.63f, 22.62f)
                lineTo(271.67f, 280f)
                lineTo(218f, 333.65f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 195.35f, 311f)
                lineTo(249f, 257.33f)
                lineToRelative(-53.69f, -53.69f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 218f, 181f)
                lineToRelative(53.69f, 53.7f)
                lineTo(325.36f, 181f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 348f, 203.64f)
                lineToRelative(-53.7f, 53.69f)
                close()
            }
        }.build()

        return _CiBackspace!!
    }

@Suppress("ObjectPropertyName")
private var _CiBackspace: ImageVector? = null
