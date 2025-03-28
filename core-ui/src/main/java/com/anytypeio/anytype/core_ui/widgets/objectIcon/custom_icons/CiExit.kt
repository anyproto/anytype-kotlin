package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiExit: ImageVector
    get() {
        if (_CiExit != null) {
            return _CiExit!!
        }
        _CiExit = ImageVector.Builder(
            name = "CiExit",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(336f, 376f)
                verticalLineTo(272f)
                horizontalLineTo(191f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineTo(336f)
                verticalLineTo(136f)
                arcToRelative(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, -56f, -56f)
                horizontalLineTo(88f)
                arcToRelative(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, -56f, 56f)
                verticalLineTo(376f)
                arcToRelative(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, 56f)
                horizontalLineTo(280f)
                arcTo(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 336f, 376f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(425.37f, 272f)
                lineToRelative(-52.68f, 52.69f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 22.62f, 22.62f)
                lineToRelative(80f, -80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -22.62f)
                lineToRelative(-80f, -80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -22.62f, 22.62f)
                lineTo(425.37f, 240f)
                horizontalLineTo(336f)
                verticalLineToRelative(32f)
                close()
            }
        }.build()

        return _CiExit!!
    }

@Suppress("ObjectPropertyName")
private var _CiExit: ImageVector? = null
