package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiReceipt: ImageVector
    get() {
        if (_CiReceipt != null) {
            return _CiReceipt!!
        }
        _CiReceipt = ImageVector.Builder(
            name = "CiReceipt",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(483.82f, 32.45f)
                arcToRelative(16.28f, 16.28f, 0f, isMoreThanHalf = false, isPositiveArc = false, -11.23f, 1.37f)
                lineTo(448f, 46.1f)
                horizontalLineToRelative(0f)
                lineToRelative(-24.8f, -12.4f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -14.31f, 0f)
                lineTo(383.78f, 46.11f)
                horizontalLineToRelative(0f)
                lineTo(359f, 33.7f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -14.36f, 0f)
                lineTo(320f, 46.07f)
                lineTo(320f, 46.07f)
                lineTo(295.55f, 33.73f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -14.35f, -0.06f)
                lineTo(256f, 46.12f)
                horizontalLineToRelative(0f)
                lineToRelative(-24.8f, -12.43f)
                arcToRelative(16.05f, 16.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, -14.33f, 0f)
                lineTo(192f, 46.1f)
                horizontalLineToRelative(0f)
                lineTo(167.16f, 33.69f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -19.36f, 3.94f)
                arcTo(16.25f, 16.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 144f, 48.28f)
                lineTo(144f, 288f)
                arcToRelative(0f, 0f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.05f, 0.05f)
                lineTo(336f, 288.05f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 32f)
                lineTo(368f, 424f)
                curveToRelative(0f, 30.93f, 33.07f, 56f, 64f, 56f)
                horizontalLineToRelative(12f)
                arcToRelative(52f, 52f, 0f, isMoreThanHalf = false, isPositiveArc = false, 52f, -52f)
                lineTo(496f, 48f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 483.82f, 32.45f)
                close()
                moveTo(416f, 240f)
                lineTo(288.5f, 240f)
                curveToRelative(-8.64f, 0f, -16.1f, -6.64f, -16.48f, -15.28f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 288f, 208f)
                lineTo(415.5f, 208f)
                curveToRelative(8.64f, 0f, 16.1f, 6.64f, 16.48f, 15.28f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 416f, 240f)
                close()
                moveTo(416f, 160f)
                lineTo(224.5f, 160f)
                curveToRelative(-8.64f, 0f, -16.1f, -6.64f, -16.48f, -15.28f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 224f, 128f)
                lineTo(415.5f, 128f)
                curveToRelative(8.64f, 0f, 16.1f, 6.64f, 16.48f, 15.28f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 416f, 160f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(336f, 424f)
                verticalLineTo(336f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, -16f)
                horizontalLineTo(48f)
                arcToRelative(32.1f, 32.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, 32.05f)
                curveToRelative(0f, 50.55f, 5.78f, 71.57f, 14.46f, 87.57f)
                curveTo(45.19f, 466.79f, 71.86f, 480f, 112f, 480f)
                horizontalLineTo(357.68f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.85f, -6.81f)
                curveTo(351.07f, 463.7f, 336f, 451f, 336f, 424f)
                close()
            }
        }.build()

        return _CiReceipt!!
    }

@Suppress("ObjectPropertyName")
private var _CiReceipt: ImageVector? = null
