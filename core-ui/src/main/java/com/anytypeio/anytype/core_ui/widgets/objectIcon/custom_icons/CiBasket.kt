package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBasket: ImageVector
    get() {
        if (_CiBasket != null) {
            return _CiBasket!!
        }
        _CiBasket = ImageVector.Builder(
            name = "CiBasket",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(424.11f, 192f)
                lineTo(360f, 192f)
                lineTo(268.8f, 70.4f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -25.6f, 0f)
                lineTo(152f, 192f)
                lineTo(87.89f, 192f)
                arcToRelative(32.57f, 32.57f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32.62f, 32.44f)
                arcToRelative(30.3f, 30.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.31f, 9f)
                lineToRelative(46.27f, 163.14f)
                arcToRelative(50.72f, 50.72f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48.84f, 36.91f)
                lineTo(360.31f, 433.49f)
                arcToRelative(51.21f, 51.21f, 0f, isMoreThanHalf = false, isPositiveArc = false, 49f, -36.86f)
                lineToRelative(46.33f, -163.36f)
                arcToRelative(15.62f, 15.62f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.46f, -2.36f)
                lineToRelative(0.53f, -4.93f)
                arcToRelative(13.3f, 13.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.09f, -1.55f)
                arcTo(32.57f, 32.57f, 0f, isMoreThanHalf = false, isPositiveArc = false, 424.11f, 192f)
                close()
                moveTo(256f, 106.67f)
                lineTo(320f, 192f)
                lineTo(192f, 192f)
                close()
                moveTo(256f, 351.67f)
                arcToRelative(37.7f, 37.7f, 0f, isMoreThanHalf = true, isPositiveArc = true, 37.88f, -37.7f)
                arcTo(37.87f, 37.87f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 351.63f)
                close()
            }
        }.build()

        return _CiBasket!!
    }

@Suppress("ObjectPropertyName")
private var _CiBasket: ImageVector? = null
