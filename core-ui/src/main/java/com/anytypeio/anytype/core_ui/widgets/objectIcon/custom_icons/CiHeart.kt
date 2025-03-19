package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiHeart: ImageVector
    get() {
        if (_CiHeart != null) {
            return _CiHeart!!
        }
        _CiHeart = ImageVector.Builder(
            name = "CiHeart",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 448f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -18f, -5.57f)
                curveToRelative(-78.59f, -53.35f, -112.62f, -89.93f, -131.39f, -112.8f)
                curveToRelative(-40f, -48.75f, -59.15f, -98.8f, -58.61f, -153f)
                curveTo(48.63f, 114.52f, 98.46f, 64f, 159.08f, 64f)
                curveToRelative(44.08f, 0f, 74.61f, 24.83f, 92.39f, 45.51f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.06f, 0f)
                curveTo(278.31f, 88.81f, 308.84f, 64f, 352.92f, 64f)
                curveTo(413.54f, 64f, 463.37f, 114.52f, 464f, 176.64f)
                curveToRelative(0.54f, 54.21f, -18.63f, 104.26f, -58.61f, 153f)
                curveToRelative(-18.77f, 22.87f, -52.8f, 59.45f, -131.39f, 112.8f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 448f)
                close()
            }
        }.build()

        return _CiHeart!!
    }

@Suppress("ObjectPropertyName")
private var _CiHeart: ImageVector? = null
