package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiExtensionPuzzle: ImageVector
    get() {
        if (_CiExtensionPuzzle != null) {
            return _CiExtensionPuzzle!!
        }
        _CiExtensionPuzzle = ImageVector.Builder(
            name = "CiExtensionPuzzle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(345.14f, 480f)
                horizontalLineTo(274f)
                arcToRelative(18f, 18f, 0f, isMoreThanHalf = false, isPositiveArc = true, -18f, -18f)
                verticalLineTo(434.29f)
                arcToRelative(31.32f, 31.32f, 0f, isMoreThanHalf = false, isPositiveArc = false, -9.71f, -22.77f)
                curveToRelative(-7.78f, -7.59f, -19.08f, -11.8f, -30.89f, -11.51f)
                curveToRelative(-21.36f, 0.5f, -39.4f, 19.3f, -39.4f, 41.06f)
                verticalLineTo(462f)
                arcToRelative(18f, 18f, 0f, isMoreThanHalf = false, isPositiveArc = true, -18f, 18f)
                horizontalLineTo(87.62f)
                arcTo(55.62f, 55.62f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 424.38f)
                verticalLineTo(354f)
                arcToRelative(18f, 18f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18f, -18f)
                horizontalLineTo(77.71f)
                curveToRelative(9.16f, 0f, 18.07f, -3.92f, 25.09f, -11f)
                arcTo(42.06f, 42.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 115f, 295.08f)
                curveTo(114.7f, 273.89f, 97.26f, 256f, 76.91f, 256f)
                horizontalLineTo(50f)
                arcToRelative(18f, 18f, 0f, isMoreThanHalf = false, isPositiveArc = true, -18f, -18f)
                verticalLineTo(167.62f)
                arcTo(55.62f, 55.62f, 0f, isMoreThanHalf = false, isPositiveArc = true, 87.62f, 112f)
                horizontalLineToRelative(55.24f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, -8f)
                verticalLineTo(97.52f)
                arcTo(65.53f, 65.53f, 0f, isMoreThanHalf = false, isPositiveArc = true, 217.54f, 32f)
                curveToRelative(35.49f, 0.62f, 64.36f, 30.38f, 64.36f, 66.33f)
                verticalLineTo(104f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 8f)
                horizontalLineToRelative(55.24f)
                arcTo(54.86f, 54.86f, 0f, isMoreThanHalf = false, isPositiveArc = true, 400f, 166.86f)
                verticalLineTo(222.1f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 8f)
                horizontalLineToRelative(5.66f)
                curveToRelative(36.58f, 0f, 66.34f, 29f, 66.34f, 64.64f)
                curveToRelative(0f, 36.61f, -29.39f, 66.4f, -65.52f, 66.4f)
                horizontalLineTo(408f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8f, 8f)
                verticalLineToRelative(56f)
                arcTo(54.86f, 54.86f, 0f, isMoreThanHalf = false, isPositiveArc = true, 345.14f, 480f)
                close()
            }
        }.build()

        return _CiExtensionPuzzle!!
    }

@Suppress("ObjectPropertyName")
private var _CiExtensionPuzzle: ImageVector? = null
