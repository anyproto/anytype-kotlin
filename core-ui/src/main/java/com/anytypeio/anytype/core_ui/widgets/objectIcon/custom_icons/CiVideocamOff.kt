package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiVideocamOff: ImageVector
    get() {
        if (_CiVideocamOff != null) {
            return _CiVideocamOff!!
        }
        _CiVideocamOff = ImageVector.Builder(
            name = "CiVideocamOff",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(336f, 179.52f)
                arcTo(67.52f, 67.52f, 0f, isMoreThanHalf = false, isPositiveArc = false, 268.48f, 112f)
                horizontalLineToRelative(-79.2f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.82f, 6.83f)
                lineTo(329.17f, 261.54f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.83f, -2.82f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(16f, 180f)
                verticalLineTo(332f)
                arcToRelative(68f, 68f, 0f, isMoreThanHalf = false, isPositiveArc = false, 68f, 68f)
                horizontalLineTo(268f)
                arcToRelative(67.66f, 67.66f, 0f, isMoreThanHalf = false, isPositiveArc = false, 42.84f, -15.24f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.33f, -6f)
                lineTo(54.41f, 122f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4.87f, -0.62f)
                arcTo(68f, 68f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 180f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(464f, 384.39f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13f, -2.77f)
                arcToRelative(15.77f, 15.77f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.71f, -1.54f)
                lineToRelative(-82.71f, -58.22f)
                horizontalLineToRelative(0f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 352f, 295.7f)
                verticalLineTo(216.3f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.58f, -26.16f)
                lineToRelative(82.71f, -58.22f)
                arcToRelative(15.77f, 15.77f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.71f, -1.54f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 45f, 29.24f)
                verticalLineTo(352.38f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 32f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(416f, 416f)
                lineTo(80f, 80f)
            }
        }.build()

        return _CiVideocamOff!!
    }

@Suppress("ObjectPropertyName")
private var _CiVideocamOff: ImageVector? = null
