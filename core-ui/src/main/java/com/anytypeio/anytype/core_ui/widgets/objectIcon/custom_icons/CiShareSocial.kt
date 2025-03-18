package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiShareSocial: ImageVector
    get() {
        if (_CiShareSocial != null) {
            return _CiShareSocial!!
        }
        _CiShareSocial = ImageVector.Builder(
            name = "CiShareSocial",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(384f, 336f)
                arcToRelative(63.78f, 63.78f, 0f, isMoreThanHalf = false, isPositiveArc = false, -46.12f, 19.7f)
                lineToRelative(-148f, -83.27f)
                arcToRelative(63.85f, 63.85f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -32.86f)
                lineToRelative(148f, -83.27f)
                arcToRelative(63.8f, 63.8f, 0f, isMoreThanHalf = true, isPositiveArc = false, -15.73f, -27.87f)
                lineToRelative(-148f, 83.27f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = false, 0f, 88.6f)
                lineToRelative(148f, 83.27f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = false, 384f, 336f)
                close()
            }
        }.build()

        return _CiShareSocial!!
    }

@Suppress("ObjectPropertyName")
private var _CiShareSocial: ImageVector? = null
