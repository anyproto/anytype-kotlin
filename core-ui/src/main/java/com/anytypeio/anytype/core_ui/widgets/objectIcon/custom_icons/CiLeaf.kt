package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiLeaf: ImageVector
    get() {
        if (_CiLeaf != null) {
            return _CiLeaf!!
        }
        _CiLeaf = ImageVector.Builder(
            name = "CiLeaf",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(161.35f, 242f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.62f, -0.68f)
                curveToRelative(73.63f, 69.36f, 147.51f, 111.56f, 234.45f, 133.07f)
                curveToRelative(11.73f, -32f, 12.77f, -67.22f, 2.64f, -101.58f)
                curveToRelative(-13.44f, -45.59f, -44.74f, -85.31f, -90.49f, -114.86f)
                curveToRelative(-40.84f, -26.38f, -81.66f, -33.25f, -121.15f, -39.89f)
                curveToRelative(-49.82f, -8.38f, -96.88f, -16.3f, -141.79f, -63.85f)
                curveToRelative(-5f, -5.26f, -11.81f, -7.37f, -18.32f, -5.66f)
                curveToRelative(-7.44f, 2f, -12.43f, 7.88f, -14.82f, 17.6f)
                curveToRelative(-5.6f, 22.75f, -2f, 86.51f, 13.75f, 153.82f)
                curveToRelative(25.29f, 108.14f, 65.65f, 162.86f, 95.06f, 189.73f)
                curveToRelative(38f, 34.69f, 87.62f, 53.9f, 136.93f, 53.9f)
                arcTo(186f, 186f, 0f, isMoreThanHalf = false, isPositiveArc = false, 308f, 461.56f)
                curveToRelative(41.71f, -6.32f, 76.43f, -27.27f, 96f, -57.75f)
                curveToRelative(-89.49f, -23.28f, -165.94f, -67.55f, -242f, -139.16f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 161.35f, 242f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(467.43f, 384.19f)
                curveToRelative(-16.83f, -2.59f, -33.13f, -5.84f, -49f, -9.77f)
                arcToRelative(157.71f, 157.71f, 0f, isMoreThanHalf = false, isPositiveArc = true, -12.13f, 25.68f)
                curveToRelative(-0.73f, 1.25f, -1.5f, 2.49f, -2.29f, 3.71f)
                arcToRelative(584.21f, 584.21f, 0f, isMoreThanHalf = false, isPositiveArc = false, 58.56f, 12f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = false, 4.87f, -31.62f)
                close()
            }
        }.build()

        return _CiLeaf!!
    }

@Suppress("ObjectPropertyName")
private var _CiLeaf: ImageVector? = null
