package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiHandRight: ImageVector
    get() {
        if (_CiHandRight != null) {
            return _CiHandRight!!
        }
        _CiHandRight = ImageVector.Builder(
            name = "CiHandRight",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(79.2f, 211.44f)
                horizontalLineToRelative(0f)
                curveToRelative(15.52f, -8.82f, 34.91f, -2.28f, 43.31f, 13.68f)
                lineToRelative(41.38f, 84.41f)
                arcToRelative(7f, 7f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.93f, 3.43f)
                horizontalLineToRelative(0f)
                arcToRelative(7f, 7f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.41f, -6.52f)
                verticalLineTo(72f)
                curveToRelative(0f, -13.91f, 12.85f, -24f, 26.77f, -24f)
                reflectiveCurveToRelative(26f, 10.09f, 26f, 24f)
                verticalLineTo(228.64f)
                arcTo(11.24f, 11.24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 240.79f, 240f)
                arcTo(11f, 11f, 0f, isMoreThanHalf = false, isPositiveArc = false, 252f, 229f)
                verticalLineTo(24f)
                curveToRelative(0f, -13.91f, 10.94f, -24f, 24.86f, -24f)
                reflectiveCurveTo(302f, 10.09f, 302f, 24f)
                verticalLineTo(228.64f)
                arcTo(11.24f, 11.24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 312.79f, 240f)
                arcTo(11f, 11f, 0f, isMoreThanHalf = false, isPositiveArc = false, 324f, 229f)
                verticalLineTo(56f)
                curveToRelative(0f, -13.91f, 12.08f, -24f, 26f, -24f)
                reflectiveCurveToRelative(26f, 11.09f, 26f, 25f)
                verticalLineTo(244.64f)
                arcTo(11.24f, 11.24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 386.79f, 256f)
                arcTo(11f, 11f, 0f, isMoreThanHalf = false, isPositiveArc = false, 398f, 245f)
                verticalLineTo(120f)
                curveToRelative(0f, -13.91f, 11.08f, -24f, 25f, -24f)
                reflectiveCurveToRelative(25.12f, 10.22f, 25f, 24f)
                verticalLineTo(336f)
                curveToRelative(0f, 117.41f, -72f, 176f, -160f, 176f)
                horizontalLineTo(272f)
                curveToRelative(-88f, 0f, -115.71f, -39.6f, -136f, -88f)
                lineTo(67.33f, 255f)
                curveTo(60.67f, 237f, 63.69f, 220.25f, 79.2f, 211.44f)
                close()
            }
        }.build()

        return _CiHandRight!!
    }

@Suppress("ObjectPropertyName")
private var _CiHandRight: ImageVector? = null
