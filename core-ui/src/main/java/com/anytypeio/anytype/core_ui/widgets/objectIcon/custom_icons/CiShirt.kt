package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiShirt: ImageVector
    get() {
        if (_CiShirt != null) {
            return _CiShirt!!
        }
        _CiShirt = ImageVector.Builder(
            name = "CiShirt",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 96f)
                curveToRelative(33.08f, 0f, 60.71f, -25.78f, 64f, -58f)
                curveToRelative(0.3f, -3f, -3f, -6f, -6f, -6f)
                horizontalLineToRelative(0f)
                arcToRelative(13f, 13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4.74f, 0.9f)
                curveToRelative(-0.2f, 0.08f, -21.1f, 8.1f, -53.26f, 8.1f)
                reflectiveCurveToRelative(-53.1f, -8f, -53.26f, -8.1f)
                arcToRelative(16.21f, 16.21f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.3f, -0.9f)
                horizontalLineToRelative(-0.06f)
                arcTo(5.69f, 5.69f, 0f, isMoreThanHalf = false, isPositiveArc = false, 192f, 38f)
                curveTo(195.35f, 70.16f, 223f, 96f, 256f, 96f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(485.29f, 89.9f)
                lineTo(356f, 44.64f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.27f, 3.16f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = false, isPositiveArc = true, -189.38f, 0f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 156f, 44.64f)
                lineTo(26.71f, 89.9f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16.28f, 108f)
                lineToRelative(16.63f, 88f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 46.83f, 208.9f)
                lineToRelative(48.88f, 5.52f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.1f, 8.19f)
                lineToRelative(-7.33f, 240.9f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.1f, 14.94f)
                arcTo(17.49f, 17.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, 112f, 480f)
                horizontalLineTo(400f)
                arcToRelative(17.49f, 17.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.42f, -1.55f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.1f, -14.94f)
                lineToRelative(-7.33f, -240.9f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.1f, -8.19f)
                lineToRelative(48.88f, -5.52f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 479.09f, 196f)
                lineToRelative(16.63f, -88f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 485.29f, 89.9f)
                close()
            }
        }.build()

        return _CiShirt!!
    }

@Suppress("ObjectPropertyName")
private var _CiShirt: ImageVector? = null
