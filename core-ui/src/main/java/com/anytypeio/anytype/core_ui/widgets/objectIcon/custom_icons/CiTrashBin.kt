package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiTrashBin: ImageVector
    get() {
        if (_CiTrashBin != null) {
            return _CiTrashBin!!
        }
        _CiTrashBin = ImageVector.Builder(
            name = "CiTrashBin",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(64f, 48f)
                lineTo(448f, 48f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 480f, 80f)
                lineTo(480f, 96f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 448f, 128f)
                lineTo(64f, 128f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 96f)
                lineTo(32f, 80f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 64f, 48f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(74.45f, 160f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, 8.83f)
                lineTo(92.76f, 421.39f)
                arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 0.22f)
                arcTo(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 140.45f, 464f)
                horizontalLineTo(371.54f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 47.67f, -42.39f)
                lineToRelative(0f, -0.21f)
                lineToRelative(26.27f, -252.57f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, -8.83f)
                close()
                moveTo(323.31f, 340.69f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, -22.63f, 22.62f)
                lineTo(256f, 318.63f)
                lineToRelative(-44.69f, 44.68f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.63f, -22.62f)
                lineTo(233.37f, 296f)
                lineToRelative(-44.69f, -44.69f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.63f, -22.62f)
                lineTo(256f, 273.37f)
                lineToRelative(44.68f, -44.68f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.63f, 22.62f)
                lineTo(278.62f, 296f)
                close()
            }
        }.build()

        return _CiTrashBin!!
    }

@Suppress("ObjectPropertyName")
private var _CiTrashBin: ImageVector? = null
