package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiNotificationsCircle: ImageVector
    get() {
        if (_CiNotificationsCircle != null) {
            return _CiNotificationsCircle!!
        }
        _CiNotificationsCircle = ImageVector.Builder(
            name = "CiNotificationsCircle",
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
                moveTo(256f, 384f)
                curveToRelative(-20.9f, 0f, -37.52f, -8.86f, -39.75f, -27.58f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, -4.42f)
                horizontalLineToRelative(71.45f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 4.48f)
                curveTo(293.15f, 374.85f, 276.68f, 384f, 256f, 384f)
                close()
                moveTo(354f, 336f)
                lineTo(158f, 336f)
                curveToRelative(-11.84f, 0f, -18f, -15f, -11.19f, -23f)
                curveToRelative(16.33f, -19.34f, 27.87f, -27.47f, 27.87f, -80.8f)
                curveToRelative(0f, -48.87f, 25.74f, -66.21f, 47f, -74.67f)
                arcToRelative(11.35f, 11.35f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.33f, -6.68f)
                curveTo(231.7f, 138.6f, 242.14f, 128f, 256f, 128f)
                reflectiveCurveToRelative(24.28f, 10.6f, 28f, 22.86f)
                arcToRelative(11.39f, 11.39f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.34f, 6.68f)
                curveToRelative(21.21f, 8.44f, 47f, 25.81f, 47f, 74.67f)
                curveToRelative(0f, 53.33f, 11.53f, 61.46f, 27.86f, 80.8f)
                curveTo(371.94f, 321f, 365.77f, 336f, 354f, 336f)
                close()
            }
        }.build()

        return _CiNotificationsCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiNotificationsCircle: ImageVector? = null
