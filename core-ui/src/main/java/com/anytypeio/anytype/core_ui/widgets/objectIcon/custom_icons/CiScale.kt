package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiScale: ImageVector
    get() {
        if (_CiScale != null) {
            return _CiScale!!
        }
        _CiScale = ImageVector.Builder(
            name = "CiScale",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(368f, 32f)
                lineTo(144f, 32f)
                arcTo(112.12f, 112.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 144f)
                lineTo(32f, 368f)
                arcTo(112.12f, 112.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 144f, 480f)
                lineTo(368f, 480f)
                arcTo(112.12f, 112.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 480f, 368f)
                lineTo(480f, 144f)
                arcTo(112.12f, 112.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 368f, 32f)
                close()
                moveTo(404.21f, 210f)
                lineTo(370.89f, 249.21f)
                arcTo(41.76f, 41.76f, 0f, isMoreThanHalf = false, isPositiveArc = true, 339f, 264.05f)
                arcToRelative(42.32f, 42.32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.29f, -6.38f)
                curveToRelative(-14.22f, -8.78f, -36.3f, -19.25f, -60.69f, -19.25f)
                reflectiveCurveToRelative(-46.47f, 10.47f, -60.69f, 19.25f)
                arcToRelative(41.86f, 41.86f, 0f, isMoreThanHalf = false, isPositiveArc = true, -54.2f, -8.46f)
                lineTo(107.79f, 210f)
                arcToRelative(50.48f, 50.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.49f, -70.27f)
                curveTo(140.12f, 114.38f, 187.65f, 84.16f, 256f, 84.16f)
                reflectiveCurveToRelative(115.88f, 30.22f, 143.72f, 55.57f)
                arcTo(50.48f, 50.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 404.21f, 210f)
                close()
            }
        }.build()

        return _CiScale!!
    }

@Suppress("ObjectPropertyName")
private var _CiScale: ImageVector? = null
