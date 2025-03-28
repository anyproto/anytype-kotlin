package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiLockOpen: ImageVector
    get() {
        if (_CiLockOpen != null) {
            return _CiLockOpen!!
        }
        _CiLockOpen = ImageVector.Builder(
            name = "CiLockOpen",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(368f, 192f)
                horizontalLineTo(192f)
                verticalLineTo(112f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = true, 128f, 0f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 0f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = true, isPositiveArc = false, -192f, 0f)
                verticalLineToRelative(80f)
                horizontalLineTo(144f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64f, 64f)
                verticalLineTo(432f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                horizontalLineTo(368f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                verticalLineTo(256f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 368f, 192f)
                close()
            }
        }.build()

        return _CiLockOpen!!
    }

@Suppress("ObjectPropertyName")
private var _CiLockOpen: ImageVector? = null
