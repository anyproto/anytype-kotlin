package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiGolf: ImageVector
    get() {
        if (_CiGolf != null) {
            return _CiGolf!!
        }
        _CiGolf = ImageVector.Builder(
            name = "CiGolf",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(272f, 320.46f)
                verticalLineTo(202.3f)
                lineToRelative(166.62f, -75.73f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -29.14f)
                lineToRelative(-176f, -80f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 240f, 32f)
                verticalLineTo(191.66f)
                curveToRelative(0f, 0.23f, 0f, 0.47f, 0f, 0.7f)
                verticalLineToRelative(128.1f)
                quadToRelative(8f, -0.45f, 16f, -0.46f)
                reflectiveQuadTo(272f, 320.46f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(463.33f, 457.5f)
                curveToRelative(-8.56f, -42.85f, -35.11f, -78.74f, -76.78f, -103.8f)
                curveTo(354.05f, 334.15f, 313.88f, 322.4f, 272f, 320f)
                verticalLineToRelative(79.75f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, -32f, 0f)
                verticalLineTo(320f)
                curveToRelative(-41.88f, 2.4f, -82.05f, 14.15f, -114.55f, 33.7f)
                curveToRelative(-41.67f, 25.06f, -68.22f, 60.95f, -76.78f, 103.8f)
                arcToRelative(32.49f, 32.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.44f, 27.08f)
                curveTo(61.13f, 492f, 70f, 496f, 80f, 496f)
                horizontalLineTo(432f)
                curveToRelative(10f, 0f, 18.88f, -4.05f, 24.9f, -11.42f)
                arcTo(32.49f, 32.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, 463.33f, 457.5f)
                close()
            }
        }.build()

        return _CiGolf!!
    }

@Suppress("ObjectPropertyName")
private var _CiGolf: ImageVector? = null
