package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPower: ImageVector
    get() {
        if (_CiPower != null) {
            return _CiPower!!
        }
        _CiPower = ImageVector.Builder(
            name = "CiPower",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 464f)
                curveTo(141.31f, 464f, 48f, 370.53f, 48f, 255.65f)
                curveToRelative(0f, -62.45f, 27.25f, -121f, 74.76f, -160.55f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = true, isPositiveArc = true, 28.17f, 33.8f)
                curveTo(113.48f, 160.1f, 92f, 206.3f, 92f, 255.65f)
                curveTo(92f, 346.27f, 165.57f, 420f, 256f, 420f)
                reflectiveCurveToRelative(164f, -73.73f, 164f, -164.35f)
                arcTo(164f, 164f, 0f, isMoreThanHalf = false, isPositiveArc = false, 360.17f, 129f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = true, isPositiveArc = true, 28f, -33.92f)
                arcTo(207.88f, 207.88f, 0f, isMoreThanHalf = false, isPositiveArc = true, 464f, 255.65f)
                curveTo(464f, 370.53f, 370.69f, 464f, 256f, 464f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 272f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22f, -22f)
                verticalLineTo(70f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 44f, 0f)
                verticalLineTo(250f)
                arcTo(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 272f)
                close()
            }
        }.build()

        return _CiPower!!
    }

@Suppress("ObjectPropertyName")
private var _CiPower: ImageVector? = null
