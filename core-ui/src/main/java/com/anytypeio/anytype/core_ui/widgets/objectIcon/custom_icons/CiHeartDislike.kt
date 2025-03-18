package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiHeartDislike: ImageVector
    get() {
        if (_CiHeartDislike != null) {
            return _CiHeartDislike!!
        }
        _CiHeartDislike = ImageVector.Builder(
            name = "CiHeartDislike",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(417.84f, 448f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.35f, -4.72f)
                lineTo(40.65f, 75.28f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 22.7f, -22.56f)
                lineToRelative(365.83f, 368f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 417.84f, 448f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(364.92f, 80f)
                curveToRelative(-44.09f, 0f, -74.61f, 24.82f, -92.39f, 45.5f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, -9.06f, 0f)
                curveTo(245.69f, 104.82f, 215.16f, 80f, 171.08f, 80f)
                arcToRelative(107.71f, 107.71f, 0f, isMoreThanHalf = false, isPositiveArc = false, -31f, 4.54f)
                lineToRelative(269.13f, 270.7f)
                curveToRelative(3f, -3.44f, 5.7f, -6.64f, 8.14f, -9.6f)
                curveToRelative(40f, -48.75f, 59.15f, -98.79f, 58.61f, -153f)
                curveTo(475.37f, 130.53f, 425.54f, 80f, 364.92f, 80f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(69f, 149.15f)
                arcToRelative(115.06f, 115.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, -9f, 43.49f)
                curveToRelative(-0.54f, 54.21f, 18.63f, 104.25f, 58.61f, 153f)
                curveToRelative(18.77f, 22.87f, 52.8f, 59.45f, 131.39f, 112.8f)
                arcToRelative(31.88f, 31.88f, 0f, isMoreThanHalf = false, isPositiveArc = false, 36f, 0f)
                curveToRelative(20.35f, -13.82f, 37.7f, -26.5f, 52.58f, -38.12f)
                close()
            }
        }.build()

        return _CiHeartDislike!!
    }

@Suppress("ObjectPropertyName")
private var _CiHeartDislike: ImageVector? = null
