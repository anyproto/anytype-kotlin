package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiSchool: ImageVector
    get() {
        if (_CiSchool != null) {
            return _CiSchool!!
        }
        _CiSchool = ImageVector.Builder(
            name = "CiSchool",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 368f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -7.94f, -2.11f)
                lineTo(108f, 285.84f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -12f, 6.94f)
                verticalLineTo(368f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.23f, 14f)
                lineToRelative(144f, 80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.54f, 0f)
                lineToRelative(144f, -80f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 416f, 368f)
                verticalLineTo(292.78f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -12f, -6.94f)
                lineTo(263.94f, 365.89f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 368f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(495.92f, 190.5f)
                reflectiveCurveToRelative(0f, -0.08f, 0f, -0.11f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, -12.28f)
                lineToRelative(-224f, -128f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -15.88f, 0f)
                lineToRelative(-224f, 128f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 27.78f)
                lineToRelative(224f, 128f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.88f, 0f)
                lineTo(461f, 221.28f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 1.74f)
                verticalLineTo(367.55f)
                curveToRelative(0f, 8.61f, 6.62f, 16f, 15.23f, 16.43f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 496f, 368f)
                verticalLineTo(192f)
                arcTo(14.76f, 14.76f, 0f, isMoreThanHalf = false, isPositiveArc = false, 495.92f, 190.5f)
                close()
            }
        }.build()

        return _CiSchool!!
    }

@Suppress("ObjectPropertyName")
private var _CiSchool: ImageVector? = null
