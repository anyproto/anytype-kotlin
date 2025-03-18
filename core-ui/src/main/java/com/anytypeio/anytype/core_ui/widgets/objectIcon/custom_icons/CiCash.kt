package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCash: ImageVector
    get() {
        if (_CiCash != null) {
            return _CiCash!!
        }
        _CiCash = ImageVector.Builder(
            name = "CiCash",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(448f, 400f)
                horizontalLineTo(64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineTo(448f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(416f, 448f)
                horizontalLineTo(96f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineTo(416f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(32f, 272f)
                horizontalLineTo(16f)
                verticalLineToRelative(48f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 32f)
                horizontalLineTo(96f)
                verticalLineTo(336f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 272f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(480f, 240f)
                horizontalLineToRelative(16f)
                verticalLineTo(176f)
                horizontalLineTo(480f)
                arcToRelative(96.11f, 96.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, -96f, -96f)
                verticalLineTo(64f)
                horizontalLineTo(128f)
                verticalLineTo(80f)
                arcToRelative(96.11f, 96.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, -96f, 96f)
                horizontalLineTo(16f)
                verticalLineToRelative(64f)
                curveToRelative(5f, 0f, 10.34f, 0f, 16f, 0f)
                arcToRelative(96.11f, 96.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, 96f, 96f)
                verticalLineToRelative(16f)
                horizontalLineTo(384f)
                verticalLineTo(336f)
                arcTo(96.11f, 96.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, 480f, 240f)
                close()
                moveTo(256f, 304f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = true, isPositiveArc = true, 96f, -96f)
                arcTo(96.11f, 96.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 304f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 208f)
                moveToRelative(-64f, 0f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = true, 128f, 0f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = true, -128f, 0f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(416f, 336f)
                verticalLineToRelative(16f)
                horizontalLineToRelative(48f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, -32f)
                verticalLineTo(272f)
                horizontalLineTo(480f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 416f, 336f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(480f, 144f)
                horizontalLineToRelative(16f)
                verticalLineTo(96f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, -32f)
                horizontalLineTo(416f)
                verticalLineTo(80f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 480f, 144f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(96f, 80f)
                verticalLineTo(64f)
                horizontalLineTo(48f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 96f)
                verticalLineToRelative(48f)
                horizontalLineTo(32f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 96f, 80f)
                close()
            }
        }.build()

        return _CiCash!!
    }

@Suppress("ObjectPropertyName")
private var _CiCash: ImageVector? = null
