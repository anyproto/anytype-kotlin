package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCreate: ImageVector
    get() {
        if (_CiCreate != null) {
            return _CiCreate!!
        }
        _CiCreate = ImageVector.Builder(
            name = "CiCreate",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(459.94f, 53.25f)
                arcToRelative(16.06f, 16.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, -23.22f, -0.56f)
                lineTo(424.35f, 65f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 11.31f)
                lineToRelative(11.34f, 11.32f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 11.34f, 0f)
                lineToRelative(12.06f, -12f)
                curveTo(465.19f, 69.54f, 465.76f, 59.62f, 459.94f, 53.25f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(399.34f, 90f)
                lineTo(218.82f, 270.2f)
                arcToRelative(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.31f, 3.93f)
                lineTo(208.16f, 299f)
                arcToRelative(3.91f, 3.91f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.86f, 4.86f)
                lineToRelative(24.85f, -8.35f)
                arcToRelative(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.93f, -2.31f)
                lineTo(422f, 112.66f)
                arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 422f, 100f)
                lineTo(412.05f, 90f)
                arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 399.34f, 90f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(386.34f, 193.66f)
                lineTo(264.45f, 315.79f)
                arcTo(41.08f, 41.08f, 0f, isMoreThanHalf = false, isPositiveArc = true, 247.58f, 326f)
                lineToRelative(-25.9f, 8.67f)
                arcToRelative(35.92f, 35.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, -44.33f, -44.33f)
                lineToRelative(8.67f, -25.9f)
                arcToRelative(41.08f, 41.08f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10.19f, -16.87f)
                lineTo(318.34f, 125.66f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 312.69f, 112f)
                horizontalLineTo(104f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, -56f, 56f)
                verticalLineTo(408f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, 56f)
                horizontalLineTo(344f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, -56f)
                verticalLineTo(199.31f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 386.34f, 193.66f)
                close()
            }
        }.build()

        return _CiCreate!!
    }

@Suppress("ObjectPropertyName")
private var _CiCreate: ImageVector? = null
