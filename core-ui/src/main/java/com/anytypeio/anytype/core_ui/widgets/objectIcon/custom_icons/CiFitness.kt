package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFitness: ImageVector
    get() {
        if (_CiFitness != null) {
            return _CiFitness!!
        }
        _CiFitness = ImageVector.Builder(
            name = "CiFitness",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(193.69f, 152.84f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 29.64f, 2.56f)
                lineToRelative(36.4f, 121.36f)
                lineToRelative(30f, -59.92f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.62f, 0f)
                lineTo(345.89f, 272f)
                horizontalLineToRelative(96.76f)
                arcTo(213.08f, 213.08f, 0f, isMoreThanHalf = false, isPositiveArc = false, 464f, 176.65f)
                curveTo(463.37f, 114.54f, 413.54f, 64f, 352.92f, 64f)
                curveToRelative(-48.09f, 0f, -80f, 29.54f, -96.92f, 51f)
                curveToRelative(-16.88f, -21.49f, -48.83f, -51f, -96.92f, -51f)
                curveTo(98.46f, 64f, 48.63f, 114.54f, 48f, 176.65f)
                arcTo(211.13f, 211.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56.93f, 240f)
                horizontalLineToRelative(93.18f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(321.69f, 295.16f)
                lineTo(304f, 259.78f)
                lineToRelative(-33.69f, 67.38f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 336f)
                quadToRelative(-0.67f, 0f, -1.38f, -0.06f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14f, -11.34f)
                lineToRelative(-36.4f, -121.36f)
                lineToRelative(-30f, 59.92f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 160f, 272f)
                horizontalLineTo(69.35f)
                quadToRelative(14f, 29.29f, 37.27f, 57.66f)
                curveToRelative(18.77f, 22.88f, 52.8f, 59.46f, 131.39f, 112.81f)
                arcToRelative(31.84f, 31.84f, 0f, isMoreThanHalf = false, isPositiveArc = false, 36f, 0f)
                curveToRelative(78.59f, -53.35f, 112.62f, -89.93f, 131.39f, -112.81f)
                arcToRelative(316.79f, 316.79f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19f, -25.66f)
                horizontalLineTo(336f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 321.69f, 295.16f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(464f, 272f)
                horizontalLineTo(442.65f)
                arcToRelative(260.11f, 260.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, -18.25f, 32f)
                horizontalLineTo(464f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(48f, 240f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineTo(69.35f)
                arcToRelative(225.22f, 225.22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -12.42f, -32f)
                close()
            }
        }.build()

        return _CiFitness!!
    }

@Suppress("ObjectPropertyName")
private var _CiFitness: ImageVector? = null
