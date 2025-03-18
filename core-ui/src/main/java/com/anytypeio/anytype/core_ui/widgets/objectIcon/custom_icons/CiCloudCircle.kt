package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCloudCircle: ImageVector
    get() {
        if (_CiCloudCircle != null) {
            return _CiCloudCircle!!
        }
        _CiCloudCircle = ImageVector.Builder(
            name = "CiCloudCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 48f)
                curveTo(141.13f, 48f, 48f, 141.13f, 48f, 256f)
                reflectiveCurveToRelative(93.13f, 208f, 208f, 208f)
                reflectiveCurveToRelative(208f, -93.13f, 208f, -208f)
                reflectiveCurveTo(370.87f, 48f, 256f, 48f)
                close()
                moveTo(326f, 328f)
                lineTo(193.05f, 328f)
                curveToRelative(-31.53f, 0f, -57.56f, -25.58f, -57f, -57.11f)
                curveToRelative(0.53f, -31.74f, 23.68f, -49.95f, 51.35f, -54.3f)
                arcToRelative(7.92f, 7.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.16f, -5f)
                curveTo(202.07f, 189.22f, 223.63f, 168f, 256f, 168f)
                curveToRelative(33.17f, 0f, 61.85f, 22.49f, 70.14f, 60.21f)
                arcToRelative(17.75f, 17.75f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.18f, 13.43f)
                curveTo(357.79f, 246.05f, 376f, 259.21f, 376f, 284f)
                curveTo(376f, 314.28f, 353.5f, 328f, 326f, 328f)
                close()
            }
        }.build()

        return _CiCloudCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiCloudCircle: ImageVector? = null
