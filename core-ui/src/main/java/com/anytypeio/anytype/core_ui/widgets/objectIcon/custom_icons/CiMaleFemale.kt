package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMaleFemale: ImageVector
    get() {
        if (_CiMaleFemale != null) {
            return _CiMaleFemale!!
        }
        _CiMaleFemale = ImageVector.Builder(
            name = "CiMaleFemale",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(426f, 16f)
                horizontalLineTo(352f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 44f)
                horizontalLineToRelative(20.89f)
                lineToRelative(-37.1f, 37.09f)
                arcTo(157.68f, 157.68f, 0f, isMoreThanHalf = false, isPositiveArc = false, 216f, 42f)
                curveTo(128.88f, 42f, 58f, 112.88f, 58f, 200f)
                curveToRelative(0f, 79.66f, 59.26f, 145.72f, 136f, 156.46f)
                verticalLineTo(394f)
                horizontalLineTo(166f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 44f)
                horizontalLineToRelative(28f)
                verticalLineToRelative(36f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 44f, 0f)
                verticalLineTo(438f)
                horizontalLineToRelative(28f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -44f)
                horizontalLineTo(238f)
                verticalLineTo(356.46f)
                curveToRelative(76.74f, -10.74f, 136f, -76.8f, 136f, -156.46f)
                arcToRelative(157.15f, 157.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, -14f, -64.92f)
                lineToRelative(44f, -44f)
                verticalLineTo(112f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 44f, 0f)
                verticalLineTo(38f)
                arcTo(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 426f, 16f)
                close()
                moveTo(216f, 314f)
                arcTo(114f, 114f, 0f, isMoreThanHalf = true, isPositiveArc = true, 330f, 200f)
                arcTo(114.13f, 114.13f, 0f, isMoreThanHalf = false, isPositiveArc = true, 216f, 314f)
                close()
            }
        }.build()

        return _CiMaleFemale!!
    }

@Suppress("ObjectPropertyName")
private var _CiMaleFemale: ImageVector? = null
