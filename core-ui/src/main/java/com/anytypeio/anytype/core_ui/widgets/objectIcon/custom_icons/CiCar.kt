package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCar: ImageVector
    get() {
        if (_CiCar != null) {
            return _CiCar!!
        }
        _CiCar = ImageVector.Builder(
            name = "CiCar",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(447.68f, 220.78f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1f, -3.08f)
                lineToRelative(-37.78f, -88.16f)
                curveTo(400.19f, 109.17f, 379f, 96f, 354.89f, 96f)
                lineTo(157.11f, 96f)
                curveToRelative(-24.09f, 0f, -45.3f, 13.17f, -54f, 33.54f)
                lineTo(65.29f, 217.7f)
                arcTo(15.72f, 15.72f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 224f)
                lineTo(64f, 400f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                horizontalLineToRelative(32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, -16f)
                lineTo(128f, 384f)
                lineTo(384f, 384f)
                verticalLineToRelative(16f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                horizontalLineToRelative(32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, -16f)
                lineTo(448f, 224f)
                arcTo(16.15f, 16.15f, 0f, isMoreThanHalf = false, isPositiveArc = false, 447.68f, 220.78f)
                close()
                moveTo(144f, 320f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 144f, 320f)
                close()
                moveTo(368f, 320f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 368f, 320f)
                close()
                moveTo(104.26f, 208f)
                lineToRelative(28.23f, -65.85f)
                curveTo(136.11f, 133.69f, 146f, 128f, 157.11f, 128f)
                lineTo(354.89f, 128f)
                curveToRelative(11.1f, 0f, 21f, 5.69f, 24.62f, 14.15f)
                lineTo(407.74f, 208f)
                close()
            }
        }.build()

        return _CiCar!!
    }

@Suppress("ObjectPropertyName")
private var _CiCar: ImageVector? = null
