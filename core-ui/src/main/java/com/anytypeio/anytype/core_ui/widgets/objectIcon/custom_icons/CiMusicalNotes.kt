package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMusicalNotes: ImageVector
    get() {
        if (_CiMusicalNotes != null) {
            return _CiMusicalNotes!!
        }
        _CiMusicalNotes = ImageVector.Builder(
            name = "CiMusicalNotes",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(421.84f, 37.37f)
                arcToRelative(25.86f, 25.86f, 0f, isMoreThanHalf = false, isPositiveArc = false, -22.6f, -4.46f)
                lineTo(199.92f, 86.49f)
                arcTo(32.3f, 32.3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 176f, 118f)
                verticalLineToRelative(226f)
                curveToRelative(0f, 6.74f, -4.36f, 12.56f, -11.11f, 14.83f)
                lineToRelative(-0.12f, 0.05f)
                lineToRelative(-52f, 18f)
                curveTo(92.88f, 383.53f, 80f, 402f, 80f, 423.91f)
                arcToRelative(55.54f, 55.54f, 0f, isMoreThanHalf = false, isPositiveArc = false, 23.23f, 45.63f)
                arcTo(54.78f, 54.78f, 0f, isMoreThanHalf = false, isPositiveArc = false, 135.34f, 480f)
                arcToRelative(55.82f, 55.82f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17.75f, -2.93f)
                lineToRelative(0.38f, -0.13f)
                lineTo(175.31f, 469f)
                arcTo(47.84f, 47.84f, 0f, isMoreThanHalf = false, isPositiveArc = false, 208f, 423.91f)
                verticalLineToRelative(-212f)
                curveToRelative(0f, -7.29f, 4.77f, -13.21f, 12.16f, -15.07f)
                lineToRelative(0.21f, -0.06f)
                lineTo(395f, 150.14f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5f, 3.86f)
                verticalLineTo(295.93f)
                curveToRelative(0f, 6.75f, -4.25f, 12.38f, -11.11f, 14.68f)
                lineToRelative(-0.25f, 0.09f)
                lineToRelative(-50.89f, 18.11f)
                arcTo(49.09f, 49.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 304f, 375.92f)
                arcToRelative(55.67f, 55.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, 23.23f, 45.8f)
                arcToRelative(54.63f, 54.63f, 0f, isMoreThanHalf = false, isPositiveArc = false, 49.88f, 7.35f)
                lineToRelative(0.36f, -0.12f)
                lineTo(399.31f, 421f)
                arcTo(47.83f, 47.83f, 0f, isMoreThanHalf = false, isPositiveArc = false, 432f, 375.92f)
                verticalLineTo(58f)
                arcTo(25.74f, 25.74f, 0f, isMoreThanHalf = false, isPositiveArc = false, 421.84f, 37.37f)
                close()
            }
        }.build()

        return _CiMusicalNotes!!
    }

@Suppress("ObjectPropertyName")
private var _CiMusicalNotes: ImageVector? = null
