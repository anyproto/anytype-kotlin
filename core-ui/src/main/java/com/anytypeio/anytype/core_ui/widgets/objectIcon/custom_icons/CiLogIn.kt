package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiLogIn: ImageVector
    get() {
        if (_CiLogIn != null) {
            return _CiLogIn!!
        }
        _CiLogIn = ImageVector.Builder(
            name = "CiLogIn",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(392f, 80f)
                horizontalLineTo(232f)
                arcToRelative(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, -56f, 56f)
                verticalLineTo(240f)
                horizontalLineTo(329.37f)
                lineToRelative(-52.68f, -52.69f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.62f, -22.62f)
                lineToRelative(80f, 80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 22.62f)
                lineToRelative(-80f, 80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.62f, -22.62f)
                lineTo(329.37f, 272f)
                horizontalLineTo(176f)
                verticalLineTo(376f)
                curveToRelative(0f, 32.05f, 33.79f, 56f, 64f, 56f)
                horizontalLineTo(392f)
                arcToRelative(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, -56f)
                verticalLineTo(136f)
                arcTo(56.06f, 56.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 392f, 80f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(80f, 240f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 32f)
                horizontalLineToRelative(96f)
                verticalLineTo(240f)
                close()
            }
        }.build()

        return _CiLogIn!!
    }

@Suppress("ObjectPropertyName")
private var _CiLogIn: ImageVector? = null
