package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiClipboard: ImageVector
    get() {
        if (_CiClipboard != null) {
            return _CiClipboard!!
        }
        _CiClipboard = ImageVector.Builder(
            name = "CiClipboard",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(368f, 48f)
                lineTo(356.59f, 48f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -7.44f, -5.08f)
                arcTo(42.18f, 42.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 309.87f, 16f)
                lineTo(202.13f, 16f)
                arcToRelative(42.18f, 42.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, -39.28f, 26.92f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 155.41f, 48f)
                lineTo(144f, 48f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64f, 64f)
                lineTo(80f, 432f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                lineTo(368f, 496f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                lineTo(432f, 112f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 368f, 48f)
                close()
                moveTo(319.87f, 112f)
                lineTo(192.13f, 112f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                lineTo(319.87f, 80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
            }
        }.build()

        return _CiClipboard!!
    }

@Suppress("ObjectPropertyName")
private var _CiClipboard: ImageVector? = null
