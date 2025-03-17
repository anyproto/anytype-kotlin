package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiRibbon: ImageVector
    get() {
        if (_CiRibbon != null) {
            return _CiRibbon!!
        }
        _CiRibbon = ImageVector.Builder(
            name = "CiRibbon",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(269f, 335.61f)
                quadToRelative(-6.33f, 0.47f, -12.78f, 0.47f)
                curveToRelative(-5.23f, 0f, -10.4f, -0.24f, -15.51f, -0.69f)
                arcToRelative(176.11f, 176.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, -127.67f, -72.94f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.77f, 0.35f)
                lineToRelative(-72f, 129.4f)
                curveToRelative(-2.74f, 5f, -3.17f, 11f, -0.28f, 15.88f)
                arcTo(16.78f, 16.78f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48.22f, 416f)
                horizontalLineToRelative(78f)
                arcToRelative(15.28f, 15.28f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.62f, 7.33f)
                lineTo(178.5f, 488f)
                arcToRelative(16.26f, 16.26f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.75f, 8f)
                horizontalLineToRelative(0f)
                curveToRelative(5.94f, -0.33f, 12.09f, -4.19f, 14.56f, -9.6f)
                lineToRelative(66.11f, -145.15f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 269f, 335.61f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(477.64f, 391.88f)
                lineTo(406.11f, 262.71f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.74f, -0.36f)
                arcToRelative(176.5f, 176.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -78.31f, 61.42f)
                arcToRelative(16.09f, 16.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8.72f, 8.25f)
                lineToRelative(-36.86f, 81.1f)
                arcToRelative(7.92f, 7.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 6.6f)
                lineToRelative(30.27f, 66.59f)
                curveToRelative(2.45f, 5.41f, 8.59f, 9.36f, 14.52f, 9.69f)
                horizontalLineToRelative(0f)
                arcToRelative(16.3f, 16.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.7f, -8.12f)
                lineTo(372.5f, 423.3f)
                curveToRelative(2.89f, -4.85f, 8.13f, -7.33f, 13.78f, -7.3f)
                horizontalLineToRelative(78.77f)
                curveToRelative(6.67f, 0f, 11.72f, -3.48f, 14f, -10f)
                arcTo(16.92f, 16.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, 477.64f, 391.88f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(208.25f, 160f)
                arcToRelative(48.01f, 48f, 0f, isMoreThanHalf = true, isPositiveArc = false, 96.02f, 0f)
                arcToRelative(48.01f, 48f, 0f, isMoreThanHalf = true, isPositiveArc = false, -96.02f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256.26f, 16f)
                curveToRelative(-79.42f, 0f, -144f, 64.59f, -144f, 144f)
                reflectiveCurveToRelative(64.61f, 144f, 144f, 144f)
                reflectiveCurveToRelative(144f, -64.6f, 144f, -144f)
                reflectiveCurveTo(335.67f, 16f, 256.26f, 16f)
                close()
                moveTo(256.26f, 240f)
                arcToRelative(80f, 80f, 0f, isMoreThanHalf = true, isPositiveArc = true, 80f, -80f)
                arcTo(80.1f, 80.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256.26f, 240f)
                close()
            }
        }.build()

        return _CiRibbon!!
    }

@Suppress("ObjectPropertyName")
private var _CiRibbon: ImageVector? = null
