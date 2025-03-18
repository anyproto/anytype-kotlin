package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiOptions: ImageVector
    get() {
        if (_CiOptions != null) {
            return _CiOptions!!
        }
        _CiOptions = ImageVector.Builder(
            name = "CiOptions",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(64f, 144f)
                horizontalLineTo(290.75f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 90.5f, 0f)
                horizontalLineTo(448f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                horizontalLineTo(381.25f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, -90.5f, 0f)
                horizontalLineTo(64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(448f, 368f)
                horizontalLineTo(381.25f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, -90.5f, 0f)
                horizontalLineTo(64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineTo(290.75f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 90.5f, 0f)
                horizontalLineTo(448f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(448f, 240f)
                horizontalLineTo(221.25f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, -90.5f, 0f)
                horizontalLineTo(64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineToRelative(66.75f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 90.5f, 0f)
                horizontalLineTo(448f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                close()
            }
        }.build()

        return _CiOptions!!
    }

@Suppress("ObjectPropertyName")
private var _CiOptions: ImageVector? = null
