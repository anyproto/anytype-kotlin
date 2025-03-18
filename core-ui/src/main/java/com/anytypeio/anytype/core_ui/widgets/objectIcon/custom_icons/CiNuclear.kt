package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiNuclear: ImageVector
    get() {
        if (_CiNuclear != null) {
            return _CiNuclear!!
        }
        _CiNuclear = ImageVector.Builder(
            name = "CiNuclear",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(258.9f, 48f)
                curveTo(141.92f, 46.42f, 46.42f, 141.92f, 48f, 258.9f)
                curveTo(49.56f, 371.09f, 140.91f, 462.44f, 253.1f, 464f)
                curveToRelative(117f, 1.6f, 212.48f, -93.9f, 210.88f, -210.88f)
                curveTo(462.44f, 140.91f, 371.09f, 49.56f, 258.9f, 48f)
                close()
                moveTo(429f, 239.92f)
                lineToRelative(-93.08f, -0.1f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.95f, -1.57f)
                arcToRelative(80.08f, 80.08f, 0f, isMoreThanHalf = false, isPositiveArc = false, -27.44f, -44.17f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.54f, -2.43f)
                lineToRelative(41.32f, -83.43f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.87f, -0.81f)
                arcTo(176.2f, 176.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 431f, 237.71f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 429f, 239.92f)
                close()
                moveTo(208.2f, 260.38f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = true, isPositiveArc = true, 43.42f, 43.42f)
                arcTo(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 208.2f, 260.38f)
                close()
                moveTo(164.65f, 108.22f)
                lineTo(206f, 191.65f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.54f, 2.43f)
                arcTo(80.08f, 80.08f, 0f, isMoreThanHalf = false, isPositiveArc = false, 178f, 238.25f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, 1.57f)
                lineToRelative(-93.08f, 0.1f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, -2.21f)
                arcToRelative(176.2f, 176.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 80.82f, -130.3f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 164.65f, 108.22f)
                close()
                moveTo(164.28f, 403.56f)
                lineTo(220.59f, 329.47f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.43f, -0.6f)
                arcToRelative(79.84f, 79.84f, 0f, isMoreThanHalf = false, isPositiveArc = false, 66f, 0f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.43f, 0.6f)
                lineToRelative(56.31f, 74.09f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.54f, 2.92f)
                arcToRelative(175.65f, 175.65f, 0f, isMoreThanHalf = false, isPositiveArc = true, -182.36f, 0f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 164.28f, 403.56f)
                close()
            }
        }.build()

        return _CiNuclear!!
    }

@Suppress("ObjectPropertyName")
private var _CiNuclear: ImageVector? = null
