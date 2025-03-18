package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPint: ImageVector
    get() {
        if (_CiPint != null) {
            return _CiPint!!
        }
        _CiPint = ImageVector.Builder(
            name = "CiPint",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(399f, 99.29f)
                curveToRelative(-0.15f, -2.13f, -0.3f, -4.35f, -0.44f, -6.68f)
                lineTo(395.69f, 46f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, -31.91f, -30f)
                horizontalLineTo(148.21f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 116.3f, 46f)
                lineToRelative(-2.91f, 46.63f)
                curveToRelative(-0.14f, 2.31f, -0.29f, 4.51f, -0.43f, 6.62f)
                curveToRelative(-1.29f, 19.24f, -2.23f, 33.14f, 3.73f, 65.66f)
                curveToRelative(1.67f, 9.11f, 5.22f, 22.66f, 9.73f, 39.82f)
                curveToRelative(12.61f, 48f, 33.71f, 128.36f, 33.71f, 195.63f)
                verticalLineTo(472f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24f, 24f)
                horizontalLineTo(327.87f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 24f, -24f)
                verticalLineTo(400.38f)
                curveToRelative(0f, -77.09f, 21.31f, -153.29f, 34f, -198.81f)
                curveToRelative(4.38f, -15.63f, 7.83f, -28f, 9.41f, -36.62f)
                curveTo(401.27f, 132.44f, 400.33f, 118.53f, 399f, 99.29f)
                close()
                moveTo(364f, 51.75f)
                lineToRelative(1.5f, 24f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4f, 4.25f)
                horizontalLineToRelative(-211f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4f, -4.25f)
                lineToRelative(1.48f, -24f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 152f, 48f)
                horizontalLineTo(360f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 364f, 51.75f)
                close()
            }
        }.build()

        return _CiPint!!
    }

@Suppress("ObjectPropertyName")
private var _CiPint: ImageVector? = null
