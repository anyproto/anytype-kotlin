package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiNotificationsOffCircle: ImageVector
    get() {
        if (_CiNotificationsOffCircle != null) {
            return _CiNotificationsOffCircle!!
        }
        _CiNotificationsOffCircle = ImageVector.Builder(
            name = "CiNotificationsOffCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 48f)
                curveTo(141.31f, 48f, 48f, 141.31f, 48f, 256f)
                reflectiveCurveToRelative(93.31f, 208f, 208f, 208f)
                reflectiveCurveToRelative(208f, -93.31f, 208f, -208f)
                reflectiveCurveTo(370.69f, 48f, 256f, 48f)
                close()
                moveTo(146.83f, 313f)
                curveToRelative(16.33f, -19.34f, 27.86f, -27.47f, 27.86f, -80.8f)
                quadToRelative(0f, -3.75f, 0.2f, -7.26f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7f, -2.52f)
                lineToRelative(98f, 106.87f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.94f, 6.7f)
                lineTo(158f, 335.99f)
                curveTo(146.18f, 336f, 140.06f, 321f, 146.83f, 313f)
                close()
                moveTo(295.76f, 356.41f)
                curveTo(293.53f, 375.14f, 276.92f, 384f, 256f, 384f)
                reflectiveCurveToRelative(-37.51f, -8.86f, -39.75f, -27.58f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, -4.42f)
                horizontalLineToRelative(71.53f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 295.76f, 356.42f)
                close()
                moveTo(362.76f, 373.83f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.6f, -1.08f)
                lineToRelative(-192f, -212f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.68f, -21.52f)
                lineToRelative(192f, 212f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 362.76f, 373.84f)
                close()
                moveTo(361f, 323.21f)
                lineTo(216.49f, 165.53f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.3f, -6.36f)
                curveToRelative(1.31f, -0.58f, 2.61f, -1.12f, 3.89f, -1.63f)
                arcToRelative(11.33f, 11.33f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.32f, -6.68f)
                curveTo(231.72f, 138.6f, 242.15f, 128f, 256f, 128f)
                reflectiveCurveToRelative(24.29f, 10.6f, 28f, 22.86f)
                arcToRelative(11.34f, 11.34f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.34f, 6.68f)
                curveToRelative(21.21f, 8.44f, 47f, 25.81f, 47f, 74.67f)
                curveToRelative(0f, 53.33f, 11.54f, 61.46f, 27.87f, 80.8f)
                arcToRelative(12.09f, 12.09f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.76f, 7.25f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 361f, 323.21f)
                close()
            }
        }.build()

        return _CiNotificationsOffCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiNotificationsOffCircle: ImageVector? = null
