package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMale: ImageVector
    get() {
        if (_CiMale != null) {
            return _CiMale!!
        }
        _CiMale = ImageVector.Builder(
            name = "CiMale",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(442f, 48f)
                horizontalLineTo(352f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 44f)
                horizontalLineToRelative(36.89f)
                lineTo(328.5f, 152.39f)
                curveToRelative(-68.19f, -52.86f, -167f, -48f, -229.54f, 14.57f)
                horizontalLineToRelative(0f)
                curveTo(31.12f, 234.81f, 31.12f, 345.19f, 99f, 413f)
                arcTo(174.21f, 174.21f, 0f, isMoreThanHalf = false, isPositiveArc = false, 345f, 413f)
                curveToRelative(62.57f, -62.58f, 67.43f, -161.35f, 14.57f, -229.54f)
                lineTo(420f, 123.11f)
                verticalLineTo(160f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 44f, 0f)
                verticalLineTo(70f)
                arcTo(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 442f, 48f)
                close()
                moveTo(313.92f, 381.92f)
                arcToRelative(130.13f, 130.13f, 0f, isMoreThanHalf = false, isPositiveArc = true, -183.84f, 0f)
                curveToRelative(-50.69f, -50.68f, -50.69f, -133.16f, 0f, -183.84f)
                reflectiveCurveToRelative(133.16f, -50.69f, 183.84f, 0f)
                reflectiveCurveTo(364.61f, 331.24f, 313.92f, 381.92f)
                close()
            }
        }.build()

        return _CiMale!!
    }

@Suppress("ObjectPropertyName")
private var _CiMale: ImageVector? = null
