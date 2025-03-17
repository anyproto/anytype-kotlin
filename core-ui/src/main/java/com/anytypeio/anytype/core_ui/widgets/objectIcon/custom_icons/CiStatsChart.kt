package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiStatsChart: ImageVector
    get() {
        if (_CiStatsChart != null) {
            return _CiStatsChart!!
        }
        _CiStatsChart = ImageVector.Builder(
            name = "CiStatsChart",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(104f, 496f)
                horizontalLineTo(72f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, -24f)
                verticalLineTo(328f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, -24f)
                horizontalLineToRelative(32f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 24f)
                verticalLineTo(472f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 104f, 496f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(328f, 496f)
                horizontalLineTo(296f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, -24f)
                verticalLineTo(232f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, -24f)
                horizontalLineToRelative(32f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 24f)
                verticalLineTo(472f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 328f, 496f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(440f, 496f)
                horizontalLineTo(408f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, -24f)
                verticalLineTo(120f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, -24f)
                horizontalLineToRelative(32f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 24f)
                verticalLineTo(472f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 440f, 496f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(216f, 496f)
                horizontalLineTo(184f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24f, -24f)
                verticalLineTo(40f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, -24f)
                horizontalLineToRelative(32f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 24f)
                verticalLineTo(472f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 216f, 496f)
                close()
            }
        }.build()

        return _CiStatsChart!!
    }

@Suppress("ObjectPropertyName")
private var _CiStatsChart: ImageVector? = null
