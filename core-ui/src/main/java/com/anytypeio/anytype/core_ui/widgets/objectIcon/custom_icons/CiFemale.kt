package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFemale: ImageVector
    get() {
        if (_CiFemale != null) {
            return _CiFemale!!
        }
        _CiFemale = ImageVector.Builder(
            name = "CiFemale",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(430f, 190f)
                curveToRelative(0f, -95.94f, -78.06f, -174f, -174f, -174f)
                reflectiveCurveTo(82f, 94.06f, 82f, 190f)
                curveToRelative(0f, 88.49f, 66.4f, 161.77f, 152f, 172.61f)
                lineTo(234f, 394f)
                lineTo(198f, 394f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 44f)
                horizontalLineToRelative(36f)
                verticalLineToRelative(36f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 44f, 0f)
                lineTo(278f, 438f)
                horizontalLineToRelative(36f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -44f)
                lineTo(278f, 394f)
                lineTo(278f, 362.61f)
                curveTo(363.6f, 351.77f, 430f, 278.49f, 430f, 190f)
                close()
                moveTo(126f, 190f)
                curveToRelative(0f, -71.68f, 58.32f, -130f, 130f, -130f)
                reflectiveCurveToRelative(130f, 58.32f, 130f, 130f)
                reflectiveCurveTo(327.68f, 320f, 256f, 320f)
                reflectiveCurveTo(126f, 261.68f, 126f, 190f)
                close()
            }
        }.build()

        return _CiFemale!!
    }

@Suppress("ObjectPropertyName")
private var _CiFemale: ImageVector? = null
