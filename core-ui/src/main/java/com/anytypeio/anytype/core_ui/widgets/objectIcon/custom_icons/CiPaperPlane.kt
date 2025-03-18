package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPaperPlane: ImageVector
    get() {
        if (_CiPaperPlane != null) {
            return _CiPaperPlane!!
        }
        _CiPaperPlane = ImageVector.Builder(
            name = "CiPaperPlane",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(473f, 39.05f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, -25.5f, -5.46f)
                lineTo(47.47f, 185f)
                lineToRelative(-0.08f, 0f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, 45.16f)
                lineToRelative(0.41f, 0.13f)
                lineToRelative(137.3f, 58.63f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.54f, -3.59f)
                lineTo(422f, 80f)
                arcToRelative(7.07f, 7.07f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10f, 10f)
                lineTo(226.66f, 310.26f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.59f, 15.54f)
                lineToRelative(58.65f, 137.38f)
                curveToRelative(0.06f, 0.2f, 0.12f, 0.38f, 0.19f, 0.57f)
                curveToRelative(3.2f, 9.27f, 11.3f, 15.81f, 21.09f, 16.25f)
                curveToRelative(0.43f, 0f, 0.58f, 0f, 1f, 0f)
                arcToRelative(24.63f, 24.63f, 0f, isMoreThanHalf = false, isPositiveArc = false, 23f, -15.46f)
                lineTo(478.39f, 64.62f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 473f, 39.05f)
                close()
            }
        }.build()

        return _CiPaperPlane!!
    }

@Suppress("ObjectPropertyName")
private var _CiPaperPlane: ImageVector? = null
