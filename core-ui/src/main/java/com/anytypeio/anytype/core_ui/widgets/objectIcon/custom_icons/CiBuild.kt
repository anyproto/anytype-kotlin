package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBuild: ImageVector
    get() {
        if (_CiBuild != null) {
            return _CiBuild!!
        }
        _CiBuild = ImageVector.Builder(
            name = "CiBuild",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(469.54f, 120.52f)
                horizontalLineToRelative(0f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -25.54f, -4f)
                lineTo(382.56f, 178f)
                arcToRelative(16.12f, 16.12f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.63f, 0f)
                lineTo(333.37f, 151.4f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -22.63f)
                lineToRelative(61.18f, -61.19f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4.78f, -25.92f)
                horizontalLineToRelative(0f)
                curveTo(343.56f, 21f, 285.88f, 31.78f, 249.51f, 67.88f)
                curveToRelative(-30.9f, 30.68f, -40.11f, 78.62f, -25.25f, 131.53f)
                arcToRelative(15.89f, 15.89f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.49f, 16f)
                lineTo(53.29f, 367.46f)
                arcToRelative(64.17f, 64.17f, 0f, isMoreThanHalf = true, isPositiveArc = false, 90.6f, 90.64f)
                lineTo(297.57f, 291.25f)
                arcToRelative(15.9f, 15.9f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15.77f, -4.57f)
                arcToRelative(179.3f, 179.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 46.22f, 6.37f)
                curveToRelative(33.4f, 0f, 62.71f, -10.81f, 83.85f, -31.64f)
                curveTo(482.56f, 222.84f, 488.53f, 157.42f, 469.54f, 120.52f)
                close()
                moveTo(99.48f, 447.15f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 28.34f, -28.35f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 99.48f, 447.15f)
                close()
            }
        }.build()

        return _CiBuild!!
    }

@Suppress("ObjectPropertyName")
private var _CiBuild: ImageVector? = null
