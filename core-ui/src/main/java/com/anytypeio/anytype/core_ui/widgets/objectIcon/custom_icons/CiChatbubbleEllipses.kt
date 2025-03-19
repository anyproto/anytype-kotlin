package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiChatbubbleEllipses: ImageVector
    get() {
        if (_CiChatbubbleEllipses != null) {
            return _CiChatbubbleEllipses!!
        }
        _CiChatbubbleEllipses = ImageVector.Builder(
            name = "CiChatbubbleEllipses",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(398f, 81.84f)
                arcTo(227.4f, 227.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 255.82f, 32f)
                curveTo(194.9f, 32f, 138f, 55.47f, 95.46f, 98.09f)
                curveTo(54.35f, 139.33f, 31.82f, 193.78f, 32f, 251.37f)
                arcTo(215.66f, 215.66f, 0f, isMoreThanHalf = false, isPositiveArc = false, 67.65f, 370.13f)
                lineToRelative(0.19f, 0.27f)
                curveToRelative(0.28f, 0.41f, 0.57f, 0.82f, 0.86f, 1.22f)
                reflectiveCurveToRelative(0.65f, 0.92f, 0.73f, 1.05f)
                lineToRelative(0.22f, 0.4f)
                curveToRelative(1.13f, 2f, 2f, 4.44f, 1.23f, 6.9f)
                lineTo(52.46f, 446.63f)
                arcToRelative(29.13f, 29.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.2f, 7.63f)
                arcTo(25.69f, 25.69f, 0f, isMoreThanHalf = false, isPositiveArc = false, 76.83f, 480f)
                arcToRelative(29.44f, 29.44f, 0f, isMoreThanHalf = false, isPositiveArc = false, 10.45f, -2.29f)
                lineToRelative(67.49f, -24.36f)
                lineToRelative(0.85f, -0.33f)
                arcToRelative(14.75f, 14.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.8f, -1.15f)
                arcToRelative(15.12f, 15.12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.37f, 1f)
                curveToRelative(1.62f, 0.63f, 16.33f, 6.26f, 31.85f, 10.6f)
                curveToRelative(12.9f, 3.6f, 39.74f, 9f, 60.77f, 9f)
                curveToRelative(59.65f, 0f, 115.35f, -23.1f, 156.83f, -65.06f)
                curveTo(457.36f, 365.77f, 480f, 310.42f, 480f, 251.49f)
                arcToRelative(213.5f, 213.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4.78f, -45f)
                curveTo(464.88f, 157.87f, 437.46f, 113.59f, 398f, 81.84f)
                close()
                moveTo(87.48f, 380f)
                horizontalLineToRelative(0f)
                close()
                moveTo(160f, 288f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 160f, 288f)
                close()
                moveTo(256f, 288f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 288f)
                close()
                moveTo(352f, 288f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 352f, 288f)
                close()
            }
        }.build()

        return _CiChatbubbleEllipses!!
    }

@Suppress("ObjectPropertyName")
private var _CiChatbubbleEllipses: ImageVector? = null
