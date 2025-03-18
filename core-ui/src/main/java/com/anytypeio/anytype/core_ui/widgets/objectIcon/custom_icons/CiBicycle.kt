package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBicycle: ImageVector
    get() {
        if (_CiBicycle != null) {
            return _CiBicycle!!
        }
        _CiBicycle = ImageVector.Builder(
            name = "CiBicycle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(388f, 448f)
                arcToRelative(92f, 92f, 0f, isMoreThanHalf = true, isPositiveArc = true, 92f, -92f)
                arcTo(92.1f, 92.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 388f, 448f)
                close()
                moveTo(388f, 296f)
                arcToRelative(60f, 60f, 0f, isMoreThanHalf = true, isPositiveArc = false, 60f, 60f)
                arcTo(60.07f, 60.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 388f, 296f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(124f, 448f)
                arcToRelative(92f, 92f, 0f, isMoreThanHalf = true, isPositiveArc = true, 92f, -92f)
                arcTo(92.1f, 92.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 124f, 448f)
                close()
                moveTo(124f, 296f)
                arcToRelative(60f, 60f, 0f, isMoreThanHalf = true, isPositiveArc = false, 60f, 60f)
                arcTo(60.07f, 60.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 124f, 296f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(320f, 128f)
                arcToRelative(31.89f, 31.89f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, -32.1f)
                arcTo(31.55f, 31.55f, 0f, isMoreThanHalf = false, isPositiveArc = false, 320.2f, 64f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = false, -0.2f, 64f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(367.55f, 192f)
                horizontalLineTo(323.79f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.51f, -2.08f)
                lineToRelative(-31.74f, -58.17f)
                horizontalLineToRelative(0f)
                arcTo(31f, 31f, 0f, isMoreThanHalf = false, isPositiveArc = false, 239.16f, 124f)
                horizontalLineToRelative(0f)
                lineTo(169.3f, 194.4f)
                arcToRelative(32.56f, 32.56f, 0f, isMoreThanHalf = false, isPositiveArc = false, -9.3f, 22.4f)
                curveToRelative(0f, 17.4f, 12.6f, 23.6f, 18.5f, 27.1f)
                curveTo(207f, 260.32f, 227.07f, 272.33f, 238.08f, 279f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.92f, 3.41f)
                verticalLineToRelative(69.12f)
                curveToRelative(0f, 8.61f, 6.62f, 16f, 15.23f, 16.43f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 272f, 352f)
                verticalLineTo(266f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.66f, -13f)
                lineToRelative(-37f, -26.61f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.58f, -6f)
                lineToRelative(42f, -44.79f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6.42f, 0.79f)
                lineTo(298f, 215.77f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 312f, 224f)
                horizontalLineToRelative(56f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, -16.77f)
                curveTo(383.58f, 198.62f, 376.16f, 192f, 367.55f, 192f)
                close()
            }
        }.build()

        return _CiBicycle!!
    }

@Suppress("ObjectPropertyName")
private var _CiBicycle: ImageVector? = null
