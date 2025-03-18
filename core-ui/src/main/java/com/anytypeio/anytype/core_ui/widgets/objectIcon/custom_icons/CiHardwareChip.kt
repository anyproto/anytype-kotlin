package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiHardwareChip: ImageVector
    get() {
        if (_CiHardwareChip != null) {
            return _CiHardwareChip!!
        }
        _CiHardwareChip = ImageVector.Builder(
            name = "CiHardwareChip",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(168f, 160f)
                lineTo(344f, 160f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 352f, 168f)
                lineTo(352f, 344f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 344f, 352f)
                lineTo(168f, 352f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 160f, 344f)
                lineTo(160f, 168f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 168f, 160f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(464f, 192f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                horizontalLineTo(448f)
                verticalLineTo(128f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64f, -64f)
                horizontalLineTo(352f)
                verticalLineTo(48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, 0f)
                verticalLineTo(64f)
                horizontalLineTo(272f)
                verticalLineTo(48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, 0f)
                verticalLineTo(64f)
                horizontalLineTo(192f)
                verticalLineTo(48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, 0f)
                verticalLineTo(64f)
                horizontalLineTo(128f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64f, 64f)
                verticalLineToRelative(32f)
                horizontalLineTo(48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineTo(64f)
                verticalLineToRelative(48f)
                horizontalLineTo(48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineTo(64f)
                verticalLineToRelative(48f)
                horizontalLineTo(48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineTo(64f)
                verticalLineToRelative(32f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                horizontalLineToRelative(32f)
                verticalLineToRelative(16f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 0f)
                verticalLineTo(448f)
                horizontalLineToRelative(48f)
                verticalLineToRelative(16f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 0f)
                verticalLineTo(448f)
                horizontalLineToRelative(48f)
                verticalLineToRelative(16f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 0f)
                verticalLineTo(448f)
                horizontalLineToRelative(32f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                verticalLineTo(352f)
                horizontalLineToRelative(16f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                horizontalLineTo(448f)
                verticalLineTo(272f)
                horizontalLineToRelative(16f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32f)
                horizontalLineTo(448f)
                verticalLineTo(192f)
                close()
                moveTo(384f, 352f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 32f)
                horizontalLineTo(160f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, -32f)
                verticalLineTo(160f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, -32f)
                horizontalLineTo(352f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 32f)
                close()
            }
        }.build()

        return _CiHardwareChip!!
    }

@Suppress("ObjectPropertyName")
private var _CiHardwareChip: ImageVector? = null
