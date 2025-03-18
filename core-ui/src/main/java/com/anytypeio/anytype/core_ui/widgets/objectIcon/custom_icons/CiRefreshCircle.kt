package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiRefreshCircle: ImageVector
    get() {
        if (_CiRefreshCircle != null) {
            return _CiRefreshCircle!!
        }
        _CiRefreshCircle = ImageVector.Builder(
            name = "CiRefreshCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 48f)
                curveTo(141.31f, 48f, 48f, 141.32f, 48f, 256f)
                curveToRelative(0f, 114.86f, 93.14f, 208f, 208f, 208f)
                curveToRelative(114.69f, 0f, 208f, -93.31f, 208f, -208f)
                curveTo(464f, 141.13f, 370.87f, 48f, 256f, 48f)
                close()
                moveTo(256f, 361f)
                arcToRelative(94f, 94f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -188f)
                horizontalLineToRelative(4.21f)
                lineTo(246.1f, 158.9f)
                arcToRelative(14f, 14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19.8f, -19.8f)
                lineToRelative(40f, 40f)
                arcToRelative(14f, 14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 19.8f)
                lineToRelative(-40f, 40f)
                arcToRelative(14f, 14f, 0f, isMoreThanHalf = false, isPositiveArc = true, -19.8f, -19.8f)
                lineToRelative(18f, -18f)
                curveTo(261.72f, 201f, 259f, 201f, 256f, 201f)
                arcToRelative(66f, 66f, 0f, isMoreThanHalf = true, isPositiveArc = false, 66f, 66f)
                arcToRelative(14f, 14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28f, 0f)
                arcTo(94.11f, 94.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 361f)
                close()
            }
        }.build()

        return _CiRefreshCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiRefreshCircle: ImageVector? = null
