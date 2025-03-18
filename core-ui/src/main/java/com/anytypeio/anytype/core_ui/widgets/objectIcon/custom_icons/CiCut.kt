package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCut: ImageVector
    get() {
        if (_CiCut != null) {
            return _CiCut!!
        }
        _CiCut = ImageVector.Builder(
            name = "CiCut",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(103.48f, 224f)
                arcToRelative(71.64f, 71.64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 44.76f, -15.66f)
                lineToRelative(41.5f, 16.89f)
                lineToRelative(6.82f, -12.63f)
                arcToRelative(39.15f, 39.15f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.32f, -6.37f)
                lineToRelative(14.22f, -14.42f)
                lineToRelative(-41.17f, -24.94f)
                arcTo(72f, 72f, 0f, isMoreThanHalf = true, isPositiveArc = false, 103.48f, 224f)
                close()
                moveTo(103.48f, 112f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = true, isPositiveArc = true, -40f, 40f)
                arcTo(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, 103.48f, 112f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(480f, 169f)
                lineToRelative(-5.52f, -12.58f)
                curveToRelative(-4.48f, -10.42f, -14.74f, -16f, -32.78f, -17.85f)
                curveToRelative(-10.12f, -1f, -26.95f, -1.24f, -49.69f, 3.81f)
                curveToRelative(-20f, 4.45f, -122.14f, 28.2f, -164.95f, 58.62f)
                curveTo(206.81f, 215.39f, 203f, 234.67f, 200f, 250.16f)
                curveToRelative(-2.78f, 14.14f, -5f, 25.31f, -18f, 35f)
                curveToRelative(-15f, 11.14f, -27.27f, 16.38f, -33.58f, 18.6f)
                arcToRelative(71.74f, 71.74f, 0f, isMoreThanHalf = true, isPositiveArc = false, 24.79f, 38f)
                close()
                moveTo(255.48f, 256f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 16f, -16f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 255.48f, 256f)
                close()
                moveTo(103.48f, 400f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = true, isPositiveArc = true, 40f, -40f)
                arcTo(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, 103.48f, 400f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(343.79f, 259.87f)
                lineToRelative(-83.74f, 48.18f)
                lineToRelative(27.63f, 13.08f)
                lineToRelative(3.62f, 1.74f)
                curveTo(310f, 331.92f, 359.74f, 356f, 410.53f, 359f)
                curveToRelative(3.89f, 0.23f, 7.47f, 0.34f, 10.78f, 0.34f)
                curveTo(442f, 359.31f, 453f, 354f, 459.75f, 350f)
                lineTo(480f, 336f)
                close()
            }
        }.build()

        return _CiCut!!
    }

@Suppress("ObjectPropertyName")
private var _CiCut: ImageVector? = null
