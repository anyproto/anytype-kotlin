package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiRainy: ImageVector
    get() {
        if (_CiRainy != null) {
            return _CiRainy!!
        }
        _CiRainy = ImageVector.Builder(
            name = "CiRainy",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(456.26f, 139.37f)
                curveToRelative(-16.77f, -16.73f, -39.17f, -28.41f, -65.17f, -34f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.19f, -9f)
                arcToRelative(142.24f, 142.24f, 0f, isMoreThanHalf = false, isPositiveArc = false, -42.19f, -53.21f)
                curveTo(314.48f, 25.39f, 286.23f, 16f, 256f, 16f)
                arcToRelative(140.24f, 140.24f, 0f, isMoreThanHalf = false, isPositiveArc = false, -93.5f, 35.32f)
                curveToRelative(-24.2f, 21.56f, -40.91f, 51.34f, -48.43f, 85.83f)
                arcToRelative(16.05f, 16.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.72f, 12.18f)
                curveToRelative(-25f, 6.3f, -35.71f, 12.54f, -49.21f, 24.56f)
                curveTo(34f, 190.93f, 24f, 214.14f, 24f, 240.8f)
                curveToRelative(0f, 30.55f, 11.23f, 55.64f, 32.47f, 72.56f)
                curveTo(75.08f, 328.17f, 100.5f, 336f, 130f, 336f)
                horizontalLineTo(364f)
                curveToRelative(33.2f, 0f, 64.11f, -11.46f, 87f, -32.28f)
                curveToRelative(23.84f, -21.65f, 37f, -51.67f, 37f, -84.52f)
                curveTo(488f, 187.71f, 477f, 160.11f, 456.26f, 139.37f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(112f, 448f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.3f, -24.88f)
                lineToRelative(32f, -48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26.62f, 17.76f)
                lineToRelative(-32f, 48f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 112f, 448f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(160f, 496f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.29f, -24.88f)
                lineToRelative(64f, -96f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26.62f, 17.76f)
                lineToRelative(-64f, 96f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 160f, 496f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(272f, 448f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.3f, -24.88f)
                lineToRelative(32f, -48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26.62f, 17.76f)
                lineToRelative(-32f, 48f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 272f, 448f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(320f, 496f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.3f, -24.88f)
                lineToRelative(64f, -96f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26.62f, 17.76f)
                lineToRelative(-64f, 96f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 320f, 496f)
                close()
            }
        }.build()

        return _CiRainy!!
    }

@Suppress("ObjectPropertyName")
private var _CiRainy: ImageVector? = null
