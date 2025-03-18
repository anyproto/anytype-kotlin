package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiLibrary: ImageVector
    get() {
        if (_CiLibrary != null) {
            return _CiLibrary!!
        }
        _CiLibrary = ImageVector.Builder(
            name = "CiLibrary",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(64f, 480f)
                horizontalLineTo(48f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, -32f)
                verticalLineTo(112f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 48f, 80f)
                horizontalLineTo(64f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 32f)
                verticalLineTo(448f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 64f, 480f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(240f, 176f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, -32f)
                horizontalLineTo(144f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, 32f)
                verticalLineToRelative(28f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, 4f)
                horizontalLineTo(236f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, -4f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(112f, 448f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 32f)
                horizontalLineToRelative(64f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, -32f)
                verticalLineTo(418f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2f, -2f)
                horizontalLineTo(114f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2f, 2f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(114f, 240f)
                lineTo(238f, 240f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 240f, 242f)
                lineTo(240f, 382f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 238f, 384f)
                lineTo(114f, 384f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 112f, 382f)
                lineTo(112f, 242f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 114f, 240f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(320f, 480f)
                horizontalLineTo(288f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, -32f)
                verticalLineTo(64f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, -32f)
                horizontalLineToRelative(32f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 32f)
                verticalLineTo(448f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 320f, 480f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(495.89f, 445.45f)
                lineToRelative(-32.23f, -340f)
                curveToRelative(-1.48f, -15.65f, -16.94f, -27f, -34.53f, -25.31f)
                lineToRelative(-31.85f, 3f)
                curveToRelative(-17.59f, 1.67f, -30.65f, 15.71f, -29.17f, 31.36f)
                lineToRelative(32.23f, 340f)
                curveToRelative(1.48f, 15.65f, 16.94f, 27f, 34.53f, 25.31f)
                lineToRelative(31.85f, -3f)
                curveTo(484.31f, 475.14f, 497.37f, 461.1f, 495.89f, 445.45f)
                close()
            }
        }.build()

        return _CiLibrary!!
    }

@Suppress("ObjectPropertyName")
private var _CiLibrary: ImageVector? = null
