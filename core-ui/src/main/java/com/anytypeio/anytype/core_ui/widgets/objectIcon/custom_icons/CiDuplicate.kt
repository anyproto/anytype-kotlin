package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiDuplicate: ImageVector
    get() {
        if (_CiDuplicate != null) {
            return _CiDuplicate!!
        }
        _CiDuplicate = ImageVector.Builder(
            name = "CiDuplicate",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(408f, 112f)
                horizontalLineTo(184f)
                arcToRelative(72f, 72f, 0f, isMoreThanHalf = false, isPositiveArc = false, -72f, 72f)
                verticalLineTo(408f)
                arcToRelative(72f, 72f, 0f, isMoreThanHalf = false, isPositiveArc = false, 72f, 72f)
                horizontalLineTo(408f)
                arcToRelative(72f, 72f, 0f, isMoreThanHalf = false, isPositiveArc = false, 72f, -72f)
                verticalLineTo(184f)
                arcTo(72f, 72f, 0f, isMoreThanHalf = false, isPositiveArc = false, 408f, 112f)
                close()
                moveTo(375.55f, 312f)
                horizontalLineTo(312f)
                verticalLineToRelative(63.55f)
                curveToRelative(0f, 8.61f, -6.62f, 16f, -15.23f, 16.43f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 280f, 376f)
                verticalLineTo(312f)
                horizontalLineTo(216.45f)
                curveToRelative(-8.61f, 0f, -16f, -6.62f, -16.43f, -15.23f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 216f, 280f)
                horizontalLineToRelative(64f)
                verticalLineTo(216.45f)
                curveToRelative(0f, -8.61f, 6.62f, -16f, 15.23f, -16.43f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 312f, 216f)
                verticalLineToRelative(64f)
                horizontalLineToRelative(64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 16.77f)
                curveTo(391.58f, 305.38f, 384.16f, 312f, 375.55f, 312f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(395.88f, 80f)
                arcTo(72.12f, 72.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 328f, 32f)
                horizontalLineTo(104f)
                arcToRelative(72f, 72f, 0f, isMoreThanHalf = false, isPositiveArc = false, -72f, 72f)
                verticalLineTo(328f)
                arcToRelative(72.12f, 72.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 67.88f)
                verticalLineTo(160f)
                arcToRelative(80f, 80f, 0f, isMoreThanHalf = false, isPositiveArc = true, 80f, -80f)
                close()
            }
        }.build()

        return _CiDuplicate!!
    }

@Suppress("ObjectPropertyName")
private var _CiDuplicate: ImageVector? = null
