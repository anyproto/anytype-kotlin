package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiEnter: ImageVector
    get() {
        if (_CiEnter != null) {
            return _CiEnter!!
        }
        _CiEnter = ImageVector.Builder(
            name = "CiEnter",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(160f, 136f)
                verticalLineTo(240f)
                horizontalLineTo(313.37f)
                lineToRelative(-52.68f, -52.69f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.62f, -22.62f)
                lineToRelative(80f, 80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 22.62f)
                lineToRelative(-80f, 80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.62f, -22.62f)
                lineTo(313.37f, 272f)
                horizontalLineTo(160f)
                verticalLineTo(376f)
                arcToRelative(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, 56f)
                horizontalLineTo(424f)
                arcToRelative(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, -56f)
                verticalLineTo(136f)
                arcToRelative(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, -56f, -56f)
                horizontalLineTo(216f)
                arcTo(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 160f, 136f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(48f, 240f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineTo(160f)
                verticalLineTo(240f)
                close()
            }
        }.build()

        return _CiEnter!!
    }

@Suppress("ObjectPropertyName")
private var _CiEnter: ImageVector? = null
