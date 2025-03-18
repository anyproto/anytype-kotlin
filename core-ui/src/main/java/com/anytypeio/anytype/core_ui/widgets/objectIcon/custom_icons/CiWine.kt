package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiWine: ImageVector
    get() {
        if (_CiWine != null) {
            return _CiWine!!
        }
        _CiWine = ImageVector.Builder(
            name = "CiWine",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(414.56f, 94.92f)
                lineTo(414.56f, 80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, -16f)
                lineTo(113.44f, 64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 16f)
                lineTo(97.44f, 94.92f)
                curveToRelative(-1.46f, 11.37f, -9.65f, 90.74f, 36.93f, 144.69f)
                curveToRelative(24.87f, 28.8f, 60.36f, 44.85f, 105.63f, 47.86f)
                lineTo(240f, 416f)
                lineTo(160f, 416f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                lineTo(352f, 448f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                lineTo(272f, 416f)
                lineTo(272f, 287.47f)
                curveToRelative(45.27f, -3f, 80.76f, -19.06f, 105.63f, -47.86f)
                curveTo(424.21f, 185.66f, 416f, 106.29f, 414.56f, 94.92f)
                close()
                moveTo(129.26f, 98.33f)
                arcToRelative(15.14f, 15.14f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.18f, -2.33f)
                lineTo(382.56f, 96f)
                arcToRelative(15.14f, 15.14f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.18f, 2.33f)
                arcToRelative(201.91f, 201.91f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 45.67f)
                lineTo(129.32f, 144f)
                arcTo(204.29f, 204.29f, 0f, isMoreThanHalf = false, isPositiveArc = true, 129.26f, 98.33f)
                close()
            }
        }.build()

        return _CiWine!!
    }

@Suppress("ObjectPropertyName")
private var _CiWine: ImageVector? = null
