package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPrism: ImageVector
    get() {
        if (_CiPrism != null) {
            return _CiPrism!!
        }
        _CiPrism = ImageVector.Builder(
            name = "CiPrism",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(487.83f, 319.44f)
                lineTo(295.63f, 36.88f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, -79.26f, 0f)
                lineTo(24.17f, 319.44f)
                arcTo(47.1f, 47.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 41.1f, 387.57f)
                lineTo(233.3f, 490.32f)
                arcToRelative(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 45.4f, 0f)
                lineTo(470.9f, 387.57f)
                arcToRelative(47.1f, 47.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16.93f, -68.13f)
                close()
                moveTo(56.57f, 360.44f)
                arcToRelative(16.12f, 16.12f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8f, -10.38f)
                arcToRelative(16.8f, 16.8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.37f, -13.62f)
                lineTo(232.66f, 69.26f)
                curveToRelative(2.18f, -3.21f, 7.34f, -1.72f, 7.34f, 2.13f)
                verticalLineToRelative(374f)
                curveToRelative(0f, 5.9f, -6.54f, 9.63f, -11.87f, 6.78f)
                close()
            }
        }.build()

        return _CiPrism!!
    }

@Suppress("ObjectPropertyName")
private var _CiPrism: ImageVector? = null
