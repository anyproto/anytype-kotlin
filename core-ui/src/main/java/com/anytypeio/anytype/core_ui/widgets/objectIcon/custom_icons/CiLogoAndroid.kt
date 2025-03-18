package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiLogoAndroid: ImageVector
    get() {
        if (_CiLogoAndroid != null) {
            return _CiLogoAndroid!!
        }
        _CiLogoAndroid = ImageVector.Builder(
            name = "CiLogoAndroid",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(380.91f, 199f)
                lineToRelative(42.47f, -73.57f)
                arcToRelative(8.63f, 8.63f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.12f, -11.76f)
                arcToRelative(8.52f, 8.52f, 0f, isMoreThanHalf = false, isPositiveArc = false, -11.71f, 3.12f)
                lineToRelative(-43f, 74.52f)
                curveToRelative(-32.83f, -15f, -69.78f, -23.35f, -109.52f, -23.35f)
                reflectiveCurveToRelative(-76.69f, 8.36f, -109.52f, 23.35f)
                lineToRelative(-43f, -74.52f)
                arcToRelative(8.6f, 8.6f, 0f, isMoreThanHalf = true, isPositiveArc = false, -14.88f, 8.64f)
                lineTo(131f, 199f)
                curveTo(57.8f, 238.64f, 8.19f, 312.77f, 0f, 399.55f)
                lineTo(512f, 399.55f)
                curveTo(503.81f, 312.77f, 454.2f, 238.64f, 380.91f, 199f)
                close()
                moveTo(138.45f, 327.65f)
                arcToRelative(21.46f, 21.46f, 0f, isMoreThanHalf = true, isPositiveArc = true, 21.46f, -21.46f)
                arcTo(21.47f, 21.47f, 0f, isMoreThanHalf = false, isPositiveArc = true, 138.45f, 327.65f)
                close()
                moveTo(373.45f, 327.65f)
                arcTo(21.46f, 21.46f, 0f, isMoreThanHalf = true, isPositiveArc = true, 395f, 306.19f)
                arcTo(21.47f, 21.47f, 0f, isMoreThanHalf = false, isPositiveArc = true, 373.49f, 327.65f)
                close()
            }
        }.build()

        return _CiLogoAndroid!!
    }

@Suppress("ObjectPropertyName")
private var _CiLogoAndroid: ImageVector? = null
