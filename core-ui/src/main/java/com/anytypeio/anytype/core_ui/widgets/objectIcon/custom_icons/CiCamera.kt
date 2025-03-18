package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCamera: ImageVector
    get() {
        if (_CiCamera != null) {
            return _CiCamera!!
        }
        _CiCamera = ImageVector.Builder(
            name = "CiCamera",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 272f)
                moveToRelative(-64f, 0f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = true, 128f, 0f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = true, -128f, 0f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(432f, 144f)
                horizontalLineTo(373f)
                curveToRelative(-3f, 0f, -6.72f, -1.94f, -9.62f, -5f)
                lineTo(337.44f, 98.06f)
                arcToRelative(15.52f, 15.52f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.37f, -1.85f)
                curveTo(327.11f, 85.76f, 315f, 80f, 302f, 80f)
                horizontalLineTo(210f)
                curveToRelative(-13f, 0f, -25.11f, 5.76f, -34.07f, 16.21f)
                arcToRelative(15.52f, 15.52f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.37f, 1.85f)
                lineToRelative(-25.94f, 41f)
                curveToRelative(-2.22f, 2.42f, -5.34f, 5f, -8.62f, 5f)
                verticalLineToRelative(-8f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, -16f)
                horizontalLineTo(100f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16f, 16f)
                verticalLineToRelative(8f)
                horizontalLineTo(80f)
                arcToRelative(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, -48f, 48f)
                verticalLineTo(384f)
                arcToRelative(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 48f)
                horizontalLineTo(432f)
                arcToRelative(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, -48f)
                verticalLineTo(192f)
                arcTo(48.05f, 48.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 432f, 144f)
                close()
                moveTo(256f, 368f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = true, isPositiveArc = true, 96f, -96f)
                arcTo(96.11f, 96.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 368f)
                close()
            }
        }.build()

        return _CiCamera!!
    }

@Suppress("ObjectPropertyName")
private var _CiCamera: ImageVector? = null
