package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPulse: ImageVector
    get() {
        if (_CiPulse != null) {
            return _CiPulse!!
        }
        _CiPulse = ImageVector.Builder(
            name = "CiPulse",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(432f, 272f)
                arcToRelative(48.09f, 48.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, -45.25f, 32f)
                horizontalLineTo(347.53f)
                lineToRelative(-28.35f, -85.06f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -30.56f, 0.66f)
                lineTo(244.11f, 375.36f)
                lineToRelative(-52.33f, -314f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -31.3f, -1.25f)
                lineTo(99.51f, 304f)
                horizontalLineTo(48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineToRelative(64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.52f, -12.12f)
                lineToRelative(45.34f, -181.37f)
                lineToRelative(51.36f, 308.12f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 239.1f, 464f)
                curveToRelative(0.3f, 0f, 0.6f, 0f, 0.91f, 0f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.37f, -11.6f)
                lineToRelative(49.8f, -174.28f)
                lineToRelative(15.64f, 46.94f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 336f, 336f)
                horizontalLineToRelative(50.75f)
                arcTo(48f, 48f, 0f, isMoreThanHalf = true, isPositiveArc = false, 432f, 272f)
                close()
            }
        }.build()

        return _CiPulse!!
    }

@Suppress("ObjectPropertyName")
private var _CiPulse: ImageVector? = null
