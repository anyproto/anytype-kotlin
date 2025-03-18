package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiSad: ImageVector
    get() {
        if (_CiSad != null) {
            return _CiSad!!
        }
        _CiSad = ImageVector.Builder(
            name = "CiSad",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(414.39f, 97.61f)
                arcTo(224f, 224f, 0f, isMoreThanHalf = true, isPositiveArc = false, 97.61f, 414.39f)
                arcTo(224f, 224f, 0f, isMoreThanHalf = true, isPositiveArc = false, 414.39f, 97.61f)
                close()
                moveTo(184f, 208f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24f, 24f)
                arcTo(23.94f, 23.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, 184f, 208f)
                close()
                moveTo(160.33f, 357.83f)
                curveToRelative(12f, -40.3f, 50.2f, -69.83f, 95.62f, -69.83f)
                reflectiveCurveToRelative(83.62f, 29.53f, 95.71f, 69.83f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 343.84f, 368f)
                horizontalLineTo(168.15f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 160.33f, 357.83f)
                close()
                moveTo(328f, 256f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, 24f, -24f)
                arcTo(23.94f, 23.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, 328f, 256f)
                close()
            }
        }.build()

        return _CiSad!!
    }

@Suppress("ObjectPropertyName")
private var _CiSad: ImageVector? = null
