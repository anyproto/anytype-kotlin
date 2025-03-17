package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBasketball: ImageVector
    get() {
        if (_CiBasketball != null) {
            return _CiBasketball!!
        }
        _CiBasketball = ImageVector.Builder(
            name = "CiBasketball",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 233.37f)
                lineToRelative(34.45f, -34.45f)
                arcTo(207.08f, 207.08f, 0f, isMoreThanHalf = false, isPositiveArc = true, 240.33f, 63.67f)
                curveToRelative(0f, -5f, 0.19f, -10.05f, 0.54f, -15f)
                arcTo(207.09f, 207.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 120.67f, 98f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(313.14f, 176.23f)
                lineTo(391.33f, 98f)
                arcTo(207.07f, 207.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 273f, 48.8f)
                curveToRelative(-0.41f, 4.9f, -0.64f, 9.86f, -0.64f, 14.87f)
                arcTo(175.25f, 175.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 313.14f, 176.23f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(335.77f, 198.86f)
                arcToRelative(175.25f, 175.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 112.56f, 40.81f)
                curveToRelative(5f, 0f, 10f, -0.23f, 14.87f, -0.64f)
                arcTo(207.07f, 207.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 414f, 120.67f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(176.23f, 313.14f)
                arcTo(175.23f, 175.23f, 0f, isMoreThanHalf = false, isPositiveArc = false, 63.67f, 272.33f)
                quadToRelative(-7.52f, 0f, -14.87f, 0.64f)
                arcTo(207.07f, 207.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 98f, 391.33f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 278.63f)
                lineToRelative(-34.45f, 34.45f)
                arcToRelative(207.08f, 207.08f, 0f, isMoreThanHalf = false, isPositiveArc = true, 50.12f, 135.25f)
                curveToRelative(0f, 5f, -0.19f, 10.05f, -0.54f, 15f)
                arcTo(207.06f, 207.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 391.33f, 414f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(448.33f, 271.67f)
                arcToRelative(207.08f, 207.08f, 0f, isMoreThanHalf = false, isPositiveArc = true, -135.25f, -50.12f)
                lineTo(278.63f, 256f)
                lineTo(414f, 391.33f)
                arcToRelative(207.09f, 207.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 49.39f, -120.2f)
                curveTo(458.38f, 271.48f, 453.37f, 271.67f, 448.33f, 271.67f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(233.37f, 256f)
                lineTo(98f, 120.67f)
                arcToRelative(207.06f, 207.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, -49.39f, 120.2f)
                curveToRelative(5f, -0.35f, 10f, -0.54f, 15f, -0.54f)
                arcToRelative(207.08f, 207.08f, 0f, isMoreThanHalf = false, isPositiveArc = true, 135.25f, 50.12f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(120.67f, 414f)
                arcTo(207.07f, 207.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 239f, 463.2f)
                quadToRelative(0.63f, -7.35f, 0.64f, -14.87f)
                arcToRelative(175.23f, 175.23f, 0f, isMoreThanHalf = false, isPositiveArc = false, -40.81f, -112.56f)
                close()
            }
        }.build()

        return _CiBasketball!!
    }

@Suppress("ObjectPropertyName")
private var _CiBasketball: ImageVector? = null
