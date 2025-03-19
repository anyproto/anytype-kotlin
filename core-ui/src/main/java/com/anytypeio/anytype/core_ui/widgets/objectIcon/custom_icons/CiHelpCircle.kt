package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiHelpCircle: ImageVector
    get() {
        if (_CiHelpCircle != null) {
            return _CiHelpCircle!!
        }
        _CiHelpCircle = ImageVector.Builder(
            name = "CiHelpCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 64f)
                curveTo(150f, 64f, 64f, 150f, 64f, 256f)
                reflectiveCurveToRelative(86f, 192f, 192f, 192f)
                reflectiveCurveToRelative(192f, -86f, 192f, -192f)
                reflectiveCurveTo(362f, 64f, 256f, 64f)
                close()
                moveTo(250f, 368f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20f, -20f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 250f, 368f)
                close()
                moveTo(283.44f, 266f)
                curveTo(267.23f, 276.88f, 265f, 286.85f, 265f, 296f)
                arcToRelative(14f, 14f, 0f, isMoreThanHalf = false, isPositiveArc = true, -28f, 0f)
                curveToRelative(0f, -21.91f, 10.08f, -39.33f, 30.82f, -53.26f)
                curveTo(287.1f, 229.8f, 298f, 221.6f, 298f, 203.57f)
                curveToRelative(0f, -12.26f, -7f, -21.57f, -21.49f, -28.46f)
                curveToRelative(-3.41f, -1.62f, -11f, -3.2f, -20.34f, -3.09f)
                curveToRelative(-11.72f, 0.15f, -20.82f, 2.95f, -27.83f, 8.59f)
                curveTo(215.12f, 191.25f, 214f, 202.83f, 214f, 203f)
                arcToRelative(14f, 14f, 0f, isMoreThanHalf = true, isPositiveArc = true, -28f, -1.35f)
                curveToRelative(0.11f, -2.43f, 1.8f, -24.32f, 24.77f, -42.8f)
                curveToRelative(11.91f, -9.58f, 27.06f, -14.56f, 45f, -14.78f)
                curveToRelative(12.7f, -0.15f, 24.63f, 2f, 32.72f, 5.82f)
                curveTo(312.7f, 161.34f, 326f, 180.43f, 326f, 203.57f)
                curveTo(326f, 237.4f, 303.39f, 252.59f, 283.44f, 266f)
                close()
            }
        }.build()

        return _CiHelpCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiHelpCircle: ImageVector? = null
