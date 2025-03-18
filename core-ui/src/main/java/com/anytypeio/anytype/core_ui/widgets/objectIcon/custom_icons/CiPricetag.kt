package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPricetag: ImageVector
    get() {
        if (_CiPricetag != null) {
            return _CiPricetag!!
        }
        _CiPricetag = ImageVector.Builder(
            name = "CiPricetag",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(467f, 45.2f)
                arcTo(44.45f, 44.45f, 0f, isMoreThanHalf = false, isPositiveArc = false, 435.29f, 32f)
                horizontalLineTo(312.36f)
                arcToRelative(30.63f, 30.63f, 0f, isMoreThanHalf = false, isPositiveArc = false, -21.52f, 8.89f)
                lineTo(45.09f, 286.59f)
                arcToRelative(44.82f, 44.82f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 63.32f)
                lineToRelative(117f, 117f)
                arcToRelative(44.83f, 44.83f, 0f, isMoreThanHalf = false, isPositiveArc = false, 63.34f, 0f)
                lineToRelative(245.65f, -245.6f)
                arcTo(30.6f, 30.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 480f, 199.8f)
                verticalLineToRelative(-123f)
                arcTo(44.24f, 44.24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 467f, 45.2f)
                close()
                moveTo(384f, 160f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 384f, 160f)
                close()
            }
        }.build()

        return _CiPricetag!!
    }

@Suppress("ObjectPropertyName")
private var _CiPricetag: ImageVector? = null
