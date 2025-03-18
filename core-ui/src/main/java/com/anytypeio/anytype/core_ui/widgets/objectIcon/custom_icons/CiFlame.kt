package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFlame: ImageVector
    get() {
        if (_CiFlame != null) {
            return _CiFlame!!
        }
        _CiFlame = ImageVector.Builder(
            name = "CiFlame",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(394.23f, 197.56f)
                arcToRelative(300.43f, 300.43f, 0f, isMoreThanHalf = false, isPositiveArc = false, -53.37f, -90f)
                curveTo(301.2f, 61.65f, 249.05f, 32f, 208f, 32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -15.48f, 20f)
                curveToRelative(13.87f, 53f, -14.88f, 97.07f, -45.31f, 143.72f)
                curveTo(122f, 234.36f, 96f, 274.27f, 96f, 320f)
                curveToRelative(0f, 88.22f, 71.78f, 160f, 160f, 160f)
                reflectiveCurveToRelative(160f, -71.78f, 160f, -160f)
                curveTo(416f, 276.7f, 408.68f, 235.51f, 394.23f, 197.56f)
                close()
                moveTo(288.33f, 418.69f)
                curveTo(278f, 429.69f, 265.05f, 432f, 256f, 432f)
                reflectiveCurveToRelative(-22f, -2.31f, -32.33f, -13.31f)
                reflectiveCurveTo(208f, 390.24f, 208f, 368f)
                curveToRelative(0f, -25.14f, 8.82f, -44.28f, 17.34f, -62.78f)
                curveToRelative(4.95f, -10.74f, 10f, -21.67f, 13f, -33.37f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12.49f, -4.51f)
                arcTo(126.48f, 126.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 275f, 292f)
                curveToRelative(18.17f, 24f, 29f, 52.42f, 29f, 76f)
                curveTo(304f, 390.24f, 298.58f, 407.77f, 288.33f, 418.69f)
                close()
            }
        }.build()

        return _CiFlame!!
    }

@Suppress("ObjectPropertyName")
private var _CiFlame: ImageVector? = null
