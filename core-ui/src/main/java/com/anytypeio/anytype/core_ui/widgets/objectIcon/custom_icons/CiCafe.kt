package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCafe: ImageVector
    get() {
        if (_CiCafe != null) {
            return _CiCafe!!
        }
        _CiCafe = ImageVector.Builder(
            name = "CiCafe",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(432f, 64f)
                lineTo(96f, 64f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, 80f)
                lineTo(80f, 272f)
                arcToRelative(96.11f, 96.11f, 0f, isMoreThanHalf = false, isPositiveArc = false, 96f, 96f)
                lineTo(288f, 368f)
                arcToRelative(96.11f, 96.11f, 0f, isMoreThanHalf = false, isPositiveArc = false, 96f, -96f)
                lineTo(384f, 192f)
                horizontalLineToRelative(18f)
                arcToRelative(62.07f, 62.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 62f, -62f)
                lineTo(464f, 96f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 432f, 64f)
                close()
                moveTo(432f, 130f)
                arcToRelative(30f, 30f, 0f, isMoreThanHalf = false, isPositiveArc = true, -30f, 30f)
                lineTo(384f, 160f)
                lineTo(384f, 96f)
                horizontalLineToRelative(48f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(400f, 400f)
                horizontalLineTo(64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineTo(400f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                close()
            }
        }.build()

        return _CiCafe!!
    }

@Suppress("ObjectPropertyName")
private var _CiCafe: ImageVector? = null
