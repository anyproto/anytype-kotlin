package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiInformationCircle: ImageVector
    get() {
        if (_CiInformationCircle != null) {
            return _CiInformationCircle!!
        }
        _CiInformationCircle = ImageVector.Builder(
            name = "CiInformationCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 56f)
                curveTo(145.72f, 56f, 56f, 145.72f, 56f, 256f)
                reflectiveCurveToRelative(89.72f, 200f, 200f, 200f)
                reflectiveCurveToRelative(200f, -89.72f, 200f, -200f)
                reflectiveCurveTo(366.28f, 56f, 256f, 56f)
                close()
                moveTo(256f, 138f)
                arcToRelative(26f, 26f, 0f, isMoreThanHalf = true, isPositiveArc = true, -26f, 26f)
                arcTo(26f, 26f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 138f)
                close()
                moveTo(304f, 364f)
                lineTo(216f, 364f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineToRelative(28f)
                lineTo(244f, 244f)
                lineTo(228f, 244f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineToRelative(32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 16f)
                lineTo(276f, 332f)
                horizontalLineToRelative(28f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
            }
        }.build()

        return _CiInformationCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiInformationCircle: ImageVector? = null
