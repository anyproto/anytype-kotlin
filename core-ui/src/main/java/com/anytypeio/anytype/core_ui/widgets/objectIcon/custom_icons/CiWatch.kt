package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiWatch: ImageVector
    get() {
        if (_CiWatch != null) {
            return _CiWatch!!
        }
        _CiWatch = ImageVector.Builder(
            name = "CiWatch",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(192f, 136f)
                lineTo(320f, 136f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = true, 376f, 192f)
                lineTo(376f, 320f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = true, 320f, 376f)
                lineTo(192f, 376f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = true, 136f, 320f)
                lineTo(136f, 192f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = true, 192f, 136f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(336f, 96f)
                lineTo(336f, 32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, -16f)
                lineTo(192f, 16f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 16f)
                lineTo(176f, 96f)
                arcToRelative(80.09f, 80.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, -80f, 80f)
                lineTo(96f, 336f)
                arcToRelative(80.09f, 80.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, 80f)
                verticalLineToRelative(64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                lineTo(320f, 496f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, -16f)
                lineTo(336f, 416f)
                arcToRelative(80.09f, 80.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, -80f)
                lineTo(416f, 176f)
                arcTo(80.09f, 80.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 336f, 96f)
                close()
                moveTo(392f, 320f)
                arcToRelative(72.08f, 72.08f, 0f, isMoreThanHalf = false, isPositiveArc = true, -72f, 72f)
                lineTo(192f, 392f)
                arcToRelative(72.08f, 72.08f, 0f, isMoreThanHalf = false, isPositiveArc = true, -72f, -72f)
                lineTo(120f, 192f)
                arcToRelative(72.08f, 72.08f, 0f, isMoreThanHalf = false, isPositiveArc = true, 72f, -72f)
                lineTo(320f, 120f)
                arcToRelative(72.08f, 72.08f, 0f, isMoreThanHalf = false, isPositiveArc = true, 72f, 72f)
                close()
            }
        }.build()

        return _CiWatch!!
    }

@Suppress("ObjectPropertyName")
private var _CiWatch: ImageVector? = null
