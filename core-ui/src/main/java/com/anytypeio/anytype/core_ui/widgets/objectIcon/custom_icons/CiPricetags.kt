package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPricetags: ImageVector
    get() {
        if (_CiPricetags != null) {
            return _CiPricetags!!
        }
        _CiPricetags = ImageVector.Builder(
            name = "CiPricetags",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(448f, 183.8f)
                verticalLineToRelative(-123f)
                arcTo(44.66f, 44.66f, 0f, isMoreThanHalf = false, isPositiveArc = false, 403.29f, 16f)
                horizontalLineTo(280.36f)
                arcToRelative(30.62f, 30.62f, 0f, isMoreThanHalf = false, isPositiveArc = false, -21.51f, 8.89f)
                lineTo(13.09f, 270.58f)
                arcToRelative(44.86f, 44.86f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 63.34f)
                lineToRelative(117f, 117f)
                arcToRelative(44.84f, 44.84f, 0f, isMoreThanHalf = false, isPositiveArc = false, 63.33f, 0f)
                lineTo(439.11f, 205.31f)
                arcTo(30.6f, 30.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 448f, 183.8f)
                close()
                moveTo(352f, 144f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 352f, 144f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(496f, 64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 16f)
                verticalLineTo(207.37f)
                lineTo(218.69f, 468.69f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = false, 22.62f, 22.62f)
                lineToRelative(262f, -262f)
                arcTo(29.84f, 29.84f, 0f, isMoreThanHalf = false, isPositiveArc = false, 512f, 208f)
                verticalLineTo(80f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 496f, 64f)
                close()
            }
        }.build()

        return _CiPricetags!!
    }

@Suppress("ObjectPropertyName")
private var _CiPricetags: ImageVector? = null
