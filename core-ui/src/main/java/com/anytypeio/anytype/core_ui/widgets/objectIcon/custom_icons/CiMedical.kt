package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMedical: ImageVector
    get() {
        if (_CiMedical != null) {
            return _CiMedical!!
        }
        _CiMedical = ImageVector.Builder(
            name = "CiMedical",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(272f, 464f)
                lineTo(240f, 464f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, -32f)
                lineToRelative(0.05f, -85.82f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6f, -3.47f)
                lineToRelative(-74.34f, 43.06f)
                arcToRelative(31.48f, 31.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -43f, -11.52f)
                lineTo(68.21f, 345.61f)
                lineToRelative(-0.06f, -0.1f)
                arcToRelative(31.65f, 31.65f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.56f, -42.8f)
                lineToRelative(74.61f, -43.25f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -6.92f)
                lineTo(79.78f, 209.33f)
                arcToRelative(31.41f, 31.41f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.55f, -43f)
                lineToRelative(16.44f, -28.55f)
                arcToRelative(31.48f, 31.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19.27f, -14.74f)
                arcToRelative(31.14f, 31.14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.8f, 3.2f)
                lineToRelative(74.31f, 43f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, -3.47f)
                lineTo(208f, 80f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, -32f)
                horizontalLineToRelative(32f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 32f)
                lineTo(304f, 165.72f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 3.47f)
                lineToRelative(74.34f, -43.06f)
                arcToRelative(31.51f, 31.51f, 0f, isMoreThanHalf = false, isPositiveArc = true, 43f, 11.52f)
                lineToRelative(16.49f, 28.64f)
                lineToRelative(0.06f, 0.09f)
                arcToRelative(31.52f, 31.52f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.64f, 42.86f)
                lineToRelative(-74.53f, 43.2f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 6.92f)
                lineToRelative(74.53f, 43.2f)
                arcToRelative(31.42f, 31.42f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.56f, 43f)
                lineToRelative(-16.44f, 28.55f)
                arcToRelative(31.48f, 31.48f, 0f, isMoreThanHalf = false, isPositiveArc = true, -19.27f, 14.74f)
                arcToRelative(31.14f, 31.14f, 0f, isMoreThanHalf = false, isPositiveArc = true, -23.8f, -3.2f)
                lineToRelative(-74.31f, -43f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6f, 3.46f)
                lineTo(304f, 432f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 272f, 464f)
                close()
                moveTo(178.44f, 266.52f)
                horizontalLineToRelative(0f)
                close()
                moveTo(178.44f, 245.52f)
                horizontalLineToRelative(0f)
                close()
                moveTo(333.54f, 245.44f)
                close()
                moveTo(333.54f, 245.44f)
                horizontalLineToRelative(0f)
                close()
            }
        }.build()

        return _CiMedical!!
    }

@Suppress("ObjectPropertyName")
private var _CiMedical: ImageVector? = null
