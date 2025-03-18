package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiRadio: ImageVector
    get() {
        if (_CiRadio != null) {
            return _CiRadio!!
        }
        _CiRadio = ImageVector.Builder(
            name = "CiRadio",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 256f)
                moveToRelative(-36f, 0f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = true, isPositiveArc = true, 72f, 0f)
                arcToRelative(36f, 36f, 0f, isMoreThanHalf = true, isPositiveArc = true, -72f, 0f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(190.24f, 341.77f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16.46f, -7.38f)
                arcToRelative(118f, 118f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -156.76f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32.87f, 29.24f)
                arcToRelative(74f, 74f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 98.29f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16.43f, 36.61f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(321.76f, 341.77f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16.43f, -36.61f)
                arcToRelative(74f, 74f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -98.29f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32.87f, -29.24f)
                arcToRelative(118f, 118f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 156.76f)
                arcTo(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 321.76f, 341.77f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(139.29f, 392.72f)
                arcToRelative(21.92f, 21.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16.08f, -7f)
                arcToRelative(190f, 190f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -259.49f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32.13f, 30.06f)
                arcToRelative(146f, 146f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 199.38f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16.06f, 37f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(372.71f, 392.72f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16.06f, -37f)
                arcToRelative(146f, 146f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -199.38f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32.13f, -30.06f)
                arcToRelative(190f, 190f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 259.49f)
                arcTo(21.92f, 21.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, 372.71f, 392.72f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(429f, 438f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16.39f, -36.67f)
                arcToRelative(218.34f, 218.34f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -290.66f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32.78f, -29.34f)
                arcToRelative(262.34f, 262.34f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 349.34f)
                arcTo(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 429f, 438f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(83f, 438f)
                arcToRelative(21.94f, 21.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16.41f, -7.33f)
                arcToRelative(262.34f, 262.34f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -349.34f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32.78f, 29.34f)
                arcToRelative(218.34f, 218.34f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 290.66f)
                arcTo(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 83f, 438f)
                close()
            }
        }.build()

        return _CiRadio!!
    }

@Suppress("ObjectPropertyName")
private var _CiRadio: ImageVector? = null
