package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiArrowRedo: ImageVector
    get() {
        if (_CiArrowRedo != null) {
            return _CiArrowRedo!!
        }
        _CiArrowRedo = ImageVector.Builder(
            name = "CiArrowRedo",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(58.79f, 439.13f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 48f, 424f)
                curveToRelative(0f, -73.1f, 14.68f, -131.56f, 43.65f, -173.77f)
                curveToRelative(35f, -51f, 90.21f, -78.46f, 164.35f, -81.87f)
                verticalLineTo(88f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 27.05f, -11.57f)
                lineToRelative(176f, 168f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 23.14f)
                lineToRelative(-176f, 168f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 424f)
                verticalLineTo(344.23f)
                curveToRelative(-45f, 1.36f, -79f, 8.65f, -106.07f, 22.64f)
                curveToRelative(-29.25f, 15.12f, -50.46f, 37.71f, -73.32f, 67f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -17.82f, 5.28f)
                close()
            }
        }.build()

        return _CiArrowRedo!!
    }

@Suppress("ObjectPropertyName")
private var _CiArrowRedo: ImageVector? = null
