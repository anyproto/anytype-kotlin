package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPerson: ImageVector
    get() {
        if (_CiPerson != null) {
            return _CiPerson!!
        }
        _CiPerson = ImageVector.Builder(
            name = "CiPerson",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(332.64f, 64.58f)
                curveTo(313.18f, 43.57f, 286f, 32f, 256f, 32f)
                curveToRelative(-30.16f, 0f, -57.43f, 11.5f, -76.8f, 32.38f)
                curveToRelative(-19.58f, 21.11f, -29.12f, 49.8f, -26.88f, 80.78f)
                curveTo(156.76f, 206.28f, 203.27f, 256f, 256f, 256f)
                reflectiveCurveToRelative(99.16f, -49.71f, 103.67f, -110.82f)
                curveTo(361.94f, 114.48f, 352.34f, 85.85f, 332.64f, 64.58f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(432f, 480f)
                horizontalLineTo(80f)
                arcTo(31f, 31f, 0f, isMoreThanHalf = false, isPositiveArc = true, 55.8f, 468.87f)
                curveToRelative(-6.5f, -7.77f, -9.12f, -18.38f, -7.18f, -29.11f)
                curveTo(57.06f, 392.94f, 83.4f, 353.61f, 124.8f, 326f)
                curveToRelative(36.78f, -24.51f, 83.37f, -38f, 131.2f, -38f)
                reflectiveCurveToRelative(94.42f, 13.5f, 131.2f, 38f)
                curveToRelative(41.4f, 27.6f, 67.74f, 66.93f, 76.18f, 113.75f)
                curveToRelative(1.94f, 10.73f, -0.68f, 21.34f, -7.18f, 29.11f)
                arcTo(31f, 31f, 0f, isMoreThanHalf = false, isPositiveArc = true, 432f, 480f)
                close()
            }
        }.build()

        return _CiPerson!!
    }

@Suppress("ObjectPropertyName")
private var _CiPerson: ImageVector? = null
