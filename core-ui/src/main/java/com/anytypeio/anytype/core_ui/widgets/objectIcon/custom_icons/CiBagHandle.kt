package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBagHandle: ImageVector
    get() {
        if (_CiBagHandle != null) {
            return _CiBagHandle!!
        }
        _CiBagHandle = ImageVector.Builder(
            name = "CiBagHandle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(454.65f, 169.4f)
                arcTo(31.82f, 31.82f, 0f, isMoreThanHalf = false, isPositiveArc = false, 432f, 160f)
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
                arcTo(31.75f, 31.75f, 0f, isMoreThanHalf = false, isPositiveArc = false, 454.65f, 169.4f)
                close()
                moveTo(176f, 144f)
                arcToRelative(80f, 80f, 0f, isMoreThanHalf = false, isPositiveArc = true, 160f, 0f)
                verticalLineToRelative(16f)
                lineTo(176f, 160f)
                close()
                moveTo(368f, 240f)
                arcToRelative(112f, 112f, 0f, isMoreThanHalf = false, isPositiveArc = true, -224f, 0f)
                lineTo(144f, 224f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 0f)
                verticalLineToRelative(16f)
                arcToRelative(80f, 80f, 0f, isMoreThanHalf = false, isPositiveArc = false, 160f, 0f)
                lineTo(336f, 224f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 0f)
                close()
            }
        }.build()

        return _CiBagHandle!!
    }

@Suppress("ObjectPropertyName")
private var _CiBagHandle: ImageVector? = null
