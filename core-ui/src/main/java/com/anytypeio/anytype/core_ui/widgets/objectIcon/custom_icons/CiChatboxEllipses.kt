package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiChatboxEllipses: ImageVector
    get() {
        if (_CiChatboxEllipses != null) {
            return _CiChatboxEllipses!!
        }
        _CiChatboxEllipses = ImageVector.Builder(
            name = "CiChatboxEllipses",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(408f, 48f)
                lineTo(104f, 48f)
                arcToRelative(72.08f, 72.08f, 0f, isMoreThanHalf = false, isPositiveArc = false, -72f, 72f)
                lineTo(32f, 312f)
                arcToRelative(72.08f, 72.08f, 0f, isMoreThanHalf = false, isPositiveArc = false, 72f, 72f)
                horizontalLineToRelative(24f)
                verticalLineToRelative(64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 26.25f, 12.29f)
                lineTo(245.74f, 384f)
                lineTo(408f, 384f)
                arcToRelative(72.08f, 72.08f, 0f, isMoreThanHalf = false, isPositiveArc = false, 72f, -72f)
                lineTo(480f, 120f)
                arcTo(72.08f, 72.08f, 0f, isMoreThanHalf = false, isPositiveArc = false, 408f, 48f)
                close()
                moveTo(160f, 248f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 160f, 248f)
                close()
                moveTo(256f, 248f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 248f)
                close()
                moveTo(352f, 248f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 352f, 248f)
                close()
            }
        }.build()

        return _CiChatboxEllipses!!
    }

@Suppress("ObjectPropertyName")
private var _CiChatboxEllipses: ImageVector? = null
