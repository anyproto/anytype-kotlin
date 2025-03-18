package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiHelpBuoy: ImageVector
    get() {
        if (_CiHelpBuoy != null) {
            return _CiHelpBuoy!!
        }
        _CiHelpBuoy = ImageVector.Builder(
            name = "CiHelpBuoy",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(414.39f, 97.61f)
                arcTo(224f, 224f, 0f, isMoreThanHalf = true, isPositiveArc = false, 97.61f, 414.39f)
                arcTo(224f, 224f, 0f, isMoreThanHalf = true, isPositiveArc = false, 414.39f, 97.61f)
                close()
                moveTo(192.13f, 260.18f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = true, 59.69f, 59.69f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = true, 192.13f, 260.18f)
                close()
                moveTo(432.13f, 193.54f)
                lineTo(335.76f, 199.38f)
                arcToRelative(4.06f, 4.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.44f, -1.59f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = false, isPositiveArc = false, -18.07f, -18.07f)
                arcToRelative(4.06f, 4.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.59f, -3.44f)
                lineToRelative(5.84f, -96.37f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.42f, -3.51f)
                arcTo(193f, 193f, 0f, isMoreThanHalf = false, isPositiveArc = true, 435.6f, 188.12f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 432.09f, 193.54f)
                close()
                moveTo(193.54f, 79.91f)
                lineToRelative(5.84f, 96.37f)
                arcToRelative(4.06f, 4.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.59f, 3.44f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = false, isPositiveArc = false, -18.07f, 18.07f)
                arcToRelative(4.06f, 4.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.44f, 1.59f)
                lineToRelative(-96.37f, -5.84f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.51f, -5.42f)
                arcTo(193f, 193f, 0f, isMoreThanHalf = false, isPositiveArc = true, 188.12f, 76.4f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 193.54f, 79.91f)
                close()
                moveTo(79.91f, 318.46f)
                lineToRelative(96.37f, -5.84f)
                arcToRelative(4.06f, 4.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.44f, 1.59f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18.07f, 18.07f)
                arcToRelative(4.06f, 4.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.59f, 3.44f)
                lineToRelative(-5.84f, 96.37f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.42f, 3.51f)
                arcTo(193f, 193f, 0f, isMoreThanHalf = false, isPositiveArc = true, 76.4f, 323.88f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 79.91f, 318.46f)
                close()
                moveTo(318.46f, 432.09f)
                lineToRelative(-5.84f, -96.37f)
                arcToRelative(4.06f, 4.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.59f, -3.44f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18.07f, -18.07f)
                arcToRelative(4.06f, 4.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.44f, -1.59f)
                lineToRelative(96.37f, 5.84f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.51f, 5.42f)
                arcTo(193f, 193f, 0f, isMoreThanHalf = false, isPositiveArc = true, 323.88f, 435.6f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 318.46f, 432.09f)
                close()
            }
        }.build()

        return _CiHelpBuoy!!
    }

@Suppress("ObjectPropertyName")
private var _CiHelpBuoy: ImageVector? = null
