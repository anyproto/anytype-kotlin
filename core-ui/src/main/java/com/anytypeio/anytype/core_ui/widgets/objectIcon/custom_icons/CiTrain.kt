package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiTrain: ImageVector
    get() {
        if (_CiTrain != null) {
            return _CiTrain!!
        }
        _CiTrain = ImageVector.Builder(
            name = "CiTrain",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(233.34f, 326.66f)
                arcToRelative(31.94f, 32.05f, 45f, isMoreThanHalf = true, isPositiveArc = false, 45.33f, -45.33f)
                arcToRelative(31.94f, 32.05f, 45f, isMoreThanHalf = true, isPositiveArc = false, -45.33f, 45.33f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(352f, 32f)
                lineTo(337f, 32f)
                arcToRelative(10f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, -7.87f, -3.78f)
                arcTo(31.94f, 31.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 304f, 16f)
                lineTo(208f, 16f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, -26.11f, 13.52f)
                arcTo(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 177f, 32f)
                lineTo(160f, 32f)
                curveToRelative(-36.81f, 0f, -64f, 28.84f, -64f, 64f)
                lineTo(96f, 351f)
                curveToRelative(0f, 23.27f, 25.6f, 42.06f, 83f, 60.94f)
                arcToRelative(753f, 753f, 0f, isMoreThanHalf = false, isPositiveArc = false, 73.77f, 19.73f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.46f, 0f)
                arcTo(753f, 753f, 0f, isMoreThanHalf = false, isPositiveArc = false, 333f, 411.94f)
                curveToRelative(57.4f, -18.88f, 83f, -37.67f, 83f, -60.94f)
                lineTo(416f, 96f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 352f, 32f)
                close()
                moveTo(184f, 128f)
                lineTo(328f, 128f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 8f)
                verticalLineToRelative(48f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8f, 8f)
                lineTo(184f, 192f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8f, -8f)
                lineTo(176f, 136f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 184f, 128f)
                close()
                moveTo(260.18f, 367.87f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = true, 59.69f, -59.69f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = true, 260.18f, 367.87f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(395.31f, 468.69f)
                lineTo(347.63f, 421f)
                curveToRelative(-6.09f, -6.1f, -16f, -6.66f, -22.38f, -0.86f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.56f, 23.16f)
                lineToRelative(4.68f, 4.69f)
                horizontalLineTo(182.63f)
                lineToRelative(4.36f, -4.37f)
                curveToRelative(6.1f, -6.09f, 6.66f, -16f, 0.86f, -22.38f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -23.16f, -0.56f)
                lineToRelative(-48f, 48f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = false, 22.62f, 22.62f)
                lineTo(150.63f, 480f)
                horizontalLineTo(361.37f)
                lineToRelative(11.32f, 11.31f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 22.62f, -22.62f)
                close()
            }
        }.build()

        return _CiTrain!!
    }

@Suppress("ObjectPropertyName")
private var _CiTrain: ImageVector? = null
