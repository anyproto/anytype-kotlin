package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiImages: ImageVector
    get() {
        if (_CiImages != null) {
            return _CiImages!!
        }
        _CiImages = ImageVector.Builder(
            name = "CiImages",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(450.29f, 112f)
                lineTo(142f, 112f)
                curveToRelative(-34f, 0f, -62f, 27.51f, -62f, 61.33f)
                lineTo(80f, 418.67f)
                curveTo(80f, 452.49f, 108f, 480f, 142f, 480f)
                lineTo(450f, 480f)
                curveToRelative(34f, 0f, 62f, -26.18f, 62f, -60f)
                lineTo(512f, 173.33f)
                curveTo(512f, 139.51f, 484.32f, 112f, 450.29f, 112f)
                close()
                moveTo(373.14f, 173.34f)
                arcToRelative(46f, 46f, 0f, isMoreThanHalf = true, isPositiveArc = true, -46.28f, 46f)
                arcTo(46.19f, 46.19f, 0f, isMoreThanHalf = false, isPositiveArc = true, 373.14f, 173.33f)
                close()
                moveTo(141.59f, 449.34f)
                curveToRelative(-17f, 0f, -29.86f, -13.75f, -29.86f, -30.66f)
                lineTo(111.73f, 353.85f)
                lineToRelative(90.46f, -80.79f)
                arcToRelative(46.54f, 46.54f, 0f, isMoreThanHalf = false, isPositiveArc = true, 63.44f, 1.83f)
                lineTo(328.27f, 337f)
                lineToRelative(-113f, 112.33f)
                close()
                moveTo(480f, 418.67f)
                arcToRelative(30.67f, 30.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -30.71f, 30.66f)
                lineTo(259f, 449.33f)
                lineTo(376.08f, 333f)
                arcToRelative(46.24f, 46.24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 59.44f, -0.16f)
                lineTo(480f, 370.59f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(384f, 32f)
                horizontalLineTo(64f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 96f)
                verticalLineTo(352f)
                arcToRelative(64.11f, 64.11f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 62f)
                verticalLineTo(152f)
                arcToRelative(72f, 72f, 0f, isMoreThanHalf = false, isPositiveArc = true, 72f, -72f)
                horizontalLineTo(446f)
                arcTo(64.11f, 64.11f, 0f, isMoreThanHalf = false, isPositiveArc = false, 384f, 32f)
                close()
            }
        }.build()

        return _CiImages!!
    }

@Suppress("ObjectPropertyName")
private var _CiImages: ImageVector? = null
