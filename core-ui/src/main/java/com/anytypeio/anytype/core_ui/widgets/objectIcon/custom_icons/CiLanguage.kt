package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiLanguage: ImageVector
    get() {
        if (_CiLanguage != null) {
            return _CiLanguage!!
        }
        _CiLanguage = ImageVector.Builder(
            name = "CiLanguage",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(478.33f, 433.6f)
                lineToRelative(-90f, -218f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, -40.67f, 0f)
                lineToRelative(-90f, 218f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = true, isPositiveArc = false, 40.67f, 16.79f)
                lineTo(316.66f, 406f)
                horizontalLineTo(419.33f)
                lineToRelative(18.33f, 44.39f)
                arcTo(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 458f, 464f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 20.32f, -30.4f)
                close()
                moveTo(334.83f, 362f)
                lineTo(368f, 281.65f)
                lineTo(401.17f, 362f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(267.84f, 342.92f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4.89f, -30.7f)
                curveToRelative(-0.2f, -0.15f, -15f, -11.13f, -36.49f, -34.73f)
                curveToRelative(39.65f, -53.68f, 62.11f, -114.75f, 71.27f, -143.49f)
                horizontalLineTo(330f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -44f)
                horizontalLineTo(214f)
                verticalLineTo(70f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, -44f, 0f)
                verticalLineTo(90f)
                horizontalLineTo(54f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 44f)
                horizontalLineTo(251.25f)
                curveToRelative(-9.52f, 26.95f, -27.05f, 69.5f, -53.79f, 108.36f)
                curveToRelative(-31.41f, -41.68f, -43.08f, -68.65f, -43.17f, -68.87f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, -40.58f, 17f)
                curveToRelative(0.58f, 1.38f, 14.55f, 34.23f, 52.86f, 83.93f)
                curveToRelative(0.92f, 1.19f, 1.83f, 2.35f, 2.74f, 3.51f)
                curveToRelative(-39.24f, 44.35f, -77.74f, 71.86f, -93.85f, 80.74f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = true, isPositiveArc = false, 21.07f, 38.63f)
                curveToRelative(2.16f, -1.18f, 48.6f, -26.89f, 101.63f, -85.59f)
                curveToRelative(22.52f, 24.08f, 38f, 35.44f, 38.93f, 36.1f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 30.75f, -4.9f)
                close()
            }
        }.build()

        return _CiLanguage!!
    }

@Suppress("ObjectPropertyName")
private var _CiLanguage: ImageVector? = null
