package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMan: ImageVector
    get() {
        if (_CiMan != null) {
            return _CiMan!!
        }
        _CiMan = ImageVector.Builder(
            name = "CiMan",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 56f)
                moveToRelative(-56f, 0f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = true, isPositiveArc = true, 112f, 0f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = true, isPositiveArc = true, -112f, 0f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(304f, 128f)
                horizontalLineTo(208f)
                arcToRelative(64.19f, 64.19f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64f, 64f)
                verticalLineTo(299.52f)
                curveToRelative(0f, 10.85f, 8.43f, 20.08f, 19.27f, 20.47f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, 184f, 300f)
                verticalLineTo(200.27f)
                arcToRelative(8.18f, 8.18f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.47f, -8.25f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.53f, 8f)
                verticalLineTo(489f)
                arcToRelative(23f, 23f, 0f, isMoreThanHalf = false, isPositiveArc = false, 23f, 23f)
                horizontalLineToRelative(0f)
                arcToRelative(23f, 23f, 0f, isMoreThanHalf = false, isPositiveArc = false, 23f, -23f)
                verticalLineTo(346.34f)
                arcTo(10.24f, 10.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 255.33f, 336f)
                arcTo(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 266f, 346f)
                verticalLineTo(489f)
                arcToRelative(23f, 23f, 0f, isMoreThanHalf = false, isPositiveArc = false, 23f, 23f)
                horizontalLineToRelative(0f)
                arcToRelative(23f, 23f, 0f, isMoreThanHalf = false, isPositiveArc = false, 23f, -23f)
                verticalLineTo(200.27f)
                arcToRelative(8.18f, 8.18f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.47f, -8.25f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.53f, 8f)
                verticalLineToRelative(99.52f)
                curveToRelative(0f, 10.85f, 8.43f, 20.08f, 19.27f, 20.47f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, 368f, 300f)
                verticalLineTo(192f)
                arcTo(64.19f, 64.19f, 0f, isMoreThanHalf = false, isPositiveArc = false, 304f, 128f)
                close()
            }
        }.build()

        return _CiMan!!
    }

@Suppress("ObjectPropertyName")
private var _CiMan: ImageVector? = null
