package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCrop: ImageVector
    get() {
        if (_CiCrop != null) {
            return _CiCrop!!
        }
        _CiCrop = ImageVector.Builder(
            name = "CiCrop",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(458f, 346f)
                horizontalLineTo(192f)
                arcToRelative(26f, 26f, 0f, isMoreThanHalf = false, isPositiveArc = true, -26f, -26f)
                verticalLineTo(54f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, -44f, 0f)
                verticalLineToRelative(68f)
                horizontalLineTo(54f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 44f)
                horizontalLineToRelative(68f)
                verticalLineTo(320f)
                arcToRelative(70.08f, 70.08f, 0f, isMoreThanHalf = false, isPositiveArc = false, 70f, 70f)
                horizontalLineTo(346f)
                verticalLineToRelative(68f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 44f, 0f)
                verticalLineTo(390f)
                horizontalLineToRelative(68f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -44f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(214f, 166f)
                horizontalLineTo(320f)
                arcToRelative(26f, 26f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26f, 26f)
                verticalLineTo(298f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 44f, 0f)
                verticalLineTo(192f)
                arcToRelative(70.08f, 70.08f, 0f, isMoreThanHalf = false, isPositiveArc = false, -70f, -70f)
                horizontalLineTo(214f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 44f)
                close()
            }
        }.build()

        return _CiCrop!!
    }

@Suppress("ObjectPropertyName")
private var _CiCrop: ImageVector? = null
