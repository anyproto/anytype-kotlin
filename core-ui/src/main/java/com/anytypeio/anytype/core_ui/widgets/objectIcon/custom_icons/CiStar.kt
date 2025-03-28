package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiStar: ImageVector
    get() {
        if (_CiStar != null) {
            return _CiStar!!
        }
        _CiStar = ImageVector.Builder(
            name = "CiStar",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(394f, 480f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -9.39f, -3f)
                lineTo(256f, 383.76f)
                lineTo(127.39f, 477f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24.55f, -18.08f)
                lineTo(153f, 310.35f)
                lineTo(23f, 221.2f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 192f)
                horizontalLineTo(192.38f)
                lineToRelative(48.4f, -148.95f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 30.44f, 0f)
                lineToRelative(48.4f, 149f)
                horizontalLineTo(480f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.05f, 29.2f)
                lineTo(359f, 310.35f)
                lineToRelative(50.13f, 148.53f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 394f, 480f)
                close()
            }
        }.build()

        return _CiStar!!
    }

@Suppress("ObjectPropertyName")
private var _CiStar: ImageVector? = null
