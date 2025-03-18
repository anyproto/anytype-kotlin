package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiSettings: ImageVector
    get() {
        if (_CiSettings != null) {
            return _CiSettings!!
        }
        _CiSettings = ImageVector.Builder(
            name = "CiSettings",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 256f)
                moveToRelative(-48f, 0f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = true, isPositiveArc = true, 96f, 0f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = true, isPositiveArc = true, -96f, 0f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(470.39f, 300f)
                lineToRelative(-0.47f, -0.38f)
                lineToRelative(-31.56f, -24.75f)
                arcToRelative(16.11f, 16.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6.1f, -13.33f)
                lineToRelative(0f, -11.56f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6.11f, -13.22f)
                lineTo(469.92f, 212f)
                lineToRelative(0.47f, -0.38f)
                arcToRelative(26.68f, 26.68f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.9f, -34.06f)
                lineToRelative(-42.71f, -73.9f)
                arcToRelative(1.59f, 1.59f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.13f, -0.22f)
                arcTo(26.86f, 26.86f, 0f, isMoreThanHalf = false, isPositiveArc = false, 401f, 92.14f)
                lineToRelative(-0.35f, 0.13f)
                lineTo(363.55f, 107.2f)
                arcToRelative(15.94f, 15.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.47f, -1.29f)
                quadToRelative(-4.92f, -3.1f, -10f, -5.86f)
                arcToRelative(15.94f, 15.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8.19f, -11.82f)
                lineTo(325.3f, 48.64f)
                lineToRelative(-0.12f, -0.72f)
                arcTo(27.22f, 27.22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 298.76f, 26f)
                horizontalLineTo(213.24f)
                arcToRelative(26.92f, 26.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, -26.45f, 22.39f)
                lineToRelative(-0.09f, 0.56f)
                lineToRelative(-5.57f, 39.67f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 173f, 100.44f)
                curveToRelative(-3.42f, 1.84f, -6.76f, 3.79f, -10f, 5.82f)
                arcToRelative(15.92f, 15.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.43f, 1.27f)
                lineToRelative(-37.13f, -15f)
                lineToRelative(-0.35f, -0.14f)
                arcToRelative(26.87f, 26.87f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32.48f, 11.34f)
                lineToRelative(-0.13f, 0.22f)
                lineTo(35.71f, 177.9f)
                arcTo(26.71f, 26.71f, 0f, isMoreThanHalf = false, isPositiveArc = false, 41.61f, 212f)
                lineToRelative(0.47f, 0.38f)
                lineToRelative(31.56f, 24.75f)
                arcToRelative(16.11f, 16.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6.1f, 13.33f)
                lineToRelative(0f, 11.56f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6.11f, 13.22f)
                lineTo(42.08f, 300f)
                lineToRelative(-0.47f, 0.38f)
                arcToRelative(26.68f, 26.68f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.9f, 34.06f)
                lineToRelative(42.71f, 73.9f)
                arcToRelative(1.59f, 1.59f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.13f, 0.22f)
                arcTo(26.86f, 26.86f, 0f, isMoreThanHalf = false, isPositiveArc = false, 111f, 419.86f)
                lineToRelative(0.35f, -0.13f)
                lineToRelative(37.07f, -14.93f)
                arcToRelative(15.94f, 15.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.47f, 1.29f)
                quadToRelative(4.92f, 3.11f, 10f, 5.86f)
                arcToRelative(15.94f, 15.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.19f, 11.82f)
                lineToRelative(5.56f, 39.59f)
                lineToRelative(0.12f, 0.72f)
                arcTo(27.22f, 27.22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 213.24f, 486f)
                horizontalLineToRelative(85.52f)
                arcToRelative(26.92f, 26.92f, 0f, isMoreThanHalf = false, isPositiveArc = false, 26.45f, -22.39f)
                lineToRelative(0.09f, -0.56f)
                lineToRelative(5.57f, -39.67f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.18f, -11.82f)
                curveToRelative(3.42f, -1.84f, 6.76f, -3.79f, 10f, -5.82f)
                arcToRelative(15.92f, 15.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.43f, -1.27f)
                lineToRelative(37.13f, 14.95f)
                lineToRelative(0.35f, 0.14f)
                arcToRelative(26.85f, 26.85f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32.48f, -11.34f)
                arcToRelative(2.53f, 2.53f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.13f, -0.22f)
                lineToRelative(42.71f, -73.89f)
                arcTo(26.7f, 26.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, 470.39f, 300f)
                close()
                moveTo(335.91f, 259.76f)
                arcToRelative(80f, 80f, 0f, isMoreThanHalf = true, isPositiveArc = true, -83.66f, -83.67f)
                arcTo(80.21f, 80.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 335.91f, 259.76f)
                close()
            }
        }.build()

        return _CiSettings!!
    }

@Suppress("ObjectPropertyName")
private var _CiSettings: ImageVector? = null
