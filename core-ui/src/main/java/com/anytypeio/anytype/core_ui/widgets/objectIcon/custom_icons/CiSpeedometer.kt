package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiSpeedometer: ImageVector
    get() {
        if (_CiSpeedometer != null) {
            return _CiSpeedometer!!
        }
        _CiSpeedometer = ImageVector.Builder(
            name = "CiSpeedometer",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(425.7f, 118.25f)
                arcTo(240f, 240f, 0f, isMoreThanHalf = false, isPositiveArc = false, 76.32f, 447f)
                lineToRelative(0.18f, 0.2f)
                curveToRelative(0.33f, 0.35f, 0.64f, 0.71f, 1f, 1.05f)
                curveToRelative(0.74f, 0.84f, 1.58f, 1.79f, 2.57f, 2.78f)
                arcToRelative(41.17f, 41.17f, 0f, isMoreThanHalf = false, isPositiveArc = false, 60.36f, -0.42f)
                arcToRelative(157.13f, 157.13f, 0f, isMoreThanHalf = false, isPositiveArc = true, 231.26f, 0f)
                arcToRelative(41.18f, 41.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 60.65f, 0.06f)
                lineToRelative(3.21f, -3.5f)
                lineToRelative(0.18f, -0.2f)
                arcToRelative(239.93f, 239.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, -10f, -328.76f)
                close()
                moveTo(240f, 128f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 0f)
                verticalLineToRelative(32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 0f)
                close()
                moveTo(128f, 304f)
                lineTo(96f, 304f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineToRelative(32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
                moveTo(176.8f, 208.8f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.62f, 0f)
                lineToRelative(-22.63f, -22.62f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.63f, -22.63f)
                lineToRelative(22.62f, 22.63f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 176.8f, 208.8f)
                close()
                moveTo(326.1f, 231.9f)
                lineTo(278.6f, 307.4f)
                arcToRelative(31f, 31f, 0f, isMoreThanHalf = false, isPositiveArc = true, -7f, 7f)
                arcToRelative(30.11f, 30.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, -35f, -49f)
                lineToRelative(75.5f, -47.5f)
                arcToRelative(10.23f, 10.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.7f, 0f)
                arcTo(10.06f, 10.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, 326.1f, 231.9f)
                close()
                moveTo(357.82f, 208.8f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.62f, -22.62f)
                lineToRelative(22.62f, -22.63f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.63f, 22.63f)
                close()
                moveTo(423.7f, 436.4f)
                horizontalLineToRelative(0f)
                close()
                moveTo(416f, 304f)
                lineTo(384f, 304f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineToRelative(32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
            }
        }.build()

        return _CiSpeedometer!!
    }

@Suppress("ObjectPropertyName")
private var _CiSpeedometer: ImageVector? = null
