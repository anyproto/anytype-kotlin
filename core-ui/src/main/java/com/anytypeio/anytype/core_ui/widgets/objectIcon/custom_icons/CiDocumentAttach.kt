package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiDocumentAttach: ImageVector
    get() {
        if (_CiDocumentAttach != null) {
            return _CiDocumentAttach!!
        }
        _CiDocumentAttach = ImageVector.Builder(
            name = "CiDocumentAttach",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(460f, 240f)
                horizontalLineTo(320f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -48f, -48f)
                verticalLineTo(52f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, -4f)
                horizontalLineTo(214.75f)
                arcToRelative(65.42f, 65.42f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.5f, -9.81f)
                curveTo(196.72f, 23.88f, 179.59f, 16f, 160f, 16f)
                curveToRelative(-37.68f, 0f, -64f, 29.61f, -64f, 72f)
                verticalLineTo(232f)
                curveToRelative(0f, 25f, 20.34f, 40f, 40f, 40f)
                arcToRelative(39.57f, 39.57f, 0f, isMoreThanHalf = false, isPositiveArc = false, 40f, -40f)
                verticalLineTo(80f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, 0f)
                verticalLineTo(232f)
                arcToRelative(7.75f, 7.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8f, 8f)
                curveToRelative(-2.23f, 0f, -8f, -1.44f, -8f, -8f)
                verticalLineTo(88f)
                curveToRelative(0f, -19.34f, 8.41f, -40f, 32f, -40f)
                curveToRelative(29.69f, 0f, 32f, 30.15f, 32f, 39.38f)
                verticalLineTo(226.13f)
                curveToRelative(0f, 17.45f, -5.47f, 33.23f, -15.41f, 44.46f)
                curveTo(166.5f, 282f, 152.47f, 288f, 136f, 288f)
                reflectiveCurveToRelative(-30.5f, -6f, -40.59f, -17.41f)
                curveTo(85.47f, 259.36f, 80f, 243.58f, 80f, 226.13f)
                verticalLineTo(144f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -32f, 0f)
                verticalLineToRelative(82.13f)
                curveToRelative(0f, 51.51f, 33.19f, 89.63f, 80f, 93.53f)
                verticalLineTo(432f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                horizontalLineTo(400f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                verticalLineTo(244f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 460f, 240f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(320f, 208f)
                horizontalLineTo(449.81f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.41f, -3.41f)
                lineTo(307.41f, 60.78f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 304f, 62.19f)
                verticalLineTo(192f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 320f, 208f)
                close()
            }
        }.build()

        return _CiDocumentAttach!!
    }

@Suppress("ObjectPropertyName")
private var _CiDocumentAttach: ImageVector? = null
