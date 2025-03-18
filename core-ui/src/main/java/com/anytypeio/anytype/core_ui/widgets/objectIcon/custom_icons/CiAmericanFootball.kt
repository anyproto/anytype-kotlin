package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiAmericanFootball: ImageVector
    get() {
        if (_CiAmericanFootball != null) {
            return _CiAmericanFootball!!
        }
        _CiAmericanFootball = ImageVector.Builder(
            name = "CiAmericanFootball",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(122.06f, 122.06f)
                curveToRelative(-44.37f, 44.37f, -66.71f, 100.61f, -78f, 145.28f)
                lineTo(244.66f, 467.9f)
                curveToRelative(44.67f, -11.25f, 100.91f, -33.59f, 145.28f, -78f)
                reflectiveCurveToRelative(66.71f, -100.61f, 78f, -145.28f)
                lineTo(267.34f, 44.1f)
                curveTo(222.67f, 55.35f, 166.43f, 77.69f, 122.06f, 122.06f)
                close()
                moveTo(378.79f, 378.78f)
                horizontalLineToRelative(0f)
                close()
                moveTo(300.65f, 189f)
                lineTo(323f, 166.71f)
                arcTo(15.78f, 15.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, 345.29f, 189f)
                lineTo(323f, 211.35f)
                lineToRelative(11.16f, 11.17f)
                arcToRelative(15.78f, 15.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.32f, 22.32f)
                lineToRelative(-11.16f, -11.16f)
                lineTo(278.32f, 256f)
                lineToRelative(11.16f, 11.16f)
                arcToRelative(15.78f, 15.78f, 0f, isMoreThanHalf = true, isPositiveArc = true, -22.32f, 22.32f)
                lineTo(256f, 278.32f)
                lineToRelative(-22.32f, 22.33f)
                lineToRelative(11.16f, 11.16f)
                arcToRelative(15.78f, 15.78f, 0f, isMoreThanHalf = true, isPositiveArc = true, -22.32f, 22.32f)
                lineTo(211.35f, 323f)
                lineTo(189f, 345.29f)
                arcTo(15.78f, 15.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, 166.71f, 323f)
                lineTo(189f, 300.65f)
                lineToRelative(-11.16f, -11.17f)
                arcToRelative(15.78f, 15.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.32f, -22.32f)
                lineToRelative(11.16f, 11.16f)
                lineTo(233.68f, 256f)
                lineToRelative(-11.16f, -11.16f)
                arcToRelative(15.78f, 15.78f, 0f, isMoreThanHalf = true, isPositiveArc = true, 22.32f, -22.32f)
                lineTo(256f, 233.68f)
                lineToRelative(22.32f, -22.33f)
                lineToRelative(-11.16f, -11.16f)
                arcToRelative(15.78f, 15.78f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.32f, -22.32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(476.57f, 199.63f)
                curveToRelative(7.31f, -54.53f, 4f, -120.26f, -20f, -144.21f)
                reflectiveCurveToRelative(-89.68f, -27.3f, -144.21f, -20f)
                curveToRelative(-2.51f, 0.34f, -5.16f, 0.72f, -7.91f, 1.15f)
                lineToRelative(171f, 171f)
                curveTo(475.85f, 204.79f, 476.23f, 202.14f, 476.57f, 199.63f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(35.43f, 312.37f)
                curveToRelative(-7.31f, 54.53f, -4f, 120.26f, 20f, 144.21f)
                curveTo(72.17f, 473.33f, 109.34f, 480f, 148.84f, 480f)
                arcToRelative(387f, 387f, 0f, isMoreThanHalf = false, isPositiveArc = false, 50.79f, -3.43f)
                curveToRelative(2.51f, -0.34f, 5.16f, -0.72f, 7.91f, -1.15f)
                lineToRelative(-171f, -171f)
                curveTo(36.15f, 307.21f, 35.77f, 309.86f, 35.43f, 312.37f)
                close()
            }
        }.build()

        return _CiAmericanFootball!!
    }

@Suppress("ObjectPropertyName")
private var _CiAmericanFootball: ImageVector? = null
