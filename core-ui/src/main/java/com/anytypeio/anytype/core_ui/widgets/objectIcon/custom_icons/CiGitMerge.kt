package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiGitMerge: ImageVector
    get() {
        if (_CiGitMerge != null) {
            return _CiGitMerge!!
        }
        _CiGitMerge = ImageVector.Builder(
            name = "CiGitMerge",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(385f, 224f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, -55.33f, 31.89f)
                curveToRelative(-42.23f, -1.21f, -85.19f, -12.72f, -116.21f, -31.33f)
                curveToRelative(-32.2f, -19.32f, -49.71f, -44f, -52.15f, -73.35f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = false, -64.31f, 0.18f)
                lineTo(97f, 360.61f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = false, 64f, 0f)
                lineTo(161f, 266.15f)
                curveToRelative(44.76f, 34f, 107.28f, 52.38f, 168.56f, 53.76f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = false, 385f, 224f)
                close()
                moveTo(129f, 64f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 97f, 96f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 129f, 64f)
                close()
                moveTo(129f, 448f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 129f, 448f)
                close()
                moveTo(385f, 320f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 385f, 320f)
                close()
            }
        }.build()

        return _CiGitMerge!!
    }

@Suppress("ObjectPropertyName")
private var _CiGitMerge: ImageVector? = null
