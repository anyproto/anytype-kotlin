package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiUmbrella: ImageVector
    get() {
        if (_CiUmbrella != null) {
            return _CiUmbrella!!
        }
        _CiUmbrella = ImageVector.Builder(
            name = "CiUmbrella",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(414.39f, 113.61f)
                arcTo(222.26f, 222.26f, 0f, isMoreThanHalf = false, isPositiveArc = false, 278.06f, 49.07f)
                arcToRelative(8.09f, 8.09f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6.88f, -5.62f)
                arcToRelative(15.79f, 15.79f, 0f, isMoreThanHalf = false, isPositiveArc = false, -30.36f, 0f)
                arcToRelative(8.09f, 8.09f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6.88f, 5.62f)
                arcTo(224f, 224f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 271.52f)
                arcToRelative(16.41f, 16.41f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.24f, 13.87f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 20.07f, -2.08f)
                arcToRelative(51.89f, 51.89f, 0f, isMoreThanHalf = false, isPositiveArc = true, 73.31f, -0.06f)
                arcToRelative(15.94f, 15.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 22.6f, 0.15f)
                arcToRelative(62.59f, 62.59f, 0f, isMoreThanHalf = false, isPositiveArc = true, 81.49f, -5.87f)
                horizontalLineToRelative(0f)
                arcToRelative(8.24f, 8.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.29f, 6.59f)
                verticalLineTo(431.54f)
                curveToRelative(0f, 8.6f, -6.6f, 16f, -15.19f, 16.44f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 208f, 432f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16.29f, -16f)
                curveToRelative(-9f, 0.16f, -15.9f, 8.11f, -15.7f, 17.1f)
                arcTo(48.06f, 48.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 223.38f, 480f)
                curveToRelative(26.88f, 0.34f, 48.62f, -21.93f, 48.62f, -48.81f)
                verticalLineTo(284.12f)
                arcToRelative(8.24f, 8.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.29f, -6.59f)
                horizontalLineToRelative(0f)
                arcToRelative(62.59f, 62.59f, 0f, isMoreThanHalf = false, isPositiveArc = true, 81.4f, 5.78f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 22.62f, 0f)
                arcToRelative(51.91f, 51.91f, 0f, isMoreThanHalf = false, isPositiveArc = true, 73.38f, 0f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19.54f, 2.41f)
                arcTo(16.4f, 16.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 480f, 271.51f)
                arcTo(222.54f, 222.54f, 0f, isMoreThanHalf = false, isPositiveArc = false, 414.39f, 113.61f)
                close()
            }
        }.build()

        return _CiUmbrella!!
    }

@Suppress("ObjectPropertyName")
private var _CiUmbrella: ImageVector? = null
