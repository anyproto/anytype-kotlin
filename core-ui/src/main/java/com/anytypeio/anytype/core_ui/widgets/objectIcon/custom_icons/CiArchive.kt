package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiArchive: ImageVector
    get() {
        if (_CiArchive != null) {
            return _CiArchive!!
        }
        _CiArchive = ImageVector.Builder(
            name = "CiArchive",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(64f, 164f)
                verticalLineTo(408f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, 56f)
                horizontalLineTo(392f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, -56f)
                verticalLineTo(164f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, -4f)
                horizontalLineTo(68f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 164f)
                close()
                moveTo(331f, 315.63f)
                lineToRelative(-63.69f, 63.68f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.62f, 0f)
                lineTo(181f, 315.63f)
                curveToRelative(-6.09f, -6.09f, -6.65f, -16f, -0.85f, -22.38f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.16f, -0.56f)
                lineTo(240f, 329.37f)
                verticalLineTo(224.45f)
                curveToRelative(0f, -8.61f, 6.62f, -16f, 15.23f, -16.43f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 272f, 224f)
                verticalLineTo(329.37f)
                lineToRelative(36.69f, -36.68f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.16f, 0.56f)
                curveTo(337.65f, 299.62f, 337.09f, 309.54f, 331f, 315.63f)
                close()
            }
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
        }.build()

        return _CiArchive!!
    }

@Suppress("ObjectPropertyName")
private var _CiArchive: ImageVector? = null
