package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCheckmarkDoneCircle: ImageVector
    get() {
        if (_CiCheckmarkDoneCircle != null) {
            return _CiCheckmarkDoneCircle!!
        }
        _CiCheckmarkDoneCircle = ImageVector.Builder(
            name = "CiCheckmarkDoneCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(258.9f, 48f)
                curveTo(141.92f, 46.42f, 46.42f, 141.92f, 48f, 258.9f)
                curveTo(49.56f, 371.09f, 140.91f, 462.44f, 253.1f, 464f)
                curveToRelative(117f, 1.6f, 212.48f, -93.9f, 210.88f, -210.88f)
                curveTo(462.44f, 140.91f, 371.09f, 49.56f, 258.9f, 48f)
                close()
                moveTo(242.11f, 240.47f)
                lineToRelative(51.55f, -59f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24.1f, 21.06f)
                lineToRelative(-51.55f, 59f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24.1f, -21.06f)
                close()
                moveTo(203.25f, 331.32f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.62f, 0f)
                lineToRelative(-47.95f, -48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 22.64f, -22.62f)
                lineToRelative(48f, 48f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 203.25f, 331.32f)
                close()
                moveTo(380.05f, 202.53f)
                lineTo(268.17f, 330.53f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256.66f, 336f)
                horizontalLineToRelative(-0.54f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.32f, -4.69f)
                lineToRelative(-47.94f, -48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 22.64f, -22.62f)
                lineToRelative(29.8f, 29.83f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 11.68f, -0.39f)
                lineToRelative(95f, -108.66f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24.1f, 21.06f)
                close()
            }
        }.build()

        return _CiCheckmarkDoneCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiCheckmarkDoneCircle: ImageVector? = null
