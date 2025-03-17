package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBody: ImageVector
    get() {
        if (_CiBody != null) {
            return _CiBody!!
        }
        _CiBody = ImageVector.Builder(
            name = "CiBody",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 56f)
                moveToRelative(-56f, 0f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = true, isPositiveArc = true, 112f, 0f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = true, isPositiveArc = true, -112f, 0f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(437f, 128f)
                horizontalLineTo(75f)
                arcToRelative(27f, 27f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 54f)
                horizontalLineTo(176.88f)
                curveToRelative(6.91f, 0f, 15f, 3.09f, 19.58f, 15f)
                curveToRelative(5.35f, 13.83f, 2.73f, 40.54f, -0.57f, 61.23f)
                lineToRelative(-4.32f, 24.45f)
                arcToRelative(0.42f, 0.42f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.12f, 0.35f)
                lineToRelative(-34.6f, 196.81f)
                arcTo(27.43f, 27.43f, 0f, isMoreThanHalf = false, isPositiveArc = false, 179f, 511.58f)
                arcToRelative(27.06f, 27.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 31.42f, -22.29f)
                lineToRelative(23.91f, -136.8f)
                reflectiveCurveTo(242f, 320f, 256f, 320f)
                curveToRelative(14.23f, 0f, 21.74f, 32.49f, 21.74f, 32.49f)
                lineToRelative(23.91f, 136.92f)
                arcToRelative(27.24f, 27.24f, 0f, isMoreThanHalf = true, isPositiveArc = false, 53.62f, -9.6f)
                lineTo(320.66f, 283f)
                arcToRelative(0.45f, 0.45f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.11f, -0.35f)
                lineToRelative(-4.33f, -24.45f)
                curveToRelative(-3.3f, -20.69f, -5.92f, -47.4f, -0.57f, -61.23f)
                curveToRelative(4.56f, -11.88f, 12.91f, -15f, 19.28f, -15f)
                horizontalLineTo(437f)
                arcToRelative(27f, 27f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -54f)
                close()
            }
        }.build()

        return _CiBody!!
    }

@Suppress("ObjectPropertyName")
private var _CiBody: ImageVector? = null
