package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMicOffCircle: ImageVector
    get() {
        if (_CiMicOffCircle != null) {
            return _CiMicOffCircle!!
        }
        _CiMicOffCircle = ImageVector.Builder(
            name = "CiMicOffCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 48f)
                curveTo(141.31f, 48f, 48f, 141.31f, 48f, 256f)
                reflectiveCurveToRelative(93.31f, 208f, 208f, 208f)
                reflectiveCurveToRelative(208f, -93.31f, 208f, -208f)
                reflectiveCurveTo(370.69f, 48f, 256f, 48f)
                close()
                moveTo(256f, 128f)
                horizontalLineToRelative(0f)
                arcToRelative(48.14f, 48.14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 48f, 48f)
                verticalLineToRelative(64f)
                arcToRelative(47.84f, 47.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.63f, 7.71f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.46f, 1f)
                lineToRelative(-84.42f, -92.86f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.47f, -4.77f)
                arcTo(48.08f, 48.08f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 128f)
                close()
                moveTo(288f, 384f)
                lineTo(224.45f, 384f)
                curveToRelative(-8.61f, 0f, -16f, -6.62f, -16.43f, -15.23f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 224f, 352f)
                horizontalLineToRelative(16f)
                lineTo(240f, 334.26f)
                arcToRelative(103.71f, 103.71f, 0f, isMoreThanHalf = false, isPositiveArc = true, -49.21f, -23.38f)
                curveTo(170.94f, 293.83f, 160f, 271.58f, 160f, 248.22f)
                lineTo(160f, 224f)
                arcToRelative(15.91f, 15.91f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16.39f, -16f)
                arcTo(16.26f, 16.26f, 0f, isMoreThanHalf = false, isPositiveArc = true, 192f, 224.4f)
                verticalLineToRelative(23.82f)
                curveToRelative(0f, 25.66f, 28f, 55.48f, 64f, 55.48f)
                curveToRelative(1.67f, 0f, 3.37f, -0.09f, 5.06f, -0.24f)
                arcToRelative(3.94f, 3.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.29f, 1.29f)
                lineToRelative(21.07f, 23.19f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.89f, 3.26f)
                arcTo(100.33f, 100.33f, 0f, isMoreThanHalf = false, isPositiveArc = true, 272f, 334.26f)
                lineTo(272f, 352f)
                horizontalLineToRelative(15.55f)
                curveToRelative(8.61f, 0f, 16f, 6.62f, 16.43f, 15.23f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 288f, 384f)
                close()
                moveTo(210.11f, 245.09f)
                lineToRelative(36.46f, 40.11f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.95f, 1.66f)
                arcToRelative(48.26f, 48.26f, 0f, isMoreThanHalf = false, isPositiveArc = true, -37.25f, -41f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 210.11f, 245.09f)
                close()
                moveTo(362.76f, 364.84f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.6f, -1.08f)
                lineToRelative(-192f, -210f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.68f, -21.52f)
                lineToRelative(192f, 210f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 362.76f, 364.84f)
                close()
                moveTo(352f, 248.22f)
                arcToRelative(77.12f, 77.12f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.93f, 40.87f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.19f, 0.3f)
                lineToRelative(-19.19f, -21.1f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.76f, -4.16f)
                arcTo(43.35f, 43.35f, 0f, isMoreThanHalf = false, isPositiveArc = false, 320f, 248.22f)
                verticalLineToRelative(-23.8f)
                arcToRelative(16.3f, 16.3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.64f, -16.24f)
                curveToRelative(9.88f, -1.48f, 18.36f, 6.51f, 18.36f, 16.12f)
                close()
            }
        }.build()

        return _CiMicOffCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiMicOffCircle: ImageVector? = null
