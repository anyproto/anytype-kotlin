package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiIdCard: ImageVector
    get() {
        if (_CiIdCard != null) {
            return _CiIdCard!!
        }
        _CiIdCard = ImageVector.Builder(
            name = "CiIdCard",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(368f, 16f)
                horizontalLineTo(144f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, 80f)
                verticalLineTo(432f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                horizontalLineTo(368f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                verticalLineTo(80f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 368f, 16f)
                close()
                moveTo(333.48f, 284.51f)
                curveToRelative(7.57f, 8.17f, 11.27f, 19.16f, 10.39f, 30.94f)
                curveTo(342.14f, 338.91f, 324.25f, 358f, 304f, 358f)
                reflectiveCurveToRelative(-38.17f, -19.09f, -39.88f, -42.55f)
                curveToRelative(-0.86f, -11.9f, 2.81f, -22.91f, 10.34f, -31f)
                reflectiveCurveTo(292.4f, 272f, 304f, 272f)
                arcTo(39.65f, 39.65f, 0f, isMoreThanHalf = false, isPositiveArc = true, 333.48f, 284.51f)
                close()
                moveTo(192f, 80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, -16f)
                horizontalLineToRelative(96f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                horizontalLineTo(208f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 192f, 80f)
                close()
                moveTo(381f, 443.83f)
                arcToRelative(12.05f, 12.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, -9.31f, 4.17f)
                horizontalLineTo(236.31f)
                arcToRelative(12.05f, 12.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, -9.31f, -4.17f)
                arcToRelative(13f, 13f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.76f, -10.92f)
                curveToRelative(3.25f, -17.56f, 13.38f, -32.31f, 29.3f, -42.66f)
                curveTo(267.68f, 381.06f, 285.6f, 376f, 304f, 376f)
                reflectiveCurveToRelative(36.32f, 5.06f, 50.46f, 14.25f)
                curveToRelative(15.92f, 10.35f, 26.05f, 25.1f, 29.3f, 42.66f)
                arcTo(13f, 13f, 0f, isMoreThanHalf = false, isPositiveArc = true, 381f, 443.83f)
                close()
            }
        }.build()

        return _CiIdCard!!
    }

@Suppress("ObjectPropertyName")
private var _CiIdCard: ImageVector? = null
