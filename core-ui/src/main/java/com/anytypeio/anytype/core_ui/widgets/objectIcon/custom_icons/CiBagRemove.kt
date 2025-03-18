package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBagRemove: ImageVector
    get() {
        if (_CiBagRemove != null) {
            return _CiBagRemove!!
        }
        _CiBagRemove = ImageVector.Builder(
            name = "CiBagRemove",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(454.66f, 169.4f)
                arcTo(31.86f, 31.86f, 0f, isMoreThanHalf = false, isPositiveArc = false, 432f, 160f)
                lineTo(368f, 160f)
                lineTo(368f, 144f)
                arcToRelative(112f, 112f, 0f, isMoreThanHalf = false, isPositiveArc = false, -224f, 0f)
                verticalLineToRelative(16f)
                lineTo(80f, 160f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, 32f)
                lineTo(48f, 408f)
                curveToRelative(0f, 39f, 33f, 72f, 72f, 72f)
                lineTo(392f, 480f)
                arcToRelative(72.22f, 72.22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 50.48f, -20.55f)
                arcTo(69.48f, 69.48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 464f, 409.25f)
                lineTo(464f, 192f)
                arcTo(31.78f, 31.78f, 0f, isMoreThanHalf = false, isPositiveArc = false, 454.66f, 169.4f)
                close()
                moveTo(320f, 336f)
                lineTo(192f, 336f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                lineTo(320f, 304f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
                moveTo(336f, 160f)
                lineTo(176f, 160f)
                lineTo(176f, 144f)
                arcToRelative(80f, 80f, 0f, isMoreThanHalf = false, isPositiveArc = true, 160f, 0f)
                close()
            }
        }.build()

        return _CiBagRemove!!
    }

@Suppress("ObjectPropertyName")
private var _CiBagRemove: ImageVector? = null
