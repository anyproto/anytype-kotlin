package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFlower: ImageVector
    get() {
        if (_CiFlower != null) {
            return _CiFlower!!
        }
        _CiFlower = ImageVector.Builder(
            name = "CiFlower",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 256f)
                moveToRelative(-48f, 0f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = true, isPositiveArc = true, 96f, 0f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = true, isPositiveArc = true, -96f, 0f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(475.93f, 303.91f)
                arcToRelative(67.49f, 67.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, -44.34f, -115.53f)
                arcToRelative(5.2f, 5.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.58f, -3.21f)
                horizontalLineToRelative(0f)
                arcToRelative(5.21f, 5.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1f, -5.51f)
                arcTo(67.83f, 67.83f, 0f, isMoreThanHalf = false, isPositiveArc = false, 378f, 66.33f)
                horizontalLineToRelative(-0.25f)
                arcTo(67.13f, 67.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, 332.35f, 84f)
                arcToRelative(5.21f, 5.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.52f, 1f)
                horizontalLineToRelative(0f)
                arcToRelative(5.23f, 5.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.22f, -4.58f)
                arcToRelative(67.68f, 67.68f, 0f, isMoreThanHalf = false, isPositiveArc = false, -135.23f, 0f)
                arcTo(5.2f, 5.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 185.17f, 85f)
                horizontalLineToRelative(0f)
                arcToRelative(5.21f, 5.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.52f, -1f)
                arcToRelative(67.11f, 67.11f, 0f, isMoreThanHalf = false, isPositiveArc = false, -45.44f, -17.69f)
                horizontalLineTo(134f)
                arcTo(67.91f, 67.91f, 0f, isMoreThanHalf = false, isPositiveArc = false, 84f, 179.65f)
                arcToRelative(5.21f, 5.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1f, 5.51f)
                horizontalLineToRelative(0f)
                arcToRelative(5.2f, 5.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.58f, 3.21f)
                arcToRelative(67.71f, 67.71f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 135.23f)
                arcTo(5.23f, 5.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, 85f, 326.83f)
                horizontalLineToRelative(0f)
                arcToRelative(5.22f, 5.22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, 5.52f)
                arcToRelative(67.54f, 67.54f, 0f, isMoreThanHalf = false, isPositiveArc = false, 50.08f, 113f)
                horizontalLineToRelative(0.25f)
                arcTo(67.38f, 67.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 179.65f, 428f)
                arcToRelative(5.21f, 5.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.51f, -1f)
                horizontalLineToRelative(0f)
                arcToRelative(5.2f, 5.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.21f, 4.58f)
                arcToRelative(67.71f, 67.71f, 0f, isMoreThanHalf = false, isPositiveArc = false, 135.23f, 0f)
                arcToRelative(5.23f, 5.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.22f, -4.58f)
                horizontalLineToRelative(0f)
                arcToRelative(5.21f, 5.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.51f, 1f)
                arcToRelative(67.38f, 67.38f, 0f, isMoreThanHalf = false, isPositiveArc = false, 45.29f, 17.42f)
                horizontalLineToRelative(0.25f)
                arcToRelative(67.48f, 67.48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 50.08f, -113f)
                arcToRelative(5.22f, 5.22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, -5.52f)
                horizontalLineToRelative(0f)
                arcToRelative(5.23f, 5.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.58f, -3.22f)
                arcTo(67.31f, 67.31f, 0f, isMoreThanHalf = false, isPositiveArc = false, 475.93f, 303.91f)
                close()
                moveTo(256f, 336f)
                arcToRelative(80f, 80f, 0f, isMoreThanHalf = true, isPositiveArc = true, 80f, -80f)
                arcTo(80.09f, 80.09f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 336f)
                close()
            }
        }.build()

        return _CiFlower!!
    }

@Suppress("ObjectPropertyName")
private var _CiFlower: ImageVector? = null
