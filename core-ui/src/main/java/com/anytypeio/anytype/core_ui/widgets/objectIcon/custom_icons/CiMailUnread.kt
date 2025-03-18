package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMailUnread: ImageVector
    get() {
        if (_CiMailUnread != null) {
            return _CiMailUnread!!
        }
        _CiMailUnread = ImageVector.Builder(
            name = "CiMailUnread",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(496f, 128.05f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 389.62f, 80f)
                horizontalLineToRelative(0f)
                arcToRelative(64.52f, 64.52f, 0f, isMoreThanHalf = false, isPositiveArc = false, -12.71f, 15.3f)
                lineToRelative(0f, 0.06f)
                curveToRelative(-0.54f, 0.9f, -1.05f, 1.82f, -1.55f, 2.74f)
                lineToRelative(-0.24f, 0.49f)
                curveToRelative(-0.42f, 0.79f, -0.81f, 1.59f, -1.19f, 2.4f)
                curveToRelative(-0.12f, 0.25f, -0.23f, 0.5f, -0.34f, 0.75f)
                curveToRelative(-0.33f, 0.73f, -0.65f, 1.47f, -0.95f, 2.22f)
                curveToRelative(-0.13f, 0.31f, -0.25f, 0.62f, -0.37f, 0.93f)
                curveToRelative(-0.27f, 0.7f, -0.53f, 1.4f, -0.78f, 2.11f)
                lineToRelative(-0.36f, 1.06f)
                curveToRelative(-0.22f, 0.68f, -0.43f, 1.37f, -0.63f, 2.06f)
                curveToRelative(-0.12f, 0.39f, -0.23f, 0.77f, -0.33f, 1.16f)
                curveToRelative(-0.19f, 0.67f, -0.35f, 1.35f, -0.51f, 2f)
                curveToRelative(-0.1f, 0.41f, -0.2f, 0.82f, -0.29f, 1.23f)
                curveToRelative(-0.14f, 0.68f, -0.27f, 1.37f, -0.39f, 2f)
                curveToRelative(-0.08f, 0.42f, -0.16f, 0.84f, -0.23f, 1.26f)
                curveToRelative(-0.11f, 0.7f, -0.2f, 1.41f, -0.29f, 2.12f)
                curveToRelative(-0.05f, 0.41f, -0.11f, 0.82f, -0.16f, 1.24f)
                curveToRelative(-0.08f, 0.77f, -0.13f, 1.54f, -0.19f, 2.32f)
                curveToRelative(0f, 0.36f, -0.06f, 0.72f, -0.08f, 1.08f)
                curveToRelative(-0.06f, 1.14f, -0.1f, 2.28f, -0.1f, 3.44f)
                horizontalLineToRelative(0f)
                curveToRelative(0f, 1f, 0f, 2f, 0.08f, 2.94f)
                lineToRelative(0f, 0.64f)
                quadToRelative(0.08f, 1.41f, 0.21f, 2.82f)
                lineToRelative(0.06f, 0.48f)
                curveToRelative(0.09f, 0.85f, 0.19f, 1.69f, 0.32f, 2.52f)
                curveToRelative(0f, 0.17f, 0f, 0.35f, 0.07f, 0.52f)
                curveToRelative(0.14f, 0.91f, 0.31f, 1.81f, 0.49f, 2.71f)
                curveToRelative(0f, 0.22f, 0.09f, 0.43f, 0.13f, 0.65f)
                curveToRelative(0.18f, 0.86f, 0.38f, 1.72f, 0.6f, 2.57f)
                curveToRelative(0f, 0.07f, 0f, 0.13f, 0f, 0.19f)
                curveToRelative(0.23f, 0.89f, 0.48f, 1.76f, 0.75f, 2.63f)
                lineToRelative(0.21f, 0.68f)
                curveToRelative(0.27f, 0.85f, 0.55f, 1.68f, 0.85f, 2.51f)
                curveToRelative(0.06f, 0.18f, 0.13f, 0.36f, 0.2f, 0.54f)
                curveToRelative(0.27f, 0.71f, 0.55f, 1.42f, 0.84f, 2.12f)
                curveToRelative(0.08f, 0.21f, 0.16f, 0.41f, 0.25f, 0.61f)
                curveToRelative(0.34f, 0.79f, 0.69f, 1.58f, 1.06f, 2.36f)
                lineToRelative(0.33f, 0.67f)
                curveToRelative(0.35f, 0.7f, 0.7f, 1.4f, 1.07f, 2.09f)
                arcToRelative(64.34f, 64.34f, 0f, isMoreThanHalf = false, isPositiveArc = false, 22.14f, 23.81f)
                horizontalLineToRelative(0f)
                arcToRelative(62.22f, 62.22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.62f, 4.15f)
                lineToRelative(0.39f, 0.18f)
                quadToRelative(2.66f, 1.2f, 5.43f, 2.16f)
                lineToRelative(0.95f, 0.32f)
                lineToRelative(1.5f, 0.47f)
                curveToRelative(0.45f, 0.14f, 0.9f, 0.26f, 1.36f, 0.39f)
                lineToRelative(1.92f, 0.5f)
                lineToRelative(1.73f, 0.4f)
                lineToRelative(1.15f, 0.23f)
                lineToRelative(1.83f, 0.33f)
                lineToRelative(0.94f, 0.15f)
                curveToRelative(0.9f, 0.13f, 1.81f, 0.25f, 2.72f, 0.35f)
                lineToRelative(0.77f, 0.07f)
                curveToRelative(0.73f, 0.06f, 1.47f, 0.12f, 2.21f, 0.16f)
                lineToRelative(0.86f, 0.05f)
                curveToRelative(1f, 0f, 1.94f, 0.08f, 2.92f, 0.08f)
                horizontalLineToRelative(0f)
                curveToRelative(1.16f, 0f, 2.3f, 0f, 3.44f, -0.1f)
                lineToRelative(1.08f, -0.08f)
                curveToRelative(0.78f, -0.06f, 1.55f, -0.11f, 2.32f, -0.19f)
                lineToRelative(1.25f, -0.16f)
                curveToRelative(0.7f, -0.09f, 1.41f, -0.18f, 2.11f, -0.29f)
                lineToRelative(1.26f, -0.23f)
                curveToRelative(0.68f, -0.12f, 1.37f, -0.25f, 2f, -0.39f)
                lineToRelative(1.23f, -0.29f)
                curveToRelative(0.68f, -0.16f, 1.36f, -0.32f, 2f, -0.51f)
                curveToRelative(0.39f, -0.1f, 0.77f, -0.21f, 1.16f, -0.33f)
                curveToRelative(0.69f, -0.2f, 1.38f, -0.41f, 2.06f, -0.63f)
                lineToRelative(1.06f, -0.36f)
                curveToRelative(0.71f, -0.25f, 1.41f, -0.51f, 2.11f, -0.78f)
                lineToRelative(0.93f, -0.37f)
                curveToRelative(0.75f, -0.3f, 1.49f, -0.62f, 2.22f, -0.95f)
                lineToRelative(0.75f, -0.34f)
                curveToRelative(0.81f, -0.38f, 1.61f, -0.77f, 2.4f, -1.19f)
                lineToRelative(0.49f, -0.24f)
                curveToRelative(0.92f, -0.5f, 1.84f, -1f, 2.74f, -1.55f)
                lineToRelative(0.06f, 0f)
                arcTo(64.52f, 64.52f, 0f, isMoreThanHalf = false, isPositiveArc = false, 480f, 170.38f)
                horizontalLineToRelative(0f)
                arcTo(63.81f, 63.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, 496f, 128.05f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(371.38f, 202.53f)
                lineToRelative(-105.56f, 82.1f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -19.64f, 0f)
                lineToRelative(-144f, -112f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 19.64f, -25.26f)
                lineTo(256f, 251.73f)
                lineToRelative(94.22f, -73.28f)
                arcTo(95.86f, 95.86f, 0f, isMoreThanHalf = false, isPositiveArc = true, 348.81f, 80f)
                horizontalLineTo(88f)
                arcToRelative(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, -56f, 56f)
                verticalLineTo(376f)
                arcToRelative(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, 56f)
                horizontalLineTo(424f)
                arcToRelative(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, -56f)
                verticalLineTo(211.19f)
                arcToRelative(95.85f, 95.85f, 0f, isMoreThanHalf = false, isPositiveArc = true, -108.62f, -8.66f)
                close()
            }
        }.build()

        return _CiMailUnread!!
    }

@Suppress("ObjectPropertyName")
private var _CiMailUnread: ImageVector? = null
