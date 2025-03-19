package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBeaker: ImageVector
    get() {
        if (_CiBeaker != null) {
            return _CiBeaker!!
        }
        _CiBeaker = ImageVector.Builder(
            name = "CiBeaker",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(444f, 32f)
                horizontalLineTo(128f)
                curveToRelative(-19.38f, 0f, -45.9f, 4.34f, -64.11f, 24.77f)
                curveTo(52.17f, 69.92f, 48f, 85.66f, 48f, 96f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.8f, 15.85f)
                curveTo(91.7f, 116f, 96f, 117.79f, 96f, 136f)
                verticalLineTo(400f)
                arcTo(80.07f, 80.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 176f, 480f)
                horizontalLineTo(368f)
                arcToRelative(80.11f, 80.11f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, -80f)
                verticalLineTo(96f)
                curveToRelative(0f, -12.55f, 7.46f, -27.25f, 10f, -31.36f)
                lineToRelative(0.1f, -0.14f)
                curveToRelative(0.22f, -0.35f, 0.5f, -0.72f, 0.78f, -1.1f)
                curveToRelative(2f, -2.79f, 5.09f, -7f, 5.09f, -12.95f)
                curveTo(464f, 39.79f, 454.89f, 32f, 444f, 32f)
                close()
                moveTo(84.11f, 83.08f)
                curveToRelative(5.24f, -8.87f, 17.17f, -19f, 44.29f, -19f)
                horizontalLineTo(422.83f)
                curveTo(419.3f, 72.87f, 416f, 84.27f, 416f, 96f)
                verticalLineToRelative(64f)
                horizontalLineTo(128f)
                verticalLineTo(136f)
                curveTo(128f, 98.68f, 106.65f, 87.86f, 84.11f, 83.08f)
                close()
            }
        }.build()

        return _CiBeaker!!
    }

@Suppress("ObjectPropertyName")
private var _CiBeaker: ImageVector? = null
