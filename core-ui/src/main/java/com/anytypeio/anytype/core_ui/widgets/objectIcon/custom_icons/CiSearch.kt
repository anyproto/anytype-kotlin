package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiSearch: ImageVector
    get() {
        if (_CiSearch != null) {
            return _CiSearch!!
        }
        _CiSearch = ImageVector.Builder(
            name = "CiSearch",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(456.69f, 421.39f)
                lineTo(362.6f, 327.3f)
                arcToRelative(173.81f, 173.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, 34.84f, -104.58f)
                curveTo(397.44f, 126.38f, 319.06f, 48f, 222.72f, 48f)
                reflectiveCurveTo(48f, 126.38f, 48f, 222.72f)
                reflectiveCurveToRelative(78.38f, 174.72f, 174.72f, 174.72f)
                arcTo(173.81f, 173.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, 327.3f, 362.6f)
                lineToRelative(94.09f, 94.09f)
                arcToRelative(25f, 25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 35.3f, -35.3f)
                close()
                moveTo(97.92f, 222.72f)
                arcToRelative(124.8f, 124.8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 124.8f, 124.8f)
                arcTo(124.95f, 124.95f, 0f, isMoreThanHalf = false, isPositiveArc = true, 97.92f, 222.72f)
                close()
            }
        }.build()

        return _CiSearch!!
    }

@Suppress("ObjectPropertyName")
private var _CiSearch: ImageVector? = null
