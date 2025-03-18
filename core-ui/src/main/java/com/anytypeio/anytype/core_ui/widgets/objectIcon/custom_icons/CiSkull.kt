package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiSkull: ImageVector
    get() {
        if (_CiSkull != null) {
            return _CiSkull!!
        }
        _CiSkull = ImageVector.Builder(
            name = "CiSkull",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(402f, 76.94f)
                curveTo(362.61f, 37.63f, 310.78f, 16f, 256f, 16f)
                horizontalLineToRelative(-0.37f)
                arcTo(208f, 208f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 224f)
                lineTo(48f, 324.67f)
                arcTo(79.62f, 79.62f, 0f, isMoreThanHalf = false, isPositiveArc = false, 98.29f, 399f)
                lineTo(122f, 408.42f)
                arcToRelative(15.92f, 15.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.75f, 11.72f)
                lineToRelative(10f, 50.13f)
                arcTo(32.09f, 32.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 173.12f, 496f)
                lineTo(184f, 496f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, -8f)
                lineTo(192f, 448.45f)
                curveToRelative(0f, -8.61f, 6.62f, -16f, 15.23f, -16.43f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 224f, 448f)
                verticalLineToRelative(40f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 8f)
                horizontalLineToRelative(0f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, -8f)
                lineTo(240f, 448.45f)
                curveToRelative(0f, -8.61f, 6.62f, -16f, 15.23f, -16.43f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 272f, 448f)
                verticalLineToRelative(40f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 8f)
                horizontalLineToRelative(0f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, -8f)
                lineTo(288f, 448.45f)
                curveToRelative(0f, -8.61f, 6.62f, -16f, 15.23f, -16.43f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 320f, 448f)
                verticalLineToRelative(40f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 8f)
                horizontalLineToRelative(10.88f)
                arcToRelative(32.09f, 32.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 31.38f, -25.72f)
                lineToRelative(10f, -50.14f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 390f, 408.42f)
                lineTo(413.71f, 399f)
                arcTo(79.62f, 79.62f, 0f, isMoreThanHalf = false, isPositiveArc = false, 464f, 324.67f)
                verticalLineToRelative(-99f)
                curveTo(464f, 169.67f, 442f, 116.86f, 402f, 76.94f)
                close()
                moveTo(171.66f, 335.88f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = true, isPositiveArc = true, 52.22f, -52.22f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = true, 171.66f, 335.88f)
                close()
                moveTo(281f, 397.25f)
                arcTo(16.37f, 16.37f, 0f, isMoreThanHalf = false, isPositiveArc = true, 271.7f, 400f)
                lineTo(240.3f, 400f)
                arcToRelative(16.37f, 16.37f, 0f, isMoreThanHalf = false, isPositiveArc = true, -9.28f, -2.75f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6.6f, -16.9f)
                lineToRelative(15.91f, -47.6f)
                curveTo(243f, 326f, 247.25f, 321f, 254f, 320.13f)
                curveToRelative(8.26f, -1f, 14f, 2.87f, 17.61f, 12.22f)
                lineToRelative(16f, 48f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 281f, 397.25f)
                close()
                moveTo(347.68f, 335.88f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = true, isPositiveArc = true, 52.22f, -52.22f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = true, 347.66f, 335.88f)
                close()
            }
        }.build()

        return _CiSkull!!
    }

@Suppress("ObjectPropertyName")
private var _CiSkull: ImageVector? = null
