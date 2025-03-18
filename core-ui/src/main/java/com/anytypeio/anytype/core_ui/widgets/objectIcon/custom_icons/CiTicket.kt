package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiTicket: ImageVector
    get() {
        if (_CiTicket != null) {
            return _CiTicket!!
        }
        _CiTicket = ImageVector.Builder(
            name = "CiTicket",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(490.18f, 181.4f)
                lineToRelative(-44.13f, -44.13f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, -27f, -1f)
                arcToRelative(30.81f, 30.81f, 0f, isMoreThanHalf = false, isPositiveArc = true, -41.68f, -1.6f)
                horizontalLineToRelative(0f)
                arcTo(30.81f, 30.81f, 0f, isMoreThanHalf = false, isPositiveArc = true, 375.77f, 93f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1f, -27f)
                lineTo(330.6f, 21.82f)
                arcToRelative(19.91f, 19.91f, 0f, isMoreThanHalf = false, isPositiveArc = false, -28.13f, 0f)
                lineTo(232.12f, 92.16f)
                arcToRelative(39.87f, 39.87f, 0f, isMoreThanHalf = false, isPositiveArc = false, -9.57f, 15.5f)
                arcToRelative(7.71f, 7.71f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.83f, 4.83f)
                arcToRelative(39.78f, 39.78f, 0f, isMoreThanHalf = false, isPositiveArc = false, -15.5f, 9.58f)
                lineTo(21.82f, 302.47f)
                arcToRelative(19.91f, 19.91f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 28.13f)
                lineTo(66f, 374.73f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, 27f, 1f)
                arcToRelative(30.69f, 30.69f, 0f, isMoreThanHalf = false, isPositiveArc = true, 43.28f, 43.28f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, 27f)
                lineToRelative(44.13f, 44.13f)
                arcToRelative(19.91f, 19.91f, 0f, isMoreThanHalf = false, isPositiveArc = false, 28.13f, 0f)
                lineToRelative(180.4f, -180.4f)
                arcToRelative(39.82f, 39.82f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.58f, -15.49f)
                arcToRelative(7.69f, 7.69f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.84f, -4.84f)
                arcToRelative(39.84f, 39.84f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.49f, -9.57f)
                lineToRelative(70.34f, -70.35f)
                arcTo(19.91f, 19.91f, 0f, isMoreThanHalf = false, isPositiveArc = false, 490.18f, 181.4f)
                close()
                moveTo(261.81f, 151.75f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.63f, 0f)
                lineToRelative(-11.51f, -11.51f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.63f, -22.62f)
                lineToRelative(11.51f, 11.5f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 261.81f, 151.75f)
                close()
                moveTo(305.81f, 195.75f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.62f, 0f)
                lineToRelative(-11f, -11f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 22.63f, -22.63f)
                lineToRelative(11f, 11f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 305.83f, 195.78f)
                close()
                moveTo(349.81f, 239.75f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.63f, 0f)
                lineToRelative(-11f, -11f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.63f, -22.62f)
                lineToRelative(11f, 11f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 349.86f, 239.8f)
                close()
                moveTo(394.24f, 284.29f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.63f, 0f)
                lineToRelative(-11.44f, -11.5f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 22.68f, -22.57f)
                lineToRelative(11.45f, 11.49f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 394.29f, 284.34f)
                close()
            }
        }.build()

        return _CiTicket!!
    }

@Suppress("ObjectPropertyName")
private var _CiTicket: ImageVector? = null
