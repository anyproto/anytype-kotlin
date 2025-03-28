package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBus: ImageVector
    get() {
        if (_CiBus != null) {
            return _CiBus!!
        }
        _CiBus = ImageVector.Builder(
            name = "CiBus",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(400f, 32f)
                lineTo(112f, 32f)
                arcTo(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 80f)
                lineTo(64f, 400f)
                arcToRelative(47.91f, 47.91f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 35.74f)
                lineTo(80f, 454f)
                arcToRelative(26f, 26f, 0f, isMoreThanHalf = false, isPositiveArc = false, 26f, 26f)
                horizontalLineToRelative(28f)
                arcToRelative(26f, 26f, 0f, isMoreThanHalf = false, isPositiveArc = false, 26f, -26f)
                verticalLineToRelative(-6f)
                lineTo(352f, 448f)
                verticalLineToRelative(6f)
                arcToRelative(26f, 26f, 0f, isMoreThanHalf = false, isPositiveArc = false, 26f, 26f)
                horizontalLineToRelative(28f)
                arcToRelative(26f, 26f, 0f, isMoreThanHalf = false, isPositiveArc = false, 26f, -26f)
                lineTo(432f, 435.74f)
                arcTo(47.91f, 47.91f, 0f, isMoreThanHalf = false, isPositiveArc = false, 448f, 400f)
                lineTo(448f, 80f)
                arcTo(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 400f, 32f)
                close()
                moveTo(147.47f, 399.82f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 28.35f, -28.35f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 147.47f, 399.82f)
                close()
                moveTo(236f, 288f)
                lineTo(112f, 288f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, -16f)
                lineTo(96f, 144f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, -16f)
                lineTo(236f, 128f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 4f)
                lineTo(240f, 284f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 236f, 288f)
                close()
                moveTo(256f, 96f)
                lineTo(112.46f, 96f)
                curveToRelative(-8.6f, 0f, -16f, -6.6f, -16.44f, -15.19f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 112f, 64f)
                lineTo(399.54f, 64f)
                curveToRelative(8.6f, 0f, 16f, 6.6f, 16.44f, 15.19f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 400f, 96f)
                lineTo(256f, 96f)
                close()
                moveTo(276f, 128f)
                lineTo(400f, 128f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 16f)
                lineTo(416f, 272f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, 16f)
                lineTo(276f, 288f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4f, -4f)
                lineTo(272f, 132f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 276f, 128f)
                close()
                moveTo(336.18f, 371.47f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 28.35f, 28.35f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 336.18f, 371.47f)
                close()
            }
        }.build()

        return _CiBus!!
    }

@Suppress("ObjectPropertyName")
private var _CiBus: ImageVector? = null
