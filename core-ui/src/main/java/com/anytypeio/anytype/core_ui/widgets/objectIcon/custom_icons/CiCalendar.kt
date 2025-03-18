package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCalendar: ImageVector
    get() {
        if (_CiCalendar != null) {
            return _CiCalendar!!
        }
        _CiCalendar = ImageVector.Builder(
            name = "CiCalendar",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(480f, 128f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64f, -64f)
                horizontalLineTo(400f)
                verticalLineTo(48.45f)
                curveToRelative(0f, -8.61f, -6.62f, -16f, -15.23f, -16.43f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 368f, 48f)
                verticalLineTo(64f)
                horizontalLineTo(144f)
                verticalLineTo(48.45f)
                curveToRelative(0f, -8.61f, -6.62f, -16f, -15.23f, -16.43f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 112f, 48f)
                verticalLineTo(64f)
                horizontalLineTo(96f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64f, 64f)
                verticalLineToRelative(12f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, 4f)
                horizontalLineTo(476f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, -4f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(32f, 416f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                lineTo(416f, 480f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                lineTo(480f, 179f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3f, -3f)
                lineTo(35f, 176f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3f, 3f)
                close()
                moveTo(376f, 208f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24f, 24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 376f, 208f)
                close()
                moveTo(376f, 288f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24f, 24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 376f, 288f)
                close()
                moveTo(296f, 208f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24f, 24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 296f, 208f)
                close()
                moveTo(296f, 288f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24f, 24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 296f, 288f)
                close()
                moveTo(296f, 368f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24f, 24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 296f, 368f)
                close()
                moveTo(216f, 288f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24f, 24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 216f, 288f)
                close()
                moveTo(216f, 368f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24f, 24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 216f, 368f)
                close()
                moveTo(136f, 288f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24f, 24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 136f, 288f)
                close()
                moveTo(136f, 368f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24f, 24f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 136f, 368f)
                close()
            }
        }.build()

        return _CiCalendar!!
    }

@Suppress("ObjectPropertyName")
private var _CiCalendar: ImageVector? = null
