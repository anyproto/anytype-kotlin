package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiEgg: ImageVector
    get() {
        if (_CiEgg != null) {
            return _CiEgg!!
        }
        _CiEgg = ImageVector.Builder(
            name = "CiEgg",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 480f)
                curveToRelative(-52.57f, 0f, -96.72f, -17.54f, -127.7f, -50.73f)
                curveTo(96.7f, 395.4f, 80f, 346.05f, 80f, 286.55f)
                curveTo(80f, 230.5f, 101.48f, 168f, 138.93f, 115f)
                curveTo(175.65f, 63f, 219.41f, 32f, 256f, 32f)
                reflectiveCurveToRelative(80.35f, 31f, 117.07f, 83f)
                curveTo(410.52f, 168f, 432f, 230.5f, 432f, 286.55f)
                curveToRelative(0f, 59.5f, -16.7f, 108.85f, -48.3f, 142.72f)
                curveTo(352.72f, 462.46f, 308.57f, 480f, 256f, 480f)
                close()
            }
        }.build()

        return _CiEgg!!
    }

@Suppress("ObjectPropertyName")
private var _CiEgg: ImageVector? = null
