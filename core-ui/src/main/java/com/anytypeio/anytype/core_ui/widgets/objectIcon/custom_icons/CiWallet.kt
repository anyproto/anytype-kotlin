package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiWallet: ImageVector
    get() {
        if (_CiWallet != null) {
            return _CiWallet!!
        }
        _CiWallet = ImageVector.Builder(
            name = "CiWallet",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(95.5f, 104f)
                horizontalLineToRelative(320f)
                arcToRelative(87.73f, 87.73f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.18f, 0.71f)
                arcToRelative(66f, 66f, 0f, isMoreThanHalf = false, isPositiveArc = false, -77.51f, -55.56f)
                lineTo(86f, 94.08f)
                lineToRelative(-0.3f, 0f)
                arcToRelative(66f, 66f, 0f, isMoreThanHalf = false, isPositiveArc = false, -41.07f, 26.13f)
                arcTo(87.57f, 87.57f, 0f, isMoreThanHalf = false, isPositiveArc = true, 95.5f, 104f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(415.5f, 128f)
                horizontalLineTo(95.5f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64f, 64f)
                verticalLineTo(384f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                horizontalLineToRelative(320f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                verticalLineTo(192f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 415.5f, 128f)
                close()
                moveTo(368f, 320f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 368f, 320f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(32f, 259.5f)
                verticalLineTo(160f)
                curveToRelative(0f, -21.67f, 12f, -58f, 53.65f, -65.87f)
                curveTo(121f, 87.5f, 156f, 87.5f, 156f, 87.5f)
                reflectiveCurveToRelative(23f, 16f, 4f, 16f)
                reflectiveCurveTo(141.5f, 128f, 160f, 128f)
                reflectiveCurveToRelative(0f, 23.5f, 0f, 23.5f)
                lineTo(85.5f, 236f)
                close()
            }
        }.build()

        return _CiWallet!!
    }

@Suppress("ObjectPropertyName")
private var _CiWallet: ImageVector? = null
