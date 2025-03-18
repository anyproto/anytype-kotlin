package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiSunny: ImageVector
    get() {
        if (_CiSunny != null) {
            return _CiSunny!!
        }
        _CiSunny = ImageVector.Builder(
            name = "CiSunny",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 118f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22f, -22f)
                verticalLineTo(48f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 44f, 0f)
                verticalLineTo(96f)
                arcTo(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 118f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 486f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22f, -22f)
                verticalLineTo(416f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 44f, 0f)
                verticalLineToRelative(48f)
                arcTo(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 486f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(369.14f, 164.86f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -15.56f, -37.55f)
                lineToRelative(33.94f, -33.94f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.11f, 31.11f)
                lineToRelative(-33.94f, 33.94f)
                arcTo(21.93f, 21.93f, 0f, isMoreThanHalf = false, isPositiveArc = true, 369.14f, 164.86f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(108.92f, 425.08f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -15.55f, -37.56f)
                lineToRelative(33.94f, -33.94f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = true, isPositiveArc = true, 31.11f, 31.11f)
                lineToRelative(-33.94f, 33.94f)
                arcTo(21.94f, 21.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, 108.92f, 425.08f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(464f, 278f)
                horizontalLineTo(416f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -44f)
                horizontalLineToRelative(48f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 44f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(96f, 278f)
                horizontalLineTo(48f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -44f)
                horizontalLineTo(96f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 44f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(403.08f, 425.08f)
                arcToRelative(21.94f, 21.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, -15.56f, -6.45f)
                lineToRelative(-33.94f, -33.94f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.11f, -31.11f)
                lineToRelative(33.94f, 33.94f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -15.55f, 37.56f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(142.86f, 164.86f)
                arcToRelative(21.89f, 21.89f, 0f, isMoreThanHalf = false, isPositiveArc = true, -15.55f, -6.44f)
                lineTo(93.37f, 124.48f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.11f, -31.11f)
                lineToRelative(33.94f, 33.94f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -15.56f, 37.55f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 358f)
                arcTo(102f, 102f, 0f, isMoreThanHalf = true, isPositiveArc = true, 358f, 256f)
                arcTo(102.12f, 102.12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 358f)
                close()
            }
        }.build()

        return _CiSunny!!
    }

@Suppress("ObjectPropertyName")
private var _CiSunny: ImageVector? = null
