package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiColorFill: ImageVector
    get() {
        if (_CiColorFill != null) {
            return _CiColorFill!!
        }
        _CiColorFill = ImageVector.Builder(
            name = "CiColorFill",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(416f, 480f)
                curveToRelative(-35.29f, 0f, -64f, -29.11f, -64f, -64.88f)
                curveToRelative(0f, -33.29f, 28.67f, -65.4f, 44.08f, -82.64f)
                curveToRelative(1.87f, -2.1f, 3.49f, -3.91f, 4.68f, -5.31f)
                arcToRelative(19.94f, 19.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, 30.55f, 0f)
                curveToRelative(1.13f, 1.31f, 2.63f, 3f, 4.36f, 4.93f)
                curveToRelative(15.5f, 17.3f, 44.33f, 49.51f, 44.33f, 83.05f)
                curveTo(480f, 450.89f, 451.29f, 480f, 416f, 480f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(398.23f, 276.64f)
                lineTo(166.89f, 47.22f)
                arcToRelative(52.1f, 52.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -73.6f, 0f)
                lineToRelative(-4.51f, 4.51f)
                arcTo(53.2f, 53.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 72.89f, 89.06f)
                arcTo(51.66f, 51.66f, 0f, isMoreThanHalf = false, isPositiveArc = false, 88.14f, 126f)
                lineToRelative(41.51f, 41.5f)
                lineTo(45f, 252f)
                arcToRelative(44.52f, 44.52f, 0f, isMoreThanHalf = false, isPositiveArc = false, -13f, 32f)
                arcToRelative(42.81f, 42.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.5f, 30.84f)
                lineToRelative(131.24f, 126f)
                arcToRelative(44f, 44f, 0f, isMoreThanHalf = false, isPositiveArc = false, 61.08f, -0.18f)
                lineTo(361.93f, 320.38f)
                arcToRelative(15.6f, 15.6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.23f, -4.29f)
                arcToRelative(69.21f, 69.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.93f, -0.86f)
                horizontalLineToRelative(0.3f)
                arcToRelative(22.53f, 22.53f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.84f, -38.59f)
                close()
                moveTo(152.29f, 144.85f)
                lineToRelative(-41.53f, -41.52f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -28.34f)
                lineToRelative(5.16f, -5.15f)
                arcToRelative(20.07f, 20.07f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.39f, 0f)
                lineTo(186f, 111.21f)
                close()
            }
        }.build()

        return _CiColorFill!!
    }

@Suppress("ObjectPropertyName")
private var _CiColorFill: ImageVector? = null
