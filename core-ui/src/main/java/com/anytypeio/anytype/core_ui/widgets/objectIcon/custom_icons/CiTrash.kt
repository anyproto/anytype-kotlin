package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiTrash: ImageVector
    get() {
        if (_CiTrash != null) {
            return _CiTrash!!
        }
        _CiTrash = ImageVector.Builder(
            name = "CiTrash",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(432f, 96f)
                lineTo(336f, 96f)
                lineTo(336f, 72f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = false, -40f, -40f)
                lineTo(216f, 32f)
                arcToRelative(40f, 40f, 0f, isMoreThanHalf = false, isPositiveArc = false, -40f, 40f)
                lineTo(176f, 96f)
                lineTo(80f, 96f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                lineTo(97f, 128f)
                lineTo(116f, 432.92f)
                curveToRelative(1.42f, 26.85f, 22f, 47.08f, 48f, 47.08f)
                lineTo(348f, 480f)
                curveToRelative(26.13f, 0f, 46.3f, -19.78f, 48f, -47f)
                lineTo(415f, 128f)
                horizontalLineToRelative(17f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                close()
                moveTo(192.57f, 416f)
                lineTo(192f, 416f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, -15.43f)
                lineToRelative(-8f, -224f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -1.14f)
                lineToRelative(8f, 224f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 192.57f, 416f)
                close()
                moveTo(272f, 400f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 0f)
                lineTo(240f, 176f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 0f)
                close()
                moveTo(304f, 96f)
                lineTo(208f, 96f)
                lineTo(208f, 72f)
                arcToRelative(7.91f, 7.91f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, -8f)
                horizontalLineToRelative(80f)
                arcToRelative(7.91f, 7.91f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 8f)
                close()
                moveTo(336f, 400.57f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 320f, 416f)
                horizontalLineToRelative(-0.58f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 304f, 399.43f)
                lineToRelative(8f, -224f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, 1.14f)
                close()
            }
        }.build()

        return _CiTrash!!
    }

@Suppress("ObjectPropertyName")
private var _CiTrash: ImageVector? = null
