package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPizza: ImageVector
    get() {
        if (_CiPizza != null) {
            return _CiPizza!!
        }
        _CiPizza = ImageVector.Builder(
            name = "CiPizza",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(441.82f, 67.83f)
                lineToRelative(0f, 0f)
                curveTo(383.44f, 44.73f, 317.3f, 32f, 255.56f, 32f)
                curveTo(192f, 32f, 125.76f, 44.53f, 69f, 67.26f)
                curveTo(48.7f, 75.49f, 45.21f, 90f, 48.71f, 100.82f)
                lineTo(52.78f, 111f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 21.31f, 8.69f)
                curveToRelative(10.8f, -4.76f, 23.93f, -10.54f, 27f, -11.78f)
                curveTo(145.1f, 89.64f, 198.71f, 80f, 256f, 80f)
                curveToRelative(57.47f, 0f, 108.09f, 9.24f, 154.76f, 28.25f)
                horizontalLineToRelative(0f)
                curveToRelative(4.42f, 1.8f, 14.88f, 6.42f, 26.17f, 11.46f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 21.35f, -8.59f)
                lineTo(462f, 102f)
                lineToRelative(0.34f, -0.9f)
                curveTo(465.79f, 90.89f, 462.48f, 76.05f, 441.82f, 67.83f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(409.18f, 140.86f)
                curveTo(363.67f, 122.53f, 307.68f, 112f, 255.56f, 112f)
                arcToRelative(425f, 425f, 0f, isMoreThanHalf = false, isPositiveArc = false, -153.74f, 28.89f)
                curveToRelative(-0.53f, 0.21f, -2.06f, 0.88f, -4.29f, 1.88f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, 21.27f)
                curveToRelative(4f, 8.71f, 9.42f, 20.58f, 15.5f, 33.89f)
                curveTo(137.94f, 270f, 199.21f, 404f, 227.26f, 462f)
                arcTo(31.74f, 31.74f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 480f)
                horizontalLineToRelative(0f)
                arcToRelative(31.73f, 31.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, 28.76f, -18.06f)
                lineToRelative(0.06f, -0.13f)
                lineToRelative(137.3f, -297.57f)
                arcToRelative(15.94f, 15.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8.31f, -21.45f)
                curveToRelative(-2.26f, -0.95f, -3.85f, -1.61f, -4.5f, -1.87f)
                close()
                moveTo(194.08f, 223.93f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 29.85f, -29.85f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 194.08f, 223.93f)
                close()
                moveTo(258.08f, 351.93f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 29.85f, -29.85f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 258.08f, 351.93f)
                close()
                moveTo(322.08f, 239.93f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 29.85f, -29.85f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 322.08f, 239.93f)
                close()
            }
        }.build()

        return _CiPizza!!
    }

@Suppress("ObjectPropertyName")
private var _CiPizza: ImageVector? = null
