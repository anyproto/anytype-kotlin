package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFileTrayFull: ImageVector
    get() {
        if (_CiFileTrayFull != null) {
            return _CiFileTrayFull!!
        }
        _CiFileTrayFull = ImageVector.Builder(
            name = "CiFileTrayFull",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(479.66f, 268.7f)
                lineToRelative(-32f, -151.81f)
                curveTo(441.48f, 83.77f, 417.68f, 64f, 384f, 64f)
                lineTo(128f, 64f)
                curveToRelative(-16.8f, 0f, -31f, 4.69f, -42.1f, 13.94f)
                reflectiveCurveToRelative(-18.37f, 22.31f, -21.58f, 38.89f)
                lineToRelative(-32f, 151.87f)
                arcTo(16.65f, 16.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 272f)
                lineTo(32f, 384f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                lineTo(416f, 448f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                lineTo(480f, 272f)
                arcTo(16.65f, 16.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 479.66f, 268.7f)
                close()
                moveTo(95.66f, 123.3f)
                curveToRelative(0f, -0.1f, 0f, -0.19f, 0f, -0.28f)
                curveToRelative(3.55f, -18.43f, 13.81f, -27f, 32.29f, -27f)
                lineTo(384f, 96.02f)
                curveToRelative(18.61f, 0f, 28.87f, 8.55f, 32.27f, 26.91f)
                curveToRelative(0f, 0.13f, 0.05f, 0.26f, 0.07f, 0.39f)
                lineToRelative(26.93f, 127.88f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.92f, 4.82f)
                lineTo(320f, 256.02f)
                arcToRelative(15.92f, 15.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 15.82f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = true, isPositiveArc = true, -96f, 0f)
                arcTo(15.92f, 15.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, 192f, 256f)
                lineTo(72.65f, 256f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.92f, -4.82f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(368f, 160f)
                horizontalLineTo(144f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineTo(368f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(384f, 224f)
                horizontalLineTo(128f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineTo(384f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
            }
        }.build()

        return _CiFileTrayFull!!
    }

@Suppress("ObjectPropertyName")
private var _CiFileTrayFull: ImageVector? = null
