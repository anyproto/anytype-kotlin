package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiAlarm: ImageVector
    get() {
        if (_CiAlarm != null) {
            return _CiAlarm!!
        }
        _CiAlarm = ImageVector.Builder(
            name = "CiAlarm",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(153.59f, 110.46f)
                arcTo(21.41f, 21.41f, 0f, isMoreThanHalf = false, isPositiveArc = false, 152.48f, 79f)
                horizontalLineToRelative(0f)
                arcTo(62.67f, 62.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, 112f, 64f)
                lineToRelative(-3.27f, 0.09f)
                lineToRelative(-0.48f, 0f)
                curveTo(74.4f, 66.15f, 48f, 95.55f, 48.07f, 131f)
                curveToRelative(0f, 19f, 8f, 29.06f, 14.32f, 37.11f)
                arcToRelative(20.61f, 20.61f, 0f, isMoreThanHalf = false, isPositiveArc = false, 14.7f, 7.8f)
                curveToRelative(0.26f, 0f, 0.7f, 0.05f, 2f, 0.05f)
                arcToRelative(19.06f, 19.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.75f, -5.89f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(403.79f, 64.11f)
                lineToRelative(-3.27f, -0.1f)
                horizontalLineTo(400f)
                arcToRelative(62.67f, 62.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, -40.52f, 15f)
                arcToRelative(21.41f, 21.41f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.11f, 31.44f)
                lineToRelative(60.77f, 59.65f)
                arcTo(19.06f, 19.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 432.93f, 176f)
                curveToRelative(1.28f, 0f, 1.72f, 0f, 2f, -0.05f)
                arcToRelative(20.61f, 20.61f, 0f, isMoreThanHalf = false, isPositiveArc = false, 14.69f, -7.8f)
                curveToRelative(6.36f, -8.05f, 14.28f, -18.08f, 14.32f, -37.11f)
                curveTo(464f, 95.55f, 437.6f, 66.15f, 403.79f, 64.11f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256.07f, 96f)
                curveToRelative(-97f, 0f, -176f, 78.95f, -176f, 176f)
                arcToRelative(175.23f, 175.23f, 0f, isMoreThanHalf = false, isPositiveArc = false, 40.81f, 112.56f)
                lineTo(84.76f, 420.69f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = false, 22.63f, 22.62f)
                lineToRelative(36.12f, -36.12f)
                arcToRelative(175.63f, 175.63f, 0f, isMoreThanHalf = false, isPositiveArc = false, 225.12f, 0f)
                lineToRelative(36.13f, 36.12f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = false, 22.63f, -22.62f)
                lineToRelative(-36.13f, -36.13f)
                arcTo(175.17f, 175.17f, 0f, isMoreThanHalf = false, isPositiveArc = false, 432.07f, 272f)
                curveTo(432.07f, 175f, 353.12f, 96f, 256.07f, 96f)
                close()
                moveTo(272.07f, 272f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, 16f)
                horizontalLineToRelative(-80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineToRelative(64f)
                lineTo(240.07f, 160f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 0f)
                close()
            }
        }.build()

        return _CiAlarm!!
    }

@Suppress("ObjectPropertyName")
private var _CiAlarm: ImageVector? = null
