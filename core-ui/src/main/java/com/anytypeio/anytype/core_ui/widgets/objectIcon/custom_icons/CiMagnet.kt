package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMagnet: ImageVector
    get() {
        if (_CiMagnet != null) {
            return _CiMagnet!!
        }
        _CiMagnet = ImageVector.Builder(
            name = "CiMagnet",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(191.98f, 463.79f)
                lineTo(191.98f, 415.79f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(90.16f, 421.61f)
                lineTo(124.1f, 387.67f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(47.98f, 319.79f)
                lineTo(95.98f, 319.79f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(267.56f, 312.32f)
                lineToRelative(-31.11f, 31.11f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 22.63f)
                lineToRelative(45.26f, 45.25f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 22.62f, 0f)
                lineToRelative(31.12f, -31.11f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -5.66f)
                lineToRelative(-62.23f, -62.22f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 267.56f, 312.32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(131.8f, 176.55f)
                lineToRelative(-31.11f, 31.12f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 22.62f)
                lineToRelative(45.25f, 45.26f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 22.63f, 0f)
                lineToRelative(31.11f, -31.11f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -5.66f)
                lineToRelative(-62.22f, -62.23f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 131.8f, 176.55f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(428.85f, 83.28f)
                arcToRelative(144f, 144f, 0f, isMoreThanHalf = false, isPositiveArc = false, -203.71f, -0.06f)
                lineToRelative(-65.06f, 65.05f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 5.66f)
                lineToRelative(62.23f, 62.22f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.66f, 0f)
                lineToRelative(65f, -65.05f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 68.46f, 0.59f)
                curveToRelative(18.3f, 18.92f, 17.47f, 49.24f, -1.14f, 67.85f)
                lineTo(295.85f, 284f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 5.66f)
                lineToRelative(62.22f, 62.23f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.66f, 0f)
                lineToRelative(64.08f, -64.08f)
                curveTo(484.18f, 231.47f, 485.18f, 139.68f, 428.85f, 83.28f)
                close()
            }
        }.build()

        return _CiMagnet!!
    }

@Suppress("ObjectPropertyName")
private var _CiMagnet: ImageVector? = null
