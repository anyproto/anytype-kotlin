package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFlag: ImageVector
    get() {
        if (_CiFlag != null) {
            return _CiFlag!!
        }
        _CiFlag = ImageVector.Builder(
            name = "CiFlag",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(80f, 480f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, -16f)
                verticalLineTo(68.13f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 75.9f, 47.41f)
                curveTo(88f, 40.38f, 112.38f, 32f, 160f, 32f)
                curveToRelative(37.21f, 0f, 78.83f, 14.71f, 115.55f, 27.68f)
                curveTo(305.12f, 70.13f, 333.05f, 80f, 352f, 80f)
                arcToRelative(183.84f, 183.84f, 0f, isMoreThanHalf = false, isPositiveArc = false, 71f, -14.5f)
                arcToRelative(18f, 18f, 0f, isMoreThanHalf = false, isPositiveArc = true, 25f, 16.58f)
                verticalLineTo(301.44f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, -12f, 18.31f)
                curveToRelative(-8.71f, 3.81f, -40.51f, 16.25f, -84f, 16.25f)
                curveToRelative(-24.14f, 0f, -54.38f, -7.14f, -86.39f, -14.71f)
                curveTo(229.63f, 312.79f, 192.43f, 304f, 160f, 304f)
                curveToRelative(-36.87f, 0f, -55.74f, 5.58f, -64f, 9.11f)
                verticalLineTo(464f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 80f, 480f)
                close()
            }
        }.build()

        return _CiFlag!!
    }

@Suppress("ObjectPropertyName")
private var _CiFlag: ImageVector? = null
