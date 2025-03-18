package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBandage: ImageVector
    get() {
        if (_CiBandage != null) {
            return _CiBandage!!
        }
        _CiBandage = ImageVector.Builder(
            name = "CiBandage",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(275.8f, 157f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -22.63f, 0f)
                lineToRelative(-93.34f, 93.34f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 22.63f)
                lineToRelative(79.2f, 79.2f)
                horizontalLineToRelative(0f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 22.63f, 0f)
                lineTo(355f, 258.83f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -22.63f)
                close()
                moveTo(219.31f, 267.31f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -22.62f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 219.31f, 267.31f)
                close()
                moveTo(267.31f, 315.31f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -22.62f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 267.31f, 315.31f)
                close()
                moveTo(267.31f, 219.31f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -22.62f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 267.31f, 219.31f)
                close()
                moveTo(315.31f, 267.31f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -22.62f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 315.31f, 267.31f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(465.61f, 46.39f)
                arcToRelative(104.38f, 104.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, -147.25f, 0f)
                lineTo(248.6f, 116.28f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.2f, 6.58f)
                arcToRelative(35.74f, 35.74f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.69f, -2.54f)
                arcToRelative(47.7f, 47.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 33.94f, 14.06f)
                lineToRelative(79.19f, 79.19f)
                arcToRelative(47.7f, 47.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.06f, 33.94f)
                arcToRelative(35.68f, 35.68f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.54f, 11.69f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.58f, 4.2f)
                lineToRelative(69.89f, -69.76f)
                arcToRelative(104.38f, 104.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -147.25f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(254.34f, 386.83f)
                arcToRelative(47.91f, 47.91f, 0f, isMoreThanHalf = false, isPositiveArc = true, -33.94f, -14f)
                lineTo(141.21f, 293.6f)
                arcToRelative(47.81f, 47.81f, 0f, isMoreThanHalf = false, isPositiveArc = true, -9.43f, -13.38f)
                curveToRelative(-4.59f, -9.7f, -1.39f, -25f, 2.48f, -36.9f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.64f, -4f)
                lineTo(50.39f, 316.36f)
                arcTo(104.12f, 104.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 197.64f, 463.61f)
                lineToRelative(72.75f, -72.88f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4.21f, -6.58f)
                curveTo(262f, 385.73f, 257.78f, 386.83f, 254.34f, 386.83f)
                close()
            }
        }.build()

        return _CiBandage!!
    }

@Suppress("ObjectPropertyName")
private var _CiBandage: ImageVector? = null
