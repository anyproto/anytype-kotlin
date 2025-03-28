package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiPrint: ImageVector
    get() {
        if (_CiPrint != null) {
            return _CiPrint!!
        }
        _CiPrint = ImageVector.Builder(
            name = "CiPrint",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(408f, 112f)
                horizontalLineTo(106f)
                arcToRelative(58f, 58f, 0f, isMoreThanHalf = false, isPositiveArc = false, -58f, 58f)
                verticalLineTo(328f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, 56f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(39.68f)
                arcTo(40.32f, 40.32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 152.32f, 464f)
                horizontalLineTo(359.68f)
                arcTo(40.32f, 40.32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 400f, 423.68f)
                verticalLineTo(384f)
                horizontalLineToRelative(8f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, -56f)
                verticalLineTo(168f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 408f, 112f)
                close()
                moveTo(368f, 423.68f)
                arcToRelative(8.35f, 8.35f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8.32f, 8.32f)
                horizontalLineTo(152.32f)
                arcToRelative(8.35f, 8.35f, 0f, isMoreThanHalf = false, isPositiveArc = true, -8.32f, -8.32f)
                verticalLineTo(264.32f)
                arcToRelative(8.35f, 8.35f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.32f, -8.32f)
                horizontalLineTo(359.68f)
                arcToRelative(8.35f, 8.35f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8.32f, 8.32f)
                close()
                moveTo(394f, 207.92f)
                arcToRelative(24f, 24f, 0f, isMoreThanHalf = true, isPositiveArc = true, 22f, -22f)
                arcTo(24f, 24f, 0f, isMoreThanHalf = false, isPositiveArc = true, 394f, 207.92f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(344f, 48f)
                horizontalLineTo(168f)
                arcToRelative(56.09f, 56.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, -55.42f, 48f)
                horizontalLineTo(399.42f)
                arcTo(56.09f, 56.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 344f, 48f)
                close()
            }
        }.build()

        return _CiPrint!!
    }

@Suppress("ObjectPropertyName")
private var _CiPrint: ImageVector? = null
