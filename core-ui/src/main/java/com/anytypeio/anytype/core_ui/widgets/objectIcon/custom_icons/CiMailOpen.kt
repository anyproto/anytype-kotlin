package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMailOpen: ImageVector
    get() {
        if (_CiMailOpen != null) {
            return _CiMailOpen!!
        }
        _CiMailOpen = ImageVector.Builder(
            name = "CiMailOpen",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(448.67f, 154.45f)
                lineTo(274.1f, 68.2f)
                arcToRelative(41.1f, 41.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -36.2f, 0f)
                lineTo(63.33f, 154.45f)
                arcTo(55.6f, 55.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 204.53f)
                verticalLineTo(389.14f)
                curveToRelative(0f, 30.88f, 25.42f, 56f, 56.67f, 56f)
                horizontalLineTo(423.33f)
                curveToRelative(31.25f, 0f, 56.67f, -25.12f, 56.67f, -56f)
                verticalLineTo(204.53f)
                arcTo(55.6f, 55.6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 448.67f, 154.45f)
                close()
                moveTo(252.38f, 96.82f)
                arcToRelative(8.22f, 8.22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.24f, 0f)
                lineTo(429f, 180.48f)
                lineToRelative(-172f, 85f)
                arcToRelative(8.22f, 8.22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -7.24f, 0f)
                lineTo(80.35f, 181.81f)
                close()
            }
        }.build()

        return _CiMailOpen!!
    }

@Suppress("ObjectPropertyName")
private var _CiMailOpen: ImageVector? = null
