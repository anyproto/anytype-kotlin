package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiHeartHalf: ImageVector
    get() {
        if (_CiHeartHalf != null) {
            return _CiHeartHalf!!
        }
        _CiHeartHalf = ImageVector.Builder(
            name = "CiHeartHalf",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(352.92f, 64f)
                curveToRelative(-48.09f, 0f, -80f, 29.54f, -96.92f, 51f)
                curveToRelative(-16.88f, -21.49f, -48.83f, -51f, -96.92f, -51f)
                curveTo(98.46f, 64f, 48.63f, 114.54f, 48f, 176.65f)
                curveToRelative(-0.54f, 54.21f, 18.63f, 104.27f, 58.61f, 153f)
                curveToRelative(18.77f, 22.88f, 52.8f, 59.46f, 131.39f, 112.81f)
                arcToRelative(31.84f, 31.84f, 0f, isMoreThanHalf = false, isPositiveArc = false, 36f, 0f)
                curveToRelative(78.59f, -53.35f, 112.62f, -89.93f, 131.39f, -112.81f)
                curveToRelative(40f, -48.74f, 59.15f, -98.8f, 58.61f, -153f)
                curveTo(463.37f, 114.54f, 413.54f, 64f, 352.92f, 64f)
                close()
                moveTo(256f, 416f)
                verticalLineTo(207.58f)
                curveToRelative(0f, -19.63f, 5.23f, -38.76f, 14.21f, -56.22f)
                arcToRelative(1.19f, 1.19f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.08f, -0.16f)
                arcToRelative(123f, 123f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21.77f, -28.51f)
                curveTo(310.19f, 105f, 330.66f, 96f, 352.92f, 96f)
                curveToRelative(43.15f, 0f, 78.62f, 36.32f, 79.07f, 81f)
                curveTo(433f, 281.61f, 343.63f, 356.51f, 256f, 416f)
                close()
            }
        }.build()

        return _CiHeartHalf!!
    }

@Suppress("ObjectPropertyName")
private var _CiHeartHalf: ImageVector? = null
