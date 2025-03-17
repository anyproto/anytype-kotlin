package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiDocumentLock: ImageVector
    get() {
        if (_CiDocumentLock != null) {
            return _CiDocumentLock!!
        }
        _CiDocumentLock = ImageVector.Builder(
            name = "CiDocumentLock",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(288f, 192f)
                horizontalLineTo(417.81f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.41f, -3.41f)
                lineTo(275.41f, 44.78f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 272f, 46.19f)
                verticalLineTo(176f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 288f, 192f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 272f)
                curveToRelative(-8.82f, 0f, -16f, 6.28f, -16f, 14f)
                verticalLineToRelative(18f)
                horizontalLineToRelative(32f)
                verticalLineTo(286f)
                curveTo(272f, 278.28f, 264.82f, 272f, 256f, 272f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(428f, 224f)
                horizontalLineTo(288f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -48f, -48f)
                verticalLineTo(36f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, -4f)
                horizontalLineTo(144f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, 96f)
                verticalLineTo(416f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                horizontalLineTo(368f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                verticalLineTo(228f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 428f, 224f)
                close()
                moveTo(336f, 384f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 32f)
                horizontalLineTo(208f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, -32f)
                verticalLineTo(336f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, -32f)
                verticalLineTo(286f)
                curveToRelative(0f, -25.36f, 21.53f, -46f, 48f, -46f)
                reflectiveCurveToRelative(48f, 20.64f, 48f, 46f)
                verticalLineToRelative(18f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 32f)
                close()
            }
        }.build()

        return _CiDocumentLock!!
    }

@Suppress("ObjectPropertyName")
private var _CiDocumentLock: ImageVector? = null
