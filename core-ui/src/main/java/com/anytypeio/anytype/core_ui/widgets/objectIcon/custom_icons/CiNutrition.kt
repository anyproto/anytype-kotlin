package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiNutrition: ImageVector
    get() {
        if (_CiNutrition != null) {
            return _CiNutrition!!
        }
        _CiNutrition = ImageVector.Builder(
            name = "CiNutrition",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(439f, 166.29f)
                curveToRelative(-18.67f, -32.57f, -47.46f, -50.81f, -85.57f, -54.23f)
                curveToRelative(-20.18f, -1.8f, -39f, 3.37f, -57.23f, 8.38f)
                curveTo(282.05f, 124.33f, 268.68f, 128f, 256f, 128f)
                reflectiveCurveToRelative(-26f, -3.68f, -40.06f, -7.57f)
                curveToRelative(-18.28f, -5f, -37.18f, -10.26f, -57.43f, -8.36f)
                curveTo(122.12f, 115.48f, 93f, 134.18f, 74.2f, 166.15f)
                curveTo(56.82f, 195.76f, 48f, 236.76f, 48f, 288f)
                curveToRelative(0f, 40.4f, 15f, 90.49f, 40f, 134f)
                curveToRelative(12.82f, 22.25f, 47f, 74f, 87.16f, 74f)
                curveToRelative(30.77f, 0f, 47.15f, -9.44f, 59.11f, -16.33f)
                curveToRelative(8.3f, -4.78f, 13.31f, -7.67f, 21.69f, -7.67f)
                reflectiveCurveToRelative(13.39f, 2.89f, 21.69f, 7.67f)
                curveTo(289.65f, 486.56f, 306f, 496f, 336.8f, 496f)
                curveToRelative(40.17f, 0f, 74.34f, -51.76f, 87.16f, -74f)
                curveToRelative(25.07f, -43.5f, 40f, -93.59f, 40f, -134f)
                curveTo(464f, 235.43f, 455.82f, 195.62f, 439f, 166.29f)
                close()
                moveTo(216f, 352f)
                curveToRelative(-13.25f, 0f, -24f, -21.49f, -24f, -48f)
                reflectiveCurveToRelative(10.75f, -48f, 24f, -48f)
                reflectiveCurveToRelative(24f, 21.49f, 24f, 48f)
                reflectiveCurveTo(229.25f, 352f, 216f, 352f)
                close()
                moveTo(296f, 352f)
                curveToRelative(-13.25f, 0f, -24f, -21.49f, -24f, -48f)
                reflectiveCurveToRelative(10.75f, -48f, 24f, -48f)
                reflectiveCurveToRelative(24f, 21.49f, 24f, 48f)
                reflectiveCurveTo(309.25f, 352f, 296f, 352f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(265.1f, 111.93f)
                curveToRelative(13.16f, -1.75f, 37.86f, -7.83f, 58.83f, -28.79f)
                arcToRelative(98f, 98f, 0f, isMoreThanHalf = false, isPositiveArc = false, 28f, -58.2f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 343.38f, 16f)
                curveToRelative(-12.71f, 0.95f, -36.76f, 5.87f, -58.73f, 27.85f)
                arcTo(97.6f, 97.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 103.2f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 265.1f, 111.93f)
                close()
            }
        }.build()

        return _CiNutrition!!
    }

@Suppress("ObjectPropertyName")
private var _CiNutrition: ImageVector? = null
