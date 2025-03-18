package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiColorWand: ImageVector
    get() {
        if (_CiColorWand != null) {
            return _CiColorWand!!
        }
        _CiColorWand = ImageVector.Builder(
            name = "CiColorWand",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(96f, 208f)
                horizontalLineTo(48f)
                curveToRelative(-8.8f, 0f, -16f, -7.2f, -16f, -16f)
                reflectiveCurveToRelative(7.2f, -16f, 16f, -16f)
                horizontalLineToRelative(48f)
                curveToRelative(8.8f, 0f, 16f, 7.2f, 16f, 16f)
                reflectiveCurveTo(104.8f, 208f, 96f, 208f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(124.1f, 140.1f)
                curveToRelative(-4.2f, 0f, -8.3f, -1.7f, -11.3f, -4.7f)
                lineToRelative(-33.9f, -33.9f)
                curveToRelative(-6.2f, -6.2f, -6.2f, -16.4f, 0f, -22.6f)
                reflectiveCurveToRelative(16.4f, -6.2f, 22.6f, 0f)
                lineToRelative(33.9f, 33.9f)
                curveToRelative(6.3f, 6.2f, 6.3f, 16.4f, 0f, 22.6f)
                curveTo(132.4f, 138.4f, 128.4f, 140.1f, 124.1f, 140.1f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(192f, 112f)
                curveToRelative(-8.8f, 0f, -16f, -7.2f, -16f, -16f)
                verticalLineTo(48f)
                curveToRelative(0f, -8.8f, 7.2f, -16f, 16f, -16f)
                reflectiveCurveToRelative(16f, 7.2f, 16f, 16f)
                verticalLineToRelative(48f)
                curveTo(208f, 104.8f, 200.8f, 112f, 192f, 112f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(259.9f, 140.1f)
                curveToRelative(-8.8f, 0f, -16f, -7.2f, -16f, -16f)
                curveToRelative(0f, -4.2f, 1.7f, -8.3f, 4.7f, -11.3f)
                lineToRelative(33.9f, -33.9f)
                curveToRelative(6.2f, -6.2f, 16.4f, -6.2f, 22.6f, 0f)
                curveToRelative(6.2f, 6.2f, 6.2f, 16.4f, 0f, 22.6f)
                lineToRelative(-33.9f, 33.9f)
                curveTo(268.2f, 138.4f, 264.1f, 140.1f, 259.9f, 140.1f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(90.2f, 309.8f)
                curveToRelative(-8.8f, 0f, -16f, -7.2f, -16f, -16f)
                curveToRelative(0f, -4.2f, 1.7f, -8.3f, 4.7f, -11.3f)
                lineToRelative(33.9f, -33.9f)
                curveToRelative(6.2f, -6.2f, 16.4f, -6.2f, 22.6f, 0f)
                reflectiveCurveToRelative(6.2f, 16.4f, 0f, 22.6f)
                lineToRelative(-33.9f, 33.9f)
                curveTo(98.5f, 308.1f, 94.4f, 309.8f, 90.2f, 309.8f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(234.2f, 167f)
                curveToRelative(-18.4f, -18.7f, -48.5f, -19f, -67.2f, -0.7f)
                reflectiveCurveToRelative(-19f, 48.5f, -0.7f, 67.2f)
                curveToRelative(0.2f, 0.2f, 0.5f, 0.5f, 0.7f, 0.7f)
                lineToRelative(39.5f, 39.5f)
                curveToRelative(3.1f, 3.1f, 8.2f, 3.1f, 11.3f, 0f)
                lineToRelative(55.9f, -55.9f)
                curveToRelative(3.1f, -3.1f, 3.1f, -8.2f, 0f, -11.3f)
                lineTo(234.2f, 167f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(457f, 389.8f)
                lineTo(307.6f, 240.4f)
                curveToRelative(-3.1f, -3.1f, -8.2f, -3.1f, -11.3f, 0f)
                lineToRelative(-55.9f, 55.9f)
                curveToRelative(-3.1f, 3.1f, -3.1f, 8.2f, 0f, 11.3f)
                lineTo(389.8f, 457f)
                curveToRelative(18.4f, 18.7f, 48.5f, 19f, 67.2f, 0.7f)
                curveToRelative(18.7f, -18.4f, 19f, -48.5f, 0.7f, -67.2f)
                curveTo(457.5f, 390.3f, 457.3f, 390f, 457f, 389.8f)
                lineTo(457f, 389.8f)
                close()
            }
        }.build()

        return _CiColorWand!!
    }

@Suppress("ObjectPropertyName")
private var _CiColorWand: ImageVector? = null
