package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBalloon: ImageVector
    get() {
        if (_CiBalloon != null) {
            return _CiBalloon!!
        }
        _CiBalloon = ImageVector.Builder(
            name = "CiBalloon",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(391f, 307.27f)
                curveToRelative(32.75f, -46.35f, 46.59f, -101.63f, 39f, -155.68f)
                arcTo(175.82f, 175.82f, 0f, isMoreThanHalf = false, isPositiveArc = false, 231.38f, 2f)
                curveToRelative(-96f, 13.49f, -163.14f, 102.58f, -149.65f, 198.58f)
                curveToRelative(7.57f, 53.89f, 36.12f, 103.16f, 80.37f, 138.74f)
                curveTo(186.68f, 359f, 214.41f, 372.82f, 240.72f, 379f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 9.22f)
                lineToRelative(-4.87f, 26.38f)
                arcToRelative(16.29f, 16.29f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.48f, 10.57f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 14.2f, 8.61f)
                arcToRelative(15.21f, 15.21f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.23f, -0.16f)
                lineToRelative(17.81f, -2.5f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.09f, 1.14f)
                curveToRelative(16.72f, 36.31f, 45.46f, 63.85f, 82.15f, 78.36f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 21f, -9.65f)
                curveToRelative(2.83f, -8.18f, -1.64f, -17.07f, -9.68f, -20.28f)
                arcToRelative(118.57f, 118.57f, 0f, isMoreThanHalf = false, isPositiveArc = true, -59.3f, -51.88f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.45f, -3f)
                lineToRelative(7.4f, -1f)
                arcToRelative(16.54f, 16.54f, 0f, isMoreThanHalf = false, isPositiveArc = false, 10.08f, -5.23f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.39f, -17.8f)
                lineToRelative(-12.06f, -24.23f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 326.35f, 367f)
                curveTo(349.94f, 353.83f, 372.8f, 333f, 391f, 307.27f)
                close()
                moveTo(236.1f, 324.05f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.88f, -1.12f)
                curveToRelative(-41.26f, -16.32f, -76.3f, -52.7f, -91.45f, -94.94f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 30.12f, -10.8f)
                curveToRelative(14.5f, 40.44f, 47.27f, 65.77f, 73.1f, 76f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.89f, 30.88f)
                close()
            }
        }.build()

        return _CiBalloon!!
    }

@Suppress("ObjectPropertyName")
private var _CiBalloon: ImageVector? = null
