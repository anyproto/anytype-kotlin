package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCloudUpload: ImageVector
    get() {
        if (_CiCloudUpload != null) {
            return _CiCloudUpload!!
        }
        _CiCloudUpload = ImageVector.Builder(
            name = "CiCloudUpload",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(473.66f, 210f)
                curveToRelative(-14f, -10.38f, -31.2f, -18f, -49.36f, -22.11f)
                arcToRelative(16.11f, 16.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, -12.19f, -12.22f)
                curveToRelative(-7.8f, -34.75f, -24.59f, -64.55f, -49.27f, -87.13f)
                curveTo(334.15f, 62.25f, 296.21f, 47.79f, 256f, 47.79f)
                curveToRelative(-35.35f, 0f, -68f, 11.08f, -94.37f, 32.05f)
                arcToRelative(150.07f, 150.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, -42.06f, 53f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.31f, 8.87f)
                curveToRelative(-26.75f, 5.4f, -50.9f, 16.87f, -69.34f, 33.12f)
                curveTo(13.46f, 197.33f, 0f, 227.24f, 0f, 261.39f)
                curveToRelative(0f, 34.52f, 14.49f, 66f, 40.79f, 88.76f)
                curveToRelative(25.12f, 21.69f, 58.94f, 33.64f, 95.21f, 33.64f)
                horizontalLineTo(240f)
                verticalLineTo(230.42f)
                lineToRelative(-36.69f, 36.69f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -23.16f, -0.56f)
                curveToRelative(-5.8f, -6.37f, -5.24f, -16.3f, 0.85f, -22.39f)
                lineToRelative(63.69f, -63.68f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.62f, 0f)
                lineTo(331f, 244.14f)
                curveToRelative(6.28f, 6.29f, 6.64f, 16.6f, 0.39f, 22.91f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.68f, 0.06f)
                lineTo(272f, 230.42f)
                verticalLineTo(383.79f)
                horizontalLineTo(396f)
                curveToRelative(31.34f, 0f, 59.91f, -8.8f, 80.45f, -24.77f)
                curveToRelative(23.26f, -18.1f, 35.55f, -44f, 35.55f, -74.83f)
                curveTo(512f, 254.25f, 498.74f, 228.58f, 473.66f, 210f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(240f, 448.21f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = false, 32f, 0f)
                verticalLineTo(383.79f)
                horizontalLineTo(240f)
                close()
            }
        }.build()

        return _CiCloudUpload!!
    }

@Suppress("ObjectPropertyName")
private var _CiCloudUpload: ImageVector? = null
