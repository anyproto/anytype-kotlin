package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiDesktop: ImageVector
    get() {
        if (_CiDesktop != null) {
            return _CiDesktop!!
        }
        _CiDesktop = ImageVector.Builder(
            name = "CiDesktop",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(16f, 352f)
                arcToRelative(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 48f)
                lineTo(197.88f, 400f)
                lineToRelative(-4f, 32f)
                lineTo(144f, 432f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                lineTo(368f, 464f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                lineTo(318.12f, 432f)
                lineToRelative(-4f, -32f)
                lineTo(448f, 400f)
                arcToRelative(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, -48f)
                lineTo(496f, 304f)
                lineTo(16f, 304f)
                close()
                moveTo(256f, 336f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, -16f, 16f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 336f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(496f, 96f)
                arcToRelative(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, -48f, -48f)
                horizontalLineTo(64f)
                arcTo(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 96f)
                verticalLineTo(288f)
                horizontalLineTo(496f)
                close()
            }
        }.build()

        return _CiDesktop!!
    }

@Suppress("ObjectPropertyName")
private var _CiDesktop: ImageVector? = null
