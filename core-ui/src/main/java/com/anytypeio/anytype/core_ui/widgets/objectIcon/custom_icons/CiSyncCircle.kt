package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiSyncCircle: ImageVector
    get() {
        if (_CiSyncCircle != null) {
            return _CiSyncCircle!!
        }
        _CiSyncCircle = ImageVector.Builder(
            name = "CiSyncCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 48f)
                curveTo(141.13f, 48f, 48f, 141.13f, 48f, 256f)
                reflectiveCurveToRelative(93.13f, 208f, 208f, 208f)
                reflectiveCurveToRelative(208f, -93.13f, 208f, -208f)
                reflectiveCurveTo(370.87f, 48f, 256f, 48f)
                close()
                moveTo(339.69f, 330.65f)
                arcToRelative(112.24f, 112.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, -195f, -61.29f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -20.13f, -24.67f)
                lineToRelative(23.6f, -23.6f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.37f, -0.25f)
                lineToRelative(24.67f, 23.6f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -18f, 26f)
                arcToRelative(80.25f, 80.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 138.72f, 38.83f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.77f, 21.41f)
                close()
                moveTo(387.45f, 267.31f)
                lineTo(363.85f, 290.91f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.37f, 0.25f)
                lineToRelative(-24.67f, -23.6f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17.68f, -26.11f)
                arcTo(80.17f, 80.17f, 0f, isMoreThanHalf = false, isPositiveArc = false, 196f, 202.64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, -23.82f, -21.37f)
                arcToRelative(112.17f, 112.17f, 0f, isMoreThanHalf = false, isPositiveArc = true, 194.88f, 61.57f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20.39f, 24.47f)
                close()
            }
        }.build()

        return _CiSyncCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiSyncCircle: ImageVector? = null
