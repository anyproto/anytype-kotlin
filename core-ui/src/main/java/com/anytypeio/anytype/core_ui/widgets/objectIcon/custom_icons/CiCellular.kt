package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCellular: ImageVector
    get() {
        if (_CiCellular != null) {
            return _CiCellular!!
        }
        _CiCellular = ImageVector.Builder(
            name = "CiCellular",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(472f, 432f)
                horizontalLineTo(424f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, -24f)
                verticalLineTo(104f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, -24f)
                horizontalLineToRelative(48f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 24f)
                verticalLineTo(408f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 472f, 432f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(344f, 432f)
                horizontalLineTo(296f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, -24f)
                verticalLineTo(184f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, -24f)
                horizontalLineToRelative(48f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 24f)
                verticalLineTo(408f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 344f, 432f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(216f, 432f)
                horizontalLineTo(168f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, -24f)
                verticalLineTo(248f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, -24f)
                horizontalLineToRelative(48f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 24f)
                verticalLineTo(408f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 216f, 432f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(88f, 432f)
                horizontalLineTo(40f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, -24f)
                verticalLineTo(312f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, -24f)
                horizontalLineTo(88f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 24f)
                verticalLineToRelative(96f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 88f, 432f)
                close()
            }
        }.build()

        return _CiCellular!!
    }

@Suppress("ObjectPropertyName")
private var _CiCellular: ImageVector? = null
