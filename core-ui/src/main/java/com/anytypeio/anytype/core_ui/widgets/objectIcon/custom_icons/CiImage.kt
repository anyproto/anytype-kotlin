package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiImage: ImageVector
    get() {
        if (_CiImage != null) {
            return _CiImage!!
        }
        _CiImage = ImageVector.Builder(
            name = "CiImage",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(416f, 64f)
                lineTo(96f, 64f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64f, 64f)
                lineTo(32f, 384f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                lineTo(416f, 448f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                lineTo(480f, 128f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 416f, 64f)
                close()
                moveTo(336f, 128f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = true, isPositiveArc = true, -48f, 48f)
                arcTo(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, 336f, 128f)
                close()
                moveTo(96f, 416f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, -32f)
                lineTo(64f, 316.37f)
                lineToRelative(94.84f, -84.3f)
                arcToRelative(48.06f, 48.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, 65.8f, 1.9f)
                lineToRelative(64.95f, 64.81f)
                lineTo(172.37f, 416f)
                close()
                moveTo(448f, 384f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 32f)
                lineTo(217.63f, 416f)
                lineTo(339.05f, 294.58f)
                arcToRelative(47.72f, 47.72f, 0f, isMoreThanHalf = false, isPositiveArc = true, 61.64f, -0.16f)
                lineTo(448f, 333.84f)
                close()
            }
        }.build()

        return _CiImage!!
    }

@Suppress("ObjectPropertyName")
private var _CiImage: ImageVector? = null
