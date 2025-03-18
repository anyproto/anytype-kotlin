package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFileTrayStacked: ImageVector
    get() {
        if (_CiFileTrayStacked != null) {
            return _CiFileTrayStacked!!
        }
        _CiFileTrayStacked = ImageVector.Builder(
            name = "CiFileTrayStacked",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(464f, 352f)
                horizontalLineTo(320f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 16f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -96f, 0f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, -16f)
                horizontalLineTo(48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 16f)
                verticalLineToRelative(64f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                horizontalLineTo(416f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                verticalLineTo(368f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 464f, 352f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(479.46f, 187.88f)
                lineTo(447.61f, 68.45f)
                curveTo(441.27f, 35.59f, 417.54f, 16f, 384f, 16f)
                horizontalLineTo(128f)
                curveToRelative(-16.8f, 0f, -31f, 4.69f, -42.1f, 13.94f)
                reflectiveCurveTo(67.66f, 52f, 64.4f, 68.4f)
                lineTo(32.54f, 187.88f)
                arcTo(15.9f, 15.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 192f)
                verticalLineToRelative(48f)
                curveToRelative(0f, 35.29f, 28.71f, 80f, 64f, 80f)
                horizontalLineTo(416f)
                curveToRelative(35.29f, 0f, 64f, -44.71f, 64f, -80f)
                verticalLineTo(192f)
                arcTo(15.9f, 15.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 479.46f, 187.88f)
                close()
                moveTo(440.57f, 176f)
                horizontalLineTo(320f)
                arcToRelative(15.92f, 15.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 15.82f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = true, isPositiveArc = true, -96f, 0f)
                arcTo(15.92f, 15.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, 192f, 176f)
                horizontalLineTo(71.43f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.93f, -2.52f)
                lineTo(95.71f, 75f)
                curveTo(99.26f, 56.59f, 109.52f, 48f, 128f, 48f)
                horizontalLineTo(384f)
                curveToRelative(18.59f, 0f, 28.84f, 8.53f, 32.25f, 26.85f)
                lineToRelative(26.25f, 98.63f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 440.57f, 176f)
                close()
            }
        }.build()

        return _CiFileTrayStacked!!
    }

@Suppress("ObjectPropertyName")
private var _CiFileTrayStacked: ImageVector? = null
