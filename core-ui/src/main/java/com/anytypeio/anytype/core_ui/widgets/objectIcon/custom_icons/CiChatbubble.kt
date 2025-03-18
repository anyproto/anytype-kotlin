package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiChatbubble: ImageVector
    get() {
        if (_CiChatbubble != null) {
            return _CiChatbubble!!
        }
        _CiChatbubble = ImageVector.Builder(
            name = "CiChatbubble",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(76.83f, 480f)
                arcToRelative(25.69f, 25.69f, 0f, isMoreThanHalf = false, isPositiveArc = true, -25.57f, -25.74f)
                arcToRelative(29.13f, 29.13f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.2f, -7.63f)
                lineTo(70.88f, 380f)
                curveToRelative(0.77f, -2.46f, -0.1f, -4.94f, -1.23f, -6.9f)
                lineToRelative(-0.22f, -0.4f)
                curveToRelative(-0.08f, -0.13f, -0.46f, -0.66f, -0.73f, -1.05f)
                reflectiveCurveToRelative(-0.58f, -0.81f, -0.86f, -1.22f)
                lineToRelative(-0.19f, -0.27f)
                arcTo(215.66f, 215.66f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 251.37f)
                curveToRelative(-0.18f, -57.59f, 22.35f, -112f, 63.46f, -153.28f)
                curveTo(138f, 55.47f, 194.9f, 32f, 255.82f, 32f)
                arcTo(227.4f, 227.4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 398f, 81.84f)
                curveToRelative(39.45f, 31.75f, 66.87f, 76f, 77.21f, 124.68f)
                arcToRelative(213.5f, 213.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.78f, 45f)
                curveToRelative(0f, 58.93f, -22.64f, 114.28f, -63.76f, 155.87f)
                curveToRelative(-41.48f, 42f, -97.18f, 65.06f, -156.83f, 65.06f)
                curveToRelative(-21f, 0f, -47.87f, -5.36f, -60.77f, -9f)
                curveToRelative(-15.52f, -4.34f, -30.23f, -10f, -31.85f, -10.6f)
                arcToRelative(15.12f, 15.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.37f, -1f)
                arcToRelative(14.75f, 14.75f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.8f, 1.15f)
                lineToRelative(-0.85f, 0.33f)
                lineTo(87.28f, 477.71f)
                arcTo(29.44f, 29.44f, 0f, isMoreThanHalf = false, isPositiveArc = true, 76.83f, 480f)
                close()
                moveTo(74.83f, 448.2f)
                close()
                moveTo(87.48f, 380f)
                horizontalLineToRelative(0f)
                close()
            }
        }.build()

        return _CiChatbubble!!
    }

@Suppress("ObjectPropertyName")
private var _CiChatbubble: ImageVector? = null
