package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiTimer: ImageVector
    get() {
        if (_CiTimer != null) {
            return _CiTimer!!
        }
        _CiTimer = ImageVector.Builder(
            name = "CiTimer",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 48f)
                curveTo(141.12f, 48f, 48f, 141.12f, 48f, 256f)
                reflectiveCurveToRelative(93.12f, 208f, 208f, 208f)
                reflectiveCurveToRelative(208f, -93.12f, 208f, -208f)
                reflectiveCurveTo(370.88f, 48f, 256f, 48f)
                close()
                moveTo(173.67f, 162.34f)
                lineToRelative(105f, 71f)
                arcToRelative(32.5f, 32.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -37.25f, 53.26f)
                arcToRelative(33.21f, 33.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8f, -8f)
                lineToRelative(-71f, -105f)
                arcToRelative(8.13f, 8.13f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.32f, -11.32f)
                close()
                moveTo(256f, 432f)
                curveTo(159f, 432f, 80f, 353.05f, 80f, 256f)
                arcToRelative(174.55f, 174.55f, 0f, isMoreThanHalf = false, isPositiveArc = true, 53.87f, -126.72f)
                arcToRelative(14.15f, 14.15f, 0f, isMoreThanHalf = true, isPositiveArc = true, 19.64f, 20.37f)
                arcTo(146.53f, 146.53f, 0f, isMoreThanHalf = false, isPositiveArc = false, 108.3f, 256f)
                curveToRelative(0f, 81.44f, 66.26f, 147.7f, 147.7f, 147.7f)
                reflectiveCurveTo(403.7f, 337.44f, 403.7f, 256f)
                curveToRelative(0f, -76.67f, -58.72f, -139.88f, -133.55f, -147f)
                verticalLineTo(164f)
                arcToRelative(14.15f, 14.15f, 0f, isMoreThanHalf = true, isPositiveArc = true, -28.3f, 0f)
                verticalLineTo(94.15f)
                arcTo(14.15f, 14.15f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 80f)
                curveTo(353.05f, 80f, 432f, 159f, 432f, 256f)
                reflectiveCurveTo(353.05f, 432f, 256f, 432f)
                close()
            }
        }.build()

        return _CiTimer!!
    }

@Suppress("ObjectPropertyName")
private var _CiTimer: ImageVector? = null
