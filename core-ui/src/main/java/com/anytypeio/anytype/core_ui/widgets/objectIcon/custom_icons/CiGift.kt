package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiGift: ImageVector
    get() {
        if (_CiGift != null) {
            return _CiGift!!
        }
        _CiGift = ImageVector.Builder(
            name = "CiGift",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(80f, 416f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                horizontalLineToRelative(92f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, -4f)
                verticalLineTo(292f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, -4f)
                horizontalLineTo(88f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, 8f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(240f, 252f)
                lineTo(240f, 144f)
                horizontalLineToRelative(32f)
                lineTo(272f, 252f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, 4f)
                lineTo(416f, 256f)
                arcToRelative(47.93f, 47.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, -2.75f)
                horizontalLineToRelative(0f)
                arcTo(48.09f, 48.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 464f, 208f)
                lineTo(464f, 192f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, -48f, -48f)
                lineTo(375.46f, 144f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.7f, -3f)
                arcTo(72f, 72f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 58.82f)
                arcTo(72f, 72f, 0f, isMoreThanHalf = false, isPositiveArc = false, 138.24f, 141f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.7f, 3f)
                lineTo(96f, 144f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, -48f, 48f)
                verticalLineToRelative(16f)
                arcToRelative(48.09f, 48.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 45.25f)
                horizontalLineToRelative(0f)
                arcTo(47.93f, 47.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, 96f, 256f)
                lineTo(236f, 256f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 240f, 252f)
                close()
                moveTo(272f, 104f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = true, isPositiveArc = true, 40f, 40f)
                lineTo(272f, 144f)
                close()
                moveTo(197.14f, 64.1f)
                arcTo(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, 240f, 104f)
                verticalLineToRelative(40f)
                lineTo(200f, 144f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.86f, -79.89f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(276f, 480f)
                horizontalLineToRelative(92f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                verticalLineTo(296f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, -8f)
                horizontalLineTo(276f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, 4f)
                verticalLineTo(476f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 276f, 480f)
                close()
            }
        }.build()

        return _CiGift!!
    }

@Suppress("ObjectPropertyName")
private var _CiGift: ImageVector? = null
