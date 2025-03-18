package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiTrailSign: ImageVector
    get() {
        if (_CiTrailSign != null) {
            return _CiTrailSign!!
        }
        _CiTrailSign = ImageVector.Builder(
            name = "CiTrailSign",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(491.31f, 324.69f)
                lineTo(432f, 265.37f)
                arcTo(31.8f, 31.8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 409.37f, 256f)
                horizontalLineTo(272f)
                verticalLineTo(224f)
                horizontalLineTo(416f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, -32f)
                verticalLineTo(96f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, -32f)
                horizontalLineTo(272f)
                verticalLineTo(48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, 0f)
                verticalLineTo(64f)
                horizontalLineTo(102.63f)
                arcTo(31.8f, 31.8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, 73.37f)
                lineTo(20.69f, 132.69f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 22.62f)
                lineTo(80f, 214.63f)
                arcTo(31.8f, 31.8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 102.63f, 224f)
                horizontalLineTo(240f)
                verticalLineToRelative(32f)
                horizontalLineTo(96f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, 32f)
                verticalLineToRelative(96f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 32f)
                horizontalLineTo(240f)
                verticalLineToRelative(48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 0f)
                verticalLineTo(416f)
                horizontalLineTo(409.37f)
                arcTo(31.8f, 31.8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 432f, 406.63f)
                lineToRelative(59.31f, -59.32f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 491.31f, 324.69f)
                close()
            }
        }.build()

        return _CiTrailSign!!
    }

@Suppress("ObjectPropertyName")
private var _CiTrailSign: ImageVector? = null
