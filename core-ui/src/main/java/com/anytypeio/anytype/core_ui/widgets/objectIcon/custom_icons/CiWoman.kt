package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiWoman: ImageVector
    get() {
        if (_CiWoman != null) {
            return _CiWoman!!
        }
        _CiWoman = ImageVector.Builder(
            name = "CiWoman",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(255.75f, 56f)
                moveToRelative(-56f, 0f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = true, isPositiveArc = true, 112f, 0f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = true, isPositiveArc = true, -112f, 0f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(394.63f, 277.9f)
                lineTo(384.3f, 243.49f)
                reflectiveCurveToRelative(0f, -0.07f, 0f, -0.11f)
                lineToRelative(-22.46f, -74.86f)
                horizontalLineToRelative(-0.05f)
                lineToRelative(-2.51f, -8.45f)
                arcToRelative(44.87f, 44.87f, 0f, isMoreThanHalf = false, isPositiveArc = false, -43f, -32.08f)
                horizontalLineToRelative(-120f)
                arcToRelative(44.84f, 44.84f, 0f, isMoreThanHalf = false, isPositiveArc = false, -43f, 32.08f)
                lineToRelative(-2.51f, 8.45f)
                horizontalLineToRelative(-0.06f)
                lineToRelative(-22.46f, 74.86f)
                reflectiveCurveToRelative(0f, 0.07f, 0f, 0.11f)
                lineTo(117.88f, 277.9f)
                curveToRelative(-3.12f, 10.39f, 2.3f, 21.66f, 12.57f, 25.14f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, 25.6f, -13.18f)
                lineToRelative(25.58f, -85.25f)
                horizontalLineToRelative(0f)
                lineToRelative(2.17f, -7.23f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 199.33f, 200f)
                arcToRelative(7.78f, 7.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.17f, 1.61f)
                verticalLineToRelative(0f)
                lineTo(155.43f, 347.4f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 170.75f, 368f)
                horizontalLineToRelative(29f)
                verticalLineTo(482.69f)
                curveToRelative(0f, 16.46f, 10.53f, 29.31f, 24f, 29.31f)
                reflectiveCurveToRelative(24f, -12.85f, 24f, -29.31f)
                verticalLineTo(368f)
                horizontalLineToRelative(16f)
                verticalLineTo(482.69f)
                curveToRelative(0f, 16.46f, 10.53f, 29.31f, 24f, 29.31f)
                reflectiveCurveToRelative(24f, -12.85f, 24f, -29.31f)
                verticalLineTo(368f)
                horizontalLineToRelative(30f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.33f, -20.6f)
                lineTo(313.34f, 201.59f)
                arcToRelative(7.52f, 7.52f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.16f, -1.59f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15.54f, -2.63f)
                lineToRelative(2.17f, 7.23f)
                horizontalLineToRelative(0f)
                lineToRelative(25.57f, 85.25f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, 382.05f, 303f)
                curveTo(392.32f, 299.56f, 397.74f, 288.29f, 394.63f, 277.9f)
                close()
            }
        }.build()

        return _CiWoman!!
    }

@Suppress("ObjectPropertyName")
private var _CiWoman: ImageVector? = null
