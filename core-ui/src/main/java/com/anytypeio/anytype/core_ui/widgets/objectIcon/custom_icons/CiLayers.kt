package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiLayers: ImageVector
    get() {
        if (_CiLayers != null) {
            return _CiLayers!!
        }
        _CiLayers = ImageVector.Builder(
            name = "CiLayers",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 256f)
                curveToRelative(-13.47f, 0f, -26.94f, -2.39f, -37.44f, -7.17f)
                lineToRelative(-148f, -67.49f)
                curveTo(63.79f, 178.26f, 48f, 169.25f, 48f, 152.24f)
                reflectiveCurveToRelative(15.79f, -26f, 22.58f, -29.12f)
                lineTo(219.86f, 55.05f)
                curveToRelative(20.57f, -9.4f, 51.61f, -9.4f, 72.19f, 0f)
                lineToRelative(149.37f, 68.07f)
                curveToRelative(6.79f, 3.09f, 22.58f, 12.1f, 22.58f, 29.12f)
                reflectiveCurveToRelative(-15.79f, 26f, -22.58f, 29.11f)
                lineToRelative(-148f, 67.48f)
                curveTo(282.94f, 253.61f, 269.47f, 256f, 256f, 256f)
                close()
                moveTo(432.76f, 155.14f)
                horizontalLineToRelative(0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(441.36f, 226.81f)
                lineTo(426.27f, 220f)
                lineTo(387.5f, 237.74f)
                lineToRelative(-94f, 43f)
                curveToRelative(-10.5f, 4.8f, -24f, 7.19f, -37.44f, 7.19f)
                reflectiveCurveToRelative(-26.93f, -2.39f, -37.42f, -7.19f)
                lineToRelative(-94.07f, -43f)
                lineTo(85.79f, 220f)
                lineToRelative(-15.22f, 6.84f)
                curveTo(63.79f, 229.93f, 48f, 239f, 48f, 256f)
                reflectiveCurveToRelative(15.79f, 26.08f, 22.56f, 29.17f)
                lineToRelative(148f, 67.63f)
                curveTo(229f, 357.6f, 242.49f, 360f, 256f, 360f)
                reflectiveCurveToRelative(26.94f, -2.4f, 37.44f, -7.19f)
                lineTo(441.31f, 285.2f)
                curveTo(448.12f, 282.11f, 464f, 273.09f, 464f, 256f)
                reflectiveCurveTo(448.23f, 229.93f, 441.36f, 226.81f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(441.36f, 330.8f)
                lineTo(426.27f, 324f)
                lineTo(387.5f, 341.73f)
                lineToRelative(-94f, 42.95f)
                curveToRelative(-10.5f, 4.78f, -24f, 7.18f, -37.44f, 7.18f)
                reflectiveCurveToRelative(-26.93f, -2.39f, -37.42f, -7.18f)
                lineToRelative(-94.07f, -43f)
                lineTo(85.79f, 324f)
                lineToRelative(-15.22f, 6.84f)
                curveTo(63.79f, 333.93f, 48f, 343f, 48f, 360f)
                reflectiveCurveToRelative(15.79f, 26.07f, 22.56f, 29.15f)
                lineToRelative(148f, 67.59f)
                curveTo(229f, 461.52f, 242.54f, 464f, 256f, 464f)
                reflectiveCurveToRelative(26.88f, -2.48f, 37.38f, -7.27f)
                lineToRelative(147.92f, -67.57f)
                curveTo(448.12f, 386.08f, 464f, 377.06f, 464f, 360f)
                reflectiveCurveTo(448.23f, 333.93f, 441.36f, 330.8f)
                close()
            }
        }.build()

        return _CiLayers!!
    }

@Suppress("ObjectPropertyName")
private var _CiLayers: ImageVector? = null
