package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiToday: ImageVector
    get() {
        if (_CiToday != null) {
            return _CiToday!!
        }
        _CiToday = ImageVector.Builder(
            name = "CiToday",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(416f, 64f)
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
                verticalLineTo(128f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 416f, 64f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(477f, 176f)
                horizontalLineTo(35f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3f, 3f)
                verticalLineTo(416f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                horizontalLineTo(416f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                verticalLineTo(179f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 477f, 176f)
                close()
                moveTo(224f, 307.43f)
                arcTo(28.57f, 28.57f, 0f, isMoreThanHalf = false, isPositiveArc = true, 195.43f, 336f)
                horizontalLineTo(124.57f)
                arcTo(28.57f, 28.57f, 0f, isMoreThanHalf = false, isPositiveArc = true, 96f, 307.43f)
                verticalLineTo(236.57f)
                arcTo(28.57f, 28.57f, 0f, isMoreThanHalf = false, isPositiveArc = true, 124.57f, 208f)
                horizontalLineToRelative(70.86f)
                arcTo(28.57f, 28.57f, 0f, isMoreThanHalf = false, isPositiveArc = true, 224f, 236.57f)
                close()
            }
        }.build()

        return _CiToday!!
    }

@Suppress("ObjectPropertyName")
private var _CiToday: ImageVector? = null
