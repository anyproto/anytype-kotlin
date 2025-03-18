package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiHome: ImageVector
    get() {
        if (_CiHome != null) {
            return _CiHome!!
        }
        _CiHome = ImageVector.Builder(
            name = "CiHome",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(261.56f, 101.28f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -11.06f, 0f)
                lineTo(66.4f, 277.15f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.47f, 5.79f)
                lineTo(63.9f, 448f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 32f)
                horizontalLineTo(192f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, -16f)
                verticalLineTo(328f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, -8f)
                horizontalLineToRelative(80f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 8f)
                lineToRelative(0f, 136f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                horizontalLineToRelative(96.06f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, -32f)
                lineToRelative(0f, -165.06f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.47f, -5.79f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(490.91f, 244.15f)
                lineToRelative(-74.8f, -71.56f)
                lineToRelative(0f, -108.59f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, -16f)
                horizontalLineToRelative(-48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 16f)
                lineToRelative(0f, 32f)
                lineTo(278.19f, 40.62f)
                curveTo(272.77f, 35.14f, 264.71f, 32f, 256f, 32f)
                horizontalLineToRelative(0f)
                curveToRelative(-8.68f, 0f, -16.72f, 3.14f, -22.14f, 8.63f)
                lineTo(21.16f, 244.13f)
                curveToRelative(-6.22f, 6f, -7f, 15.87f, -1.34f, 22.37f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 43f, 267.56f)
                lineTo(250.5f, 69.28f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.06f, 0f)
                lineTo(469.08f, 267.56f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 22.59f, -0.44f)
                curveTo(497.81f, 260.76f, 497.3f, 250.26f, 490.91f, 244.15f)
                close()
            }
        }.build()

        return _CiHome!!
    }

@Suppress("ObjectPropertyName")
private var _CiHome: ImageVector? = null
