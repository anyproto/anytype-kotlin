package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiReloadCircle: ImageVector
    get() {
        if (_CiReloadCircle != null) {
            return _CiReloadCircle!!
        }
        _CiReloadCircle = ImageVector.Builder(
            name = "CiReloadCircle",
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
                moveTo(376f, 230.15f)
                arcToRelative(8.62f, 8.62f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8.62f, 8.62f)
                horizontalLineTo(307.84f)
                arcToRelative(8.61f, 8.61f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6.09f, -14.71f)
                lineToRelative(22.17f, -22.17f)
                lineToRelative(-5.6f, -6.51f)
                arcToRelative(87.38f, 87.38f, 0f, isMoreThanHalf = true, isPositiveArc = false, -62.94f, 148f)
                arcToRelative(87.55f, 87.55f, 0f, isMoreThanHalf = false, isPositiveArc = false, 82.42f, -58.25f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 368f, 295.8f)
                arcTo(119.4f, 119.4f, 0f, isMoreThanHalf = true, isPositiveArc = true, 255.38f, 136.62f)
                arcToRelative(118.34f, 118.34f, 0f, isMoreThanHalf = false, isPositiveArc = true, 86.36f, 36.95f)
                lineToRelative(0.56f, 0.62f)
                lineToRelative(4.31f, 5f)
                lineToRelative(14.68f, -14.68f)
                arcToRelative(8.44f, 8.44f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, -2.54f)
                arcToRelative(8.61f, 8.61f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.68f, 8.63f)
                close()
            }
        }.build()

        return _CiReloadCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiReloadCircle: ImageVector? = null
