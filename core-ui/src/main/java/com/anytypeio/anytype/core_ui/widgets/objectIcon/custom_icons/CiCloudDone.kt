package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCloudDone: ImageVector
    get() {
        if (_CiCloudDone != null) {
            return _CiCloudDone!!
        }
        _CiCloudDone = ImageVector.Builder(
            name = "CiCloudDone",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(424.44f, 227.25f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -12.12f, -12.39f)
                curveToRelative(-7.68f, -36.68f, -24.45f, -68.15f, -49.18f, -92f)
                arcTo(153.57f, 153.57f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 80f)
                curveToRelative(-35.5f, 0f, -68.24f, 11.69f, -94.68f, 33.8f)
                arcToRelative(156.24f, 156.24f, 0f, isMoreThanHalf = false, isPositiveArc = false, -42f, 56f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.37f, 9.15f)
                curveToRelative(-27f, 5.62f, -51.07f, 17.34f, -69.18f, 33.87f)
                curveTo(13.39f, 235.88f, 0f, 267.42f, 0f, 304f)
                curveToRelative(0f, 36f, 14.38f, 68.88f, 40.49f, 92.59f)
                curveTo(65.64f, 419.43f, 99.56f, 432f, 136f, 432f)
                lineTo(396f, 432f)
                curveToRelative(32.37f, 0f, 60.23f, -8.57f, 80.59f, -24.77f)
                curveTo(499.76f, 388.78f, 512f, 361.39f, 512f, 328f)
                curveTo(512f, 270.43f, 470f, 237.42f, 424.44f, 227.25f)
                close()
                moveTo(329.24f, 218.31f)
                lineTo(221.44f, 346.31f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -12f, 5.69f)
                horizontalLineToRelative(-0.27f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.88f, -5.28f)
                lineToRelative(-45.9f, -50.87f)
                curveToRelative(-5.77f, -6.39f, -5.82f, -16.33f, 0.3f, -22.4f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.16f, 0.63f)
                lineToRelative(33.9f, 37.58f)
                lineToRelative(96f, -114f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 24.48f, 20.62f)
                close()
            }
        }.build()

        return _CiCloudDone!!
    }

@Suppress("ObjectPropertyName")
private var _CiCloudDone: ImageVector? = null
