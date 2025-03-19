package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiThermometer: ImageVector
    get() {
        if (_CiThermometer != null) {
            return _CiThermometer!!
        }
        _CiThermometer = ImageVector.Builder(
            name = "CiThermometer",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(320f, 287.18f)
                verticalLineTo(81f)
                curveToRelative(0f, -35.12f, -27.89f, -64.42f, -63f, -64.95f)
                arcToRelative(64.08f, 64.08f, 0f, isMoreThanHalf = false, isPositiveArc = false, -65f, 64f)
                verticalLineTo(287.18f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.18f, 6.37f)
                arcTo(113.48f, 113.48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 144f, 384f)
                arcToRelative(112f, 112f, 0f, isMoreThanHalf = false, isPositiveArc = false, 224f, 0f)
                arcToRelative(113.48f, 113.48f, 0f, isMoreThanHalf = false, isPositiveArc = false, -44.82f, -90.45f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 320f, 287.18f)
                close()
                moveTo(254.07f, 432f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22f, -89.54f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, -13.84f)
                verticalLineTo(112.45f)
                curveToRelative(0f, -8.61f, 6.62f, -16f, 15.23f, -16.43f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 272f, 112f)
                verticalLineTo(328.58f)
                arcToRelative(16.18f, 16.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.15f, 13.94f)
                arcTo(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 254.07f, 432f)
                close()
            }
        }.build()

        return _CiThermometer!!
    }

@Suppress("ObjectPropertyName")
private var _CiThermometer: ImageVector? = null
