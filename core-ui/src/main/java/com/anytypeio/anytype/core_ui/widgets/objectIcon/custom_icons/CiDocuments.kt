package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiDocuments: ImageVector
    get() {
        if (_CiDocuments != null) {
            return _CiDocuments!!
        }
        _CiDocuments = ImageVector.Builder(
            name = "CiDocuments",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(298.39f, 248f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.86f, -6.8f)
                lineToRelative(-78.4f, -79.72f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.85f, 2.81f)
                verticalLineTo(236f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12f, 12f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(197f, 267f)
                arcTo(43.67f, 43.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 184f, 236f)
                verticalLineTo(144f)
                horizontalLineTo(112f)
                arcToRelative(64.19f, 64.19f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64f, 64f)
                verticalLineTo(432f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                horizontalLineTo(256f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                verticalLineTo(280f)
                horizontalLineTo(228f)
                arcTo(43.61f, 43.61f, 0f, isMoreThanHalf = false, isPositiveArc = true, 197f, 267f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(372f, 120f)
                horizontalLineToRelative(70.39f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.86f, -6.8f)
                lineToRelative(-78.4f, -79.72f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 360f, 36.29f)
                verticalLineTo(108f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 372f, 120f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(372f, 152f)
                arcToRelative(44.34f, 44.34f, 0f, isMoreThanHalf = false, isPositiveArc = true, -44f, -44f)
                verticalLineTo(16f)
                horizontalLineTo(220f)
                arcToRelative(60.07f, 60.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, -60f, 60f)
                verticalLineToRelative(36f)
                horizontalLineToRelative(42.12f)
                arcTo(40.81f, 40.81f, 0f, isMoreThanHalf = false, isPositiveArc = true, 231f, 124.14f)
                lineToRelative(109.16f, 111f)
                arcToRelative(41.11f, 41.11f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.83f, 29f)
                verticalLineTo(400f)
                horizontalLineToRelative(53.05f)
                curveToRelative(32.51f, 0f, 58.95f, -26.92f, 58.95f, -60f)
                verticalLineTo(152f)
                close()
            }
        }.build()

        return _CiDocuments!!
    }

@Suppress("ObjectPropertyName")
private var _CiDocuments: ImageVector? = null
