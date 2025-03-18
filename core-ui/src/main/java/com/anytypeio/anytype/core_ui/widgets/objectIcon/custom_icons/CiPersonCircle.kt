package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPersonCircle: ImageVector
    get() {
        if (_CiPersonCircle != null) {
            return _CiPersonCircle!!
        }
        _CiPersonCircle = ImageVector.Builder(
            name = "CiPersonCircle",
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
                moveTo(205.78f, 164.82f)
                curveTo(218.45f, 151.39f, 236.28f, 144f, 256f, 144f)
                reflectiveCurveToRelative(37.39f, 7.44f, 50.11f, 20.94f)
                curveTo(319f, 178.62f, 325.27f, 197f, 323.79f, 216.76f)
                curveTo(320.83f, 256f, 290.43f, 288f, 256f, 288f)
                reflectiveCurveToRelative(-64.89f, -32f, -67.79f, -71.25f)
                curveTo(186.74f, 196.83f, 193f, 178.39f, 205.78f, 164.82f)
                close()
                moveTo(256f, 432f)
                arcToRelative(175.49f, 175.49f, 0f, isMoreThanHalf = false, isPositiveArc = true, -126f, -53.22f)
                arcToRelative(122.91f, 122.91f, 0f, isMoreThanHalf = false, isPositiveArc = true, 35.14f, -33.44f)
                curveTo(190.63f, 329f, 222.89f, 320f, 256f, 320f)
                reflectiveCurveToRelative(65.37f, 9f, 90.83f, 25.34f)
                arcTo(122.87f, 122.87f, 0f, isMoreThanHalf = false, isPositiveArc = true, 382f, 378.78f)
                arcTo(175.45f, 175.45f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 432f)
                close()
            }
        }.build()

        return _CiPersonCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiPersonCircle: ImageVector? = null
