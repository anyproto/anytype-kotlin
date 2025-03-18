package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiDocumentText: ImageVector
    get() {
        if (_CiDocumentText != null) {
            return _CiDocumentText!!
        }
        _CiDocumentText = ImageVector.Builder(
            name = "CiDocumentText",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(428f, 224f)
                lineTo(288f, 224f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -48f, -48f)
                lineTo(240f, 36f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, -4f)
                lineTo(144f, 32f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, 96f)
                lineTo(80f, 416f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                lineTo(368f, 480f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                lineTo(432f, 228f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 428f, 224f)
                close()
                moveTo(336f, 384f)
                lineTo(176f, 384f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                lineTo(336f, 352f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
                moveTo(336f, 304f)
                lineTo(176f, 304f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                lineTo(336f, 272f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(419.22f, 188.59f)
                lineTo(275.41f, 44.78f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 272f, 46.19f)
                verticalLineTo(176f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 16f)
                horizontalLineTo(417.81f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 419.22f, 188.59f)
                close()
            }
        }.build()

        return _CiDocumentText!!
    }

@Suppress("ObjectPropertyName")
private var _CiDocumentText: ImageVector? = null
