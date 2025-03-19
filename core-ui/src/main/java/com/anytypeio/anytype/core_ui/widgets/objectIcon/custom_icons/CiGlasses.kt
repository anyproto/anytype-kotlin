package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiGlasses: ImageVector
    get() {
        if (_CiGlasses != null) {
            return _CiGlasses!!
        }
        _CiGlasses = ImageVector.Builder(
            name = "CiGlasses",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(464f, 184f)
                horizontalLineTo(453.1f)
                arcToRelative(78.72f, 78.72f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, -7.18f)
                curveTo(419.5f, 171f, 396.26f, 168f, 368f, 168f)
                reflectiveCurveToRelative(-51.5f, 3f, -69.06f, 8.82f)
                curveToRelative(-14.06f, 4.69f, -20.25f, 9.86f, -22.25f, 11.87f)
                horizontalLineToRelative(0f)
                arcToRelative(47.94f, 47.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, -41.36f, 0f)
                horizontalLineToRelative(0f)
                curveToRelative(-2f, -2f, -8.19f, -7.18f, -22.25f, -11.87f)
                curveTo(195.5f, 171f, 172.26f, 168f, 144f, 168f)
                reflectiveCurveToRelative(-51.5f, 3f, -69.06f, 8.82f)
                arcToRelative(78.72f, 78.72f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 7.18f)
                horizontalLineTo(48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineToRelative(0.17f)
                curveToRelative(1f, 45.46f, 6.44f, 72.78f, 18.11f, 92.23f)
                arcToRelative(66.78f, 66.78f, 0f, isMoreThanHalf = false, isPositiveArc = false, 31.92f, 28f)
                curveToRelative(12.23f, 5.24f, 27.22f, 7.79f, 45.8f, 7.79f)
                curveToRelative(24.15f, 0f, 58.48f, -3.71f, 77.72f, -35.77f)
                curveToRelative(9.68f, -16.14f, 15.09f, -37.69f, 17.21f, -70.52f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 240f, 232f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 0f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.07f, 5.71f)
                curveToRelative(2.12f, 32.83f, 7.53f, 54.38f, 17.21f, 70.52f)
                arcToRelative(66.78f, 66.78f, 0f, isMoreThanHalf = false, isPositiveArc = false, 31.92f, 28f)
                curveToRelative(12.23f, 5.24f, 27.22f, 7.79f, 45.8f, 7.79f)
                curveToRelative(24.15f, 0f, 58.48f, -3.71f, 77.72f, -35.77f)
                curveToRelative(11.67f, -19.45f, 17.13f, -46.77f, 18.11f, -92.23f)
                horizontalLineTo(464f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                close()
            }
        }.build()

        return _CiGlasses!!
    }

@Suppress("ObjectPropertyName")
private var _CiGlasses: ImageVector? = null
