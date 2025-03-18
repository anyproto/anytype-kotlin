package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPlayBack: ImageVector
    get() {
        if (_CiPlayBack != null) {
            return _CiPlayBack!!
        }
        _CiPlayBack = ImageVector.Builder(
            name = "CiPlayBack",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(30.71f, 229.47f)
                lineToRelative(188.87f, -113f)
                arcToRelative(30.54f, 30.54f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.09f, -0.39f)
                arcToRelative(33.74f, 33.74f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16.76f, 29.47f)
                verticalLineTo(224.6f)
                lineTo(448.15f, 116.44f)
                arcToRelative(30.54f, 30.54f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.09f, -0.39f)
                arcTo(33.74f, 33.74f, 0f, isMoreThanHalf = false, isPositiveArc = true, 496f, 145.52f)
                verticalLineToRelative(221f)
                arcTo(33.73f, 33.73f, 0f, isMoreThanHalf = false, isPositiveArc = true, 479.24f, 396f)
                arcToRelative(30.54f, 30.54f, 0f, isMoreThanHalf = false, isPositiveArc = true, -31.09f, -0.39f)
                lineTo(267.43f, 287.4f)
                verticalLineToRelative(79.08f)
                arcTo(33.73f, 33.73f, 0f, isMoreThanHalf = false, isPositiveArc = true, 250.67f, 396f)
                arcToRelative(30.54f, 30.54f, 0f, isMoreThanHalf = false, isPositiveArc = true, -31.09f, -0.39f)
                lineToRelative(-188.87f, -113f)
                arcToRelative(31.27f, 31.27f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -53f)
                close()
            }
        }.build()

        return _CiPlayBack!!
    }

@Suppress("ObjectPropertyName")
private var _CiPlayBack: ImageVector? = null
