package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCloud: ImageVector
    get() {
        if (_CiCloud != null) {
            return _CiCloud!!
        }
        _CiCloud = ImageVector.Builder(
            name = "CiCloud",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(396f, 432f)
                horizontalLineTo(136f)
                curveToRelative(-36.44f, 0f, -70.36f, -12.57f, -95.51f, -35.41f)
                curveTo(14.38f, 372.88f, 0f, 340f, 0f, 304f)
                curveToRelative(0f, -36.58f, 13.39f, -68.12f, 38.72f, -91.22f)
                curveToRelative(18.11f, -16.53f, 42.22f, -28.25f, 69.18f, -33.87f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 11.37f, -9.15f)
                arcToRelative(156.24f, 156.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 42.05f, -56f)
                curveTo(187.76f, 91.69f, 220.5f, 80f, 256f, 80f)
                arcToRelative(153.57f, 153.57f, 0f, isMoreThanHalf = false, isPositiveArc = true, 107.14f, 42.9f)
                curveToRelative(24.73f, 23.81f, 41.5f, 55.28f, 49.18f, 92f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12.12f, 12.39f)
                curveTo(470f, 237.42f, 512f, 270.43f, 512f, 328f)
                curveToRelative(0f, 33.39f, -12.24f, 60.78f, -35.41f, 79.23f)
                curveTo(456.23f, 423.43f, 428.37f, 432f, 396f, 432f)
                close()
            }
        }.build()

        return _CiCloud!!
    }

@Suppress("ObjectPropertyName")
private var _CiCloud: ImageVector? = null
