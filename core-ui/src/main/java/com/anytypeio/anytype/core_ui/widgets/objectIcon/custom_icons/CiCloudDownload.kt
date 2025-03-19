package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCloudDownload: ImageVector
    get() {
        if (_CiCloudDownload != null) {
            return _CiCloudDownload!!
        }
        _CiCloudDownload = ImageVector.Builder(
            name = "CiCloudDownload",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(472.7f, 189.5f)
                curveToRelative(-13.26f, -8.43f, -29.83f, -14.56f, -48.08f, -17.93f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 412f, 159.28f)
                curveToRelative(-7.86f, -34.51f, -24.6f, -64.13f, -49.15f, -86.58f)
                curveTo(334.15f, 46.45f, 296.21f, 32f, 256f, 32f)
                curveToRelative(-35.35f, 0f, -68f, 11.08f, -94.37f, 32f)
                arcToRelative(150.13f, 150.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -41.95f, 52.83f)
                arcTo(16.05f, 16.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, 108f, 125.8f)
                curveToRelative(-27.13f, 4.9f, -50.53f, 14.68f, -68.41f, 28.7f)
                curveTo(13.7f, 174.83f, 0f, 203.56f, 0f, 237.6f)
                curveTo(0f, 305f, 55.93f, 352f, 136f, 352f)
                horizontalLineTo(240f)
                verticalLineTo(224.45f)
                curveToRelative(0f, -8.61f, 6.62f, -16f, 15.23f, -16.43f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 272f, 224f)
                verticalLineTo(352f)
                horizontalLineTo(396f)
                curveToRelative(72.64f, 0f, 116f, -34.24f, 116f, -91.6f)
                curveTo(512f, 230.35f, 498.41f, 205.83f, 472.7f, 189.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(240f, 425.42f)
                lineToRelative(-36.7f, -36.64f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -22.6f, 22.65f)
                lineToRelative(64f, 63.89f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 22.6f, 0f)
                lineToRelative(64f, -63.89f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -22.6f, -22.65f)
                lineTo(272f, 425.42f)
                verticalLineTo(352f)
                horizontalLineTo(240f)
                close()
            }
        }.build()

        return _CiCloudDownload!!
    }

@Suppress("ObjectPropertyName")
private var _CiCloudDownload: ImageVector? = null
