package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiWalk: ImageVector
    get() {
        if (_CiWalk != null) {
            return _CiWalk!!
        }
        _CiWalk = ImageVector.Builder(
            name = "CiWalk",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(312.55f, 479.9f)
                lineToRelative(-56.42f, -114f)
                lineToRelative(-44.62f, -57f)
                arcTo(72.37f, 72.37f, 0f, isMoreThanHalf = false, isPositiveArc = true, 201.45f, 272f)
                verticalLineTo(143.64f)
                horizontalLineTo(217f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = true, 40f, 40f)
                verticalLineTo(365.85f)
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(127.38f, 291.78f)
                verticalLineTo(217.71f)
                reflectiveCurveToRelative(37f, -74.07f, 74.07f, -74.07f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(368.09f, 291.78f)
                arcToRelative(18.49f, 18.49f, 0f, isMoreThanHalf = false, isPositiveArc = true, -10.26f, -3.11f)
                lineTo(297.7f, 250f)
                arcTo(21.18f, 21.18f, 0f, isMoreThanHalf = false, isPositiveArc = true, 288f, 232.21f)
                verticalLineToRelative(-23.7f)
                arcToRelative(5.65f, 5.65f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.69f, -4.77f)
                lineToRelative(81.65f, 54.11f)
                arcToRelative(18.52f, 18.52f, 0f, isMoreThanHalf = false, isPositiveArc = true, -10.29f, 33.93f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(171.91f, 493.47f)
                arcToRelative(18.5f, 18.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.83f, -7.41f)
                curveToRelative(-6.14f, -8.18f, -4f, -17.18f, 3.7f, -25.92f)
                lineToRelative(59.95f, -74.66f)
                arcToRelative(7.41f, 7.41f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10.76f, 2.06f)
                curveToRelative(1.56f, 2.54f, 3.38f, 5.65f, 5.19f, 9.09f)
                curveToRelative(5.24f, 9.95f, 6f, 16.11f, -1.68f, 25.7f)
                curveToRelative(-8f, 10f, -52f, 67.44f, -52f, 67.44f)
                curveTo(180.38f, 492.75f, 175.77f, 493.47f, 171.91f, 493.47f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 16f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(257f, 69.56f)
                moveToRelative(-37.04f, 0f)
                arcToRelative(37.04f, 37.04f, 0f, isMoreThanHalf = true, isPositiveArc = true, 74.08f, 0f)
                arcToRelative(37.04f, 37.04f, 0f, isMoreThanHalf = true, isPositiveArc = true, -74.08f, 0f)
            }
        }.build()

        return _CiWalk!!
    }

@Suppress("ObjectPropertyName")
private var _CiWalk: ImageVector? = null
